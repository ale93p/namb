package fr.unice.namb.tests.bolts;

import org.apache.storm.tuple.Tuple;

public class IdentityBolt extends BaseNamedBolt {

    @Override
    public String runTask(Tuple tuple){
        return tuple.getString(0);
    }

    @Override
    public String name(){
        return "identity_bolt";
    }

}
