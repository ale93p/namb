package fr.unice.namb.utils;

import org.junit.Test;
import static org.junit.Assert.*;


import java.io.File;

public class TestConfigParser {

    @Test
    public void testNambConfigParser(){
        File defaultConf = new File("../../conf/default.yml");
        assertNotNull(ConfigParser.parseNambConfigurationFile(defaultConf));
    }

    @Test
    public void testStormConfigParser(){
        File stormDefaultConf = new File("../../conf/storm-benchmark.yml");
        assertNotNull(ConfigParser.parseStormConfigurationFile(stormDefaultConf));
    }

}
