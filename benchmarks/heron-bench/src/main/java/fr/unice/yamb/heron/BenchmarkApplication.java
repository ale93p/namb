package fr.unice.yamb.heron;


import com.twitter.heron.api.HeronSubmitter;
import fr.unice.yamb.heron.bolts.BusyWaitBolt;
import fr.unice.yamb.heron.spouts.SyntheticSpout;
import fr.unice.yamb.utils.common.AppBuilder;
import fr.unice.yamb.utils.configuration.Config;
import fr.unice.yamb.utils.configuration.schema.HeronConfigSchema;
import fr.unice.yamb.utils.configuration.schema.YambConfigSchema;
import com.twitter.heron.api.topology.BoltDeclarer;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.tuple.Fields;

import java.util.ArrayList;
import java.util.Iterator;

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

    private static TopologyBuilder buildBenchmarkTopology(YambConfigSchema conf) throws Exception{


        // General configurations
        int                     depth               = conf.getDataflow().getDepth();
        int                     totalParallelism    = conf.getDataflow().getScalability().getParallelism();
        Config.ParaBalancing    paraBalancing       = conf.getDataflow().getScalability().getBalancing();
        Config.ConnectionShape  topologyShape       = conf.getDataflow().getConnection().getShape();
        Config.TrafficRouting   trafficRouting      = conf.getDataflow().getConnection().getRouting();
        int                     processingLoad      = conf.getDataflow().getWorkload().getProcessing();
        Config.LoadBalancing    loadBalancing       = conf.getDataflow().getWorkload().getBalancing();

        // Generating app builder
        AppBuilder              app                     = new AppBuilder(depth, totalParallelism, paraBalancing, topologyShape, processingLoad, loadBalancing);
        ArrayList<Integer>      dagLevelsWidth          = app.getDagLevelsWidth();
        ArrayList<Integer>      componentsParallelism   = app.getComponentsParallelism();

        // Spout-specific configurations
        int                     numberOfSpouts      = dagLevelsWidth.get(0);
        int                     dataSize            = conf.getDatastream().getSynthetic().getData().getSize();
        int                     dataValues          = conf.getDatastream().getSynthetic().getData().getValues();
        Config.DataBalancing    dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getBalancing();
        Config.Distribution     distribution        = conf.getDatastream().getSynthetic().getFlow().getDistribution();
        int                     rate                = conf.getDatastream().getSynthetic().getFlow().getRate();

        // Bolt-specific configurations
        int     numberOfBolts   = app.getTotalComponents() - numberOfSpouts;
        boolean reliability     = conf.getDataflow().isReliable();


        Iterator<Integer> cpIterator    = componentsParallelism.iterator();
        ArrayList<String> spoutsList    = new ArrayList<>();
        ArrayList<String> boltsList     = new ArrayList<>();

        TopologyBuilder builder = new TopologyBuilder();

        String spoutName;
        // int s: represent the spout ID
        for(int s=1; s<=numberOfSpouts; s++) {
            spoutName = "spout_" + s;
            spoutsList.add(spoutName);
            builder.setSpout(spoutName,  new SyntheticSpout(dataSize, dataValues, dataValuesBalancing, distribution, rate), cpIterator.next());
        }

        int boltID = 1;
        int cycles;
        String boltName;
        //System.out.println("Topology shape: " + dagLevelsWidth.toString());
        // int i: represent the tree level
        for(int i=1; i<depth; i++){ //TODO: document this section
            int levelWidth = dagLevelsWidth.get(i); // how many bolts are in this level
            if (i==1) {
                for (int boltCount=0; boltCount<levelWidth; boltCount++){
                    boltName = "bolt_" + boltID;
                    boltsList.add(boltName);
                    cycles = app.getNextProcessing();
                    BoltDeclarer boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, reliability), cpIterator.next());
                    //System.out.print("\n" + boltName + " connects to: ");
                    for(int spout=0; spout<numberOfSpouts; spout++){
                        setRouting(boltDeclarer, spoutsList.get(spout), trafficRouting);
                        //System.out.append(spoutsList.get(spout) + " ");
                    }
                    boltID++;
                }
            }
            else{
                for (int bolt=0; bolt<levelWidth; bolt++){
                    int startingIdx = app.sumArray(dagLevelsWidth, i-2) - numberOfSpouts;
                    //System.out.print("\n" + startingIdx);
                    boltName = "bolt_" + boltID;
                    boltsList.add(boltName);
                    cycles = app.getNextProcessing();
                    BoltDeclarer boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, reliability), cpIterator.next());
                    //System.out.print("\n" + boltName + " connects to: ");
                    if (topologyShape == Config.ConnectionShape.diamond) {
                        for (int boltCount = 0; boltCount < dagLevelsWidth.get(i - 1); boltCount++) {
                            int parentBoltIdx = startingIdx + boltCount;
                            setRouting(boltDeclarer, boltsList.get(parentBoltIdx), trafficRouting);
                            //System.out.append(boltsList.get(parentBoltIdx) + " ");
                        }
                    }
                    else{
                        int parentBoltIdx = boltsList.size() - dagLevelsWidth.get(i-1);
                        setRouting(boltDeclarer, boltsList.get(parentBoltIdx), trafficRouting);
                    }
                    boltID++;

                }
            }
        }
        //System.exit(0);
        return builder;
    }

    public static void main (String[] args) throws Exception{

        String yambConfFilePath = args[0];
        String heronConfFilePath = args[1];

        // Obtaining Configurations
        Config confParser = new Config(YambConfigSchema.class, yambConfFilePath);
        YambConfigSchema yambConf = (YambConfigSchema) confParser.getConfigSchema();

        // Check configuration validity, if something wrong it throws exception
        if(yambConf != null) {
            confParser.validateConf(yambConf);

            TopologyBuilder builder = buildBenchmarkTopology(yambConf);

            if(builder != null){
                Config heronConfigParser = new Config(HeronConfigSchema.class, heronConfFilePath);
                HeronConfigSchema heronConf = (HeronConfigSchema) heronConfigParser.getConfigSchema();

                if (heronConf != null){
                    com.twitter.heron.api.Config conf = new com.twitter.heron.api.Config();

                    if(yambConf.getDataflow().isReliable()){
                        conf.setMaxSpoutPending(heronConf.getMaxSpoutPending());
                    }

                    String topologyName = "heron_bench_" + System.currentTimeMillis();
                    HeronSubmitter.submitTopology(topologyName, conf, builder.createTopology());

                }
            }




        } else {
            throw new Exception("Something went wrong during configuration checking");
        }
    }
}

