package fr.unice.namb.utils.configuration;

public class ConfigDefaults {

    public enum ConnectionShape{
        linear, diamond, star
    }


    public enum Balancing{
        balanced, increasing, decresing, bell
    }

    public enum DataType{
        numeric, text
    }

    public enum Distribution{
        uniform, burst //TODO: add normal, saw-tooth, bimodal
    }

    public static final int TOTAL_TEST_DURATION = 300;
    public static final int DF_DEPTH = 3;
    public static final int DF_SCALABILITY_PARALLELISM = 10;
    public static final ConnectionShape DF_CONNECTION_SHAPE = ConnectionShape.linear;
    public static final boolean DF_TRAFFIC_BALANCING = true;
    public static final boolean DF_MESSAGE_RELIABILITY = true;
    public static final int DF_WORKLOAD_PROCESSING = 300;
    public static final Balancing DF_WORKLOAD_BALANCING = Balancing.balanced;
    public static final int DS_SYNTHETIC_DATA_SIZE = 8;
    public static final DataType DS_SYNTHETIC_DATA_TYPE = DataType.text;
    public static final Distribution DS_SYNTHETIC_ARRIVAL_DISTRIBUTION = Distribution.uniform;
    public static final int DS_SYNTHETIC_ARRIVAL_RATE = 1000;


}
