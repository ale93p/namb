package fr.unice.namb.storm.spouts;

import fr.unice.namb.utils.common.DataStream;
import fr.unice.namb.utils.configuration.ConfigDefaults.DataBalancing;
import fr.unice.namb.utils.configuration.ConfigDefaults.Distribution;
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
    private DataBalancing dataValuesBalancing;
    private long sleepTime;
    private Distribution distribution;
    private DataStream dataStream;


    private ArrayList<byte[]> payloadArray;
    private Random index;
    private long count;

    public SyntheticSpout(int dataSize, int dataValues, DataBalancing dataValuesBalancing, Distribution flowDistribution, int flowRate) {
        this.dataSize = dataSize;
        this.dataValues = dataValues;
        this.dataValuesBalancing = dataValuesBalancing;
        this.distribution = flowDistribution;
        this.sleepTime = convertToInterval(flowRate);
    }

    private long convertToInterval(int msgPerSec){
        return 1000/msgPerSec; // Interval in ms
    }

    private ArrayList<byte[]> generatePayload(int size, DataBalancing balancing){
        String nextString;
        ArrayList<byte[]> payloadArray = new ArrayList<>();
        StringGenerator generator = new StringGenerator(size);
        for(int i=0; i<this.dataValues; i++) { //can this be optimized?
            nextString = generator.next();
            payloadArray.add(nextString.getBytes());
            if(balancing == DataBalancing.unbalanced){
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
