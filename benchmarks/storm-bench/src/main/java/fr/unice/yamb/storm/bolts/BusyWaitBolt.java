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

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value"));}

}
