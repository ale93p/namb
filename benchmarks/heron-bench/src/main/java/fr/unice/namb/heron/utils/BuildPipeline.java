package fr.unice.namb.heron.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.twitter.heron.api.topology.TopologyBuilder;

import com.twitter.heron.api.topology.BoltDeclarer;
import com.twitter.heron.api.topology.SpoutDeclarer;

import fr.unice.namb.heron.bolts.BusyWaitBolt;
import fr.unice.namb.heron.spouts.SyntheticSpout;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.common.Task;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.HeronConfigSchema;

import static fr.unice.namb.heron.utils.BuildCommons.setRouting;

public class BuildPipeline {
	public static void build(TopologyBuilder builder, AppBuilder app, HeronConfigSchema stormConf) throws Exception {
		
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
	                    SpoutDeclarer spout = builder.setSpout(newTask.getName(), new SyntheticSpout(newTask.getData(), newTask.getFlow(), newTask.isReliable(), debugFrequency), newTask.getParallelism());
	                    createdTasks.put(newTask.getName(), spout);
	                } else {
	                    //TODO add windowing
	
	//                    System.out.println(newTask.getName() + " has datasize " + newTask.getDataSize());
	
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
