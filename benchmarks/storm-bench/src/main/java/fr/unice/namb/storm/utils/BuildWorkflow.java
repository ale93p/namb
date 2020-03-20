package fr.unice.namb.storm.utils;

import fr.unice.namb.storm.bolts.BusyWaitBolt;
import fr.unice.namb.storm.bolts.WindowedBusyWaitBolt;
import fr.unice.namb.storm.spouts.SyntheticSpout;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import fr.unice.namb.utils.configuration.schema.StormConfigSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.TopologyBuilder;

import java.util.ArrayList;
import java.util.Iterator;

import static fr.unice.namb.storm.utils.BuildCommons.setRouting;
import static fr.unice.namb.storm.utils.BuildCommons.setWindow;

public class BuildWorkflow {

    public static void build(TopologyBuilder builder, AppBuilder app, NambConfigSchema conf, StormConfigSchema stormConf) throws IllegalArgumentException, Exception {

        /*
            Workflow Schema Translation
             */

        double debugFrequency = stormConf.getDebugFrequency();

        boolean reliability = conf.getWorkflow().isReliability();

        // DataStream configurations
//        int dataSize = conf.getDatastream().getSynthetic().getData().getSize();
//        int dataValues = conf.getDatastream().getSynthetic().getData().getValues();
//        Config.DataDistribution dataValuesBalancing = conf.getDatastream().getSynthetic().getData().getDistribution();
//        Config.ArrivalDistribution distribution = conf.getDatastream().getSynthetic().getFlow().getDistribution();
//        int rate = conf.getDatastream().getSynthetic().getFlow().getRate();

        ArrayList<Integer> dagLevelsWidth = app.getDagLevelsWidth();
        ArrayList<Integer> componentsParallelism = app.getComponentsParallelism();
        ArrayList<Integer> componentsLoad = app.getComponentsLoad();
        Iterator<Integer> componentParallelism = componentsParallelism.iterator();
        Iterator<Integer> componentLoad = componentsLoad.iterator();

        int numberOfSpouts = dagLevelsWidth.get(0);

        // Windowing
        boolean windowingEnabled = conf.getWorkflow().getWindowing().isEnabled();
        Config.WindowingType windowingType = conf.getWorkflow().getWindowing().getType();
        int windowDuration = conf.getWorkflow().getWindowing().getDuration();
        int windowInterval = conf.getWorkflow().getWindowing().getInterval();

        int windowedTasks = (app.getDepth() > 3) ? 2 : 1;

        ArrayList<String> spoutsList = new ArrayList<>();
        ArrayList<String> boltsList = new ArrayList<>();

        String spoutName;
        // int s: represent the spout ID
        if (app.isExternalSource()) {
            int s = 1;
            spoutName = "spout_" + s;
            spoutsList.add(spoutName);
            KafkaSpoutConfig<String, String> kafkaConfig = KafkaSpoutConfig.builder(app.getKafkaServer(), app.getKafkaTopic())
                    .setFirstPollOffsetStrategy(KafkaSpoutConfig.FirstPollOffsetStrategy.LATEST)
                    .setProp(ConsumerConfig.GROUP_ID_CONFIG, app.getKafkaServer())
                    .setRecordTranslator(new KafkaRecordTranslator(debugFrequency, spoutName))
                    .build();
            builder.setSpout(spoutName, new KafkaSpout<>(kafkaConfig), componentParallelism.next());

        } else {
            for (int s = 1; s <= numberOfSpouts; s++) {
                spoutName = "spout_" + s;
                spoutsList.add(spoutName);
                builder.setSpout(spoutName, new SyntheticSpout(conf.getDatastream().getSynthetic(), reliability, debugFrequency), componentParallelism.next());
            }
        }

        int boltID = 1;
        int cycles;
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
                        boltDeclarer = builder.setBolt(boltName, windowedBolt, componentParallelism.next());
                    } else {
                        double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                        boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, filtering, reliability, debugFrequency), componentParallelism.next());
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
                        boltDeclarer = builder.setBolt(boltName, windowedBolt, componentParallelism.next());
                    } else {
                        double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                        boltDeclarer = builder.setBolt(boltName, new BusyWaitBolt(cycles, filtering, reliability, debugFrequency), componentParallelism.next());
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
