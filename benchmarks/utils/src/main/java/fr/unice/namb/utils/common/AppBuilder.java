package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.Config;

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

    //TODO
    private ArrayList<Integer> computeComponentsParallelism() throws ArithmeticException{

        ArrayList<Integer> componentsParallelism = new ArrayList<>();
        int remainingExecutors;
        int basePar;

        switch(this.paraBalancing){
            case balanced:{
                remainingExecutors = this.parallelism%this.totalComponents;
                basePar = this.parallelism / this.totalComponents;
                for(int i=0; i<this.totalComponents; i++){
                    componentsParallelism.add( (remainingExecutors<=0) ? basePar : basePar + 1 );
                    remainingExecutors--;
                }
                break;
            }

            case increasing: {
                double variability = 0.5; //50%
                basePar = this.parallelism / this.totalComponents;

                // initialize array already
                for (int i = 0; i < this.totalComponents; i++) componentsParallelism.add(i, basePar);

                remainingExecutors = 0;
                int avgRemainingExecutors = 0;

                for (int j = 0; j < this.totalComponents; j++) {
                    //remove variability
                    for (int i = j; i < this.totalComponents; i++) {
                        int value = componentsParallelism.get(i);
                        //add avg remaining executor
                        value = value + avgRemainingExecutors;
                        //remove variability
                        value = (int) (value * (1. - variability));
                        componentsParallelism.set(i, value);
                    }
                    //check remainings
                    remainingExecutors = this.parallelism - sumArray(componentsParallelism);
                    avgRemainingExecutors = (remainingExecutors == 0 || j == this.totalComponents - 1) ? 0 : (int) (remainingExecutors / (this.totalComponents - (j + 1)));
                    variability = variability * .7;
                }

                if (remainingExecutors > 0) {
                    int value = componentsParallelism.get(componentsParallelism.size() - 1);
                    value = value + remainingExecutors;
                    componentsParallelism.set(componentsParallelism.size() - 1, value);
                }

                break;
            }

            case decreasing: {
                double variability = 0.5; //50%
                basePar = this.parallelism / this.totalComponents;

                // initialize array already
                for (int i = 0; i < this.totalComponents; i++) componentsParallelism.add(i, basePar);

                remainingExecutors = 0;
                int avgRemainingExecutors = 0;

                for (int j = this.totalComponents - 1; j >= 0; j--) {
                    //remove variability
                    for (int i = j; i >=0; i--) {
                        int value = componentsParallelism.get(i);
                        //add avg remaining executor
                        value = value + avgRemainingExecutors;
                        //remove variability
                        value = (int) (value * (1. - variability));
                        componentsParallelism.set(i, value);
                    }
                    //check remainings
                    remainingExecutors = this.parallelism - sumArray(componentsParallelism);
                    avgRemainingExecutors = (remainingExecutors == 0 || j== 0) ? 0 : (int) (remainingExecutors / (j));
                    variability = variability * .7;
                }


                if (remainingExecutors > 0) {
                    int value = componentsParallelism.get(0);
                    value = value + remainingExecutors;
                    componentsParallelism.set(0, value);
                }

                break;

            }

            case pyramid: {
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
