package fr.unice.yamb.tests.spouts;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;

public class TextSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;
    private long count;
    private long sleepTime;
    private String sampleText = "text samle";

    public TextSpout(){
        this.sleepTime = 1000;
    }

    public TextSpout(long millis){
        this.sleepTime = millis;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        this.count = 0;
        this._collector = collector;
    }

    @Override
    public void activate(){ }

    public void nextTuple(){
        Utils.sleep(this.sleepTime);
        _collector.emit(new Values(this.sampleText), count++);
        System.out.println("emitted: \"" + this.sampleText + "\"");
    }

    @Override
    public void ack(Object msgId){ super.ack(msgId); }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("text"));
    }

}
