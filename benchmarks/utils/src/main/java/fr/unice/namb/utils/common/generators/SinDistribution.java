package fr.unice.namb.utils.common.generators;

import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;

public class SinDistribution extends StreamDistribution {

	private long phaseDuration; 
	
	public SinDistribution(Flow conf) {
		super(conf);
		phaseDuration = conf.getPhase();
	}

	@Override
	public double getInterMessageTime() {
		double omega = 2 * Math.PI * (1/(double)(phaseDuration*1000));
		double t = System.currentTimeMillis();
		double s = rate/2 * Math.sin(omega*t) + rate/2;
		return 1000/s;
	}

}
