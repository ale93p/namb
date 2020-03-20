package fr.unice.namb.storm.spouts;

import fr.unice.namb.utils.common.DataGenerator;
import fr.unice.namb.utils.common.StreamGenerator;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Data;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Synthetic;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SyntheticSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;

    
    private long sleepTime;
    private Config.ArrivalDistribution distribution;
    private DataGenerator dataGenerator;
    private StreamGenerator dataStream;
    private boolean reliable;
    private int rate;

    private long count;
    private long ts;
    private String me;

    public SyntheticSpout(Data data, Flow flow, boolean isReliable, double frequency) throws Exception {
    	this.dataGenerator = new DataGenerator(data);
    	this.dataStream = new StreamGenerator(flow);
    	
        this.reliable = isReliable;
        if (frequency > 0) this.rate = (int) (1/ frequency);
        else this.rate = 0;
    }
    
    public SyntheticSpout(Synthetic conf, boolean isReliable, double frequency) throws Exception {
    	this(conf.getData(), conf.getFlow(), isReliable, frequency);
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        this.count = 0;
        this._collector = collector;
        this.me = context.getThisComponentId() + "_" + context.getThisTaskId();
    }

    public void nextTuple(){
        try {
        	String nextValue = new String(dataGenerator.getNextValue());
            
            double sleepTime = this.dataStream.getSleepTime();
            
            if(sleepTime != 0) {
                Thread.sleep(
            		(long) sleepTime, (int)((sleepTime - (long)sleepTime) * 1000000)
                );
            }

            this.count++;
            String tuple_id = UUID.randomUUID().toString();
            this.ts = System.currentTimeMillis();
            if(this.reliable) {
                _collector.emit(new Values(nextValue, tuple_id, this.count, this.ts), this.count);
            }
            else {
                _collector.emit(new Values(nextValue, tuple_id, this.count, this.ts));
            }
            
            if (this.rate > 0  && this.count % this.rate == 0){
                System.out.println("[DEBUG] [" + this.me + "] : " + tuple_id + "," + this.count + "," + this.ts + "," + nextValue);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void ack(Object msgId){ super.ack(msgId); }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("value", "id", "num", "timestamp"));
    }
}
