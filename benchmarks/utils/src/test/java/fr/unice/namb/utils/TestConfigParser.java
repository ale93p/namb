package fr.unice.namb.utils;

import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import fr.unice.namb.utils.configuration.schema.StormConfigSchema;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestConfigParser {

    @Test
    public void testNambConfigParser(){
        String defaultConf = "../../conf/default.yml";
        Config conf = new Config(NambConfigSchema.class, defaultConf);
        assertNotNull(conf.getConfigSchema());
    }

    @Test
    public void testStormConfigParser(){
        String stormDefaultConf = "../../conf/storm-benchmark.yml";
        Config conf = new Config(StormConfigSchema.class, stormDefaultConf);
        assertNotNull(conf.getConfigSchema());
    }

}
