package fr.unice.yamb.heron.bolts;

import com.twitter.heron.api.bolt.BaseRichBolt;
import com.twitter.heron.api.bolt.OutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyContext;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Tuple;
import com.twitter.heron.api.tuple.Values;
import fr.unice.yamb.utils.configuration.Config;

import java.util.Map;
import java.util.Random;

public class BusyWaitBolt extends BaseRichBolt {

    private OutputCollector _collector;
    private long _cycles;
    private double _filtering;
    private boolean _reliable;
    private int _dataSize;
    private int _rate;
    private long _count;
    private String _me;
    private Random _rand;


    public BusyWaitBolt(long cycles, double filtering, boolean msg_reliability, int dataSize, double frequency){
        this._cycles = cycles;
        this._filtering = filtering;
        this._reliable = msg_reliability;
        this._dataSize = dataSize;
        this._count = 0;
        if (frequency > 0) this._rate = (int)(1 / frequency);
        else this._rate = 0;

    }


    public BusyWaitBolt(long cycles, double filtering, boolean msg_reliability, double frequency){
        this(cycles, filtering, msg_reliability, 0, frequency);
    }




    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
        this._me = context.getThisComponentId() + "_" + context.getThisTaskId();
        if(this._filtering > 0){
            this._rand = new Random()
        }

    }

    public void execute(Tuple tuple){

        String payload = tuple.getString(0);
        if(this._dataSize > 0 && this._dataSize < payload.length()){
            payload = payload.substring(0, this._dataSize);
        }

        String id = tuple.getString(1);

        this._count ++;

        // simulate processing load
        for(long i = 0; i < this._cycles; i++){}



        Long ts = 0L;
        if(this._filtering > 0){
            if (this._rand.nextInt(Config.WF_FILTERING_PRECISION) <= this._filtering * Config.WF_FILTERING_PRECISION) {
                ts = System.currentTimeMillis();
                _collector.emit(new Values(payload, id, this._count, ts));
            }
        }
        else {
            ts = System.currentTimeMillis();
            _collector.emit(new Values(payload, id, this._count, ts));
        }

        if (this._reliable) {
            _collector.ack(tuple);
        }

        if (this._rate > 0 && this._count % this._rate == 0) {
            if (ts == 0) ts = System.currentTimeMillis();
            System.out.println("[DEBUG] [" + this._me + "] : " + id + "," + this._count + "," + ts + "," + payload);
        }

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value", "id", "timestamp"));}

}
