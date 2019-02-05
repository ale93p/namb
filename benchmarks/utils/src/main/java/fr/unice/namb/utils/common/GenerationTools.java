package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.ConfigDefaults;

import java.util.ArrayList;
import java.util.Collections;

public class GenerationTools {

    /*
    this is just a dummy implementation
    TODO: the increasing, decreasing and bell funcitons must be better ingegnerized
    */
    public static int computeNextProcessing(int actual, ConfigDefaults.Balancing balancing) throws Exception{
        switch (balancing){
            case balanced:
                return actual;
            case increasing:
                return (int)(actual * 1.2);
            case decresing:
                return (int)(actual * 0.8);
            case bell:
            default:
                throw new Exception("case " + balancing + " not yet implemented");
        }
    }

    /*
    this is basic implementation
    TODO: to be improved after implementing balancing on scalability configuration
    */
    public static ArrayList<Integer> computeComponentsParallelism(int parallelism, ArrayList<Integer> dagLevelsWidth) throws ArithmeticException{
        ArrayList<Integer> componentsParallelism = new ArrayList<>();
        int totComponents = sumArray(dagLevelsWidth);
        int remainingExecutors = parallelism%totComponents;
        int basePar = parallelism / totComponents;

        for(int i=0; i<totComponents; i++){
            componentsParallelism.add( (remainingExecutors==0) ? basePar : basePar + 1 );
            remainingExecutors--;
        }
        if (componentsParallelism.size() != totComponents){
            throw new ArithmeticException("Error computing components parallelism: final array length mismatch");
        }
        return componentsParallelism;
    }

    public static ArrayList<Integer> getTopologyShape(ConfigDefaults.ConnectionShape shape, int depth) throws Exception{
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
                throw new Exception("This shapa: <" + shape.name() + "> has not been implemented yet");
        }
    }

    public static int sumArray(ArrayList<Integer> arr, int lower, int upper) throws ArrayIndexOutOfBoundsException{
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

    public static int sumArray(ArrayList<Integer> arr, int upper){
        return sumArray(arr, 0, upper);
    }

    public static int sumArray(ArrayList<Integer> arr){
        return sumArray(arr,0, arr.size());
    }

}
