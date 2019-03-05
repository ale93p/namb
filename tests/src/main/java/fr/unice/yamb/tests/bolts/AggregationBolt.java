package fr.unice.yamb.tests.bolts;

import org.apache.storm.tuple.Tuple;

public class AggregationBolt extends WindowingBolt<Long> {

    @Override
    public String name(){ return "aggregation_bolt"; }

    @Override
    public void otherInitialization(){
        this.windowedValue = 0L;
    }

    @Override
    public Long runSubTask(Tuple tuple, Long sum){
        if (this.tuplesCounter % 1000 == 0){ sum = 0L; } // reset the sum each 1000 tuples
        long value = Long.parseLong(tuple.getString(0));
        sum += value;
        return sum;
    }

}
