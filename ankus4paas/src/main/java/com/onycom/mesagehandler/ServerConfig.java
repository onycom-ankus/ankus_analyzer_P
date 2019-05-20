package com.onycom.mesagehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
/*
 * Yaml Config loader
 */
public class ServerConfig {
	
	public AnkusSystemConfiguration configuration_loader() {
		AnkusSystemConfiguration systemConfiguration = null;
		try{
			String yamlPath = "";
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			File config = new File(yamlPath);
			systemConfiguration = mapper.readValue(config, AnkusSystemConfiguration.class);
			
		} catch (Exception e) {
			e.printStackTrace();
	//    	errorLogger.equals(e.toString());
	    }
		return systemConfiguration;
	}
}
