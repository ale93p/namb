package fr.unice.namb.tests.bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;

public abstract class BaseNamedBolt extends BaseRichBolt {

    private ThreadMXBean bean;
    private boolean cpuTimeSupported;
    private OutputCollector _collector;
    private int tuplesCounter;

    public BaseNamedBolt(){
        this.bean = ManagementFactory.getThreadMXBean();
        this.tuplesCounter = 0;

    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        _collector = collector;
        this.cpuTimeSupported = this.bean.isCurrentThreadCpuTimeSupported();
    }

    public void execute(Tuple tuple){



        String result = this.runTask(tuple);
        if (result != "none") _collector.emit(new Values(result));
        _collector.ack(tuple);

        this.tuplesCounter++;
        this.getCurrentThreadTime();

    }

    public String name(){ return "testing_bolt"; }

    public void declareOutputFields(OutputFieldsDeclarer declarer){ declarer.declare(new Fields("word"));}

    public abstract String runTask(Tuple tuple);

    private void busyWait(int millis){
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < millis){}
    }

    private double getCurrentThreadCpuTimeMillis(ThreadMXBean bean){
        return ((double)bean.getCurrentThreadCpuTime() / 1000000);
    }

    private void getCurrentThreadTime(){
        long tot, sys, usr;
        if ((this.tuplesCounter == 1 || this.tuplesCounter % 1000 == 0) && this.cpuTimeSupported){
            tot = bean.getCurrentThreadCpuTime();
            usr = bean.getCurrentThreadUserTime();
            sys = tot - usr;
            System.out.println("TOT: " + tot);
            System.out.println("SYS: " + sys);
            System.out.println("USR: " + usr);
        }
    }
}
