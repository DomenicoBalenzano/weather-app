package com.dom.weatherapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherDataTest {

    @Test
    void testWeatherDataCreation_ValidData() {
        WeatherData weatherData = new WeatherData("Roma", 25.5);
        
        assertEquals("Roma", weatherData.getCity());
        assertEquals(25.5, weatherData.getTemperature(), 0.001);
    }

    @Test
    void testWeatherDataCreation_NegativeTemperature() {
        WeatherData weatherData = new WeatherData("Mosca", -10.0);
        
        assertEquals("Mosca", weatherData.getCity());
        assertEquals(-10.0, weatherData.getTemperature(), 0.001);
    }

    @Test
    void testWeatherDataCreation_ZeroTemperature() {
        WeatherData weatherData = new WeatherData("Reykjavik", 0.0);
        
        assertEquals("Reykjavik", weatherData.getCity());
        assertEquals(0.0, weatherData.getTemperature(), 0.001);
    }

    @Test
    void testWeatherDataCreation_EmptyCity() {
        WeatherData weatherData = new WeatherData("", 15.0);
        
        assertEquals("", weatherData.getCity());
        assertEquals(15.0, weatherData.getTemperature(), 0.001);
    }

    @Test
    void testWeatherDataCreation_NullCity() {
        WeatherData weatherData = new WeatherData(null, 20.0);
        
        assertNull(weatherData.getCity());
        assertEquals(20.0, weatherData.getTemperature(), 0.001);
    }

    @Test
    void testWeatherDataCreation_ExtremeTemperature() {
        WeatherData weatherData = new WeatherData("Sahara", 50.0);
        
        assertEquals("Sahara", weatherData.getCity());
        assertEquals(50.0, weatherData.getTemperature(), 0.001);
    }

    @Test
    void testWeatherDataCreation_VeryLowTemperature() {
        WeatherData weatherData = new WeatherData("Antartide", -89.2);
        
        assertEquals("Antartide", weatherData.getCity());
        assertEquals(-89.2, weatherData.getTemperature(), 0.001);
    }

    @Test
    void testWeatherDataCreation_DecimalTemperature() {
        WeatherData weatherData = new WeatherData("Milano", 22.75);
        
        assertEquals("Milano", weatherData.getCity());
        assertEquals(22.75, weatherData.getTemperature(), 0.001);
    }
}
