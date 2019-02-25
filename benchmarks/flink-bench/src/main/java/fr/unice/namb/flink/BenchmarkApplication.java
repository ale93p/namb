package fr.unice.namb.flink;

import fr.unice.namb.flink.connectors.SyntheticConnector;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class BenchmarkApplication {



    public static void main(String[] args) throws Exception{

        String nambConfFilePath = args[0];
        //String flinkConfFilePath = args[1];

        //Obtaining Configurations
        Config confParser = new Config(NambConfigSchema.class, nambConfFilePath);
        NambConfigSchema nambConf = (NambConfigSchema) confParser.getConfigSchema();

        if(nambConf != null) {
            confParser.validateConf(nambConf);

            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


            // SyntheticConnector source = new SyntheticConnector()

            // DataStream<String> input = env.addSource();

        } else {
            throw new Exception("Something went wrong during configuration loading");
        }



    }
}
