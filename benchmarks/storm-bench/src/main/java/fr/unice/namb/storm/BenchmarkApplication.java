package fr.unice.namb.storm;


import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import fr.unice.namb.utils.configuration.schema.StormConfigSchema;
import fr.unice.namb.utils.configuration.schema.StormConfigSchema.StormDeployment;
import fr.unice.namb.storm.bolts.BusyWaitBolt;
import fr.unice.namb.storm.spouts.SyntheticSpout;

import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.ArrayList;
import java.util.Iterator;

public class BenchmarkApplication {

    private static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing, String field){
        switch(routing){
            case hash:
                bolt.partialKeyGrouping(parent, new Fields(field));
                break;
            case shuffle:
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

    private static TopologyBuilder buildBenchmarkTopology(NambConfigSchema conf) throws Exception{


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

        String nambConfFilePath = args[0];
        String stormConfFilePath = args[1];

        // Obtaining Configurations
        Config confParser = new Config(NambConfigSchema.class, nambConfFilePath);
        NambConfigSchema nambConf = (NambConfigSchema) confParser.getConfigSchema();

        // Check configuration validity, if something wrong it throws exception
        if(nambConf != null) {
            confParser.validateConf(nambConf);

            TopologyBuilder builder = buildBenchmarkTopology(nambConf);
            if (builder != null) {
                Config stormConfigParser = new Config(StormConfigSchema.class, stormConfFilePath);
                StormConfigSchema stormConf = (StormConfigSchema) stormConfigParser.getConfigSchema();

                if(stormConf != null) {
                    org.apache.storm.Config conf = new org.apache.storm.Config();
                    conf.setNumWorkers(stormConf.getWorkers());

                    if (stormConf.getDeployment() == StormDeployment.local) {
                        System.out.println("RUNNING IN LOCAL");
                        LocalCluster cluster = new LocalCluster();
                        cluster.submitTopology("local-testing", conf, builder.createTopology());
                        Thread.sleep(100000); //100s of test duration
                        cluster.shutdown();
                    } else {
                        System.out.println("RUNNING IN CLUSTER MODE");
                        String topologyName = "namb_bench_" + System.currentTimeMillis();
                        StormSubmitter.submitTopologyWithProgressBar(topologyName, conf, builder.createTopology());
                    }
                }
            } else {
                throw new Exception("Something went wrong during configuration checking");
            }
        }
    }
}

