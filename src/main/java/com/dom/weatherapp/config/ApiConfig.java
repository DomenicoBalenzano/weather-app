package com.dom.weatherapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static Properties properties;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    public static String getGeoCodingUrl() {
        return "https://geocoding-api.open-meteo.com/v1/search";
    }
    
    public static String getWeatherUrl() {
        return "https://api.open-meteo.com/v1/forecast";
    }
    
    public static String getUnits() {
        return properties.getProperty("api.units", "metric");
    }
}
