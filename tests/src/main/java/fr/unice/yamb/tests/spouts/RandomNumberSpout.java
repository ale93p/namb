package fr.unice.yamb.tests.spouts;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

public class RandomNumberSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;
    private long count;
    private long sleepTime;
    private Random randomNumber;

    public RandomNumberSpout(){
        this.sleepTime = 1000;
    }

    public RandomNumberSpout(long millis){
        this.sleepTime = millis;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        this.randomNumber = new Random();
        this.count = 0;
        this._collector = collector;
    }

    @Override
    public void activate(){ }

    public void nextTuple(){
        Utils.sleep(this.sleepTime);
        String nextTuple = String.valueOf(this.randomNumber.nextInt(100));
        _collector.emit(new Values(nextTuple), count++);
        System.out.println("emitted: \"" + nextTuple + "\"");
    }

    @Override
    public void ack(Object msgId){ super.ack(msgId); }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("number"));
    }
}
