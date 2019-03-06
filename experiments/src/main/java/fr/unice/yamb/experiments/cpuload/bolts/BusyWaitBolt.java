package fr.unice.yamb.experiments.cpuload.bolts;

import org.apache.storm.tuple.Tuple;

public class BusyWaitBolt extends BaseNamedBolt {

    private long cycles;
    private String name = "busywait_bolt";

    public BusyWaitBolt(double kCycles){
        this.cycles = Math.round(kCycles * 1000);
    }

    @Override
    public String runTask(Tuple tuple){
        for(long i = 0; i < this.cycles; i++){}
        return tuple.getString(0);
    }

    public void setName(String n){
        this.name = n;
    }

    @Override
    public String name(){
        return name;
    }

}
