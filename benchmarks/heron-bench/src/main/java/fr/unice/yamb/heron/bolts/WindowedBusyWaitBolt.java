package fr.unice.yamb.heron.bolts;

import com.twitter.heron.api.bolt.BaseWindowedBolt;
import com.twitter.heron.api.bolt.OutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import com.twitter.heron.api.tuple.Values;
import com.twitter.heron.api.windowing.TupleWindow;

import java.util.Map;

public class WindowedBusyWaitBolt extends BaseWindowedBolt {
    private OutputCollector _collector;
    private long _cycles;
    private int _rate;
    private long _count;
    private String _me;

    public WindowedBusyWaitBolt(long cycles, double frequency){
        this._cycles = cycles;
        if (frequency > 0) this._rate = (int)(1 / frequency);
        else this._rate = 0;

    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
        this._me = context.getThisComponentId() + "_" + context.getThisTaskId();
        this._count = 0;

    }

    public void execute(TupleWindow inputWindow){

        Long ts = 0L;
        String payload = null;
        String id = null;

        for (Tuple tuple : inputWindow.get()) {
            payload = tuple.getString(0);
            id = tuple.getString(1);
            this._count ++;
            // simulate processing load
            for (long i = 0; i < _cycles; i++) {
            }
            ts = System.currentTimeMillis();
            if (this._rate > 0 && this._count % this._rate == 0){
                System.out.println("[DEBUG] [" + this._me + "]: " + id + "," + this._count + "," + ts + "," + payload);
            }

        }

        _collector.emit(new Values(payload, id, ts));



    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value", "id", "timestamp"));}


}
