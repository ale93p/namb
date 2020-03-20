package fr.unice.namb.storm.utils;

import fr.unice.namb.storm.bolts.BusyWaitBolt;
import fr.unice.namb.storm.spouts.SyntheticSpout;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.common.Task;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.StormConfigSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.SpoutDeclarer;
import org.apache.storm.topology.TopologyBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import static fr.unice.namb.storm.utils.BuildCommons.setRouting;

public class BuildPipeline {

    public static void build(TopologyBuilder builder, AppBuilder app, StormConfigSchema stormConf) throws IllegalArgumentException, Exception {
         /*
        Pipeline Schema Translation
         */

        double debugFrequency = stormConf.getDebugFrequency();

        HashMap<String, Task> pipeline = app.getPipelineTree();
        ArrayList<String> dagLevel = app.getPipelineTreeSources();
        HashMap<String, Object> createdTasks = new HashMap<>();

        while (dagLevel.size() > 0) {
            ArrayList<String> nextDagLevel = new ArrayList<>();
            for (String task : dagLevel) {
                if (!createdTasks.containsKey(task)) {
                    Task newTask = pipeline.get(task);
                    if (newTask.getType() == Config.ComponentType.source) {
                        SpoutDeclarer spout = null;
                        if (newTask.isExternal()) {
                            KafkaSpoutConfig<String, String> kafkaConfig = KafkaSpoutConfig.builder(app.getKafkaServer(), newTask.getKafkaTopic())
                                    .setFirstPollOffsetStrategy(KafkaSpoutConfig.FirstPollOffsetStrategy.LATEST)
                                    .setProp(ConsumerConfig.GROUP_ID_CONFIG, newTask.getKafkaServer())
                                    .setRecordTranslator(new KafkaRecordTranslator(debugFrequency, newTask.getName()))
                                    .build();
                            spout = builder.setSpout(newTask.getName(), new KafkaSpout<>(kafkaConfig), newTask.getParallelism());
                        } else
                            spout = builder.setSpout(newTask.getName(), new SyntheticSpout(newTask.getData(), newTask.getFlow(), newTask.isReliable(), debugFrequency), newTask.getParallelism());
                        createdTasks.put(newTask.getName(), spout);
                    } else {
                        //TODO add windowing

//                            System.out.println(newTask.getName() + " has datasize " + newTask.getDataSize());


                        BoltDeclarer boltDeclarer = builder.setBolt(newTask.getName(), new BusyWaitBolt(newTask.getProcessing(), newTask.getFiltering(), newTask.isReliable(), newTask.getResizeddata(), debugFrequency), newTask.getParallelism());
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

}
