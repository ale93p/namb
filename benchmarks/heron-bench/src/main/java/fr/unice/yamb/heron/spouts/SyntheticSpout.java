package fr.unice.yamb.heron.spouts;

import com.twitter.heron.api.spout.BaseRichSpout;
import com.twitter.heron.api.topology.TopologyContext;
import fr.unice.yamb.utils.common.DataStream;
import fr.unice.yamb.utils.common.StringGenerator;
import fr.unice.yamb.utils.configuration.Config;
import com.twitter.heron.api.spout.SpoutOutputCollector;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.tuple.Fields;
import com.twitter.heron.api.tuple.Values;
import com.twitter.heron.api.utils.Utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class SyntheticSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;

    private int dataSize;
    private int dataValues;
    private Config.DataBalancing dataValuesBalancing;
    private int flowRate;
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
        this.flowRate = flowRate;
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        StringGenerator generator = new StringGenerator(this.dataSize);
        this.payloadArray = generator.generatePayload(this.dataValues, this.dataValuesBalancing);
        this.dataStream = new DataStream();
        this.sleepTime = dataStream.convertToInterval(this.flowRate);
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
