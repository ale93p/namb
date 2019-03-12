package fr.unice.yamb.heron.bolts;

import com.twitter.heron.api.bolt.BaseRichBolt;
import com.twitter.heron.api.bolt.OutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import com.twitter.heron.api.tuple.Values;

import java.util.Map;

public class BusyWaitBolt extends BaseRichBolt {

    private OutputCollector _collector;
    private long _cycles;
    private boolean _reliable;


    public BusyWaitBolt(long cycles, boolean msg_reliability){
        this._cycles = cycles;
        this._reliable = msg_reliability;
    }

    public BusyWaitBolt(long cycles){
        this(cycles, false);
    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
    }

    public void execute(Tuple tuple){

        Object payload = tuple.getValue(0);

        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}

        _collector.emit(new Values(payload));
        if (this._reliable){ _collector.ack(tuple); }

        System.out.println("Hello!");

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value"));}

}
