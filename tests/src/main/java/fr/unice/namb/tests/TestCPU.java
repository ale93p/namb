package fr.unice.namb.tests;

import fr.unice.namb.tests.bolts.*;
// import fr.unice.namb.tests.spouts.RandomNumberSpout;
import fr.unice.namb.tests.spouts.XMLSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;

public class TestCPU {

    public static void main(String[] args) throws Exception{

        long tuplesInterval = 1;

        Config conf = new Config();
        conf.put(Config.TOPOLOGY_WORKERS, 1);

        TopologyBuilder builder = new TopologyBuilder();

        IdentityBolt identityBolt =
                new IdentityBolt();
        TransformationBolt transformationBolt =
                new TransformationBolt();
        FilterBolt filterBolt =
                new FilterBolt();
        AggregationBolt aggregationBolt =
                new AggregationBolt();
        RankingBolt rankingBolt =
                new RankingBolt();


        builder.setSpout("spout", new XMLSpout(tuplesInterval), 1);
        builder.setBolt(identityBolt.name(), identityBolt, 1)
                .globalGrouping("spout");
        builder.setBolt(transformationBolt.name(), transformationBolt,1)
                .globalGrouping(identityBolt.name());
        builder.setBolt(filterBolt.name(), filterBolt, 1)
                .globalGrouping(transformationBolt.name());
        builder.setBolt(aggregationBolt.name(), aggregationBolt, 2)
                .shuffleGrouping(filterBolt.name());
        builder.setBolt(rankingBolt.name(), rankingBolt, 1)
                .globalGrouping(aggregationBolt.name());

        if (args != null && args.length > 0){
            System.out.println("RUNNING IN REMOTE");
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        }
        else {
            System.out.println("RUNNING IN LOCAL");

            long testDuration = ((10 * 1000) * tuplesInterval) + 60; // (valuesToSave * tuplesPerSave) * timePerTuple
            testDuration = 5 * 60 * 1000; // 5 minutes

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("demo", conf, builder.createTopology());

            Thread.sleep(testDuration);
            cluster.shutdown();
        }

    }



}
