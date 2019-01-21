package fr.unice.namb.tests.bolts;

import org.apache.storm.tuple.Tuple;

import java.util.ArrayList;
import java.util.Collections;

public class RankingBolt extends WindowingBolt<ArrayList<Long>> {

    @Override
    public String name(){ return "ranking_bolt"; }

    @Override
    public void otherInitialization(){
        this.windowedValue = new ArrayList<Long>();
    }

    @Override
    public ArrayList<Long> runSubTask(Tuple tuple, ArrayList<Long> sorted){
        if (this.tuplesCounter % 1000 == 0){ sorted = new ArrayList<Long>(); } // reset the sum each 1000 tuples
        long value = Long.parseLong(tuple.getString(0));
        sorted.add(value);
        Collections.sort(sorted);
        return sorted;
    }


}
