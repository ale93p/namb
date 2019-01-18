package fr.unice.namb.tests.spouts;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;

public class TestingSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;
    private long count;
    String[] words;

    public TestingSpout(){
        this.words = new String[]{
                "hello",
                "world"
        };
        this.count = 0;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){ this._collector = collector; }

    @Override
    public void activate(){}

    public void nextTuple(){
        Utils.sleep(1000);
        _collector.emit(new Values("sample"), count++);
        System.out.println("emitted");
    }

    @Override
    public void ack(Object msgId){ super.ack(msgId); }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }

}
