package fr.unice.yamb.experiments.cpuload.bolts;

import org.apache.storm.tuple.Tuple;


public abstract class WindowingBolt<T> extends BaseNamedBolt {

    public T windowedValue;

    @Override
    public String runTask(Tuple tuple) {

        this.windowedValue = runSubTask(tuple, this.windowedValue);
        return this.windowedValue.toString();

    }

    public abstract T runSubTask(Tuple tuple, T windowedValue);


}


