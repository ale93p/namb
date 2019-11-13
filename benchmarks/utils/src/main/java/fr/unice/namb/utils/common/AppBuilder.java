package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;

import java.util.*;

public class AppBuilder{

    private int depth;
    private int parallelism;
    private Config.ParaBalancing paraBalancing;
    private double variability;
    private Config.ConnectionShape shape;
    private Config.TrafficRouting trafficRouting;
    private ArrayList<Integer> dagLevelsWidth;
    private int totalComponents;
    private ArrayList<Integer> componentsParallelism;
    private int processing;
    private Config.LoadBalancing loadBalancing;
    private double filtering;
    private int filteringDagLevel;

    private boolean pipelineDefined;
    private HashMap<String, Task> pipelineTree;
    private ArrayList<String> pipelineTreeSources;

    private boolean externalSource;
    private String kafkaServer;
    private String kafkaGroup;
    private String kafkaTopic;
    private String zookeeperServer;


    private int count;
    

    public AppBuilder(NambConfigSchema conf) throws Exception{

        NambConfigSchema.Tasks pipeline [] = conf.getPipeline().getTasks();

        if(pipeline != null && pipeline.length > 0){
            this.depth = pipeline.length;
            this.totalComponents = pipeline.length;
            this.pipelineDefined = true;
            computePipelineTree(pipeline);

        }
        else {
            this.depth = conf.getWorkflow().getDepth();
            this.parallelism = conf.getWorkflow().getScalability().getParallelism();
            this.paraBalancing = conf.getWorkflow().getScalability().getBalancing();
            this.variability = conf.getWorkflow().getScalability().getVariability();
            this.shape = conf.getWorkflow().getConnection().getShape();
            this.trafficRouting = conf.getWorkflow().getConnection().getRouting();
            this.processing = (int) Math.round(conf.getWorkflow().getWorkload().getProcessing() * 1000);
            this.loadBalancing = conf.getWorkflow().getWorkload().getBalancing();
            this.filtering = conf.getWorkflow().getFiltering();
            this.filteringDagLevel = (this.filtering > 0) ? (this.depth / 2) : 0;



            this.dagLevelsWidth = computeTopologyShape();
            this.totalComponents = sumArray(this.dagLevelsWidth);
            //TODO: create processing list
            this.componentsParallelism = computeComponentsParallelism();

            this.pipelineDefined = false;
            this.kafkaServer = conf.getDatastream().getExternal().getKafka().getServer();
            this.externalSource = !(kafkaServer == null);
            if (this.externalSource){
                this.kafkaGroup = conf.getDatastream().getExternal().getKafka().getGroup();
                this.kafkaTopic = conf.getDatastream().getExternal().getKafka().getTopic();
                this.zookeeperServer = conf.getDatastream().getExternal().getZookeeper().getServer();
            }
        }



        this.count = 0;
    }

    //TODO: create the getters for the values based on the depth/component index

    // just to use utility functions
    public AppBuilder() throws Exception{

    }


    private void computePipelineTree(NambConfigSchema.Tasks[] pipeline){
        this.pipelineTree = new HashMap<String, Task>();
        this.pipelineTreeSources = new ArrayList<>();

        for(NambConfigSchema.Tasks p : pipeline ){
            String name = p.getName();
            Task newTask = null;

            String parents[] = p.getParents();
            if(parents == null){ //it's source
                newTask = new Task(name, p.getParallelism(), p.isReliability(),
                        p.getData().getSize(), p.getData().getValues(), p.getData().getDistribution(),
                        p.getFlow().getDistribution(), p.getFlow().getRate(), new ArrayList<>());
                this.pipelineTreeSources.add(name);
                this.pipelineTree.put(name, newTask);
            }
            else{ //it's a task
                List<String> parentsList = Arrays.asList(p.getParents());
                ArrayList<String> taskParents = new ArrayList<>(parentsList);
                newTask = new Task(name, p.getProcessing(), p.getParallelism(), p.getRouting(), p.isReliability(), p.getFiltering(), p.getResizeddata(), taskParents, new ArrayList<>());
                for(String parent : taskParents){
                    Task parentTask = this.pipelineTree.get(parent);
                    parentTask.addChild(name);
                }

                this.pipelineTree.put(name, newTask);
            }
        }

    }

