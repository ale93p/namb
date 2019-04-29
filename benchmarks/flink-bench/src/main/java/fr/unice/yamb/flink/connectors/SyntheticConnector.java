package fr.unice.yamb.flink.connectors;

import fr.unice.yamb.utils.common.DataStream;
import fr.unice.yamb.utils.common.StringGenerator;
import fr.unice.yamb.utils.configuration.Config;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import scala.Int;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class SyntheticConnector extends RichParallelSourceFunction<Tuple4<String, String, Long, Long>> {

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
    private int rate;
    private String me;

    public SyntheticConnector(int dataSize, int dataValues, Config.DataDistribution dataValuesBalancing, Config.ArrivalDistribution flowDistribution, int flowRate, float frequency){
        this.dataSize = dataSize;
        this.dataValues = dataValues;
        this.dataValuesBalancing = dataValuesBalancing;
        this.distribution = flowDistribution;
        this.flowRate = flowRate;
        if(frequency > 0) this.rate = (int)(1 / frequency);
        else this.rate = 0;
    }

    @Override
    public void open(Configuration parameters){

        StringGenerator generator = new StringGenerator(this.dataSize);
        this.payloadArray = generator.generatePayload(this.dataValues, this.dataValuesBalancing);
        this.dataStream = new DataStream();
        if (this.flowRate != 0)
            this.sleepTime = dataStream.convertToInterval(this.flowRate);
        this.count = 0;
        this.index = new Random();
        this.isRunning = true;
        this.me = getRuntimeContext().getTaskName() + "_" + getRuntimeContext().getIndexOfThisSubtask();

    }

    @Override
    public void run(SourceContext<Tuple4<String, String, Long, Long>> sourceContext){
        while(isRunning){
            byte[] nextValue = this.payloadArray.get(this.index.nextInt(this.payloadArray.size()));
            String tuple_id = UUID.randomUUID().toString();
            try {
                if (this.flowRate != 0) {
                    Thread.sleep(
                            this.dataStream.getInterMessageTime(this.distribution, (int) this.sleepTime)
                    );
                }
                this.count++;
                Long ts = System.currentTimeMillis();
                sourceContext.collect(new Tuple4<>(new String(nextValue), tuple_id, this.count, ts));

                if (this.rate > 0 && this.count % this.rate == 0){
                    System.out.println("[DEBUG] [" + this.me + "] : " + tuple_id + "," + this.count + "," + ts + "," + nextValue.toString());
                }
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
