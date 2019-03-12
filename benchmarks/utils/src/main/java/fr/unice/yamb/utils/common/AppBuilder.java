package fr.unice.yamb.utils.common;

import fr.unice.yamb.utils.configuration.Config;

import java.util.ArrayList;
import java.util.Collections;

public class AppBuilder{

    private int depth;
    private int parallelism;
    private Config.ParaBalancing paraBalancing;
    private Config.ConnectionShape shape;
    private ArrayList<Integer> dagLevelsWidth;
    private int totalComponents;
    private ArrayList<Integer> componentsParallelism;
    private int initialProcessing;
    private int currentProcessing;
    private Config.LoadBalancing loadBalancing;

    private int count;


    public AppBuilder(int depth, int parallelism, Config.ParaBalancing paraBalancing, Config.ConnectionShape shape, int processing, Config.LoadBalancing loadBalancing) throws Exception{

        this.depth = depth;
        this.parallelism = parallelism;
        this.paraBalancing = paraBalancing;
        this.shape = shape;
        this.initialProcessing = processing;
        this.currentProcessing = processing;
        this.loadBalancing = loadBalancing;

        this.dagLevelsWidth = computeTopologyShape();
        this.totalComponents = sumArray(this.dagLevelsWidth);
        this.componentsParallelism = computeComponentsParallelism();

        this.count = 0;
    }

    // just to use utility functions
    public AppBuilder() throws Exception{

    }

    /*
    this is just a dummy implementation
    TODO: it can be improved
    */
    public int getNextProcessing() throws Exception{
        switch (this.loadBalancing){
            case balanced:
                break;
            case increasing:
                this.currentProcessing = (int)(this.currentProcessing * 1.2);
                break;
            case decreasing:
                this.currentProcessing = (int)(this.currentProcessing * 0.8);
            case pyramid:
                this.currentProcessing = (this.count <= this.totalComponents/2) ? (int) (this.currentProcessing * 1.2) : (int) (this.currentProcessing * 0.8);
                this.count++;
            default:
                throw new Exception("case " + this.loadBalancing + " not yet implemented");
        }
        return this.currentProcessing;
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
        double variability = 0.5; //50%
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

}
