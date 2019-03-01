package fr.unice.namb.flink;

import fr.unice.namb.flink.connectors.SyntheticConnector;
import fr.unice.namb.flink.operators.BusyWaitMap;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class BenchmarkApplication {

    private static StreamExecutionEnvironment buildBenchmarkEnvironment(NambConfigSchema conf) throws Exception{

        // General configurations
        int                     depth               = conf.getDataflow().getDepth();
        int                     totalParallelism    = conf.getDataflow().getScalability().getParallelism();
        Config.ParaBalancing    paraBalancing       = conf.getDataflow().getScalability().getBalancing();
        Config.ConnectionShape  topologyShape       = conf.getDataflow().getConnection().getShape();
        Config.TrafficRouting   trafficRouting      = conf.getDataflow().getConnection().getRouting();
        int                     processingLoad      = conf.getDataflow().getWorkload().getProcessing();
        Config.LoadBalancing    loadBalancing       = conf.getDataflow().getWorkload().getBalancing();

        // Generating app builder
        AppBuilder app                                  = new AppBuilder(depth, totalParallelism, paraBalancing, topologyShape, processingLoad, loadBalancing);
        ArrayList<Integer> dagLevelsWidth               = app.getDagLevelsWidth();
        ArrayList<Integer>      componentsParallelism   = app.getComponentsParallelism();

        // Spout-specific configurations
        int                     numberOfSources     = dagLevelsWidth.get(0);
        int                     dataSize            = conf.getDatastream().getSynthetic().getData().getSize();
        int                     dataValues          = conf.getDatastream().getSynthetic().getData().getValues();
        Config.DataBalancing    dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getBalancing();
        Config.Distribution     distribution        = conf.getDatastream().getSynthetic().getFlow().getDistribution();
        int                     rate                = conf.getDatastream().getSynthetic().getFlow().getRate();

        // Bolt-specific configurations
        int     numberOfOperators   = app.getTotalComponents() - numberOfSources;
        boolean reliability         = conf.getDataflow().isReliable();

        Iterator<Integer> cpIterator    = componentsParallelism.iterator();
        ArrayList<MutablePair<String, DataStream<String>>> sourcesList = new ArrayList<>();
        ArrayList<MutablePair<String, SingleOutputStreamOperator<String>>> operatorsList = new ArrayList<>();


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String sourceName;

        for(int s=1; s<=numberOfSources; s++){
            sourceName = "source_" + s;
            DataStream<String> source = env.addSource(new SyntheticConnector(dataSize, dataValues, dataValuesBalancing, distribution, rate))
                    .setParallelism(cpIterator.next())
                    .name(sourceName);
            sourcesList.add(new MutablePair<>(sourceName, source));

        }

        if (numberOfSources > 1){
            sourceName = "unified_source";
            DataStream<String> unifiedSource = sourcesList.get(0).getRight().union(sourcesList.get(1).getRight());
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
            SingleOutputStreamOperator<String> op = null;
            if(i==1) {
                for(int opCount=0; opCount<levelWidth; opCount++) {
                    operatorName = "op_" + operatorID;
                    cycles = app.getNextProcessing();
                    op = sourcesList.get(sourcesList.size() - 1).getRight()
                            .map(new BusyWaitMap(cycles))
                            .setParallelism(cpIterator.next())
                            .name(operatorName);
                    operatorsList.add(new MutablePair<>(operatorName, op));
                    operatorID++;
                }
            }
            else{
                if(topologyShape == Config.ConnectionShape.diamond && dagLevelsWidth.get(i-1) > 1){ // diamond shape union
                    DataStream<String> diamondUnion = operatorsList.get(operatorID - 2).getRight().union(operatorsList.get(operatorID - 3).getRight());
                    for(int o=2; o<dagLevelsWidth.get(i-1); o++){
                        diamondUnion.union(operatorsList.get(o).getRight());
                    }
                    operatorName = "op_" + operatorID;
                    cycles = app.getNextProcessing();
                    op = diamondUnion
                            .map(new BusyWaitMap(cycles))
                            .setParallelism(cpIterator.next())
                            .name(operatorName);
                    operatorsList.add(new MutablePair<>(operatorName, op));
                    operatorID++;
                }
                else{
                    int parentOperatorIdx = (topologyShape == Config.ConnectionShape.diamond ||
                            (topologyShape == Config.ConnectionShape.star && i>3)) ? i - 1 : i - 2;
                    SingleOutputStreamOperator<String> parent = operatorsList.get(parentOperatorIdx).getRight();
                    for(int opCount = 0; opCount<levelWidth; opCount++){
                        operatorName = "op_" + operatorID;
                        cycles = app.getNextProcessing();
                        op = parent
                                .map(new BusyWaitMap(cycles))
                                .setParallelism(cpIterator.next())
                                .name(operatorName);
                        operatorsList.add(new MutablePair<>(operatorName, op));
                        operatorID++;
                    }
                }
            }
        }
        return env;

    }

    public static void main(String[] args) throws Exception{

        String nambConfFilePath = args[0];
        //String flinkConfFilePath = args[1];

        //Obtaining Configurations
        Config confParser = new Config(NambConfigSchema.class, nambConfFilePath);
        NambConfigSchema nambConf = (NambConfigSchema) confParser.getConfigSchema();

        if(nambConf != null) {
            confParser.validateConf(nambConf);

            StreamExecutionEnvironment env = buildBenchmarkEnvironment(nambConf);

            String executionName = "flink_bench_" + System.currentTimeMillis();
            env.execute(executionName);

        } else {
            throw new Exception("Something went wrong during configuration loading");
        }

    }
}