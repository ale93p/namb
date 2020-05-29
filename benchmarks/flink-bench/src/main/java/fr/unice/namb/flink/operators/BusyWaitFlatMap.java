package fr.unice.namb.flink.operators;

import fr.unice.namb.utils.configuration.Config;
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
    private Random _rand;
    private long _count;
    private String _me;


    public BusyWaitFlatMap(long cycles, double filtering, int dataSize, double frequency, String operator_name){
        _cycles = cycles;
        _filtering = filtering;
        _dataSize = dataSize;
        _me = operator_name;
        _count = 0;
        if(frequency > 0) _rate = (int)(1 / frequency);
        else _rate = 0;
    }

    public BusyWaitFlatMap(int cycles, double filtering, double frequency, String operator_name){
        this(cycles, filtering, 0, frequency, operator_name);
    }

    @Override
    public void open(Configuration conf){
        _me = _me + "_" + getRuntimeContext().getIndexOfThisSubtask();

        if (this._filtering > 0){
            this._rand = new Random();
        }
    }

    @Override
    public void flatMap(Tuple4<String, String, Long, Long> in, Collector<Tuple4<String, String, Long, Long>> out){




        String nextValue = in.f0;
        String tuple_id = in.f1;
        long sourceCount = in.f2;

        if(this._dataSize > 0 && this._dataSize < nextValue.length()){
            nextValue = nextValue.substring(0, this._dataSize);
        }

        _count ++;
        // simulate processing load
        for(long i = 0; i < _cycles; i++){}

        long ts = System.currentTimeMillis();
        if(this._filtering > 0) {
            if (this._rand.nextInt(Config.WF_FILTERING_PRECISION) <= this._filtering * Config.WF_FILTERING_PRECISION) {
                out.collect(new Tuple4<>(nextValue, tuple_id, sourceCount, ts));
            }
        }
        else {
            out.collect(new Tuple4<>(nextValue, tuple_id, sourceCount, ts));
        }

        if (this._rate > 0 && sourceCount % this._rate == 0){
            if (ts == 0) ts = System.currentTimeMillis();
            System.out.println("[DEBUG] [" + _me + "] : " + tuple_id + "," + this._count + "," + ts + "," + nextValue );
        }

    }
}
