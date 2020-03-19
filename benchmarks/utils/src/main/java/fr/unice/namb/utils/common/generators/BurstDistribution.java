package fr.unice.namb.utils.common.generators;

import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;

public class BurstDistribution extends DataDistribution {
	
	private double defaultInterval;
	
	private long interBurstInterval; 
    private long burstDuration;
    
    private long startTime;
	
	public BurstDistribution(Flow conf) {
		super(conf);
		this.defaultInterval = 1000./(double)this.rate;
	}

	@Override
	public double getInterMessageTime() {
		if(startTime == 0) startTime = System.currentTimeMillis();
		
		long timeFromStart = System.currentTimeMillis() - startTime;	
		long timeFromLastBurst = timeFromStart % (interBurstInterval + burstDuration);
		
        if(
        	timeFromLastBurst >= interBurstInterval && // elapsed enough time to start a new burst
        	timeFromLastBurst < interBurstInterval + burstDuration // elapsed enough time to stop the burst
        ){
            return 0; //no sleep makes it go as fast as it can
        }
        else{
            return defaultInterval;
        }
	}

}
