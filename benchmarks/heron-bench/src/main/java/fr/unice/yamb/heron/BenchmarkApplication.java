package fr.unice.yamb.heron;



import fr.unice.yamb.heron.bolts.BusyWaitBolt;
import fr.unice.yamb.heron.bolts.WindowedBusyWaitBolt;
import fr.unice.yamb.heron.spouts.SyntheticSpout;
import fr.unice.yamb.utils.common.AppBuilder;
import fr.unice.yamb.utils.configuration.Config;
import fr.unice.yamb.utils.configuration.schema.HeronConfigSchema;
import fr.unice.yamb.utils.configuration.schema.YambConfigSchema;

import com.twitter.heron.api.HeronSubmitter;
import com.twitter.heron.api.topology.BoltDeclarer;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.tuple.Fields;

import java.util.ArrayList;
import java.util.Iterator;
import java.time.Duration;

public class BenchmarkApplication {

    private static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing, String field){
        switch(routing){
            case hash:
                bolt.fieldsGrouping(parent, new Fields(field));
                break;
            case balanced:
                bolt.shuffleGrouping(parent);
            case broadcast:
                bolt.allGrouping(parent);
        }
    }

    private static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing){
        setRouting(bolt, parent, routing, "value");
    }

    private static void setWindow(WindowedBusyWaitBolt bolt, Config.WindowingType type, int duration, int interval){
        switch(type){
            case tumbling:
                bolt.withTumblingWindow(Duration.ofSeconds(duration));
                break;
            case sliding:
                bolt.withWindow(Duration.ofSeconds(duration), Duration.ofSeconds(interval));
                break;
        }
    }

    private static void setWindow(WindowedBusyWaitBolt bolt, Config.WindowingType type, int duration){
        setWindow(bolt, type, duration, 0);
    }

    private static TopologyBuilder buildBenchmarkTopology(YambConfigSchema conf, float debugFrequence) throws Exception{


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

        int                     dataSize            = conf.getDatastream().getSynthetic().getData().getSize();
        int                     dataValues          = conf.getDatastream().getSynthetic().getData().getValues();
        Config.DataDistribution    dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getDistribution();
        Config.ArrivalDistribution     distribution        = conf.getDatastream().getSynthetic().getFlow().getDistribution();
        int                     rate                = conf.getDatastream().getSynthetic().getFlow().getRate();

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
            builder.setSpout(spoutName,  new SyntheticSpout(dataSize, dataValues, dataValuesBalancing, distribution, rate, reliability, debugFrequence), cpIterator.next());
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
                        WindowedBusyWaitBolt windowedBolt = new WindowedBusyWaitBolt(cycles, debugFrequence);
                        setWindow(windowedBolt, windowingType, windowDuration, windowInterval);
                        boltDeclarer = builder.setBolt(boltName, windowedBolt, cpIterator.next());
                    }
                    else{
                        boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, reliability, debugFrequence), cpIterator.next());
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
                    boltName = "bolt_" + boltID;
                    cycles = app.getNextProcessing();
                    BoltDeclarer boltDeclarer = null;
                    if(isWindowed){
                        WindowedBusyWaitBolt windowedBolt = new WindowedBusyWaitBolt(cycles, debugFrequence);
                        setWindow(windowedBolt, windowingType, windowDuration, windowInterval);
                        boltDeclarer = builder.setBolt(boltName, windowedBolt, cpIterator.next());
                    }
                    else{
                        boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, reliability, debugFrequence), cpIterator.next());
                    }System.out.print("\n" + boltName + " connects to: ");
                    if (topologyShape == Config.ConnectionShape.diamond) {
                        for (int boltCount = 0; boltCount < dagLevelsWidth.get(i - 1); boltCount++) {
                            int startingIdx = app.sumArray(dagLevelsWidth, i-2) - numberOfSpouts;
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
        System.exit(0);
        return builder;
    }

    public static void main (String[] args) throws Exception {

        String yambConfFilePath = args[0];
        String heronConfFilePath = args[1];

        // Obtaining Configurations
        Config confParser = new Config(YambConfigSchema.class, yambConfFilePath);
        YambConfigSchema yambConf = (YambConfigSchema) confParser.getConfigSchema();

        Config heronConfigParser = new Config(HeronConfigSchema.class, heronConfFilePath);
        HeronConfigSchema heronConf = (HeronConfigSchema) heronConfigParser.getConfigSchema();

        // Check configuration validity, if something wrong it throws exception
        if(yambConf != null && heronConf != null) {
            confParser.validateConf(yambConf);

            TopologyBuilder builder = buildBenchmarkTopology(yambConf, heronConf.getDebugFrequency());

            if(builder != null){

                com.twitter.heron.api.Config conf = new com.twitter.heron.api.Config();

                if(yambConf.getDataflow().isReliable()){
                    conf.setMaxSpoutPending(heronConf.getMaxSpoutPending());
                }

                conf.setNumStmgrs(yambConf.getDataflow().getScalability().getParallelism());

                String topologyName = "yamb_bench_" + System.currentTimeMillis();
                HeronSubmitter.submitTopology(topologyName, conf, builder.createTopology());


            }




        } else {
            throw new Exception("Something went wrong during configuration checking");
        }
    }
}

