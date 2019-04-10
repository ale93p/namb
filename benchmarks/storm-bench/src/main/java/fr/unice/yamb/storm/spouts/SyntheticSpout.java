package fr.unice.yamb.storm.spouts;

import fr.unice.yamb.utils.common.StringGenerator;
import fr.unice.yamb.utils.common.DataStream;
import fr.unice.yamb.utils.configuration.Config;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class SyntheticSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;

    private int dataSize;
    private int dataValues;
    private Config.DataDistribution dataValuesBalancing;
    private int flowRate;
    private long sleepTime;
    private Config.ArrivalDistribution distribution;
    private DataStream dataStream;
    private boolean reliable;


    private ArrayList<byte[]> payloadArray;
    private Random index;
    private long count;
    private long ts;

    public SyntheticSpout(int dataSize, int dataValues, Config.DataDistribution dataValuesBalancing, Config.ArrivalDistribution flowDistribution, int flowRate, boolean reliable) {
        this.dataSize = dataSize;
        this.dataValues = dataValues;
        this.dataValuesBalancing = dataValuesBalancing;
        this.distribution = flowDistribution;
        this.flowRate = flowRate;
        this.reliable = reliable;
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        StringGenerator generator = new StringGenerator(this.dataSize);
        this.payloadArray = generator.generatePayload(this.dataValues, this.dataValuesBalancing);
        this.dataStream = new DataStream();
        if (this.flowRate != 0)
            this.sleepTime = dataStream.convertToInterval(this.flowRate);
        this.count = 0;
        this.index = new Random();
        this._collector = collector;
    }

    public void nextTuple(){
        byte[] nextValue = this.payloadArray.get(this.index.nextInt(this.payloadArray.size()));
        try {
            if (this.flowRate != 0) {
                Utils.sleep(
                        dataStream.getInterMessageTime(this.distribution, (int) this.sleepTime)
                );
            }
            this.ts = System.currentTimeMillis();
            this.count++;
            if(this.reliable) {
                _collector.emit(new Values(nextValue, this.count, this,ts), this.count);
            }
            else
                _collector.emit(new Values(nextValue, this.count, this.ts));

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void ack(Object msgId){ super.ack(msgId); }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("value", "id", "timestamp"));
    }
}
