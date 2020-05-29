package fr.unice.namb.utils.common;

import fr.unice.namb.utils.configuration.Config.ArrivalDistribution;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema.Flow;

import java.io.Serializable;

import fr.unice.namb.utils.common.generators.*;

public class StreamGenerator implements Serializable{
    
    private ArrivalDistribution distribution;
    
    private StreamDistribution dataDist;
    
    public StreamGenerator(Flow conf) throws Exception{
        distribution = conf.getDistribution();
        dataDist = getStreamDistribution(conf);
    }
    
    private StreamDistribution getStreamDistribution(Flow conf) throws Exception {
    	switch(distribution){
    	case uniform:
    		return new CbrDistribution(conf);
    	case burst:
    		return new BurstDistribution(conf);
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
