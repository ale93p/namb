package fr.unice.namb.tests.bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

public abstract class BaseNamedBolt extends BaseRichBolt {

    private ThreadMXBean bean;
    private boolean cpuTimeSupported;
    private FileWriter csvWriter;
    private OutputCollector _collector;
    public int tuplesCounter;

    public BaseNamedBolt(){

        this.tuplesCounter = 0;

    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        String outputPath = "tests/logs/" + System.currentTimeMillis() + "_" + this.name() + "_cpu_load_.csv";
        _collector = collector;
        this.bean = ManagementFactory.getThreadMXBean();
        this.cpuTimeSupported = this.bean.isCurrentThreadCpuTimeSupported();
        try {
            this.csvWriter = new FileWriter(new File(outputPath));
            //Initialize header
            this.csvWriter.write("tot,sys,usr\n");
            this.csvWriter.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
        this.otherInitialization();
    }

    public void execute(Tuple tuple){
        String result = this.runTask(tuple);
        //System.out.println(this.name() + " emitted " + result);
        if (result != null) _collector.emit(new Values(result));
        _collector.ack(tuple);

        this.tuplesCounter++;
        this.getCurrentThreadTime();

    }

    public void otherInitialization(){};

    public abstract String name();

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

            try {
                this.csvWriter.append(tot + "," + sys + "," + usr + "\n");
                this.csvWriter.flush();
            } catch(Exception e){
                e.printStackTrace();
            }

            System.out.println("TOT: " + tot);
            System.out.println("SYS: " + sys);
            System.out.println("USR: " + usr);
        }
    }
}
