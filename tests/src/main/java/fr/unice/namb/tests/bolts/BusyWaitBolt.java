package fr.unice.namb.tests.bolts;

import org.apache.storm.tuple.Tuple;

public class BusyWaitBolt extends BaseNamedBolt {

    private long cycles;

    public BusyWaitBolt(long kCycles){
        this.cycles = kCycles * 1000;
    }

    @Override
    public String runTask(Tuple tuple){
        for(long i = 0; i < this.cycles; i++){}
        return tuple.getString(0);
    }

    @Override
    public String name(){
        return "busywait_bolt";
    }

}
