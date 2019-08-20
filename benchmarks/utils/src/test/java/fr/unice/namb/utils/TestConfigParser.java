package fr.unice.namb.utils;

import fr.unice.namb.utils.configuration.Config;
import fr.unice.namb.utils.configuration.schema.FlinkConfigSchema;
import fr.unice.namb.utils.configuration.schema.NambConfigSchema;
import fr.unice.namb.utils.configuration.schema.HeronConfigSchema;
import fr.unice.namb.utils.configuration.schema.StormConfigSchema;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestConfigParser {

    @Test
    public void testWorkflowSchemaConfigParser(){
        String defaultConf = "../../conf/defaults/yamb.yml";
        Config conf = new Config(NambConfigSchema.class, defaultConf);
        assertNotNull(conf.getConfigSchema());
    }

    @Test
    public void testPipelineSchemaConfigParser(){
        String defaultConf = "../../conf/defaults/yamb_pipeline.yml";
        Config conf = new Config(NambConfigSchema.class, defaultConf);
        assertNotNull(conf.getConfigSchema());
    }

    @Test
    public void testStormConfigParser(){
        String stormDefaultConf = "../../conf/defaults/storm-benchmark.yml";
        Config conf = new Config(StormConfigSchema.class, stormDefaultConf);
        assertNotNull(conf.getConfigSchema());
    }

    @Test
    public void testHeronConfigParser(){
        String heronDefaultConf = "../../conf/defaults/heron-benchmark.yml";
        Config conf = new Config(HeronConfigSchema.class, heronDefaultConf);
        assertNotNull(conf.getConfigSchema());
    }

    @Test
    public void tesFlinkConfigParser(){
        String flinkDefaultConf = "../../conf/defaults/flink-benchmark.yml";
        Config conf = new Config(FlinkConfigSchema.class, flinkDefaultConf);
        assertNotNull(conf.getConfigSchema());
    }

}
