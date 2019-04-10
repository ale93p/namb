package fr.unice.yamb.flink.operators;

import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.util.Collector;

public class WindowedBusyWaitFunction implements AllWindowFunction<Tuple3<String, Long, Long>, Tuple3<String, Long, Long>, TimeWindow>{

    private long _cycles;

    public WindowedBusyWaitFunction(long cycles){
        this._cycles = cycles;
    }

    @Override
    public void apply(TimeWindow window, Iterable<Tuple3<String, Long, Long>> values, Collector<Tuple3<String, Long, Long>> out){

        Tuple3<String, Long, Long> value = null;
        for (Tuple3<String, Long, Long> t: values) {
            value = t;
            // simulate processing load
            for (long i = 0; i < this._cycles; i++) { }
        }

        out.collect(value);
    }

}
