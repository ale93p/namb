package fr.unice.namb.utils.common.generators;

import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;

public class SawtoothDistribution extends DataDistribution {
	
	private long phaseDuration; 
	private boolean isReverse;
	
	public SawtoothDistribution(Flow conf, boolean isReverse) {
		super(conf);
		this.phaseDuration = conf.getPhase();
		this.isReverse = isReverse;
	}
	
	public SawtoothDistribution(Flow conf) {
		this(conf, false);
	}

	@Override
	public double getInterMessageTime() {
		double k = - rate / Math.PI;
		if (isReverse) k *= -1;
		double t = System.currentTimeMillis();
		double omega = 1. / Math.tan( (t * Math.PI) / (phaseDuration*1000) ); //convert phase from seconds to ms
		double s = k * Math.atan( omega ) + rate/2;
		if ((int) s == 0) s = rate;
		return 1000 / s;
	}

}
