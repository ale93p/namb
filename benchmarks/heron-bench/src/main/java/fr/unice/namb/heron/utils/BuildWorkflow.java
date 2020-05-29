package fr.unice.namb.heron.utils;

import java.util.ArrayList;
import java.util.Iterator;

import com.twitter.heron.api.topology.BoltDeclarer;
import com.twitter.heron.api.topology.TopologyBuilder;

import fr.unice.namb.heron.bolts.BusyWaitBolt;
import fr.unice.namb.heron.bolts.WindowedBusyWaitBolt;
import fr.unice.namb.heron.spouts.SyntheticSpout;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.HeronConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;

import static fr.unice.namb.heron.utils.BuildCommons.setRouting;
import static fr.unice.namb.heron.utils.BuildCommons.setWindow;

public class BuildWorkflow {
	public static void build(TopologyBuilder builder, AppBuilder app, NambConfigSchema conf, HeronConfigSchema stormConf) throws IllegalArgumentException, Exception {
		
		double debugFrequency = stormConf.getDebugFrequency();
		
		boolean                 reliability         = conf.getWorkflow().isReliability();

        // DataStream configurations
//        int                         dataSize            = conf.getDatastream().getSynthetic().getData().getSize();
//        int                         dataValues          = conf.getDatastream().getSynthetic().getData().getValues();
//        Config.DataDistribution     dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getDistribution();
//        Config.ArrivalDistribution  distribution        = conf.getDatastream().getSynthetic().getFlow().getDistribution();
//        int                         rate                = conf.getDatastream().getSynthetic().getFlow().getRate();

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

        int windowedTasks       = (app.getDepth() > 3) ? 2 : 1;

        String spoutName;
        // int s: represent the spout ID
        for (int s = 1; s <= numberOfSpouts; s++) {
            spoutName = "spout_" + s;
            spoutsList.add(spoutName);
            builder.setSpout(spoutName, new SyntheticSpout(conf.getDatastream().getSynthetic(), reliability, debugFrequency), cpIterator.next());
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
}
