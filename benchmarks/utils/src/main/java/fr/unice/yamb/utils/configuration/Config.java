package fr.unice.yamb.utils.configuration;

import java.io.File;
import java.io.FileNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.unice.yamb.utils.common.AppBuilder;
import fr.unice.yamb.utils.configuration.schema.ConfigSchema;
import fr.unice.yamb.utils.configuration.schema.YambConfigSchema;

public class Config {

    public enum ConnectionShape{
        linear, diamond, star
    }

    public enum ParaBalancing {
        balanced, increasing, decreasing, pyramid
    }

    public enum TrafficRouting{
        balanced, hash, broadcast
    }

    public enum LoadBalancing {
        balanced, increasing, decreasing, pyramid
    }

    public enum DataBalancing{
        balanced, unbalanced
    }

    public enum Distribution{
        uniform, burst //TODO: add normal, saw-tooth, bimodal
    }

    public static final int DF_DEPTH = 3;
    public static final int DF_SCALABILITY_PARALLELISM = 10;
    public static final ParaBalancing DF_SCALABILITY_BALANCING = ParaBalancing.balanced;
    public static final ConnectionShape DF_CONNECTION_SHAPE = ConnectionShape.linear;
    public static final TrafficRouting DF_TRAFFIC_ROUTING = TrafficRouting.balanced;
    public static final boolean DF_MESSAGE_RELIABILITY = true;
    public static final int DF_WORKLOAD_PROCESSING = 300;
    public static final LoadBalancing DF_WORKLOAD_BALANCING = LoadBalancing.balanced;
    public static final int DS_SYNTHETIC_DATA_SIZE = 8;
    public static final int DS_DATA_VALUES = 100;
    public static final DataBalancing DS_DATA_BALANCING = DataBalancing.balanced;
    public static final Distribution DS_SYNTHETIC_ARRIVAL_DISTRIBUTION = Distribution.uniform;
    public static final int DS_SYNTHETIC_ARRIVAL_RATE = 1000;


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
    public static void validateConf(YambConfigSchema conf) throws Exception{

        int parallelism = conf.getDataflow().getScalability().getParallelism();
        int depth = conf.getDataflow().getDepth();
        ConnectionShape shape = conf.getDataflow().getConnection().getShape();

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
