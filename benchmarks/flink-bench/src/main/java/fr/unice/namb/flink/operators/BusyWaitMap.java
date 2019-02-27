package fr.unice.namb.flink.operators;

import org.apache.flink.api.common.functions.MapFunction;

public class BusyWaitMap implements MapFunction<String, String> {

    private long _cycles;

    public BusyWaitMap(long cycles){
        this._cycles = cycles;
    }

    @Override
    public String map(String value) throws Exception{

        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}

        return value;
    }
}
