package fr.unice.yamb.flink.operators;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.util.Collector;

public class WindowedBusyWaitFunction implements AllWindowFunction<Tuple4<String, String, Long, Long>, Tuple4<String, String, Long, Long>, TimeWindow>{

    private long _cycles;
    private int _rate;
    private String _me;

    public WindowedBusyWaitFunction(long cycles, double frequency){
        this._cycles = cycles;
        if(frequency > 0) this._rate = (int)(1 / frequency);
        else this._rate = 0;
        this._me = "windowed task";
    }

    @Override
    public void apply(TimeWindow window, Iterable<Tuple4<String, String, Long, Long>> values, Collector<Tuple4<String, String, Long, Long>> out){

        Tuple4<String, String, Long, Long> value = null;
        for (Tuple4<String, String, Long, Long> t: values) {
            String nextValue = t.f0;
            String tuple_id = t.f1;
            Long tuple_num = t.f2;
            // simulate processing load
            for (long i = 0; i < this._cycles; i++) { }
            Long ts = System.currentTimeMillis();
            if (this._rate > 0 && tuple_num % this._rate == 0){
                System.out.println("[DEBUG] [" + this._me + "] : " + tuple_id + "," + tuple_num + "," + ts + "," + nextValue);
            }
            value = t;
        }


        out.collect(value);
    }

}
