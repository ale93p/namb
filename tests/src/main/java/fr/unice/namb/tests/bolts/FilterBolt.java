package fr.unice.namb.tests.bolts;

import org.apache.storm.tuple.Tuple;

public class FilterBolt extends BaseNamedBolt {

    @Override
    public String name(){
        return "filter_bolt";
    }

    @Override
    public String runTask(Tuple tuple){
        String data = tuple.getString(0);
        if(data == "hello"){
            return data;
        }
        else
        {
            return "none";
        }
    }
}
