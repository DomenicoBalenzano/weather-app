package com.dom.weatherapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SecureLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void logInfo(String message) {
        log("INFO", message);
    }
    
    public static void logError(String message) {
        log("ERROR", message);
    }
    
    public static void logWarning(String message) {
        log("WARNING", message);
    }
    
    public static void logSecurity(String message) {
        log("SECURITY", message);
    }
    
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String sanitizedMessage = sanitizeLogMessage(message);
        System.out.printf("[%s] %s: %s%n", timestamp, level, sanitizedMessage);
    }
    
    private static String sanitizeLogMessage(String message) {
        if (message == null) {
            return "null";
        }
        
        return message.replaceAll("password=[^&]*", "password=***")
                      .replaceAll("token=[^&]*", "token=***")
                      .replaceAll("key=[^&]*", "key=***")
                      .replaceAll("api[_-]?key=[^&]*", "api_key=***");
    }
}
