package fr.unice.yamb.storm;


import fr.unice.yamb.storm.bolts.WindowedBusyWaitBolt;
import fr.unice.yamb.utils.common.AppBuilder;
import fr.unice.yamb.utils.configuration.Config;
import fr.unice.yamb.utils.configuration.schema.YambConfigSchema;
import fr.unice.yamb.utils.configuration.schema.StormConfigSchema;
import fr.unice.yamb.utils.configuration.schema.StormConfigSchema.StormDeployment;
import fr.unice.yamb.storm.bolts.BusyWaitBolt;
import fr.unice.yamb.storm.spouts.SyntheticSpout;

import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt.Duration;
import org.apache.storm.tuple.Fields;

import java.util.ArrayList;
import java.util.Iterator;

public class BenchmarkApplication {

    private static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing, String field){
        switch(routing){
            case hash:
                bolt.partialKeyGrouping(parent, new Fields(field));
                break;
            case balanced:
                bolt.shuffleGrouping(parent);
                break;
            case broadcast:
                bolt.allGrouping(parent);
                break;
        }
    }

    private static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing){
        setRouting(bolt, parent, routing, "value");
    }

    private static void setWindow(WindowedBusyWaitBolt bolt, Config.WindowingType type, int duration, int interval){
        switch(type){
            case tumbling:
                bolt.withTumblingWindow(Duration.seconds(duration));
                break;
            case sliding:
                bolt.withWindow(Duration.seconds(duration), Duration.seconds(interval));
                break;
        }
    }

    private static void setWindow(WindowedBusyWaitBolt bolt, Config.WindowingType type, int duration){
        setWindow(bolt, type, duration, 0);
    }

    private static TopologyBuilder buildBenchmarkTopology(YambConfigSchema conf) throws Exception{


        // DataFlow configurations
        int                     depth               = conf.getDataflow().getDepth();
        int                     totalParallelism    = conf.getDataflow().getScalability().getParallelism();
        Config.ParaBalancing    paraBalancing       = conf.getDataflow().getScalability().getBalancing();
        double                  variability         = conf.getDataflow().getScalability().getVariability();
        Config.ConnectionShape  topologyShape       = conf.getDataflow().getConnection().getShape();
        Config.TrafficRouting   trafficRouting      = conf.getDataflow().getConnection().getRouting();
        boolean                 reliability         = conf.getDataflow().isReliable();
        double                  processingLoad      = conf.getDataflow().getWorkload().getProcessing();
        Config.LoadBalancing    loadBalancing       = conf.getDataflow().getWorkload().getBalancing();

        // DataStream configurations
        int                         dataSize            = conf.getDatastream().getSynthetic().getData().getSize();
        int                         dataValues          = conf.getDatastream().getSynthetic().getData().getValues();
        Config.DataDistribution     dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getDistribution();
        Config.ArrivalDistribution  distribution        = conf.getDatastream().getSynthetic().getFlow().getDistribution();
        int                         rate                = conf.getDatastream().getSynthetic().getFlow().getRate();

        // Generating app builder
        AppBuilder              app                     = new AppBuilder(depth, totalParallelism, paraBalancing, variability, topologyShape, processingLoad, loadBalancing);
        ArrayList<Integer>      dagLevelsWidth          = app.getDagLevelsWidth();
        ArrayList<Integer>      componentsParallelism   = app.getComponentsParallelism();

        int     numberOfSpouts  = dagLevelsWidth.get(0);
        int     numberOfBolts   = app.getTotalComponents() - numberOfSpouts;

        // Windowing
        boolean                 windowingEnabled    = conf.getDataflow().getWindowing().isEnabled();
        Config.WindowingType    windowingType       = conf.getDataflow().getWindowing().getType();
        int                     windowDuration      = conf.getDataflow().getWindowing().getDuration();
        int                     windowInterval      = conf.getDataflow().getWindowing().getInterval();
        int                     windowedTasks       = (depth > 3) ? 2 : 1;

        Iterator<Integer> cpIterator    = componentsParallelism.iterator();
        ArrayList<String> spoutsList    = new ArrayList<>();
        ArrayList<String> boltsList     = new ArrayList<>();

        TopologyBuilder builder = new TopologyBuilder();

        String spoutName;
        // int s: represent the spout ID
        for(int s=1; s<=numberOfSpouts; s++) {
            spoutName = "spout_" + s;
            spoutsList.add(spoutName);
            builder.setSpout(spoutName,  new SyntheticSpout(dataSize, dataValues, dataValuesBalancing, distribution, rate, reliability), cpIterator.next());
        }

        int boltID = 1;
        int cycles;
        String boltName;
        //System.out.println("Topology shape: " + dagLevelsWidth.toString());
        // int i: represent the tree level
        for(int i=1; i<depth; i++){ //TODO: document this section
            int levelWidth = dagLevelsWidth.get(i); // how many bolts are in this level
            boolean isWindowed  = depth - i <= windowedTasks && windowingEnabled; // this level contains windowed tasks

            if (i==1) {
                for (int boltCount=0; boltCount<levelWidth; boltCount++){
                    boltName = "bolt_" + boltID;
                    cycles = app.getNextProcessing();
                    BoltDeclarer boltDeclarer = null;
                    if(isWindowed){
                        WindowedBusyWaitBolt windowedBolt = new WindowedBusyWaitBolt(cycles);
                        setWindow(windowedBolt, windowingType, windowDuration, windowInterval);
                        boltDeclarer = builder.setBolt(boltName, windowedBolt, cpIterator.next());
                    }
                    else{
                        boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, reliability), cpIterator.next());
                    }
                    System.out.print("\n" + boltName + " connects to: ");
                    for(int spout=0; spout<numberOfSpouts; spout++){
                        setRouting(boltDeclarer, spoutsList.get(spout), trafficRouting);
                        System.out.append(spoutsList.get(spout) + " ");
                    }
                    boltsList.add(boltName);
                    boltID++;
                }
            }
            else{
                for (int bolt=0; bolt<levelWidth; bolt++){
                    int startingIdx = app.sumArray(dagLevelsWidth, i-2) - numberOfSpouts;
                    boltName = "bolt_" + boltID;
                    cycles = app.getNextProcessing();
                    BoltDeclarer boltDeclarer = null;
                    if(isWindowed){
                        WindowedBusyWaitBolt windowedBolt = new WindowedBusyWaitBolt(cycles);
                        setWindow(windowedBolt, windowingType, windowDuration, windowInterval);
                        boltDeclarer = builder.setBolt(boltName, windowedBolt, cpIterator.next());
                    }
                    else{
                        boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, reliability), cpIterator.next());
                    }
                    System.out.print("\n" + boltName + " connects to: ");
                    if (topologyShape == Config.ConnectionShape.diamond) {
                        for (int boltCount = 0; boltCount < dagLevelsWidth.get(i - 1); boltCount++) {
                            int parentBoltIdx = startingIdx + boltCount;
                            setRouting(boltDeclarer, boltsList.get(parentBoltIdx), trafficRouting);
                            System.out.append(boltsList.get(parentBoltIdx) + " ");
                        }
                    }
                    else{
                        int parentBoltIdx;
                        if(topologyShape == Config.ConnectionShape.star && i == 2){ // right side of the star
                            parentBoltIdx = 0;
                        }
                        else if(topologyShape == Config.ConnectionShape.star && i == 3) { // first bolt after star
                            parentBoltIdx = 1;
                        }
                        else{
                            parentBoltIdx = boltsList.size() - 1;
                        }
                        setRouting(boltDeclarer, boltsList.get(parentBoltIdx), trafficRouting);
                        System.out.append(boltsList.get(parentBoltIdx) + " ");
                    }
                    boltsList.add(boltName);
                    boltID++;

                }
            }
        }

        return builder;
    }

    public static void main (String[] args) throws Exception{

        String yambConfFilePath = args[0];
        String stormConfFilePath = args[1];

        // Obtaining Configurations
        Config confParser = new Config(YambConfigSchema.class, yambConfFilePath);
        YambConfigSchema yambConf = (YambConfigSchema) confParser.getConfigSchema();

        // Check configuration validity, if something wrong it throws exception
        if(yambConf != null) {
            confParser.validateConf(yambConf);

            TopologyBuilder builder = buildBenchmarkTopology(yambConf);
            if (builder != null) {
                Config stormConfigParser = new Config(StormConfigSchema.class, stormConfFilePath);
                StormConfigSchema stormConf = (StormConfigSchema) stormConfigParser.getConfigSchema();

                if(stormConf != null) {
                    org.apache.storm.Config conf = new org.apache.storm.Config();
                    conf.setNumWorkers(stormConf.getWorkers());

                    if(yambConf.getDataflow().isReliable()){
                        conf.setMaxSpoutPending(stormConf.getMaxSpoutPending());
                    }

                    if (stormConf.getDeployment() == StormDeployment.local) {
                        System.out.println("RUNNING IN LOCAL");
                        LocalCluster cluster = new LocalCluster();
                        cluster.submitTopology("local-testing", conf, builder.createTopology());
                        Thread.sleep(100000); //100s of test duration
                        cluster.shutdown();
                    } else {
                        System.out.println("RUNNING IN CLUSTER MODE");
                        String topologyName = "yamb_bench_" + System.currentTimeMillis();
                        StormSubmitter.submitTopologyWithProgressBar(topologyName, conf, builder.createTopology());
                    }
                }
            } else {
                throw new Exception("Something went wrong during configuration checking");
            }
        }
    }
}

