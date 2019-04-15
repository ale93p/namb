package fr.unice.yamb.flink.operators;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.util.Collector;

public class WindowedBusyWaitFunction implements AllWindowFunction<Tuple3<String, Long, Long>, Tuple3<String, Long, Long>, TimeWindow>{

    private long _cycles;
    private int _rate;
    private String _me;

    public WindowedBusyWaitFunction(long cycles, int frequency){
        this._cycles = cycles;
        if(frequency > 0) this._rate = 1 / frequency;
        else this._rate = 0;
        this._me = "windowed task";
    }

    @Override
    public void apply(TimeWindow window, Iterable<Tuple3<String, Long, Long>> values, Collector<Tuple3<String, Long, Long>> out){

        Tuple3<String, Long, Long> value = null;
        for (Tuple3<String, Long, Long> t: values) {
            String nextValue = t.f0;
            Long id = t.f1;
            // simulate processing load
            for (long i = 0; i < this._cycles; i++) { }
            Long ts = System.currentTimeMillis();
            if (this._rate > 0 && id % this._rate == 0){
                System.out.println("[DEBUG] " + this._me + ": " + id + "," + ts + "," + nextValue);
            }
        }


        out.collect(value);
    }

}
