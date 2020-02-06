package fr.unice.namb.heron;



import com.twitter.heron.api.topology.SpoutDeclarer;
import fr.unice.namb.heron.bolts.BusyWaitBolt;
import fr.unice.namb.heron.bolts.WindowedBusyWaitBolt;
import fr.unice.namb.heron.spouts.SyntheticSpout;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.common.Task;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.HeronConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;

import com.twitter.heron.api.HeronSubmitter;
import com.twitter.heron.api.topology.BoltDeclarer;
import com.twitter.heron.api.topology.TopologyBuilder;
import com.twitter.heron.api.tuple.Fields;

import java.util.ArrayList;
import java.util.HashMap;
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

    private static TopologyBuilder buildBenchmarkTopology(NambConfigSchema conf, double debugFrequency) throws Exception{

        // Generating app builder
        AppBuilder app = new AppBuilder(conf);


        TopologyBuilder builder = new TopologyBuilder();

        if(! app.isPipelineDefined()) {
            boolean                 reliability         = conf.getWorkflow().isReliability();

            // DataStream configurations
            int                         dataSize            = conf.getDatastream().getSynthetic().getData().getSize();
            int                         dataValues          = conf.getDatastream().getSynthetic().getData().getValues();
            Config.DataDistribution     dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getDistribution();
            Config.ArrivalDistribution  distribution        = conf.getDatastream().getSynthetic().getFlow().getDistribution();
            int                         rate                = conf.getDatastream().getSynthetic().getFlow().getRate();

            ArrayList<Integer>      dagLevelsWidth          = app.getDagLevelsWidth();
            ArrayList<Integer>      componentsParallelism   = app.getComponentsParallelism();
            ArrayList<Integer>      componentsLoad          = app.getComponentsLoad();

            // Windowing
            boolean                 windowingEnabled    = conf.getWorkflow().getWindowing().isEnabled();
            Config.WindowingType    windowingType       = conf.getWorkflow().getWindowing().getType();
            int                     windowDuration      = conf.getWorkflow().getWindowing().getDuration();
            int                     windowInterval      = conf.getWorkflow().getWindowing().getInterval();

            int     numberOfSpouts  = dagLevelsWidth.get(0);

            Iterator<Integer> cpIterator    = componentsParallelism.iterator();
            Iterator<Integer> componentLoad = componentsLoad.iterator();
            ArrayList<String> spoutsList    = new ArrayList<>();
            ArrayList<String> boltsList     = new ArrayList<>();

            int                     windowedTasks       = (app.getDepth() > 3) ? 2 : 1;

            String spoutName;
            // int s: represent the spout ID
            for (int s = 1; s <= numberOfSpouts; s++) {
                spoutName = "spout_" + s;
                spoutsList.add(spoutName);
                builder.setSpout(spoutName, new SyntheticSpout(dataSize, dataValues, dataValuesBalancing, distribution, rate, reliability, debugFrequency), cpIterator.next());
            }

            int boltID = 1;
            long cycles;
            String boltName;
            //System.out.println("Topology shape: " + dagLevelsWidth.toString());
            // int i: represent the tree level
            for (int i = 1; i < app.getDepth(); i++) { //TODO: document this section
                int levelWidth = dagLevelsWidth.get(i); // how many bolts are in this level
                boolean isWindowed = app.getDepth() - i <= windowedTasks && windowingEnabled; // this level contains windowed tasks

                if (i == 1) {
                    for (int boltCount = 0; boltCount < levelWidth; boltCount++) {
                        boltName = "bolt_" + boltID;
                        cycles = componentLoad.next();
                        BoltDeclarer boltDeclarer = null;
                        if (isWindowed) {
                            boltName = "windowed-" + boltName;
                            WindowedBusyWaitBolt windowedBolt = new WindowedBusyWaitBolt(cycles, debugFrequency);
                            setWindow(windowedBolt, windowingType, windowDuration, windowInterval);
                            boltDeclarer = builder.setBolt(boltName, windowedBolt, cpIterator.next());
                        } else {
                            double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                            boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, filtering, reliability, debugFrequency), cpIterator.next());
                        }
                        for (int spout = 0; spout < numberOfSpouts; spout++) {
                            setRouting(boltDeclarer, spoutsList.get(spout), app.getTrafficRouting());
                            System.out.append(spoutsList.get(spout) + " ");
                        }
                        boltsList.add(boltName);
                        boltID++;
                    }
                } else {
                    for (int bolt = 0; bolt < levelWidth; bolt++) {
                        int startingIdx = app.sumArray(dagLevelsWidth, i - 2) - numberOfSpouts;
                        boltName = "bolt_" + boltID;
                        cycles = componentLoad.next();
                        BoltDeclarer boltDeclarer = null;
                        if (isWindowed) {
                            boltName = "windowed-" + boltName;
                            WindowedBusyWaitBolt windowedBolt = new WindowedBusyWaitBolt(cycles, debugFrequency);
                            setWindow(windowedBolt, windowingType, windowDuration, windowInterval);
                            boltDeclarer = builder.setBolt(boltName, windowedBolt, cpIterator.next());
                        } else {
                            double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                            boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, filtering, reliability, debugFrequency), cpIterator.next());
                        }
                        if (app.getShape() == Config.ConnectionShape.diamond) {
                            for (int boltCount = 0; boltCount < dagLevelsWidth.get(i - 1); boltCount++) {
                                int parentBoltIdx = startingIdx + boltCount;
                                setRouting(boltDeclarer, boltsList.get(parentBoltIdx), app.getTrafficRouting());
                                System.out.append(boltsList.get(parentBoltIdx) + " ");
                            }
                        } else {
                            int parentBoltIdx;
                            if (app.getShape() == Config.ConnectionShape.star && i == 2) { // right side of the star
                                parentBoltIdx = 0;
                            } else if (app.getShape() == Config.ConnectionShape.star && i == 3) { // first bolt after star
                                parentBoltIdx = 1;
                            } else {
                                parentBoltIdx = boltsList.size() - 1;
                            }
                            setRouting(boltDeclarer, boltsList.get(parentBoltIdx), app.getTrafficRouting());
                            System.out.append(boltsList.get(parentBoltIdx) + " ");
                        }
                        boltsList.add(boltName);
                        boltID++;

                    }
                }
            }
        }
        else{
            HashMap<String, Task> pipeline = app.getPipelineTree();
            ArrayList<String> dagLevel = app.getPipelineTreeSources();
            HashMap<String, Object> createdTasks = new HashMap<>();

            while (dagLevel.size() > 0) {
                ArrayList<String> nextDagLevel = new ArrayList<>();
                for (String task : dagLevel) {
                    if (!createdTasks.containsKey(task)) {
                        Task newTask = pipeline.get(task);
                        if (newTask.getType() == Config.ComponentType.source) {
                            SpoutDeclarer spout = builder.setSpout(newTask.getName(), new SyntheticSpout(newTask.getDataSize(), newTask.getDataValues(),
                                    newTask.getDataDistribution(), newTask.getFlowDistribution(), newTask.getFlowRate(), newTask.isReliable(), debugFrequency), newTask.getParallelism());
                            createdTasks.put(newTask.getName(), spout);
                        } else {
                            //TODO add windowing

//                            System.out.println(newTask.getName() + " has datasize " + newTask.getDataSize());


                            BoltDeclarer boltDeclarer = builder.setBolt(newTask.getName(), new BusyWaitBolt(newTask.getProcessing(), newTask.getFiltering(), newTask.isReliable(), newTask.getDataSize(), debugFrequency), newTask.getParallelism());
                            for (String parent : newTask.getParents()) {
                                setRouting(boltDeclarer, parent, newTask.getRouting());
                            }
                            createdTasks.put(newTask.getName(), boltDeclarer);
                        }
                    }
                    nextDagLevel.addAll(pipeline.get(task).getChilds());
                }
                dagLevel = new ArrayList<>(nextDagLevel);

            }
        }

        return builder;
    }

    public static void main (String[] args) throws Exception {

        String nambConfFilePath = args[0];
        String heronConfFilePath = args[1];

        // Obtaining Configurations
        Config confParser = new Config(NambConfigSchema.class, nambConfFilePath);
        NambConfigSchema nambConf = (NambConfigSchema) confParser.getConfigSchema();

        Config heronConfigParser = new Config(HeronConfigSchema.class, heronConfFilePath);
        HeronConfigSchema heronConf = (HeronConfigSchema) heronConfigParser.getConfigSchema();

        // Check configuration validity, if something wrong it throws exception
        if(nambConf != null && heronConf != null) {
            confParser.validateConf(nambConf);

            TopologyBuilder builder = buildBenchmarkTopology(nambConf, heronConf.getDebugFrequency());

            if(builder != null){

                com.twitter.heron.api.Config conf = new com.twitter.heron.api.Config();

                if(nambConf.getWorkflow().isReliability()){
                    conf.setMaxSpoutPending(heronConf.getMaxSpoutPending());
                }

                conf.setNumStmgrs(nambConf.getWorkflow().getScalability().getParallelism());

                String topologyName = "namb_bench_" + System.currentTimeMillis();
                HeronSubmitter.submitTopology(topologyName, conf, builder.createTopology());


            }




        } else {
            throw new Exception("Something went wrong during configuration checking");
        }
    }
}

