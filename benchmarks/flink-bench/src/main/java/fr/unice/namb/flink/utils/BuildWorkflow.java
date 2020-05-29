package fr.unice.namb.flink.utils;

import fr.unice.namb.flink.connectors.SyntheticConnector;
import fr.unice.namb.flink.operators.BusyWaitFlatMap;
import fr.unice.namb.flink.operators.WindowedBusyWaitFunction;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.FlinkConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import static fr.unice.namb.flink.utils.BuildCommons.setRouting;
import static fr.unice.namb.flink.utils.BuildCommons.setWindow;

public class BuildWorkflow {

    public static void build(StreamExecutionEnvironment env, AppBuilder app, NambConfigSchema conf, FlinkConfigSchema flinkConf) throws Exception {
        /*
        Workflow Schema Translation
         */

        double debugFrequency = flinkConf.getDebugFrequency();

        // DataFlow configurations
        int depth = conf.getWorkflow().getDepth();
//        int totalParallelism = conf.getWorkflow().getScalability().getParallelism();
//        Config.ParaBalancing paraBalancing = conf.getWorkflow().getScalability().getBalancing();
//        double variability = conf.getWorkflow().getScalability().getVariability();
        Config.ConnectionShape topologyShape = conf.getWorkflow().getConnection().getShape();
        Config.TrafficRouting trafficRouting = conf.getWorkflow().getConnection().getRouting();
//        double processingLoad = conf.getWorkflow().getWorkload().getProcessing();
//        Config.LoadBalancing loadBalancing = conf.getWorkflow().getWorkload().getBalancing();

        // Generating app builder
        ArrayList<Integer> dagLevelsWidth = app.getDagLevelsWidth();
        ArrayList<Integer> componentsParallelism = app.getComponentsParallelism();
        ArrayList<Integer> componentsLoad = app.getComponentsLoad();


        // Bolt-specific configurations
        int numberOfSources = dagLevelsWidth.get(0);

        // Windowing
        boolean windowingEnabled = conf.getWorkflow().getWindowing().isEnabled();
        Config.WindowingType windowingType = conf.getWorkflow().getWindowing().getType();
        int windowDuration = conf.getWorkflow().getWindowing().getDuration();
        int windowInterval = conf.getWorkflow().getWindowing().getInterval();
        int windowedTasks = (depth > 3) ? 2 : 1;

        Iterator<Integer> componentParallelism = componentsParallelism.iterator();
        Iterator<Integer> componentLoad = componentsLoad.iterator();
        ArrayList<MutablePair<String, DataStream<Tuple4<String, String, Long, Long>>>> sourcesList = new ArrayList<>();
        ArrayList<MutablePair<String, SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>>> operatorsList = new ArrayList<>();


        String sourceName;

            /*
            Sources Definition
             */
        if (app.isExternalSource()) {
            int s = 1;
            sourceName = "kafka_source_" + s;
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", app.getKafkaServer());
            properties.setProperty("zookeeper.connect", app.getZookeeperServer());
            properties.setProperty("group.id", app.getKafkaGroup());
            FlinkKafkaConsumer<Tuple4<String, String, Long, Long>> kafkaConsumer = new FlinkKafkaConsumer<>(app.getKafkaTopic(), new KafkaDeserializationSchema(debugFrequency, sourceName), properties);

            DataStream<Tuple4<String, String, Long, Long>> source = env
                    .addSource(kafkaConsumer)
                    .setParallelism(componentParallelism.next())
                    .name(sourceName);
            sourcesList.add(new MutablePair<>(sourceName, source));
        } else {
            for (int s = 1; s <= numberOfSources; s++) {
                sourceName = "source_" + s;
                DataStream<Tuple4<String, String, Long, Long>> source = env.addSource(new SyntheticConnector(conf.getDatastream().getSynthetic(), debugFrequency, sourceName))
                        .setParallelism(componentParallelism.next())
                        .name(sourceName);
                sourcesList.add(new MutablePair<>(sourceName, source));

            }

            if (numberOfSources > 1) {
                sourceName = "unified_source";
                DataStream<Tuple4<String, String, Long, Long>> source = sourcesList.get(0).getRight().union(sourcesList.get(1).getRight());
                for (int s = 2; s < numberOfSources; s++) {
                    source.union(sourcesList.get(s).getRight());
                }
                sourcesList.add(new MutablePair<>(sourceName, source));
            }
        }

            /*
            Tasks definition
             */

        int operatorID = 1;
        int cycles;
        String operatorName;

        for (int i = 1; i < depth; i++) {
            int levelWidth = dagLevelsWidth.get(i);
            boolean isWindowed = depth - i <= windowedTasks && windowingEnabled; // this level contains windowed tasks

            if (i == 1) {
                for (int opCount = 0; opCount < levelWidth; opCount++) {
                    operatorName = "op_" + operatorID;
                    cycles = componentLoad.next();
                    DataStream<Tuple4<String, String, Long, Long>> parent = sourcesList.get(sourcesList.size() - 1).getRight();


                    SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> op = null;
                    if (isWindowed) {
                        operatorName = "windowed-" + operatorName;
                        op = setWindow(parent, trafficRouting, windowingType, windowDuration, windowInterval)
                                .apply(new WindowedBusyWaitFunction(cycles, debugFrequency));
                    } else {
                        double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                        op = setRouting(parent, trafficRouting)
                                .flatMap(new BusyWaitFlatMap(cycles, filtering, debugFrequency, operatorName));
                    }

                    op.setParallelism(componentParallelism.next())
                            .name(operatorName);

                    operatorsList.add(new MutablePair<>(operatorName, op));
                    operatorID++;
                }
            } else {
                if (topologyShape == Config.ConnectionShape.diamond && dagLevelsWidth.get(i - 1) > 1) {// diamond shape union
                    DataStream<Tuple4<String, String, Long, Long>> diamondUnion = operatorsList.get(operatorID - 2).getRight().union(operatorsList.get(operatorID - 3).getRight());
                    //TODO: maybe this can be optimized?
                    for (int o = 2; o < dagLevelsWidth.get(i - 1); o++) {
                        diamondUnion.union(operatorsList.get(o).getRight());
                    }
                    operatorName = "op_" + operatorID;
                    cycles = componentLoad.next();


                    SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> op = null;
                    if (isWindowed) {
                        operatorName = "windowed-" + operatorName;
                        op = setWindow(diamondUnion, trafficRouting, windowingType, windowDuration, windowInterval)
                                .apply(new WindowedBusyWaitFunction(cycles, debugFrequency));
                    } else {
                        double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                        op = setRouting(diamondUnion, trafficRouting)
                                .flatMap(new BusyWaitFlatMap(cycles, filtering, debugFrequency, operatorName));
                    }

                    op.setParallelism(componentParallelism.next())
                            .name(operatorName);

                    operatorsList.add(new MutablePair<>(operatorName, op));
                    operatorID++;
                } else {
                    int parentOperatorIdx = (topologyShape == Config.ConnectionShape.diamond ||
                            (topologyShape == Config.ConnectionShape.star && i > 3)) ? i - 1 : i - 2;
                    SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> parent = operatorsList.get(parentOperatorIdx).getRight();
                    for (int opCount = 0; opCount < levelWidth; opCount++) {
                        operatorName = "op_" + operatorID;
                        cycles = componentLoad.next();

                        SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> op = null;
                        if (isWindowed) {
                            operatorName = "windowed-" + operatorName;
                            op = setWindow(parent, trafficRouting, windowingType, windowDuration, windowInterval, false)
                                    .apply(new WindowedBusyWaitFunction(cycles, debugFrequency));
                        } else {
                            double filtering = (app.getFilteringDagLevel() == i) ? app.getFiltering() : 0;
                            op = setRouting(parent, Config.TrafficRouting.none)
                                    .flatMap(new BusyWaitFlatMap(cycles, filtering, debugFrequency, operatorName));
//                            op = parent.map(new BusyWaitMap(cycles, debugFrequency));
                        }

                        op.setParallelism(componentParallelism.next())
                                .name(operatorName);

                        operatorsList.add(new MutablePair<>(operatorName, op));
                        operatorID++;
                    }
                }
            }
        }
    }
}
