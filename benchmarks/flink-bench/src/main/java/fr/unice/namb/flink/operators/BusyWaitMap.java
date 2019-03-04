package fr.unice.namb.flink.operators;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple1;

public class BusyWaitMap implements MapFunction<Tuple1<String>, Tuple1<String>> {

    private long _cycles;

    public BusyWaitMap(long cycles){
        this._cycles = cycles;
    }

    @Override
    public Tuple1<String> map(Tuple1<String> tuple) throws Exception{

        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}

        return tuple;
    }
}
