package fr.unice.yamb.storm.bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.Map;

public class WindowedBusyWaitBolt extends BaseWindowedBolt {
    private OutputCollector _collector;
    private long _cycles;
    private int _rate;
    private String _me;


    public WindowedBusyWaitBolt(long cycles, int frequency){
        this._cycles = cycles;
        if (frequency > 0) this._rate = 1/frequency;
        else this._rate = 0;
    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
        this._me = context.getThisComponentId() + "_" + context.getThisTaskId();
    }

    public void execute(TupleWindow inputWindow){

        Object payload = null;
        Long id = null;
        Long ts;

        for (Tuple tuple : inputWindow.get()) {
            payload = tuple.getValue(0);
            id = tuple.getLong(1);
            // simulate processing load
            for (long i = 0; i < _cycles; i++) {
            }
        }

        ts = System.currentTimeMillis();
        _collector.emit(new Values(payload, id, ts));

        if (this._rate > 0 && id % this._rate == 0){
            System.out.println("[DEBUG] " + this._me + ": " + id + "," + ts + "," + payload);
        }


    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value", "id", "timestamp"));}

}
