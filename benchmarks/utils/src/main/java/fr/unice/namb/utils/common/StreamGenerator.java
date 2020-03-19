package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.Config.ArrivalDistribution;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;
import fr.unice.namb.utils.common.generators.*;

public class StreamGenerator {
    
    private ArrivalDistribution distribution;
    
    private DataDistribution dataDist;
    
    public StreamGenerator(Flow conf) throws Exception{
        distribution = conf.getDistribution();
        dataDist = getDataDistribution(conf);
    }
    
    private DataDistribution getDataDistribution(Flow conf) throws Exception {
    	switch(distribution){
    	case uniform:
    		return new CbrDistribution(conf);
    	case burst:
    	case sin:
    		return new SinDistribution(conf);
    	case sawtooth:
    		return new SawtoothDistribution(conf);
    	case revsawtooth:
    		return new SawtoothDistribution(conf, true);
    	default:
    		throw new Exception("Unknown Distribution Type <" + distribution + ">");
    		
    	}
    }
    
    public double getSleepTime() {
    	return dataDist.getInterMessageTime();
    }
    
}
