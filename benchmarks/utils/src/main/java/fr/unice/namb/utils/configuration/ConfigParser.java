package fr.unice.namb.utils.configuration;

import java.io.File;
import java.io.FileNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigParser {

    public static ConfigScheme parseNambConfigurationFile(File confFile){

        ConfigScheme conf = null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            conf = mapper.readValue(confFile, ConfigScheme.class);
        } catch(FileNotFoundException e){
            System.out.println("\nWARNING: File '" + confFile + "' not found. \n" +
                    "\t> It is necessary to create it. \n" +
                    "\t  You may do it by copying 'default.yml' to 'namb.yml' in the configuration folder.");
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
