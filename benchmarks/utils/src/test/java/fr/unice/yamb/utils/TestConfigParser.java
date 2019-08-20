package fr.unice.yamb.utils;

import fr.unice.yamb.utils.configuration.Config;
import fr.unice.yamb.utils.configuration.schema.HeronConfigSchema;
import fr.unice.yamb.utils.configuration.schema.YambConfigSchema;
import fr.unice.yamb.utils.configuration.schema.StormConfigSchema;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestConfigParser {

    @Test
    public void testWorkflowSchemaConfigParser(){
        String defaultConf = "../../conf/defaults/workflow_schema.yml";
        Config conf = new Config(YambConfigSchema.class, defaultConf);
        assertNotNull(conf.getConfigSchema());
    }

    @Test
    public void testPipelineSchemaConfigParser(){
        String defaultConf = "../../conf/defaults/pipeline_schema.yml";
        Config conf = new Config(YambConfigSchema.class, defaultConf);
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
        Config conf = new Config(HeronConfigSchema.class, flinkDefaultConf);
        assertNotNull(conf.getConfigSchema());
    }

}
