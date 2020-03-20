package fr.unice.namb.flink.connectors;

import fr.unice.namb.utils.common.StreamGenerator;
import fr.unice.namb.utils.common.DataGenerator;
import fr.unice.namb.utils.configuration.Config.DataDistribution;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Data;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Synthetic;
import fr.unice.namb.utils.configuration.Config.ArrivalDistribution;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.util.UUID;

public class SyntheticConnector extends RichParallelSourceFunction<Tuple4<String, String, Long, Long>> {

	private volatile boolean isRunning;
    
    private DataGenerator dataGenerator;
    private StreamGenerator dataStream;

    private long count;
    private int rate;
    private String me;

    public SyntheticConnector(Data data, Flow flow, double frequency, String sourceName) throws Exception{
        this.dataGenerator = new DataGenerator(data);
        this.dataStream = new StreamGenerator(flow);
        
        this.me = sourceName;
        if(frequency > 0) this.rate = (int)(1 / frequency);
        else this.rate = 0;
    }
    
    public SyntheticConnector(Synthetic conf, double frequency, String sourceName) throws Exception{ 
        this(conf.getData(), conf.getFlow(), frequency, sourceName);
    }

    @Override
    public void open(Configuration parameters){
    	
        this.count = 0;
        this.isRunning = true;
        this.me = this.me + "_" + getRuntimeContext().getIndexOfThisSubtask();

    }

    @Override
    public void run(SourceContext<Tuple4<String, String, Long, Long>> sourceContext){
        while(isRunning){
            try {
                String nextValue = new String(dataGenerator.getNextValue());
                
                double sleepTime = this.dataStream.getSleepTime();
                
                if(sleepTime != 0) {
	                Thread.sleep(
                		(long) sleepTime, (int)((sleepTime - (long)sleepTime) * 1000000)
                    );
                }
                
                this.count++;
                String tuple_id = UUID.randomUUID().toString();
                Long ts = System.currentTimeMillis();
                sourceContext.collect(new Tuple4<>(nextValue, tuple_id, this.count, ts));

                ts = System.currentTimeMillis();
                if (this.rate > 0 && this.count % this.rate == 0){
                    System.out.println("[DEBUG] [" + this.me + "] : " + tuple_id + "," + this.count + "," + ts + "," + nextValue);
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
