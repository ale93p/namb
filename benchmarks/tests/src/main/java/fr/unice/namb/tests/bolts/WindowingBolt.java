package fr.unice.namb.tests.bolts;

import org.apache.storm.tuple.Tuple;

import java.util.ArrayList;


public abstract class WindowingBolt<T> extends BaseNamedBolt {

    public T windowedValue;

    @Override
    public String runTask(Tuple tuple) {

        this.windowedValue = runSubTask(tuple, this.windowedValue);
        return this.windowedValue.toString();

    }

    public abstract T runSubTask(Tuple tuple, T windowedValue);


}


