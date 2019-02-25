package fr.unice.namb.flink.connectors;

import fr.unice.namb.utils.common.DataStream;
import fr.unice.namb.utils.common.StringGenerator;
import fr.unice.namb.utils.configuration.Config;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.util.ArrayList;
import java.util.Random;

public class SyntheticConnector extends RichParallelSourceFunction<String> {

    private volatile boolean isRunning;

    private int dataSize;
    private int dataValues;
    private Config.DataBalancing dataValuesBalancing;
    private int flowRate;
    private long sleepTime;
    private Config.Distribution distribution;
    private DataStream dataStream;

    private ArrayList<byte[]> payloadArray;
    private Random index;
    private long count;

    public SyntheticConnector(int dataSize, int dataValues, Config.DataBalancing dataValuesBalancing, Config.Distribution flowDistribution, int flowRate){
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
    public void run(SourceContext<String> sourceContext){
        while(isRunning){
            byte[] nextValue = this.payloadArray.get(this.index.nextInt(this.payloadArray.size()));
            try {
                Thread.sleep(
                        dataStream.getInterMessageTime(this.distribution, (int) this.sleepTime)
                );
                sourceContext.collect(new String(nextValue));
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
