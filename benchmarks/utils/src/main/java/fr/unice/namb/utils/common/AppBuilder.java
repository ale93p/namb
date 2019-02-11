package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.ConfigDefaults;
import sun.security.krb5.Config;

import java.util.ArrayList;
import java.util.Collections;

public class AppBuilder{

    private int depth;
    private int parallelism;
    private ConfigDefaults.ConnectionShape shape;
    private ArrayList<Integer> dagLevelsWidth;
    private ArrayList<Integer> componentsParallelism;
    private ConfigDefaults.LoadBalancing loadBalancing;


    public AppBuilder(int depth, int parallelism, ConfigDefaults.ConnectionShape shape, ConfigDefaults.LoadBalancing loadBalancing) throws Exception{
        this.depth = depth;
        this.parallelism = parallelism;
        this.shape = shape;
        this.dagLevelsWidth = computeTopologyShape();
        this.componentsParallelism = computeComponentsParallelism();
        this.loadBalancing = loadBalancing;
    }

    // just to use utility functions
    public AppBuilder() throws Exception{
        this(0, 0, null, null);
    }

    /*
    this is just a dummy implementation
    TODO: the increasing, decreasing and bell funcitons must be better ingegnerized
    */
    public int getNextProcessing(int actual) throws Exception{
        switch (this.loadBalancing){
            case balanced:
                return actual;
            case increasing:
                return (int)(actual * 1.2);
            case decresing:
                return (int)(actual * 0.8);
            case bell:
            default:
                throw new Exception("case " + this.loadBalancing + " not yet implemented");
        }
    }

    public ArrayList<Integer> getDagLevelsWidth(){
        return this.dagLevelsWidth;
    }

    public ArrayList<Integer> getComponentsParallelism(){
        return this.componentsParallelism;
    }

    /*
    this is basic implementation
    TODO: to be improved after implementing balancing on scalability configuration
    */
    private ArrayList<Integer> computeComponentsParallelism() throws ArithmeticException{
        ArrayList<Integer> componentsParallelism = new ArrayList<>();
        int totComponents = sumArray(this.dagLevelsWidth);
        int remainingExecutors = this.parallelism%totComponents;
        int basePar = this.parallelism / totComponents;

        for(int i=0; i<totComponents; i++){
            componentsParallelism.add( (remainingExecutors==0) ? basePar : basePar + 1 );
            remainingExecutors--;
        }
        if (componentsParallelism.size() != totComponents){
            throw new ArithmeticException("Error computing components parallelism: final array length mismatch");
        }
        return componentsParallelism;
    }

    private ArrayList<Integer> computeTopologyShape(ConfigDefaults.ConnectionShape shape, int depth) throws Exception{
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

    private ArrayList<Integer> computeTopologyShape() throws Exception {
        return computeTopologyShape(this.shape, this.depth);
    }

    public ArrayList<Integer> getTopologyShape(ConfigDefaults.ConnectionShape shape, int depth) throws Exception{
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
