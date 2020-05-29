package fr.unice.namb.flink.utils;

import fr.unice.namb.flink.connectors.SyntheticConnector;
import fr.unice.namb.flink.operators.BusyWaitFlatMap;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.common.Task;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.FlinkConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import static fr.unice.namb.flink.utils.BuildCommons.setRouting;

public class BuildPipeline {
    public static void build(StreamExecutionEnvironment env, AppBuilder app, NambConfigSchema conf, FlinkConfigSchema flinkConf) throws Exception{
        /*
            Pipeline Schema Translation
             */

        double debugFrequency = flinkConf.getDebugFrequency();

        HashMap<String, Task> pipeline = app.getPipelineTree();
        ArrayList<String> dagLevel = app.getPipelineTreeSources();
        HashMap<String, Object> createdTasks = new HashMap<>();

        while (dagLevel.size() > 0){

            ArrayList<String> nextDagLevel = new ArrayList<>();
            for (String task : dagLevel) {
                if (!createdTasks.containsKey(task)) {
                    Task newTask = pipeline.get(task);
                    if (newTask.getType() == Config.ComponentType.source) {
                        DataStream<Tuple4<String, String, Long, Long>> source = null;
                        if(newTask.isExternal()){
                            Properties properties = new Properties();
                            properties.setProperty("bootstrap.servers", newTask.getKafkaServer());
                            properties.setProperty("zookeeper.connect", newTask.getZookeeperServer());
                            properties.setProperty("group.id", newTask.getKafkaGroup());
                            FlinkKafkaConsumer<Tuple4<String, String, Long, Long>> kafkaConsumer = new FlinkKafkaConsumer<>(newTask.getKafkaTopic(), new KafkaDeserializationSchema(debugFrequency, newTask.getName()), properties);

                            source = env
                                    .addSource(kafkaConsumer)
                                    .setParallelism((int) newTask.getParallelism())
                                    .name(newTask.getName());
                        }
                        else {
                            source = env.addSource(new SyntheticConnector(newTask.getData(), newTask.getFlow(), debugFrequency, newTask.getName()))
                                    .setParallelism((int) newTask.getParallelism())
                                    .name(newTask.getName());
                        }
                        createdTasks.put(newTask.getName(), source);
                    }
                    else{

                        ArrayList<String> parentsList = newTask.getParents();
                        SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> op = null;
                        DataStream<Tuple4<String, String, Long, Long>> streamUnion = null;
                        if(parentsList.size() > 1) {
                            if( pipeline.get(parentsList.get(0)).getType() == Config.ComponentType.source){

                                streamUnion = ((DataStream<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(0))).union((DataStream<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(1)));
                                for (int i=2; i<parentsList.size(); i++) {
                                    streamUnion.union((DataStream<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(i)));
                                }

                            }
                            else{
                                streamUnion = ((SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(0))).union((SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(1)));
                                for (int i=2; i<parentsList.size(); i++) {
                                    streamUnion.union((SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(i)));
                                }

                            }

                            //TODO: impolement windowing
                            op =  setRouting(streamUnion, newTask.getRouting())
                                    .flatMap(new BusyWaitFlatMap(newTask.getProcessing(), newTask.getFiltering(), newTask.getResizeddata(), debugFrequency, newTask.getName()));
                        }
                        else{

                            if( pipeline.get(parentsList.get(0)).getType() == Config.ComponentType.source){
                                DataStream<Tuple4<String, String, Long, Long>> parent = (DataStream<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(0));

                                op =  setRouting(parent, newTask.getRouting())
                                        .flatMap(new BusyWaitFlatMap(newTask.getProcessing(), newTask.getFiltering(), newTask.getResizeddata(), debugFrequency, newTask.getName()));
                            }
                            else{
                                SingleOutputStreamOperator<Tuple4<String, String, Long, Long>> parent = (SingleOutputStreamOperator<Tuple4<String, String, Long, Long>>) createdTasks.get(parentsList.get(0));

                                op =  setRouting(parent, newTask.getRouting())
                                        .flatMap(new BusyWaitFlatMap(newTask.getProcessing(), newTask.getFiltering(), newTask.getResizeddata(), debugFrequency, newTask.getName()));
                            }

                        }

                        op.setParallelism((int)newTask.getParallelism())
                                .name(newTask.getName());
                        createdTasks.put(newTask.getName(), op);
                    }

                }

                nextDagLevel.addAll(pipeline.get(task).getChilds());
            }
            dagLevel = new ArrayList<>(nextDagLevel);
        }
    }
}
