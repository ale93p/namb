package fr.unice.yamb.flink.operators;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;

public class BusyWaitMap extends RichMapFunction<Tuple3<String, Long, Long>, Tuple3<String, Long, Long>> {

    private long _cycles;
    private int _rate;
    private String _me;

    public BusyWaitMap(long cycles, int frequency){
        this._cycles = cycles;
        if(frequency > 0) this._rate = 1 / frequency;
        else this._rate = 0;
    }

    @Override
    public void open(Configuration conf){
        this._me = getRuntimeContext().getTaskName() + "_" + getRuntimeContext().getIndexOfThisSubtask();
    }

    @Override
    public Tuple3<String, Long, Long> map(Tuple3<String, Long, Long> tuple) throws Exception{

        String nextValue = tuple.f0;
        Long id = tuple.f1;
        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}

        Long ts = System.currentTimeMillis();
        if (this._rate > 0 && id % this._rate == 0){
            System.out.println("[DEBUG] " + this._me + ": " + id + "," + ts + "," + nextValue);
        }
        return tuple;
    }
}
