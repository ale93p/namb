package fr.unice.yamb.storm.bolts;

import fr.unice.yamb.utils.configuration.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.List;
import java.util.Map;

public class WindowedBusyWaitBolt extends BaseWindowedBolt {
    private OutputCollector _collector;
    private long _cycles;
    private boolean _reliable;



    public WindowedBusyWaitBolt(long cycles, boolean msgReliability){
        this._cycles = cycles;
        this._reliable = msgReliability;
    }

    public WindowedBusyWaitBolt(long cycles, Config.WindowingType windowType){
        this(cycles, false);
    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
    }

    public void execute(TupleWindow inputWindow){

        Object payload = null;

        for (Tuple tuple : inputWindow.get()) {
            payload = tuple.getValue(0);
            // simulate processing load
            for (long i = 0; i < _cycles; i++) {
            }
        }


        _collector.emit(new Values(payload));

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value"));}

}
