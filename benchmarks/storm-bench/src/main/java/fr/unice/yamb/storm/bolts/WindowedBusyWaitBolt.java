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


    public WindowedBusyWaitBolt(long cycles){
        this._cycles = cycles;
    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
    }

    public void execute(TupleWindow inputWindow){

        Object payload = null;
        Object id = null;
        Long ts;

        for (Tuple tuple : inputWindow.get()) {
            payload = tuple.getValue(0);
            id = tuple.getValue(1);
            // simulate processing load
            for (long i = 0; i < _cycles; i++) {
            }
        }

        ts = System.currentTimeMillis();
        _collector.emit(new Values(payload, id, ts));

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value", "id", "timestamp"));}

}
