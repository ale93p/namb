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
        int                     numberOfSources      = dagLevelsWidth.get(0);
        int                     dataSize            = conf.getDatastream().getSynthetic().getData().getSize();
        int                     dataValues          = conf.getDatastream().getSynthetic().getData().getValues();
        Config.DataBalancing    dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getBalancing();
        Config.Distribution     distribution        = conf.getDatastream().getSynthetic().getFlow().getDistribution();
        int                     rate                = conf.getDatastream().getSynthetic().getFlow().getRate();

        // Bolt-specific configurations
        int     numberOfOperators   = app.getTotalComponents() - numberOfSources;
        boolean reliability         = conf.getDataflow().isReliable();

        Iterator<Integer> cpIterator    = componentsParallelism.iterator();
        ArrayList<MutablePair<String, SingleOutputStreamOperator>> sourcesList = new ArrayList<>();
        ArrayList<MutablePair<String, SingleOutputStreamOperator>> operatorsList = new ArrayList<>();


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String sourceName = "source";
        DataStream<String> source = env.addSource(new SyntheticConnector(dataSize, dataValues, dataValuesBalancing, distribution, rate))
                                        .setParallelism(cpIterator.next())
                                        .name("source1");

        int operatorID = 1;
        int cycles;
        String operatorName;

        for(int i = 1; i<depth; i++){
            SingleOutputStreamOperator op = null;
            operatorName = "op_" + operatorID;
            cycles = app.getNextProcessing();
            if(i==1) {
                 op = source
                         .map(new BusyWaitMap(cycles))
                         .setParallelism(cpIterator.next())
                         .name(operatorName);
            }
            else{
                 op = operatorsList.get(i-2).getRight()
                         .map(new BusyWaitMap(cycles))
                         .setParallelism(cpIterator.next())
                         .name(operatorName);

            }
            operatorsList.add(new MutablePair<String, SingleOutputStreamOperator>(operatorName, op));
            operatorID++;
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