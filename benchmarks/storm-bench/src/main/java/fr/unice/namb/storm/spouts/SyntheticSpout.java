package fr.unice.namb.storm.spouts;

import fr.unice.namb.utils.common.StringGenerator;
import fr.unice.namb.utils.common.DataStream;
import fr.unice.namb.utils.configuration.Config;
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
    private Config.DataBalancing dataValuesBalancing;
    private long sleepTime;
    private Config.Distribution distribution;
    private DataStream dataStream;


    private ArrayList<byte[]> payloadArray;
    private Random index;
    private long count;

    public SyntheticSpout(int dataSize, int dataValues, Config.DataBalancing dataValuesBalancing, Config.Distribution flowDistribution, int flowRate) {
        this.dataSize = dataSize;
        this.dataValues = dataValues;
        this.dataValuesBalancing = dataValuesBalancing;
        this.distribution = flowDistribution;
        this.sleepTime = convertToInterval(flowRate);
    }

    private long convertToInterval(int msgPerSec){
        return 1000/msgPerSec; // Interval in ms
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        StringGenerator generator = new StringGenerator(this.dataSize);
        this.payloadArray = generator.generatePayload(this.dataValues, this.dataValuesBalancing);
        this.dataStream = new DataStream();
        this.count = 0;
        this.index = new Random();
        this._collector = collector;
    }

    public void nextTuple(){
        byte[] nextValue = this.payloadArray.get(this.index.nextInt(this.payloadArray.size()));
        try {
            Utils.sleep(
                    dataStream.getInterMessageTime(this.distribution, (int) this.sleepTime)
            );
            _collector.emit(new Values(nextValue), count++);
            this.count++;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void ack(Object msgId){ super.ack(msgId); }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("value"));
    }
}
