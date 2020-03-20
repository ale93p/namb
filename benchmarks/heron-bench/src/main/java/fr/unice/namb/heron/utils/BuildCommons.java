package fr.unice.namb.heron.utils;

import java.time.Duration;

import com.twitter.heron.api.topology.BoltDeclarer;
import com.twitter.heron.api.tuple.Fields;

import fr.unice.namb.heron.bolts.WindowedBusyWaitBolt;
import fr.unice.namb.utils.configuration.Config;

public class BuildCommons {
	
	public static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing, String field){
        switch(routing){
            case hash:
                bolt.fieldsGrouping(parent, new Fields(field));
                break;
            case balanced:
                bolt.shuffleGrouping(parent);
            case broadcast:
                bolt.allGrouping(parent);
        }
    }

	public static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing){
        setRouting(bolt, parent, routing, "value");
    }

	public static void setWindow(WindowedBusyWaitBolt bolt, Config.WindowingType type, int duration, int interval){
        switch(type){
            case tumbling:
                bolt.withTumblingWindow(Duration.ofSeconds(duration));
                break;
            case sliding:
                bolt.withWindow(Duration.ofSeconds(duration), Duration.ofSeconds(interval));
                break;
        }
    }

	public static void setWindow(WindowedBusyWaitBolt bolt, Config.WindowingType type, int duration){
        setWindow(bolt, type, duration, 0);
    }
	
}
