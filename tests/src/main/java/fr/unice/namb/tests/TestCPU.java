package fr.unice.namb.tests;

import fr.unice.namb.tests.bolts.IdentityBolt;
import fr.unice.namb.tests.spouts.TestingSpout;
import fr.unice.namb.tests.bolts.BaseNamedBolt;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;

public class TestCPU {

    public static void main(String[] args) throws Exception{

        Config conf = new Config();
        conf.put(Config.TOPOLOGY_WORKERS, 1);

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new TestingSpout(), 1);

        IdentityBolt bolt = new IdentityBolt();
        builder.setBolt(bolt.name(), bolt, 1).globalGrouping("spout");

        if (args != null && args.length > 0){
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
            Thread.sleep(10000);
        }
        else {
            System.out.println("RUNNING IN LOCAL");
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("demo", conf, builder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();
        }

    }



}
