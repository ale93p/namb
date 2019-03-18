package fr.unice.yamb.flink.operators;

import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.util.Collector;

public class WindowedBusyWaitFunction implements AllWindowFunction<Tuple1<String>, Tuple1<String>, TimeWindow>{

    private long _cycles;

    public WindowedBusyWaitFunction(long cycles){
        this._cycles = cycles;
    }

    @Override
    public void apply(TimeWindow window, Iterable<Tuple1<String>> values, Collector<Tuple1<String>> out){

        Tuple1<String> value = null;
        for (Tuple1<String> t: values) {
            value = t;
            // simulate processing load
            for (long i = 0; i < this._cycles; i++) { }
        }

        out.collect(value);
    }

}
