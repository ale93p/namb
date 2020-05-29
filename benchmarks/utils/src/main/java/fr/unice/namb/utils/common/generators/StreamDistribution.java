package fr.unice.namb.utils.common.generators;
import java.io.Serializable;

import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.Config.ArrivalDistribution;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;

public abstract class StreamDistribution implements Serializable {

	protected int rate;
	
	public StreamDistribution(Flow conf) {
		rate = conf.getRate();
	}
	
	public abstract double getInterMessageTime();

}
