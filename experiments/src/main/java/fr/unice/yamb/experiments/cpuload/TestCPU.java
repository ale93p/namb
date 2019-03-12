package fr.unice.yamb.experiments.cpuload;

import fr.unice.yamb.experiments.cpuload.bolts.*;
import fr.unice.yamb.experiments.cpuload.spouts.XMLSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;

public class TestCPU {

    private static long tuplesInterval = 0;

    private static void commonFullTopo(TopologyBuilder builder){
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
    }

    private static void busywaitFullTopo(TopologyBuilder builder){
        BusyWaitBolt identityBw =
                new BusyWaitBolt(1);
        identityBw.setName("identity_bw");
        BusyWaitBolt transformationBw =
                new BusyWaitBolt(400);
        transformationBw.setName("transformation_bw");
        BusyWaitBolt filterBw =
                new BusyWaitBolt(.75);
        filterBw.setName("filter_bw");
        BusyWaitBolt aggregationBw =
                new BusyWaitBolt(.8);
        aggregationBw.setName("aggregation_bw");
        BusyWaitBolt rankingBw =
                new BusyWaitBolt(100);
        rankingBw.setName("ranking_bw");

        builder.setSpout("spout", new XMLSpout(tuplesInterval), 1);

        builder.setBolt(identityBw.name(), identityBw, 1)
                .globalGrouping("spout");
        builder.setBolt(transformationBw.name(), transformationBw,1)
                .globalGrouping(identityBw.name());
        builder.setBolt(filterBw.name(), filterBw, 1)
                .globalGrouping(transformationBw.name());
        builder.setBolt(aggregationBw.name(), aggregationBw, 2)
                .shuffleGrouping(filterBw.name());
        builder.setBolt(rankingBw.name(), rankingBw, 1)
                .globalGrouping(aggregationBw.name());
    }

    public static void main(String[] args) throws Exception{
        Config conf = new Config();
        conf.put(Config.TOPOLOGY_WORKERS, 1);

        TopologyBuilder builder = new TopologyBuilder();
        commonFullTopo(builder);
        //busywaitFullTopo(builder);

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
