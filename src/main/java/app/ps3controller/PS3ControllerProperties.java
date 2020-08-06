/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.ps3controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author jaakko
 */
public class PS3ControllerProperties {
    
    private String propertiesFileName = "ps3controller.properties";
    
    private Map<PS3Button, Integer> buttonToRobotId;
    private Properties properties;
    
    public PS3ControllerProperties() throws IOException {
        this.readProperties();
    }
    
    public PS3ControllerProperties(String file) throws IOException {
        this.propertiesFileName = file;
        this.readProperties();
    }
    
    private void readProperties() throws IOException {
        this.buttonToRobotId = new HashMap<>();
        this.properties = new Properties();
        this.properties.load(new FileInputStream(Paths.get(propertiesFileName).toFile()));
        
        Arrays.asList(PS3Button.values()).forEach(button -> {
            this.buttonToRobotId.put(button, Integer.parseInt(properties.getProperty(button.name())));
        });
    }
    
    public int getButtonId(PS3Button button) {
        return this.buttonToRobotId.get(button);
    }
    
    public int getIntegerValue(String key) {
        return Integer.parseInt(this.properties.getProperty(key));
    }
    
    public boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(this.properties.getProperty(key));
    }
}
