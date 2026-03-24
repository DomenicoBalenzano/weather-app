package com.dom.weatherapp.util;

import java.util.regex.Pattern;

public class InputValidator {
    
    private static final Pattern CITY_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-',.]{1,50}$");
    private static final int MAX_CITY_LENGTH = 50;

    public static String validateAndSanitizeCity(String input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("City name cannot be null");
        }
        
        String trimmed = input.trim();
        
        if (trimmed.isEmpty() || trimmed.length() > MAX_CITY_LENGTH) {
            throw new IllegalArgumentException("City name must be between 1 and 50 characters");
        }
        
        if (!CITY_NAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("City name contains invalid characters");
        }
        
        return trimmed.toLowerCase();
    }
}
