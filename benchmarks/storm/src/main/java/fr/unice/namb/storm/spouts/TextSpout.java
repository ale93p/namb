package fr.unice.namb.storm.spouts;

import fr.unice.namb.utils.configuration.ConfigDefaults.Distribution;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class TextSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;

    private int dataSize;
    private long sleepTime;
    private Distribution distribution;
    private String payload;
    private long count;

    public TextSpout(int dataSize, Distribution distribution, int rate) {
        this.dataSize = dataSize;
        this.sleepTime = convertToInterval(rate);
        this.distribution = distribution;
    }

    private long convertToInterval(int msgPerSec){
        return 1000/msgPerSec; // Interval in ms
    }

    private String generatePayload(int size){
        byte[] payload = new byte[size];
        Arrays.fill(payload, (byte) 'P');
        return new String(payload, StandardCharsets.UTF_8);
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        this.payload = generatePayload(this.dataSize);
        this.count = 0;
        this._collector = collector;
    }

    public void nextTuple(){
        switch(this.distribution){
            case uniform:
                Utils.sleep(this.sleepTime);
                _collector.emit(new Values(this.payload), count++);
                break;
            case burst:
                //TODO: find a way to generate bursts from time to time
                //Utils.sleep(this.sleepTime);
                //_collector.emit(new Values(this.payload), count++);
                break;
        }

    }

    public void ack(Object msgId){ super.ack(msgId); }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("text"));
    }
}
