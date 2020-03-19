package fr.unice.namb.utils.configuration;

import java.io.File;
import java.io.FileNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.unice.namb.utils.common.AppBuilder;
import fr.unice.namb.utils.configuration.schema.ConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;

public class Config {

    public enum ConnectionShape{
        linear, diamond, star
    }

    public enum ParaBalancing {
        balanced, increasing, decreasing, pyramid
    }

    public enum TrafficRouting{
        balanced, hash, broadcast, none
    }

    public enum WindowingType{
        tumbling, sliding
    }

    public enum LoadBalancing {
        balanced, increasing, decreasing, pyramid
    }

    public enum DataDistribution{
        uniform, nonuniform
    }

    public enum ArrivalDistribution{
        uniform, burst, sawtooth, revsawtooth, sin
    }

    public enum ComponentType{
        source, task
    }

    public static final int                 WF_FILTERING_PRECISION              = 10000000;

    public static final int                 WF_DEPTH                            = 3;
    public static final int                 WF_SCALABILITY_PARALLELISM          = 10;
    public static final ParaBalancing       WF_SCALABILITY_BALANCING            = ParaBalancing.balanced;
    public static final double              WF_SCALABILITY_VARIABILTY           = 0.5;
    public static final ConnectionShape     WF_CONNECTION_SHAPE                 = ConnectionShape.linear;
    public static final TrafficRouting      WF_TRAFFIC_ROUTING                  = TrafficRouting.balanced;
    public static final boolean             WF_MESSAGE_RELIABILITY              = true;
    public static final boolean             WF_WINDOWING_ENABLED                = false;
    public static final WindowingType       WF_WINDOWING_TYPE                   = WindowingType.tumbling;
    public static final int                 WF_WINDOW_DURATION                  = 30;
    public static final int                 WF_WINDOW_INTERVAL                  = 10;
    public static final double              WF_WORKLOAD_PROCESSING              = 10;
    public static final LoadBalancing       WF_WORKLOAD_BALANCING               = LoadBalancing.balanced;
    public static final double              WF_FILTERING                        = 0;



    public static final int                 DS_SYNTHETIC_DATA_SIZE              = 8;
    public static final int                 DS_DATA_VALUES                      = 100;
    public static final DataDistribution    DS_DATA_DISTRIBUTION                = DataDistribution.uniform;
    public static final ArrivalDistribution DS_SYNTHETIC_ARRIVAL_DISTRIBUTION   = ArrivalDistribution.uniform;
    public static final int                 DS_SYNTHETIC_ARRIVAL_RATE           = 1000;
    public static final long				DS_SYNTETHIC_PHASE_DURATION			= 300;

    public static final String              DS_KAFKA_BOOTSTRAP_SERVER           = null;
    public static final String              DS_KAFKA_GROUP                      = "namb_default";
    public static final String              DS_KAFKA_TOPIC                      = "namb_topic";

    public static final String              DS_ZOOKEEPER_SERVER                 = "localhost:2181";



    ConfigSchema configSchema;
    Class configSchemaClass;

    public Config(Class configSchemaClass, String configFile){
        this.configSchemaClass = configSchemaClass;
        this.configSchema = this.parseConfigurationFile(new File(configFile));
    }

    public Config(){
        this(null, null);
    }

    public ConfigSchema getConfigSchema(){
        return this.configSchema;
    }

    private ConfigSchema parseConfigurationFile(File confFile){

        ConfigSchema conf = null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            conf = (ConfigSchema) mapper.readValue(confFile, this.configSchemaClass);
        } catch(FileNotFoundException e){
            System.out.println("\nWARNING: File '" + confFile + "' not found. \n" +
                    "\t> It may be necessary to create it. \n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conf;
    }

    /* Validate YAMB Configuration file */
    public static void validateConf(NambConfigSchema conf) throws Exception{

        int parallelism = conf.getWorkflow().getScalability().getParallelism();
        int depth = conf.getWorkflow().getDepth();
        ConnectionShape shape = conf.getWorkflow().getConnection().getShape();

        AppBuilder app = new AppBuilder();
        int totalComponents = app.sumArray(app.getTopologyShape(shape, depth));

        // check that total number of executors is larger than total number of components
        // or at least it should have 1 executor per component
        if (! (parallelism >= totalComponents))
            throw new Exception("Configuration: parallelism level (" + parallelism + ") must be larger than total number of components (" + totalComponents + ")");

        int values = conf.getDatastream().getSynthetic().getData().getValues();
        int size = conf.getDatastream().getSynthetic().getData().getSize();
        if (values > Math.pow(26, size))
            throw new Exception("Configuraion: number of distinct values (" + values + ") cannot exceed 26^size (" + Math.pow(26,size) + ")");
    }

}
