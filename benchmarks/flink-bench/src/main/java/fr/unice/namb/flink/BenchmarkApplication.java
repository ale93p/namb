package fr.unice.namb.flink;

import fr.unice.namb.flink.connectors.SyntheticConnector;
import fr.unice.namb.flink.operators.BusyWaitFlatMap;
import fr.unice.namb.flink.operators.WindowedBusyWaitFunction;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.common.Task;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.FlinkConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BenchmarkApplication {

    private static DataStream<Tuple4<String, String, Long, Long>> setRouting(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing, Object field, boolean apply) throws IllegalArgumentException {
        if (apply){
            switch (routing) {
                case hash:
                    if (field instanceof Integer)
                        return operator.keyBy((int) field);
                    else if (field instanceof String)
                        return operator.keyBy((String) field);
                    else
                        throw new IllegalArgumentException("Field must be <int> or <String> instead it is <" + field.getClass().getName() + ">");
                case balanced:
                    return operator.rebalance();
                case broadcast:
                    return operator.broadcast();
                case none:
                    return operator;
                default:
                    throw new ValueException(routing + " is not a valid routing type");
            }
        }
        return operator;
    }

    private static DataStream<Tuple4<String, String, Long, Long>> setRouting(DataStream<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing, Object field) throws IllegalArgumentException {
        switch (routing) {
            case hash:
                if (field instanceof Integer)
                    return operator.keyBy((int) field);
                else if (field instanceof String)
                    return operator.keyBy((String) field);
                else
                    throw new IllegalArgumentException("Field must be <int> or <String> instead it is <" + field.getClass().getName() + ">");
            case balanced:
                return operator.rebalance();
            case broadcast:
                return operator.broadcast();
            case none:
                return operator;
            default:
                throw new ValueException(routing + " is not a valid routing type");
        }
    }

    private static DataStream<Tuple4<String, String, Long, Long>> setRouting(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing, boolean apply) throws IllegalArgumentException {
        return setRouting(operator, routing, 0, apply);
    }

    private static DataStream<Tuple4<String, String, Long, Long>> setRouting(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing) throws IllegalArgumentException {
        return setRouting(operator, routing, 0, true);
    }

    private static DataStream<Tuple4<String, String, Long, Long>> setRouting(DataStream<Tuple4<String, String, Long, Long>> operator, Config.TrafficRouting routing) throws IllegalArgumentException {
        return setRouting(operator, routing, 0);
    }



    private static AllWindowedStream<Tuple4<String, String, Long, Long>, TimeWindow> setWindow(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> parent, Config.TrafficRouting trafficRouting, Config.WindowingType type, int duration, int interval, boolean applyRouting) {
        switch (type) {
            case tumbling:
                return setRouting(parent, trafficRouting, applyRouting).timeWindowAll(Time.seconds(duration));
            case sliding:
                return setRouting(parent, trafficRouting, applyRouting).timeWindowAll(Time.seconds(duration), Time.seconds(interval));
        }
        return null;
    }

    private static AllWindowedStream<Tuple4<String, String, Long, Long>, TimeWindow> setWindow(DataStream<Tuple4<String, String, Long, Long>> parent, Config.TrafficRouting trafficRouting, Config.WindowingType type, int duration, int interval) {
        switch (type) {
            case tumbling:
                return setRouting(parent, trafficRouting).timeWindowAll(Time.seconds(duration));
            case sliding:
                return setRouting(parent, trafficRouting).timeWindowAll(Time.seconds(duration), Time.seconds(interval));
        }
        return null;
    }

    private static AllWindowedStream<Tuple4<String, String, Long, Long>, TimeWindow> setWindow(SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> parent, Config.TrafficRouting trafficRouting, Config.WindowingType type, int duration, boolean apply){
        return setWindow(parent, trafficRouting, type, duration, 0);
    }

    private static AllWindowedStream<Tuple4<String, String, Long, Long>, TimeWindow> setWindow(DataStream<Tuple4<String, String, Long, Long>> parent, Config.TrafficRouting trafficRouting, Config.WindowingType type, int duration){
        return setWindow(parent, trafficRouting, type, duration, 0);
    }

    private static StreamExecutionEnvironment buildBenchmarkEnvironment(NambConfigSchema conf, double debugFrequency) throws Exception{


        AppBuilder app = new AppBuilder(conf);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        if(! app.isPipelineDefined()) {


            System.out.println("not pipeline");

            // DataFlow configurations
            int depth = conf.getWorkflow().getDepth();
            int totalParallelism = conf.getWorkflow().getScalability().getParallelism();
            Config.ParaBalancing paraBalancing = conf.getWorkflow().getScalability().getBalancing();
            double variability = conf.getWorkflow().getScalability().getVariability();
            Config.ConnectionShape topologyShape = conf.getWorkflow().getConnection().getShape();
            Config.TrafficRouting trafficRouting = conf.getWorkflow().getConnection().getRouting();
            double processingLoad = conf.getWorkflow().getWorkload().getProcessing();
            Config.LoadBalancing loadBalancing = conf.getWorkflow().getWorkload().getBalancing();

            // DataStream configurations
            int dataSize = conf.getDatastream().getSynthetic().getData().getSize();
            int dataValues = conf.getDatastream().getSynthetic().getData().getValues();
            Config.DataDistribution dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getDistribution();
            Config.ArrivalDistribution distribution = conf.getDatastream().getSynthetic().getFlow().getDistribution();
            int rate = conf.getDatastream().getSynthetic().getFlow().getRate();

            // Generating app builder
            ArrayList<Integer> dagLevelsWidth = app.getDagLevelsWidth();
            ArrayList<Integer> componentsParallelism = app.getComponentsParallelism();


            // Bolt-specific configurations
            int numberOfSources = dagLevelsWidth.get(0);
            int numberOfOperators = app.getTotalComponents() - numberOfSources;

            // Windowing
            boolean windowingEnabled = conf.getWorkflow().getWindowing().isEnabled();
            Config.WindowingType windowingType = conf.getWorkflow().getWindowing().getType();
            int windowDuration = conf.getWorkflow().getWindowing().getDuration();
            int windowInterval = conf.getWorkflow().getWindowing().getInterval();
            int windowedTasks = (depth > 3) ? 2 : 1;

            Iterator<Integer> cpIterator = componentsParallelism.iterator();
            ArrayList<MutablePair<String, DataStream<Tuple4<String, String, Long, Long>>>> sourcesList = new ArrayList<>();
            ArrayList<MutablePair<String, SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>>> operatorsList = new ArrayList<>();



            String sourceName;


            for (int s = 1; s <= numberOfSources; s++) {
                sourceName = "source_" + s;
                DataStream<Tuple4<String, String, Long, Long>> source = env.addSource(new SyntheticConnector(dataSize, dataValues, dataValuesBalancing, distribution, rate, debugFrequency, sourceName))
                        .setParallelism(cpIterator.next())
                        .name(sourceName);
                sourcesList.add(new MutablePair<>(sourceName, source));

            }

            if (numberOfSources > 1) {
                sourceName = "unified_source";
                DataStream<Tuple4<String, String, Long, Long>> source = sourcesList.get(0).getRight().union(sourcesList.get(1).getRight());
                for (int s = 2; s < numberOfSources; s++) {
                    source.union(sourcesList.get(s).getRight());
                }
                sourcesList.add(new MutablePair<>(sourceName, source));
            }


            int operatorID = 1;
            int cycles;
            String operatorName;

            for (int i = 1; i < depth; i++) {
                int levelWidth = dagLevelsWidth.get(i);
                boolean isWindowed = depth - i <= windowedTasks && windowingEnabled; // this level contains windowed tasks

                if (i == 1) {
                    for (int opCount = 0; opCount < levelWidth; opCount++) {
                        operatorName = "op_" + operatorID;
                        cycles = app.getNextProcessing();
                        DataStream<Tuple4<String, String, Long, Long>> parent = sourcesList.get(sourcesList.size() - 1).getRight();


                        SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> op = null;
                        if (isWindowed) {
                            operatorName = "windowed-" + operatorName;
                            op = setWindow(parent, trafficRouting, windowingType, windowDuration, windowInterval)
                                    .apply(new WindowedBusyWaitFunction(cycles, debugFrequency));
                        } else {
                            double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                            op = setRouting(parent, trafficRouting)
                                    .flatMap(new BusyWaitFlatMap(cycles, filtering, debugFrequency, operatorName));
                        }

                        op.setParallelism(cpIterator.next())
                                .name(operatorName);

                        operatorsList.add(new MutablePair<>(operatorName, op));
                        operatorID++;
                    }
                } else {
                    if (topologyShape == Config.ConnectionShape.diamond && dagLevelsWidth.get(i - 1) > 1) {// diamond shape union
                        DataStream<Tuple4<String, String, Long, Long>> diamondUnion = operatorsList.get(operatorID - 2).getRight().union(operatorsList.get(operatorID - 3).getRight());
                        //TODO: maybe this can be optimized?
                        for (int o = 2; o < dagLevelsWidth.get(i - 1); o++) {
                            diamondUnion.union(operatorsList.get(o).getRight());
                        }
                        operatorName = "op_" + operatorID;
                        cycles = app.getNextProcessing();


                        SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> op = null;
                        if (isWindowed) {
                            operatorName = "windowed-" + operatorName;
                            op = setWindow(diamondUnion, trafficRouting, windowingType, windowDuration, windowInterval)
                                    .apply(new WindowedBusyWaitFunction(cycles, debugFrequency));
                        } else {
                            double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                            op = setRouting(diamondUnion, trafficRouting)
                                    .flatMap(new BusyWaitFlatMap(cycles, filtering, debugFrequency, operatorName));
                        }

                        op.setParallelism(cpIterator.next())
                                .name(operatorName);

                        operatorsList.add(new MutablePair<>(operatorName, op));
                        operatorID++;
                    } else {
                        int parentOperatorIdx = (topologyShape == Config.ConnectionShape.diamond ||
                                (topologyShape == Config.ConnectionShape.star && i > 3)) ? i - 1 : i - 2;
                        SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> parent = operatorsList.get(parentOperatorIdx).getRight();
                        for (int opCount = 0; opCount < levelWidth; opCount++) {
                            operatorName = "op_" + operatorID;
                            cycles = app.getNextProcessing();

                            SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> op = null;
                            if (isWindowed) {
                                operatorName = "windowed-" + operatorName;
                                op = setWindow(parent, trafficRouting, windowingType, windowDuration, windowInterval, false)
                                        .apply(new WindowedBusyWaitFunction(cycles, debugFrequency));
                            } else {
                                double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                                op = setRouting(parent, Config.TrafficRouting.none)
                                        .flatMap(new BusyWaitFlatMap(cycles, filtering, debugFrequency, operatorName));
//                            op = parent.map(new BusyWaitMap(cycles, debugFrequency));
                            }

                            op.setParallelism(cpIterator.next())
                                    .name(operatorName);

                            operatorsList.add(new MutablePair<>(operatorName, op));
                            operatorID++;
                        }
                    }
                }
            }
        }
        else{



            HashMap<String, Task> pipeline = app.getPipelineTree();
            ArrayList<String> dagLevel = app.getPipelineTreeSources();
            HashMap<String, Object> createdTasks = new HashMap<>();

            while (dagLevel.size() > 0){

                ArrayList<String> nextDagLevel = new ArrayList<>();
                for (String task : dagLevel) {
                    if (!createdTasks.containsKey(task)) {
                        Task newTask = pipeline.get(task);
                        if (newTask.getType() == Config.ComponentType.source) {
                            DataStream<Tuple4<String, String, Long, Long>> source = env.addSource(new SyntheticConnector(newTask.getDataSize(), newTask.getDataValues(), newTask.getDataDistribution(), newTask.getFlowDistribution(), newTask.getFlowRate(), debugFrequency, newTask.getName()))
                                    .setParallelism((int) newTask.getParallelism())
                                    .name(newTask.getName());
                            createdTasks.put(newTask.getName(), source);
                        }
                        else{

                            ArrayList<String> parentsList = newTask.getParents();
                            SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> op = null;
                            DataStream<Tuple4<String, String, Long, Long>> streamUnion = null;
                            if(parentsList.size() > 1) {
                                if( pipeline.get(parentsList.get(0)).getType() == Config.ComponentType.source){

                                    streamUnion = ((DataStream<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(0))).union((DataStream<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(1)));
                                    for (int i=2; i<parentsList.size(); i++) {
                                        streamUnion.union((DataStream<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(i)));
                                    }

                                }
                                else{
                                    streamUnion = ((SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(0))).union((SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(1)));
                                    for (int i=2; i<parentsList.size(); i++) {
                                        streamUnion.union((SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(i)));
                                    }

                                }

                                //TODO: impolement windowing
                                op =  setRouting(streamUnion, newTask.getRouting())
                                        .flatMap(new BusyWaitFlatMap(newTask.getProcessing(), newTask.getFiltering(), newTask.getDataSize(), debugFrequency, newTask.getName()));
                            }
                            else{

                                if( pipeline.get(parentsList.get(0)).getType() == Config.ComponentType.source){
                                    DataStream<Tuple4<String, String, Long, Long>> parent = (DataStream<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(0));

                                    op =  setRouting(parent, newTask.getRouting())
                                            .flatMap(new BusyWaitFlatMap(newTask.getProcessing(), newTask.getFiltering(), newTask.getDataSize(), debugFrequency, newTask.getName()));
                                }
                                else{
                                    SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> parent = (SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(0));

                                    op =  setRouting(parent, newTask.getRouting())
                                            .flatMap(new BusyWaitFlatMap(newTask.getProcessing(), newTask.getFiltering(), newTask.getDataSize(), debugFrequency, newTask.getName()));
                                }

                            }

                            op.setParallelism((int)newTask.getParallelism())
                                    .name(newTask.getName());
                            createdTasks.put(newTask.getName(), op);
                        }

                    }

                    nextDagLevel.addAll(pipeline.get(task).getChilds());
                }
                dagLevel = new ArrayList<>(nextDagLevel);
            }

        }


        return env;

    }

    public static void main(String[] args) throws Exception{

        String nambConfFilePath = args[0];
        String flinkConfFilePath = args[1];

        //Obtaining Configurations
        Config confParser = new Config(NambConfigSchema.class, nambConfFilePath);
        NambConfigSchema nambConf = (NambConfigSchema) confParser.getConfigSchema();

        Config flinkConfigParser = new Config(FlinkConfigSchema.class, flinkConfFilePath);
        FlinkConfigSchema flinkConf = (FlinkConfigSchema) flinkConfigParser.getConfigSchema();

        if(nambConf != null && flinkConf != null) {

            confParser.validateConf(nambConf);

            StreamExecutionEnvironment env = buildBenchmarkEnvironment(nambConf, flinkConf.getDebugFrequency());

            if (env != null){

                String executionName = "namb_bench_" + System.currentTimeMillis();
                env.execute(executionName);
            }


        } else {
            throw new Exception("Something went wrong during configuration loading");
        }

    }
}