    public HashMap<String, Task> getPipelineTree() {
        return pipelineTree;
    }

    public ArrayList<String> getPipelineTreeSources() {
        return pipelineTreeSources;
    }

    /*
        this is just a dummy implementation
        TODO: it can be improved
        */
    public int getNextProcessing() throws Exception{
        double modifier = 1;
        switch (this.loadBalancing){
            case balanced:
                break;
            case increasing:
                modifier = 1.2;
                break;
            case decreasing:
                modifier = 0.8;
                break;
            case pyramid:
                modifier = (this.count <= this.totalComponents/2) ? 1.2 : 0.8;
                this.count++;
                break;
            default:
                throw new Exception("case " + this.loadBalancing + " not yet implemented");
        }
        this.processing = (int)(this.processing * modifier);
        return this.processing;
    }

    public ArrayList<Integer> getDagLevelsWidth(){
        return this.dagLevelsWidth;
    }

    public ArrayList<Integer> getComponentsParallelism(){
        return this.componentsParallelism;
    }


    private ArrayList<Integer> generateBalancedArray(int slots, int elements){
        ArrayList<Integer> arr = new ArrayList<>();
        int remainingElements = elements % slots;
        int basePar = elements / slots;
        for(int i=0; i<slots; i++){
            arr.add( (remainingElements<=0) ? basePar : basePar + 1 );
            remainingElements--;
        }
        return arr;
    }

    private ArrayList<Integer> generateIncreasingArray(int slots, int elements){
        ArrayList<Integer> arr = new ArrayList<>();
        double variability = this.variability; //50%
        int remainingElements = 0;
        int avgRemainingElements = 0;
        int basePar = elements / slots;

        // initialize array
        for (int i = 0; i < slots; i++) arr.add(i, basePar);

        for (int j = 0; j < slots; j++) {
            //remove variability
            for (int i = j; i < slots; i++) {
                int value = arr.get(i);
                //add avg remaining executor
                value = value + avgRemainingElements;
                //remove variability
                value = (int) Math.ceil(value * (1. - variability));
                if(value < 1) value = 1;
                arr.set(i, value);
            }
            //check remainings
            remainingElements = elements - sumArray(arr);
            avgRemainingElements = (remainingElements == 0 || j == slots - 1) ? 0 : remainingElements / (slots - (j + 1));
            variability = variability * .7;
        }

        if (remainingElements > 0) {
            int value = arr.get(arr.size() - 1);
            value = value + remainingElements;
            arr.set(arr.size() - 1, value);
        }

        return arr;
    }

    private ArrayList<Integer> generateDecreasingArray(int slots, int elements){
        ArrayList<Integer> arr = generateIncreasingArray(slots, elements);
        Collections.reverse(arr);
        return arr;
    }

