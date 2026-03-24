package com.dom.weatherapp.model;

import java.time.Instant;

/**
 * Rappresenta una entry nella cache con dati meteo e timestamp.
 * 
 * Questa classe è fondamentale per il sistema di cache intelligente:
 * - Incapsula dati meteo con metadata temporali
 * - Fornisce logica di scadenza configurabile
 * - Mantiene immutabilità dei dati cached
 * - Supporta calcoli di età per refresh automatico
 * 
 * Il pattern Value Object garantisce che le entry cache
 * siano sempre coerenti e thread-safe.
 */
public class CacheEntry {
    // Dati meteo immutabili memorizzati in cache
    private final WeatherData weatherData;
    // Timestamp esatto di quando l'entry è stata creata/inserita in cache
    private final Instant timestamp;

    /**
     * Crea una nuova entry cache con dati meteo e timestamp automatico.
     * Il timestamp viene generato automaticamente per tracciare l'età
     * dei dati nella cache e determinare quando sono scaduti.
     * 
     * @param weatherData Dati meteo da memorizzare in cache
     */
    public CacheEntry(WeatherData weatherData) {
        this.weatherData = weatherData;
        this.timestamp = Instant.now(); // Timestamp automatico per gestione scadenza
    }

    /**
     * Restituisce i dati meteo memorizzati in questa entry cache.
     * @return Dati meteo immutabili
     */
    public WeatherData getWeatherData() {
        return weatherData;
    }

    /**
     * Restituisce il timestamp di creazione dell'entry cache.
     * @return Timestamp quando l'entry è stata inserita in cache
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Determina se questa entry cache è scaduta basandosi su una soglia temporale.
     * 
     * Logica di scadenza:
     * - Calcola il tempo massimo di validità: timestamp + (minutesThreshold * 60 secondi)
     * - Confronta con il tempo attuale
     * - Restituisce true se il tempo massimo è passato (entry scaduta)
     * 
     * @param minutesThreshold Soglia in minuti dopo cui l'entry è considerata scaduta
     * @return true se l'entry è scaduta, false altrimenti
     */
    public boolean isExpired(long minutesThreshold) {
        // Calcola: (timestamp + soglia) < ora_corrente ?
        return timestamp.plusSeconds(minutesThreshold * 60).isBefore(Instant.now());
    }
}
