package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Data;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Tasks;

import java.util.ArrayList;

public class Task {
    private String name;
    private Config.ComponentType type;
    private long processing;
    private long parallelism;
    private Config.TrafficRouting routing;
    private double filtering;
    private boolean reliable;
    private ArrayList<String> parents;
    private ArrayList<String> childs;
    
    private Data data;
	private Flow flow;

    private boolean isExternal;
    private String kafkaServer;
    private String kafkaGroup;
    private String kafkaTopic;
    private String zookeeperServer;
	private int resizeddata;


    //TODO add windowing


    //Constructors

    public Task(String name, Tasks conf, ArrayList<String> parents, ArrayList<String> childs) {
        this.name = name;
        this.type = conf.getType();
        this.processing = (int) Math.round(processing * 1000);
        this.parallelism = conf.getParallelism();
        this.routing = conf.getRouting();
        this.reliable = conf.isReliability();
        this.filtering = conf.getFiltering();
        this.resizeddata = conf.getResizeddata();

        this.data = conf.getData();
        this.flow = conf.getFlow();
        
        this.parents = parents;
        this.childs = childs;

        this.isExternal = conf.getKafka().getServer() != null;
        this.kafkaServer = conf.getKafka().getServer();
        this.kafkaGroup = conf.getKafka().getGroup();
        this.kafkaTopic = conf.getKafka().getTopic();
        this.zookeeperServer = conf.getKafka().getZookeeper();
    }

    //it's source
    public Task(String name, Tasks conf, ArrayList<String> childs){
    	this(name, conf, null, childs);
    }


    public void addChild(String t){
        this.childs.add(t);
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Config.ComponentType getType() {
		return type;
	}

	public void setType(Config.ComponentType type) {
		this.type = type;
	}

	public long getProcessing() {
		return processing;
	}

	public void setProcessing(long processing) {
		this.processing = processing;
	}

	public long getParallelism() {
		return parallelism;
	}

	public void setParallelism(long parallelism) {
		this.parallelism = parallelism;
	}

	public Config.TrafficRouting getRouting() {
		return routing;
	}

	public void setRouting(Config.TrafficRouting routing) {
		this.routing = routing;
	}

	public double getFiltering() {
		return filtering;
	}

	public void setFiltering(double filtering) {
		this.filtering = filtering;
	}

	public boolean isReliable() {
		return reliable;
	}

	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}

	public ArrayList<String> getParents() {
		return parents;
	}

	public void setParents(ArrayList<String> parents) {
		this.parents = parents;
	}

	public ArrayList<String> getChilds() {
		return childs;
	}

	public void setChilds(ArrayList<String> childs) {
		this.childs = childs;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public boolean isExternal() {
		return isExternal;
	}

	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	public String getKafkaServer() {
		return kafkaServer;
	}

	public void setKafkaServer(String kafkaServer) {
		this.kafkaServer = kafkaServer;
	}

	public String getKafkaGroup() {
		return kafkaGroup;
	}

	public void setKafkaGroup(String kafkaGroup) {
		this.kafkaGroup = kafkaGroup;
	}

	public String getKafkaTopic() {
		return kafkaTopic;
	}

	public void setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
	}

	public String getZookeeperServer() {
		return zookeeperServer;
	}

	public void setZookeeperServer(String zookeeperServer) {
		this.zookeeperServer = zookeeperServer;
	}

	public int getResizeddata() {
		return resizeddata;
	}

	public void setResizeddata(int resizeddata) {
		this.resizeddata = resizeddata;
	}

    
}
