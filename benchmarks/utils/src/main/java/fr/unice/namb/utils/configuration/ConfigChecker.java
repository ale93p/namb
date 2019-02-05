package fr.unice.namb.utils.configuration;

import static fr.unice.namb.utils.common.GenerationTools.*;

public class ConfigChecker {

    public static void validateConf(ConfigScheme conf) throws Exception{

        int parallelism = conf.getDataflow().getScalability().getParallelism();
        int depth = conf.getDataflow().getDepth();
        ConfigDefaults.ConnectionShape shape = conf.getDataflow().getConnection().getShape();

        int totalComponents = sumArray(getTopologyShape(shape, depth));

        // check that total number of executors is larger than total number of components
        // or at least it should have 1 executor per component
        if (! (parallelism >= totalComponents))
            throw new Exception("Configuration: parallelism level (" + parallelism + ") must be larger than total number of components (" + totalComponents + ")");

    }

}
