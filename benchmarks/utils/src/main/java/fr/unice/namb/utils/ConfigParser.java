package fr.unice.namb.utils;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigParser {

    public static ConfigScheme parseNambConfigurationFile(File confFile){

        ConfigScheme conf = null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try{
            conf = mapper.readValue(confFile, ConfigScheme.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conf;
    }

    public static StormConfigScheme parseStormConfigurationFile(File confFile){
        StormConfigScheme conf = null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try{
            conf = mapper.readValue(confFile, StormConfigScheme.class);
        } catch (Exception e){
            e.printStackTrace();
        }
        return conf;
    }

}
