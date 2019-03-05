package fr.unice.yamb.utils;

import fr.unice.yamb.utils.common.AppBuilder;
import fr.unice.yamb.utils.configuration.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class TestAppBuilder {

    int depth;
    int parallelism;
    Config.ParaBalancing paraBalancing;
    Config.ConnectionShape shape;
    int processing;
    Config.LoadBalancing loadBalancing;
    AppBuilder builder;

    public TestAppBuilder(int depth, int parallelism, Config.ParaBalancing paraBalancing, Config.ConnectionShape shape) throws Throwable{
        this.depth = depth;
        this.parallelism = parallelism;
        this.paraBalancing = paraBalancing;
        this.shape = shape;

        //unused values in the test
        this.processing = 1;
        this.loadBalancing = Config.LoadBalancing.balanced;

        this.builder = new AppBuilder(this.depth, this.parallelism, this.paraBalancing, this.shape, this.processing, this.loadBalancing);
    }

    @Parameters(name = "Run {index}: depth={0}, parallelism={1}, paraBalancing={2}, shape={3}")
    public static Iterable<Object[]> data() throws Throwable{
        return Arrays.asList(new Object[][]{
                // balanced
                {5, 10, Config.ParaBalancing.balanced, Config.ConnectionShape.linear},
                {5, 13, Config.ParaBalancing.balanced, Config.ConnectionShape.linear},
                {5, 13, Config.ParaBalancing.balanced, Config.ConnectionShape.star},
                {5, 13, Config.ParaBalancing.balanced, Config.ConnectionShape.diamond},
                {10, 17, Config.ParaBalancing.balanced, Config.ConnectionShape.linear},
                {100, 1000000, Config.ParaBalancing.balanced, Config.ConnectionShape.linear},
                // increasing
                {3, 6, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                {5, 5, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                {5, 10, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                {5, 19, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                {5, 19, Config.ParaBalancing.increasing, Config.ConnectionShape.star},
                {5, 19, Config.ParaBalancing.increasing, Config.ConnectionShape.diamond},
                {10, 300, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                {7, 1000, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                {100, 1000000, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                //decreasing
                {5, 5, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                {5, 10, Config.ParaBalancing.decreasing, Config.ConnectionShape.linear},
                {5, 19, Config.ParaBalancing.decreasing, Config.ConnectionShape.linear},
                {5, 19, Config.ParaBalancing.decreasing, Config.ConnectionShape.star},
                {5, 19, Config.ParaBalancing.decreasing, Config.ConnectionShape.diamond},
                {10, 300, Config.ParaBalancing.decreasing, Config.ConnectionShape.linear},
                {7, 1000, Config.ParaBalancing.decreasing, Config.ConnectionShape.linear},
                {100, 1000000, Config.ParaBalancing.decreasing, Config.ConnectionShape.linear},
                // pyramid
                {5, 5, Config.ParaBalancing.increasing, Config.ConnectionShape.linear},
                {5, 10, Config.ParaBalancing.pyramid, Config.ConnectionShape.linear},
                {5, 19, Config.ParaBalancing.pyramid, Config.ConnectionShape.linear},
                {5, 19, Config.ParaBalancing.pyramid, Config.ConnectionShape.star},
                {5, 19, Config.ParaBalancing.pyramid, Config.ConnectionShape.diamond},
                {10, 300, Config.ParaBalancing.pyramid, Config.ConnectionShape.linear},
                {7, 1000, Config.ParaBalancing.pyramid, Config.ConnectionShape.linear},
                {100, 1000000, Config.ParaBalancing.pyramid, Config.ConnectionShape.linear}
        });
    }

    private boolean checkSequence(ArrayList<Integer> arr){
        int pivot = (int) Math.ceil(arr.size() / 2);

        for(int i=1; i<arr.size(); i++){
            switch(this.paraBalancing){
                case balanced:
                    if(Math.abs(arr.get(i) - arr.get(0)) > 1) return false;
                    break;
                case increasing:
                    if(arr.get(i-1)>arr.get(i)) return false;
                    break;
                case decreasing:
                    if(arr.get(i-1)<arr.get(i)) return false;
                    break;
                case pyramid:
                    if(i <= pivot && arr.get(i-1)>arr.get(i)) {
                        System.out.println("UP => " + arr.get(i-1) + " " + arr.get(i) + " " + arr.get(i+1));
                        return false;
                    }
                    else if(i > pivot && arr.get(i-1)<arr.get(i)){
                        System.out.println("UP => " + (i-1) + ":" + arr.get(i-1) + " " + i + ":" + + arr.get(i) + " " + (i+1) + ":" + arr.get(i+1));
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    @Test
    public void testComputeComponentsParallelism(){

        ArrayList<Integer> computedParallelism = builder.getComponentsParallelism();
        assertTrue(checkSequence(computedParallelism));

    }

}
