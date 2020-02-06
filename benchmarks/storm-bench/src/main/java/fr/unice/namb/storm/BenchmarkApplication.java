package fr.unice.namb.storm;

import fr.unice.namb.storm.bolts.WindowedBusyWaitBolt;
import fr.unice.namb.storm.utils.BuildPipeline;
import fr.unice.namb.storm.utils.BuildWorkflow;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import fr.unice.namb.utils.configuration.schema.StormConfigSchema;
import fr.unice.namb.utils.configuration.schema.StormConfigSchema.StormDeployment;

import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt.Duration;
import org.apache.storm.tuple.Fields;

public class BenchmarkApplication {



    private static TopologyBuilder buildBenchmarkTopology(NambConfigSchema conf, StormConfigSchema stormConf) throws Exception{

        // Generating app builder
        AppBuilder app = new AppBuilder(conf);

        TopologyBuilder builder = new TopologyBuilder();

        if(! app.isPipelineDefined()) {
            BuildWorkflow.build(builder, app, conf, stormConf);
        }
        else{
            BuildPipeline.build(builder, app, stormConf);
        }

        return builder;
    }


    public static void main (String[] args) throws Exception{

        String nambConfFilePath = args[0];
        String stormConfFilePath = args[1];

        // Obtaining Configurations
        Config confParser = new Config(NambConfigSchema.class, nambConfFilePath);
        NambConfigSchema nambConf = (NambConfigSchema) confParser.getConfigSchema();

        Config stormConfigParser = new Config(StormConfigSchema.class, stormConfFilePath);
        StormConfigSchema stormConf = (StormConfigSchema) stormConfigParser.getConfigSchema();

        // Check configuration validity, if something wrong it throws exception
        if(nambConf != null && stormConf != null) {
            confParser.validateConf(nambConf);


            TopologyBuilder builder = buildBenchmarkTopology(nambConf, stormConf);
            if (builder != null) {

                org.apache.storm.Config conf = new org.apache.storm.Config();
                conf.setNumWorkers(stormConf.getWorkers());

                if (nambConf.getWorkflow().isReliability()) {
                    conf.setMaxSpoutPending(stormConf.getMaxSpoutPending());
                }

                if (stormConf.getDeployment() == StormDeployment.local) {
                    LocalCluster cluster = new LocalCluster();
                    cluster.submitTopology("local-testing", conf, builder.createTopology());
                    Thread.sleep(100000); //100s of test duration
                    cluster.shutdown();
                } else {
                    String topologyName = "namb_bench_" + System.currentTimeMillis();
                    StormSubmitter.submitTopologyWithProgressBar(topologyName, conf, builder.createTopology());
                }
            } else {
                throw new Exception("Something went wrong during topology building");
            }
        }
    }
}

