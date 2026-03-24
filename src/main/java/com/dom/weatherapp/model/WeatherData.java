package com.dom.weatherapp.model;

import java.time.Instant;

/**
 * Modello immutabile che rappresenta i dati meteo completi.
 * 
 * Questo classe implementa il pattern Immutable Object:
 * - Tutti i campi sono final e privati
 * - Nessun setter dopo la costruzione
 * - Thread-safe per natura
 * - Previene modifiche accidentali dei dati
 * 
 * L'immutabilità è fondamentale per:
 * - Cache consistency: i dati in cache non possono cambiare
 * - Thread safety: accesso concorrente senza synchronization
 * - Predictability: comportamento prevedibile e affidabile
 */
public class WeatherData {
    // Nome della città (normalizzato dall'API)
    private final String city;
    // Temperatura in gradi Celsius (dall'API)
    private final double temperature;
    // Timestamp di creazione (automatico, per cache e debugging)
    private final Instant timestamp;

    /**
     * Costruttore che crea un'istanza immutabile di dati meteo.
     * Il timestamp viene generato automaticamente per tracciare quando
     * i dati sono stati recuperati (utile per cache e debugging).
     * 
     * @param city Nome normalizzato della città
     * @param temperature Temperatura in gradi Celsius
     */
    public WeatherData(String city, double temperature) {
        this.city = city;
        this.temperature = temperature;
        this.timestamp = Instant.now(); // Timestamp automatico per tracciabilità
    }

    /**
     * Restituisce il nome della città.
     * @return Nome della città come fornito dall'API di geocoding
     */
    public String getCity() {
        return city;
    }

    /**
     * Restituisce la temperatura attuale.
     * @return Temperatura in gradi Celsius
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Restituisce il timestamp di creazione dei dati.
     * @return Timestamp quando i dati sono stati recuperati dall'API
     */
    public Instant getTimestamp() {
        return timestamp;
    }
}
