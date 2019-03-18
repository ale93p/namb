package fr.unice.yamb.flink;

import fr.unice.yamb.flink.connectors.SyntheticConnector;
import fr.unice.yamb.flink.operators.BusyWaitMap;
import fr.unice.yamb.flink.operators.WindowedBusyWaitFunction;
import fr.unice.yamb.utils.common.AppBuilder;
import fr.unice.yamb.utils.configuration.Config;
import fr.unice.yamb.utils.configuration.schema.YambConfigSchema;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.ArrayList;
import java.util.Iterator;

public class BenchmarkApplication {

    private static void setRouting(SingleOutputStreamOperator<Tuple1<String>> operator, Config.TrafficRouting routing, Object field) throws IllegalArgumentException{
        switch(routing){
            case hash:
                if (field instanceof Integer)
                    operator.keyBy( (int) field);
                else if (field instanceof String)
                    operator.keyBy( (String) field);
                else
                    throw new IllegalArgumentException("Field must be <int> or <String> instead it is <" + field.getClass().getName() + ">");
                break;
            case balanced:
                operator.rebalance();
                break;
            case broadcast:
                operator.broadcast();
                break;
        }
    }

    private static void setRouting(SingleOutputStreamOperator<Tuple1<String>> operator, Config.TrafficRouting routing) throws IllegalArgumentException{
        setRouting(operator, routing, 0);
    }

    private static AllWindowedStream setWindow(SingleOutputStreamOperator<Tuple1<String>> parent, Config.WindowingType type, int duration, int interval){
        switch(type){
            case tumbling:
                return parent.timeWindowAll(Time.seconds(duration));
            case sliding:
                return parent.timeWindowAll(Time.seconds(duration), Time.seconds(interval));
        }
        return null;
    }

    private static AllWindowedStream setWindow(DataStream<Tuple1<String>> parent, Config.WindowingType type, int duration, int interval){
        switch(type){
            case tumbling:
                return parent.timeWindowAll(Time.seconds(duration));
            case sliding:
                return parent.timeWindowAll(Time.seconds(duration), Time.seconds(interval));
        }
        return null;
    }

