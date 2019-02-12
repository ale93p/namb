package fr.unice.namb.utils.configuration;

import java.io.File;
import java.io.FileNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.unice.namb.utils.common.AppBuilder;

public class Config {

    ConfigSchema configSchema;
    Class configSchemaClass;

    public Config(Class configSchemaClass, String configFile){
        this.configSchemaClass = configSchemaClass;
        this.configSchema = this.parseConfigurationFile(new File(configFile));
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

    /* Validate NAMB Configuration file */
    public static void validateConf(NambConfigSchema conf) throws Exception{

        int parallelism = conf.getDataflow().getScalability().getParallelism();
        int depth = conf.getDataflow().getDepth();
        ConfigDefaults.ConnectionShape shape = conf.getDataflow().getConnection().getShape();

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
