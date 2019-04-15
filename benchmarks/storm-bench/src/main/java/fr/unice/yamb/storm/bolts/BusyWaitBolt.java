package fr.unice.yamb.storm.bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class BusyWaitBolt extends BaseRichBolt {

    private OutputCollector _collector;
    private long _cycles;
    private boolean _reliable;
    private int _rate;
    private String _me;


    public BusyWaitBolt(long cycles, boolean msg_reliability, int frequency){
        this._cycles = cycles;
        this._reliable = msg_reliability;
        if(frequency!=0) this._rate = 1/frequency;
        else this._rate = 0;
    }

    public BusyWaitBolt(long cycles){
        this(cycles, false, 0);
    }

    public BusyWaitBolt(long cycles, int frequency){ this(cycles, false, frequency); }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
        this._me = context.getThisComponentId() + "_" + context.getThisTaskId();
    }

    public void execute(Tuple tuple){


        Object payload = tuple.getValue(0);
        Long id = tuple.getLong(1);

        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}

        Long ts = System.currentTimeMillis();
        _collector.emit(new Values(payload, id, ts));
        if (this._reliable){ _collector.ack(tuple); }

        if (this._rate > 0 && id % this._rate == 0){
            System.out.println("[DEBUG] " + this._me + ": " + id + "," + ts + "," + payload);
        }

        System.out.println("Hello!");

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value", "id", "timestamp"));}

}
