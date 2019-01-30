package fr.unice.namb.common;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigParser {

    public static ConfigScheme parseConfigurationFile(File confFile){

        ConfigScheme conf = null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try{
            conf = mapper.readValue(confFile, ConfigScheme.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conf;

    }

}
