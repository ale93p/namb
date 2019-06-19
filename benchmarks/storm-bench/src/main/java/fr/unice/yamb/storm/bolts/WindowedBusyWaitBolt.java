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


    public WindowedBusyWaitBolt(long cycles, double frequency){
        this._cycles = cycles;
        if (frequency > 0) this._rate = (int)(1/frequency);
        else this._rate = 0;
    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
        this._me = context.getThisComponentId() + "_" + context.getThisTaskId();
    }

    public void execute(TupleWindow inputWindow){

        Object payload = null;
        String id = null;
        Long num = null;
        Long ts = System.currentTimeMillis();


        for (Tuple tuple : inputWindow.get()) {
            payload = tuple.getValue(0);
            id = tuple.getString(1);
            num = tuple.getLong(2);
            // simulate processing load
            for (long i = 0; i < _cycles; i++) {
            }
            ts = System.currentTimeMillis();
            if (this._rate > 0 && num % this._rate == 0){
                System.out.println("[DEBUG] [" + this._me + "] : " + id + "," + num + "," + ts + "," + payload.toString());
            }
        }

        _collector.emit(new Values(payload, id, num, ts));


    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value", "id", "num", "timestamp"));}

}
