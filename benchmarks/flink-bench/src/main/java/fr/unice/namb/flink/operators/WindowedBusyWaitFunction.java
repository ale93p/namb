package fr.unice.namb.flink.operators;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.util.Collector;

public class WindowedBusyWaitFunction implements AllWindowFunction<Tuple4<String, String, Long, Long>, Tuple4<String, String, Long, Long>, TimeWindow>{

    private long _cycles;
    private int _rate;
    private String _me;
    private long _count;

    public WindowedBusyWaitFunction(long cycles, double frequency){
        this._cycles = cycles;
        if(frequency > 0) this._rate = (int)(1 / frequency);
        else this._rate = 0;
        this._me = "windowed task";
        this._count = 0;
    }

    @Override
    public void apply(TimeWindow window, Iterable<Tuple4<String, String, Long, Long>> values, Collector<Tuple4<String, String, Long, Long>> out){

        Long ts = 0L;


        Tuple4<String, String, Long, Long> value = null;
        for (Tuple4<String, String, Long, Long> t: values) {
            String nextValue = t.f0;
            String tuple_id = t.f1;
            long sourceCount = t.f2;
            this._count ++;
            // simulate processing load
            for (long i = 0; i < this._cycles; i++) { }
            ts = System.currentTimeMillis();
            if (this._rate > 0 && sourceCount % this._rate == 0){
                System.out.println("[DEBUG] [" + this._me + "] : " + tuple_id + "," + this._count + "," + ts + "," + nextValue);
            }
            value = t;
        }


        out.collect(value);
    }

}
