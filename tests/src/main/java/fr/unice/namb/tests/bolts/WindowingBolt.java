package fr.unice.namb.tests.bolts;

import org.apache.storm.tuple.Tuple;

import java.util.ArrayList;


public abstract class WindowingBolt extends BaseNamedBolt {

    public Object windowedValue;

    @Override
    public String runTask(Tuple tuple) {

        this.windowedValue = runSubTask(tuple, this.windowedValue);
        return this.windowedValue.toString();

    }

    public abstract Object runSubTask(Tuple tuple, Object windowedValue);


}


