package fr.unice.namb.storm.utils;

import fr.unice.namb.storm.bolts.WindowedBusyWaitBolt;
import fr.unice.namb.utils.configuration.Config;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;

public class BuildCommons {

    public static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing, String field){
        switch(routing){
            case hash:
                bolt.partialKeyGrouping(parent, new Fields(field));
                break;
            case balanced:
            case none:
                bolt.shuffleGrouping(parent);
                break;
            case broadcast:
                bolt.allGrouping(parent);
                break;
        }
    }

    public static void setRouting(BoltDeclarer bolt, String parent, Config.TrafficRouting routing){
        setRouting(bolt, parent, routing, "value");
    }

    public static void setWindow(WindowedBusyWaitBolt bolt, Config.WindowingType type, int duration, int interval){
        switch(type){
            case tumbling:
                bolt.withTumblingWindow(BaseWindowedBolt.Duration.seconds(duration));
                break;
            case sliding:
                bolt.withWindow(BaseWindowedBolt.Duration.seconds(duration), BaseWindowedBolt.Duration.seconds(interval));
                break;
        }
    }

    public static void setWindow(WindowedBusyWaitBolt bolt, Config.WindowingType type, int duration){
        setWindow(bolt, type, duration, 0);
    }

}
