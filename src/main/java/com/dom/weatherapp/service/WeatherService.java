package com.dom.weatherapp.service;

import com.dom.weatherapp.client.WeatherApiClient;
import com.dom.weatherapp.model.WeatherData;

import java.util.Optional;

/**
 * Servizio che contiene la logica di business per le operazioni meteo.
 * 
 * Questo classe funge da intermediario tra il controller e il client API,
 * implementando il pattern Service Layer per separare le responsabilità:
 * - Controller: gestisce l'interazione con l'utente
 * - Service: contiene la logica di business
 * - Client: gestisce la comunicazione con le API esterne
 */
public class WeatherService {
    // Client API per la comunicazione con i servizi meteo esterni
    private final WeatherApiClient apiClient = new WeatherApiClient();

    /**
     * Recupera i dati meteo per una città specificata.
     * 
     * Questo metodo implementa il pattern Facade: semplifica l'interazione
     * con il complesso sistema di API e cache sottostante.
     * 
     * @param city Nome della città per cui recuperare i dati meteo
     * @return Optional<WeatherData> vuoto se errore, altrimenti dati meteo completi
     */
    public Optional<WeatherData> getWeatherByCity(String city) {
        // Delega la richiesta al client API che gestisce cache, retry e errori
        return apiClient.fetchWeather(city);
    }
}
