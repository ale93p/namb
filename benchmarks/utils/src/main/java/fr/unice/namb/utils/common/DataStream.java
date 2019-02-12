package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.ConfigDefaults.Distribution;

public class DataStream {

    private int[] interBurstInterval= {300000, 120000, 180000, 60000}; //ms {5m, 2m, 3m, 1m}
    private int burstDuration = 20000; // 20s
    private int nextInterTimeIndex;
    private long msgCount;

    public DataStream(){
        this.nextInterTimeIndex = 0;
        this.msgCount = 0;
    }

    public long getInterMessageTime(Distribution distribution, long defaultTime) throws Exception{
        // elapsed time since last burst end

        this.msgCount++;
        switch(distribution){
            case uniform:
                return defaultTime;
            case burst:
                // reset elapsed time when burst ends
                long elapsedTime = this.msgCount*defaultTime % (interBurstInterval[this.nextInterTimeIndex] + this.burstDuration);
                this.nextInterTimeIndex = (int) ((this.msgCount*defaultTime) / (interBurstInterval[this.nextInterTimeIndex] + this.burstDuration)) % this.interBurstInterval.length;

                if(
                    // if the elapsed tame is greater or equivalent to the next inter burst interval and inside the burst duration interval
                    elapsedTime >= interBurstInterval[this.nextInterTimeIndex] &&
                    elapsedTime < interBurstInterval[this.nextInterTimeIndex] + this.burstDuration
                ){
                    return 0; // go as fast as he can
                }
                else{
                    return defaultTime;
                }
            default:
                throw new Exception("Unknown Distribution Type <" + distribution + ">");

        }
    }

}
