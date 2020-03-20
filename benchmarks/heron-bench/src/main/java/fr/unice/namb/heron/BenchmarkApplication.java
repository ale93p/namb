package fr.unice.namb.heron;

import fr.unice.namb.heron.utils.BuildPipeline;
import fr.unice.namb.heron.utils.BuildWorkflow;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.HeronConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;

import com.twitter.heron.api.HeronSubmitter;
import com.twitter.heron.api.topology.TopologyBuilder;

public class BenchmarkApplication {

    

    private static TopologyBuilder buildBenchmarkTopology(NambConfigSchema conf, HeronConfigSchema heronConf) throws Exception{

        AppBuilder app = new AppBuilder(conf);

        TopologyBuilder builder = new TopologyBuilder();

        if(! app.isPipelineDefined()) {
        	BuildWorkflow.build(builder, app, conf, heronConf);
        }
        else{
        	BuildPipeline.build(builder, app, heronConf);
        }

        return builder;
    }

    public static void main (String[] args) throws Exception {

        String nambConfFilePath = args[0];
        String heronConfFilePath = args[1];

        // Obtaining Configurations
        Config confParser = new Config(NambConfigSchema.class, nambConfFilePath);
        NambConfigSchema nambConf = (NambConfigSchema) confParser.getConfigSchema();

        Config heronConfigParser = new Config(HeronConfigSchema.class, heronConfFilePath);
        HeronConfigSchema heronConf = (HeronConfigSchema) heronConfigParser.getConfigSchema();

        // Check configuration validity, if something wrong it throws exception
        if(nambConf != null && heronConf != null) {
            confParser.validateConf(nambConf);

            TopologyBuilder builder = buildBenchmarkTopology(nambConf, heronConf);

            if(builder != null){

                com.twitter.heron.api.Config conf = new com.twitter.heron.api.Config();

                if(nambConf.getWorkflow().isReliability()){
                    conf.setMaxSpoutPending(heronConf.getMaxSpoutPending());
                }

                conf.setNumStmgrs(nambConf.getWorkflow().getScalability().getParallelism());

                String topologyName = "namb_bench_" + System.currentTimeMillis();
                HeronSubmitter.submitTopology(topologyName, conf, builder.createTopology());


            }




        } else {
            throw new Exception("Something went wrong during configuration checking");
        }
    }
}

