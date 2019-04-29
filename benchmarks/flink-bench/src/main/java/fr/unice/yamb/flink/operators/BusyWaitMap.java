package fr.unice.yamb.flink.operators;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;

public class BusyWaitMap extends RichMapFunction<Tuple4<String, String, Long, Long>, Tuple4<String, String, Long, Long>> {

    private long _cycles;
    private int _rate;
    private String _me;

    public BusyWaitMap(long cycles, float frequency){
        this._cycles = cycles;
        if(frequency > 0) this._rate = (int)(1 / frequency);
        else this._rate = 0;
    }

    @Override
    public void open(Configuration conf){
        this._me = getRuntimeContext().getTaskName() + "_" + getRuntimeContext().getIndexOfThisSubtask();
    }

    @Override
    public Tuple4<String, String, Long, Long> map(Tuple4<String, String, Long, Long> tuple) throws Exception{

        String nextValue = tuple.f0;
        String tuple_id = tuple.f1;
        Long tuple_num = tuple.f2;

        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}

        Long ts = System.currentTimeMillis();
        if (this._rate > 0 && tuple_num % this._rate == 0){
            System.out.println("[DEBUG] [" + this._me + "] : " + tuple_id + "," + tuple_num + "," + ts + "," + nextValue);
        }
        return tuple;
    }
}
