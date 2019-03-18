package fr.unice.yamb.flink.connectors;

import fr.unice.yamb.utils.common.DataStream;
import fr.unice.yamb.utils.common.StringGenerator;
import fr.unice.yamb.utils.configuration.Config;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.util.ArrayList;
import java.util.Random;

public class SyntheticConnector extends RichParallelSourceFunction<Tuple1<String>> {

    private volatile boolean isRunning;

    private int dataSize;
    private int dataValues;
    private Config.DataDistribution dataValuesBalancing;
    private int flowRate;
    private long sleepTime;
    private Config.ArrivalDistribution distribution;
    private DataStream dataStream;

    private ArrayList<byte[]> payloadArray;
    private Random index;
    private long count;

    public SyntheticConnector(int dataSize, int dataValues, Config.DataDistribution dataValuesBalancing, Config.ArrivalDistribution flowDistribution, int flowRate){
        this.dataSize = dataSize;
        this.dataValues = dataValues;
        this.dataValuesBalancing = dataValuesBalancing;
        this.distribution = flowDistribution;
        this.flowRate = flowRate;
    }

    @Override
    public void open(Configuration parameters){

        StringGenerator generator = new StringGenerator(this.dataSize);
        this.payloadArray = generator.generatePayload(this.dataValues, this.dataValuesBalancing);
        this.dataStream = new DataStream();
        this.sleepTime = dataStream.convertToInterval(this.flowRate);
        this.count = 0;
        this.index = new Random();
        this.isRunning = true;


    }

    @Override
    public void run(SourceContext<Tuple1<String>> sourceContext){
        while(isRunning){
            byte[] nextValue = this.payloadArray.get(this.index.nextInt(this.payloadArray.size()));
            try {
                Thread.sleep(
                        dataStream.getInterMessageTime(this.distribution, (int) this.sleepTime)
                );
                sourceContext.collect(new Tuple1<>(new String(nextValue)));
                this.count++;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancel(){
        this.isRunning = false;
    }

}
