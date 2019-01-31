package fr.unice.namb.storm;


import fr.unice.namb.common.ConfigParser;
import fr.unice.namb.common.ConfigScheme;
import org.apache.storm.Config;
import org.apache.storm.topology.TopologyBuilder;

import java.io.File;

public class BenchmarkApplication {

    private static void buildBenchmarkTopology(ConfigScheme conf){
        // TopologyBuilder builder = new TopologyBuilder();


        // return builder;
    }

    public static void main (String[] args) throws Exception{

        // Obtaining Configurations
        ConfigScheme benchConf = ConfigParser.parseConfigurationFile(new File("conf/default.yml"));

        System.out.println(benchConf.getDataflow().isTraffic_balancing());

        //Config stormConf = new Config();

        //TopologyBuilder builder = buildBenchmarkTopology(benchConf);

        //TODO: submit topology to cluster

    }
}
