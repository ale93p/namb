package fr.unice.yamb.experiments.cpuload.bolts;

import org.apache.storm.tuple.Tuple;

public class FilterBolt extends BaseNamedBolt {

    @Override
    public String name(){
        return "filter_bolt";
    }

    @Override
    public String runTask(Tuple tuple){
        long value = Long.parseLong(tuple.getString(0));
        if(value >= 90){
            return String.valueOf(value);
        }
        else
        {
            return null;
        }
    }
}
