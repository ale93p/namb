package fr.unice.namb.utils.common.generators;
import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.Config.ArrivalDistribution;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;

public abstract class DataDistribution {

	protected int rate;
	
	public DataDistribution(Flow conf) {
		rate = conf.getRate();
	}
	
	public abstract double getInterMessageTime();

}
