package fr.unice.namb.utils.configuration;

import fr.unice.namb.utils.common.AppBuilder;

public class ConfigChecker {

    public static void validateConf(ConfigScheme conf) throws Exception{

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