    private static StreamExecutionEnvironment buildBenchmarkEnvironment(YambConfigSchema conf) throws Exception{

        // DataFlow configurations
        int                     depth               = conf.getDataflow().getDepth();
        int                     totalParallelism    = conf.getDataflow().getScalability().getParallelism();
        Config.ParaBalancing    paraBalancing       = conf.getDataflow().getScalability().getBalancing();
        Config.ConnectionShape  topologyShape       = conf.getDataflow().getConnection().getShape();
        Config.TrafficRouting   trafficRouting      = conf.getDataflow().getConnection().getRouting();
        float                   processingLoad      = conf.getDataflow().getWorkload().getProcessing();
        Config.LoadBalancing    loadBalancing       = conf.getDataflow().getWorkload().getBalancing();

        // DataStream configurations
        int                     dataSize            = conf.getDatastream().getSynthetic().getData().getSize();
        int                     dataValues          = conf.getDatastream().getSynthetic().getData().getValues();
        Config.DataBalancing    dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getBalancing();
        Config.Distribution     distribution        = conf.getDatastream().getSynthetic().getFlow().getDistribution();
        int                     rate                = conf.getDatastream().getSynthetic().getFlow().getRate();

        // Generating app builder
        AppBuilder app                              = new AppBuilder(depth, totalParallelism, paraBalancing, topologyShape, processingLoad, loadBalancing);
        ArrayList<Integer> dagLevelsWidth           = app.getDagLevelsWidth();
        ArrayList<Integer> componentsParallelism    = app.getComponentsParallelism();


        // Bolt-specific configurations
        int     numberOfSources     = dagLevelsWidth.get(0);
        int     numberOfOperators   = app.getTotalComponents() - numberOfSources;

        // Windowing
        boolean                 windowingEnabled    = conf.getDataflow().getWindowing().isEnabled();
        Config.WindowingType    windowingType       = conf.getDataflow().getWindowing().getType();
        int                     windowDuration      = conf.getDataflow().getWindowing().getDuration();
        int                     windowInterval      = conf.getDataflow().getWindowing().getInterval();
        int                     windowedTasks       = (depth > 3) ? 2 : 1;

        Iterator<Integer> cpIterator    = componentsParallelism.iterator();
        ArrayList<MutablePair<String, DataStream<Tuple1<String>>>> sourcesList = new ArrayList<>();
        ArrayList<MutablePair<String, SingleOutputStreamOperator<Tuple1<String>>>> operatorsList = new ArrayList<>();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String sourceName;

        for(int s=1; s<=numberOfSources; s++){
            sourceName = "source_" + s;
            DataStream<Tuple1<String>> source = env.addSource(new SyntheticConnector(dataSize, dataValues, dataValuesBalancing, distribution, rate))
                    .setParallelism(cpIterator.next())
                    .name(sourceName);
            sourcesList.add(new MutablePair<>(sourceName, source));

        }

        if (numberOfSources > 1){
            sourceName = "unified_source";
            DataStream<Tuple1<String>> unifiedSource = sourcesList.get(0).getRight().union(sourcesList.get(1).getRight());
            for(int s=2; s<numberOfSources; s++){
                unifiedSource.union(sourcesList.get(s).getRight());
            }
            sourcesList.add(new MutablePair<>(sourceName, unifiedSource));
        }


        int operatorID = 1;
        int cycles;
        String operatorName;

        for(int i = 1; i<depth; i++){
            int levelWidth = dagLevelsWidth.get(i);
            boolean isWindowed  = depth - i <= windowedTasks && windowingEnabled; // this level contains windowed tasks
            SingleOutputStreamOperator<Tuple1<String>> op = null;

            if(i==1) {
                for(int opCount=0; opCount<levelWidth; opCount++) {
                    operatorName = "op_" + operatorID;
                    cycles = app.getNextProcessing();
                    DataStream<Tuple1<String>> parent = sourcesList.get(sourcesList.size() - 1).getRight();
                    if (isWindowed){
                        op = setWindow(parent, windowingType, windowDuration, windowInterval)
                                .apply(new WindowedBusyWaitFunction(cycles));
                    }
                    else{
                        op = sourcesList.get(sourcesList.size() - 1).getRight()
                                .map(new BusyWaitMap(cycles));
                    }

                    op.setParallelism(cpIterator.next())
                            .name(operatorName);

                    setRouting(op, trafficRouting);
                    operatorsList.add(new MutablePair<>(operatorName, op));
                    operatorID++;
                }
            }
            else{
                if(topologyShape == Config.ConnectionShape.diamond && dagLevelsWidth.get(i-1) > 1){ // diamond shape union
                    DataStream<Tuple1<String>> diamondUnion = operatorsList.get(operatorID - 2).getRight().union(operatorsList.get(operatorID - 3).getRight());
                    //TODO: maybe this can be optimized?
                    for(int o=2; o<dagLevelsWidth.get(i-1); o++){
                        diamondUnion.union(operatorsList.get(o).getRight());
                    }
                    operatorName = "op_" + operatorID;
                    cycles = app.getNextProcessing();


                    if (isWindowed){
                        op = setWindow(diamondUnion, windowingType, windowDuration, windowInterval)
                                .apply(new WindowedBusyWaitFunction(cycles));
                    }
                    else{
                        op = diamondUnion
                                .map(new BusyWaitMap(cycles));
                    }

                    op.setParallelism(cpIterator.next())
                            .name(operatorName);


                    op.setParallelism(cpIterator.next())
                        .name(operatorName);
                    setRouting(op, trafficRouting);
                    operatorsList.add(new MutablePair<>(operatorName, op));
                    operatorID++;
                }
                else{
                    int parentOperatorIdx = (topologyShape == Config.ConnectionShape.diamond ||
                            (topologyShape == Config.ConnectionShape.star && i>3)) ? i - 1 : i - 2;
                    SingleOutputStreamOperator<Tuple1<String>> parent = operatorsList.get(parentOperatorIdx).getRight();
                    for(int opCount = 0; opCount<levelWidth; opCount++){
                        operatorName = "op_" + operatorID;
                        cycles = app.getNextProcessing();

                        if (isWindowed){
                            op = setWindow(parent, windowingType, windowDuration, windowInterval)
                                    .apply(new WindowedBusyWaitFunction(cycles));
                        }
                        else{
                            op = parent
                                    .map(new BusyWaitMap(cycles));
                        }

                        op.setParallelism(cpIterator.next())
                                .name(operatorName);

                        setRouting(op, trafficRouting);
                        operatorsList.add(new MutablePair<>(operatorName, op));
                        operatorID++;
                    }
                }
            }
        }
        return env;

    }

    public static void main(String[] args) throws Exception{

        String yambConfFilePath = args[0];
        //String flinkConfFilePath = args[1];

        //Obtaining Configurations
        Config confParser = new Config(YambConfigSchema.class, yambConfFilePath);
        YambConfigSchema yambConf = (YambConfigSchema) confParser.getConfigSchema();

        if(yambConf != null) {
            confParser.validateConf(yambConf);

            StreamExecutionEnvironment env = buildBenchmarkEnvironment(yambConf);

            String executionName = "flink_bench_" + System.currentTimeMillis();
            env.execute(executionName);

        } else {
            throw new Exception("Something went wrong during configuration loading");
        }

    }
}