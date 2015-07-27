package com.rfc.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadProperties {
	
	public String load(String propName){
		Properties prop = new Properties();
    	InputStream input = null;
 
    	try {
 
    		String filename = "config.properties";
    		input = getClass().getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + filename);
    		    return null;
    		}
 
    		//load a properties file from class path, inside static method
    		prop.load(input);

    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally{
        	if(input!=null){
        		try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
    	
    	return prop.getProperty(propName);
    	
	}

}
