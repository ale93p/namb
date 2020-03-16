package fr.unice.namb.flink;

import fr.unice.namb.flink.utils.BuildPipeline;
import fr.unice.namb.flink.utils.BuildWorkflow;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.FlinkConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
public class BenchmarkApplication {

    private static StreamExecutionEnvironment buildBenchmarkEnvironment(NambConfigSchema conf, FlinkConfigSchema flinkConf) throws Exception{


        AppBuilder app = new AppBuilder(conf);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        if(! app.isPipelineDefined()) {
            BuildWorkflow.build(env, app, conf, flinkConf);
        }
        else{
            BuildPipeline.build(env, app, conf, flinkConf);
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

            StreamExecutionEnvironment env = buildBenchmarkEnvironment(nambConf, flinkConf);

            if (env != null){

                String executionName = "namb_bench_" + System.currentTimeMillis();
                env.execute(executionName);
            }


        } else {
            throw new Exception("Something went wrong during configuration loading");
        }

    }
}