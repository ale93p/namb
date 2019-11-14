package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.Config;

import java.util.ArrayList;

public class Task {
    private String name;
    private Config.ComponentType type;
    private int processing;
    private long parallelism;
    private Config.TrafficRouting routing;
    private double filtering;
    private boolean reliable;
    private ArrayList<String> parents;
    private ArrayList<String> childs;

    private int dataSize;
    private int dataValues;
    private Config.DataDistribution dataDistribution;
    private Config.ArrivalDistribution flowDistribution;
    private int flowRate;

    private boolean isExternal;
    private String kafkaServer;
    private String kafkaGroup;
    private String kafkaTopic;
    private String zookeeperServer;


    //TODO add windowing


    //Constructors

    public Task(String name, Config.ComponentType type, double processing, long parallelism,
                Config.TrafficRouting routing, boolean isReliable, double filtering,
                int dataSize, int dataValues, Config.DataDistribution dataDistribution,
                Config.ArrivalDistribution flowDistribution, int flowRate,
                ArrayList<String> parents, ArrayList<String> childs,
                boolean isExternal, String kafkaServer, String kafkaGroup, String kafkaTopic, String zookeeperServer) {
        this.name = name;
        this.type = type;
        this.processing = (int) Math.round(processing * 1000);
        this.parallelism = parallelism;
        this.routing = routing;
        this.reliable = isReliable;
        this.filtering = filtering;

        this.dataSize = dataSize;
        this.dataValues = dataValues;
        this.dataDistribution = dataDistribution;
        this.flowDistribution = flowDistribution;
        this.flowRate = flowRate;

        this.parents = parents;
        this.childs = childs;

        this.isExternal = isExternal;
        this.kafkaServer = kafkaServer;
        this.kafkaGroup = kafkaGroup;
        this.kafkaTopic = kafkaTopic;
        this.zookeeperServer = zookeeperServer;
    }

    //it's synthetic source
    public Task(String name, long parallelism, boolean isReliable,
                int dataSize, int dataValues, Config.DataDistribution dataDistribution,
                Config.ArrivalDistribution flowDistribution, int flowRate, ArrayList<String> childs){
        this(name, Config.ComponentType.source, 0, parallelism, null, isReliable, 0, dataSize, dataValues, dataDistribution, flowDistribution, flowRate, null, childs, false, null, null, null, null);
    }

    //it's kafka source
    public Task(String name, long parallelism, boolean isReliable,
                String kafkaServer, String kafkaGroup, String kafkaTopic, String zookeeperServer,
                ArrayList<String> childs){
        this(name, Config.ComponentType.source, 0, parallelism, null, isReliable, 0, 0, 0, null, null, 0, null, childs, true, kafkaServer, kafkaGroup, kafkaTopic, zookeeperServer);
    }

    //it's task
    public Task(String name, double processing, long parallelism, Config.TrafficRouting routing, boolean isReliable, double filtering, int dataSize, ArrayList<String> parents, ArrayList<String> childs){
        this(name, Config.ComponentType.task, processing, parallelism, routing, isReliable, filtering, dataSize, 0, null, null, 0, parents, childs, false, null, null, null, null);
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

    public int getProcessing() {
        return processing;
    }

    public void setProcessing(int processing) {
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

    public boolean isReliable() {
        return reliable;
    }

    public void setReliable(boolean reliability) {
        this.reliable = reliability;
    }

    public double getFiltering() {
        return filtering;
    }

    public void setFiltering(double filtering) {
        this.filtering = filtering;
    }

    public int getDataSize() {
        return dataSize;
    }

    public int getDataValues() {
        return dataValues;
    }

    public Config.DataDistribution getDataDistribution() {
        return dataDistribution;
    }

    public Config.ArrivalDistribution getFlowDistribution() {
        return flowDistribution;
    }

    public int getFlowRate() {
        return flowRate;
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

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }

    public String getKafkaServer() {
        return kafkaServer;
    }

    public String getKafkaGroup() {
        return kafkaGroup;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public String getZookeeperServer() {
        return zookeeperServer;
    }
}
