package fr.unice.namb.flink.operators;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.configuration.Configuration;

public class BusyWaitMap extends RichMapFunction<Tuple1<String>, Tuple1<String>> {

    private long _cycles;
    private int pID;
    private String name;

    public BusyWaitMap(long cycles){
        this._cycles = cycles;
    }

    @Override
    public void open(Configuration conf){
        this.pID = getRuntimeContext().getIndexOfThisSubtask();
        this.name = getRuntimeContext().getTaskName();
    }

    @Override
    public Tuple1<String> map(Tuple1<String> tuple) throws Exception{

        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}

        // System.out.println(name + " " + pID +  ": " + (String) tuple.getField(0));
        return tuple;
    }
}
