package fr.unice.namb.utils.common.generators;

import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;

public class CbrDistribution extends StreamDistribution {

	public CbrDistribution(Flow flowConf) {
		super(flowConf);
	}

	@Override
	public double getInterMessageTime() {
		if(this.rate > 0) return 1000/this.rate;
		else return 0;
	}

}
