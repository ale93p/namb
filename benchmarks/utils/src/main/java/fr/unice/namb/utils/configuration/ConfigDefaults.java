package fr.unice.namb.utils.configuration;

public class ConfigDefaults {

    public enum ConnectionShape{
        linear, diamond, star
    }

    public enum TrafficRouting{
        shuffle, hash, broadcast
    }

    public enum LoadBalancing {
        balanced, increasing, decresing, bell
    }

    public enum DataBalancing{
        balanced, unbalanced
    }

    public enum Distribution{
        uniform, burst //TODO: add normal, saw-tooth, bimodal
    }

    public static final int DF_DEPTH = 3;
    public static final int DF_SCALABILITY_PARALLELISM = 10;
    public static final ConnectionShape DF_CONNECTION_SHAPE = ConnectionShape.linear;
    public static final TrafficRouting DF_TRAFFIC_ROUTING = TrafficRouting.shuffle;
    public static final boolean DF_MESSAGE_RELIABILITY = true;
    public static final int DF_WORKLOAD_PROCESSING = 300;
    public static final LoadBalancing DF_WORKLOAD_BALANCING = LoadBalancing.balanced;
    public static final int DS_SYNTHETIC_DATA_SIZE = 8;
    public static final int DS_DATA_VALUES = 100;
    public static final DataBalancing DS_DATA_BALANCING = DataBalancing.balanced;
    public static final Distribution DS_SYNTHETIC_ARRIVAL_DISTRIBUTION = Distribution.uniform;
    public static final int DS_SYNTHETIC_ARRIVAL_RATE = 1000;


}
