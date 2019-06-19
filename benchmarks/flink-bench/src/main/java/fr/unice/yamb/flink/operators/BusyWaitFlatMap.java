package fr.unice.yamb.flink.operators;

import fr.unice.yamb.utils.configuration.Config;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.Random;

public class BusyWaitFlatMap extends RichFlatMapFunction<Tuple4<String, String, Long, Long>, Tuple4<String, String, Long, Long>> {

    private long _cycles;
    private double _filtering;
    private int _dataSize;
    private int _rate;
    private String _me;
    private Random _rand;


    public BusyWaitFlatMap(long cycles, double filtering, int dataSize, double frequency, String operator_name){
        this._cycles = cycles;
        this._filtering = filtering;
        this._dataSize = dataSize;
        this._me = operator_name;
        if(frequency > 0) this._rate = (int)(1 / frequency);
        else this._rate = 0;
    }

    public BusyWaitFlatMap(long cycles, double filtering, double frequency, String operator_name){
        this(cycles, filtering, 0, frequency, operator_name);
    }

    @Override
    public void open(Configuration conf){
        this._me = this._me + "_" + getRuntimeContext().getIndexOfThisSubtask();

        if (this._filtering > 0){
            this._rand = new Random();
        }
    }

    @Override
    public void flatMap(Tuple4<String, String, Long, Long> in, Collector<Tuple4<String, String, Long, Long>> out) throws Exception{

        String nextValue = in.f0;
        if(this._dataSize > 0 && this._dataSize < nextValue.length()){
            nextValue = nextValue.substring(0, this._dataSize);
        }
        String tuple_id = in.f1;
        Long tuple_num = in.f2;

        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}

        Long ts = 0L;

        if(this._filtering > 0) {
            if (this._rand.nextInt(Config.DF_FILTERING_PRECISION) <= this._filtering * Config.DF_FILTERING_PRECISION) {
                ts = System.currentTimeMillis();
                out.collect(new Tuple4<>(nextValue, tuple_id, tuple_num, ts));

            }
        }
        else {
            ts = System.currentTimeMillis();
            out.collect(new Tuple4<>(nextValue, tuple_id, tuple_num, ts));
        }

        if (this._rate > 0 && tuple_num % this._rate == 0){
            if (ts == 0) ts = System.currentTimeMillis();
            System.out.println("[DEBUG] [" + this._me + "] : " + tuple_id + "," + tuple_num + "," + ts + "," + nextValue);
        }

    }
}