    //TODO
    private ArrayList<Integer> computeComponentsParallelism() throws ArithmeticException{

        ArrayList<Integer> componentsParallelism = new ArrayList<>();

        switch(this.paraBalancing){
            case balanced:{
                componentsParallelism = generateBalancedArray(this.totalComponents, this.parallelism);
                break;
            }

            case increasing: {
                componentsParallelism = generateIncreasingArray(this.totalComponents, this.parallelism);
                break;
            }

            case decreasing: {
                componentsParallelism = generateDecreasingArray(this.totalComponents, this.parallelism);
                break;

            }

            case pyramid: {

                // initialize array
                int basePar = this.parallelism / this.totalComponents;
                int pivot = (int) Math.ceil(this.totalComponents / 2);

                int componentsPartitionA = pivot + 1;
                int parallelismPartitionA = basePar * componentsPartitionA;
                int componentsPartitionB = this.totalComponents - componentsPartitionA;
                int parallelismPartitionB = this.parallelism - parallelismPartitionA;

                // fix parallelism partition
                if (parallelismPartitionB > parallelismPartitionA){
                    int temp = parallelismPartitionA;
                    parallelismPartitionA = parallelismPartitionB;
                    parallelismPartitionB = temp;
                }
                else if(parallelismPartitionA == parallelismPartitionB){
                    parallelismPartitionA++;
                    parallelismPartitionB--;
                }

                // generate increasing partition A [0:pivot]
                ArrayList<Integer> partitionA = generateIncreasingArray(componentsPartitionA, parallelismPartitionA);
                // generate decreasing partition B [pivot:end]
                ArrayList<Integer> partitionB = generateDecreasingArray(componentsPartitionB, parallelismPartitionB);
                // concatenate arrays
                componentsParallelism.addAll(partitionA);
                componentsParallelism.addAll(partitionB);

                for (int i=pivot; i<componentsParallelism.size()-1; i++){
                    int curr = componentsParallelism.get(i);
                    int succ = componentsParallelism.get(i+1);
                    if (curr < succ){
                        componentsParallelism.set(i, succ);
                        componentsParallelism.set(i+1, curr);
                    }
                    else break;
                }
                break;
            }
        }

        if (componentsParallelism.size() != this.totalComponents){
            throw new ArithmeticException("Error computing components parallelism: final array length mismatch (array:" + componentsParallelism.size() + " != components:" + this.totalComponents + ")");
        }
        int placedExecutors = sumArray(componentsParallelism);
        if(placedExecutors != this.parallelism){
            throw new ArithmeticException("Error computing components parallelism: final placed executors mismatch (placed:" + placedExecutors + " != executors:" + this.parallelism + ")");
        }

        return componentsParallelism;
    }

    private ArrayList<Integer> computeTopologyShape(Config.ConnectionShape shape, int depth) throws Exception{
        ArrayList<Integer> dagLevelsWidth;
        switch(shape){
            case linear:
                return new ArrayList<Integer>(Collections.nCopies(depth,1));
            case star:
                dagLevelsWidth = new ArrayList<Integer>(Collections.nCopies(depth,1));
                dagLevelsWidth.set(0, 2);
                dagLevelsWidth.set(2, 2);
                return dagLevelsWidth;
            case diamond:
                dagLevelsWidth = new ArrayList<Integer>(Collections.nCopies(depth,1));
                dagLevelsWidth.set(1, 2);
                return dagLevelsWidth;
            default:
                throw new Exception("This shape <" + shape.name() + "> has not been implemented yet");
        }

    }

    public int getTotalComponents(){
        return this.totalComponents;
    }

    private ArrayList<Integer> computeTopologyShape() throws Exception {
        return computeTopologyShape(this.shape, this.depth);
    }

    public ArrayList<Integer> getTopologyShape(Config.ConnectionShape shape, int depth) throws Exception{
        return computeTopologyShape(shape, depth);
    }

    public int sumArray(ArrayList<Integer> arr, int lower, int upper) throws ArrayIndexOutOfBoundsException{
        if(lower>upper) throw new ArrayIndexOutOfBoundsException("upper limit must be greter than lower limit");

        if (lower<0) lower=0;
        else if (lower>arr.size()) lower=arr.size() - 1;

        if (upper<1) upper = 1;
        else if (upper>=arr.size()) upper=arr.size();
        else upper++;

        int sum = 0;
        for(int i = lower; i < upper; i ++){
            sum += arr.get(i);
        }
        return sum;
    }



    public int sumArray(ArrayList<Integer> arr, int upper){
        return sumArray(arr, 0, upper);
    }

    public int sumArray(ArrayList<Integer> arr){
        return sumArray(arr,0, arr.size());
    }

    public int getDepth() {
        return depth;
    }

    public int getParallelism() {
        return parallelism;
    }

    public Config.ParaBalancing getParaBalancing() {
        return paraBalancing;
    }

    public double getVariability() {
        return variability;
    }

    public Config.ConnectionShape getShape() {
        return shape;
    }

    public int getProcessing() {
        return processing;
    }

    public Config.LoadBalancing getLoadBalancing() {
        return loadBalancing;
    }

    public Config.TrafficRouting getTrafficRouting() {
        return trafficRouting;
    }

    public boolean isPipelineDefined() {
        return pipelineDefined;
    }

    public double getFiltering() {
        return filtering;
    }

    public int getFilteringDagLevel() {
        return filteringDagLevel;
    }

    public boolean isExternalSource() {
        return externalSource;
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
