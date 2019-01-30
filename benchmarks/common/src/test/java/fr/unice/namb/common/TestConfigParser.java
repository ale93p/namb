package fr.unice.namb.common;

import org.junit.Test;
import static org.junit.Assert.*;


import java.io.File;

public class TestConfigParser {

    @Test
    public void testParser(){
        File defaultConf = new File("../../conf/default.yml");
        assertNotNull(ConfigParser.parseConfigurationFile(defaultConf));
    }

}
