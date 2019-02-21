package fr.unice.namb.heron.spouts;

import com.twitter.heron.api.spout.BaseRichSpout;
import com.twitter.heron.api.topology.TopologyContext;
import fr.unice.namb.utils.common.DataStream;
import fr.unice.namb.utils.common.StringGenerator;
import fr.unice.namb.utils.configuration.Config;
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

    private ArrayList<byte[]> generatePayload(int size, Config.DataBalancing balancing){
        String nextString;
        ArrayList<byte[]> payloadArray = new ArrayList<>();
        StringGenerator generator = new StringGenerator(size);
        for(int i=0; i<this.dataValues; i++) { //can this be optimized?
            nextString = generator.next();
            payloadArray.add(nextString.getBytes());
            if(balancing == Config.DataBalancing.unbalanced){
                for(int j=1; i<Math.pow(2,i); i++){
                    payloadArray.add(nextString.getBytes());
                }
            }

        }
        return payloadArray;
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        this.payloadArray = generatePayload(this.dataSize, this.dataValuesBalancing);
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
