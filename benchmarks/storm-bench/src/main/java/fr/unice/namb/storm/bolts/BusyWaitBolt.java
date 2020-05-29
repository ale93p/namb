package fr.unice.namb.storm.bolts;

import fr.unice.namb.utils.configuration.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

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
        if(frequency!=0) this._rate = (int)(1/frequency);
        else this._rate = 0;
    }

    public BusyWaitBolt(long cycles, double filtering, boolean msg_reliability, double frequency){
        this(cycles, filtering, msg_reliability, 0, frequency);
    }



    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        this._collector = collector;
        this._me = context.getThisComponentId() + "_" + context.getThisTaskId();
        if (this._filtering > 0){
            this._rand = new Random();
        }
    }

    public void execute(Tuple tuple){


        String payload = tuple.getString(0);
        long sourceCount = tuple.getLong(2);
        if(this._dataSize > 0 && this._dataSize < payload.length()){
            payload = payload.substring(0, this._dataSize);
        }

        String id = tuple.getString(1);

        this._count ++;
        // simulate processing load
        for(long i = 0; i < this._cycles; i++);

        Long ts = System.currentTimeMillis();
        if(this._filtering > 0){
            if (this._rand.nextInt(Config.WF_FILTERING_PRECISION) <= this._filtering * Config.WF_FILTERING_PRECISION) {
                _collector.emit(new Values(payload, id, sourceCount, ts));
            }
        }
        else {
            _collector.emit(new Values(payload, id, sourceCount, ts));
        }

        if (this._reliable) {
            _collector.ack(tuple);
        }

        if (this._rate > 0 && sourceCount % this._rate == 0) {
            if (ts == 0) ts = System.currentTimeMillis();
            System.out.println("[DEBUG] [" + this._me + "] : " + id + "," + this._count + "," + ts + "," + payload);
        }


    }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("value", "id", "num", "timestamp"));}




}
