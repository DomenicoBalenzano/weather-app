package com.dom.weatherapp.client;

import com.dom.weatherapp.config.ApiConfig;
import com.dom.weatherapp.model.CacheEntry;
import com.dom.weatherapp.model.WeatherData;
import com.dom.weatherapp.util.InputValidator;
import com.dom.weatherapp.util.SecureLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client per le API meteo con sistema di cache integrato e gestione errori robusta.
 * 
 * Questa classe gestisce la comunicazione con le API esterne per ottenere dati meteo,
 * implementando un sistema di cache in-memory per ridurre le chiamate API e
 * migliorare le performance.
 */
public class WeatherApiClient {
    // Client HTTP configurato con timeout per prevenire attacchi DoS
    private final HttpClient client;
    // Parser JSON per elaborare le risposte delle API
    private final ObjectMapper mapper = new ObjectMapper();
    // Cache thread-safe per memorizzare i risultati delle chiamate API
    // Chiave: nome città (sanitizzato), Valore: entry cache con dati e timestamp
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    // Durata massima dei dati in cache prima di considerarsi scaduti (30 minuti)
    private static final long CACHE_EXPIRY_MINUTES = 30;
    // Dimensione massima della cache per prevenire esaurimento memoria (1000 entry)
    private static final int MAX_CACHE_SIZE = 1000;

    /**
     * Costruttore che inizializza l'HTTP client con configurazioni di sicurezza.
     * Il timeout di connessione previene attacchi DoS e garantisce responsività.
     */
    public WeatherApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)) // Timeout connessione 10 secondi
                .build();
    }

    /**
     * Metodo principale per recuperare dati meteo per una città specificata.
     * 
     * Flusso di esecuzione:
     * 1. Validazione e sanitizzazione dell'input
     * 2. Controllo dimensione cache con pulizia automatica se necessario
     * 3. Ricerca in cache (hit) -> restituzione dati cached
     * 4. Cache miss -> chiamata API esterna
     * 5. Salvataggio risultati in cache
     * 6. Gestione centralizzata degli errori
     * 
     * @param city Nome della città per cui recuperare i dati meteo
     * @return Optional<WeatherData> vuoto se errore, altrimenti dati meteo
     */
    public Optional<WeatherData> fetchWeather(String city) {
        try {
            // 1. VALIDAZIONE INPUT: Sanitizza e valida il nome città per sicurezza
            String validatedCity = InputValidator.validateAndSanitizeCity(city);
            SecureLogger.logInfo("Fetching weather data for city: " + validatedCity);

            // 2. GESTIONE CACHE: Controlla dimensione massima e pulisce se necessario
            // Previene esaurimento memoria e attacchi DoS tramite cache overflow
            if (cache.size() >= MAX_CACHE_SIZE) {
                SecureLogger.logSecurity("Cache size limit reached, clearing cache");
                cache.clear();
            }

            // 3. CACHE LOOKUP: Verifica presenza dati validi in cache
            CacheEntry cachedEntry = cache.get(validatedCity);
            if (cachedEntry != null && !cachedEntry.isExpired(CACHE_EXPIRY_MINUTES)) {
                SecureLogger.logInfo("Cache hit for city: " + validatedCity);
                return Optional.of(cachedEntry.getWeatherData());
            }

            // 4. CACHE MISS: Recupera dati dalle API esterne
            
            // 4.1 GEOCODING API: Converte nome città in coordinate lat/lon
            SecureLogger.logInfo("Cache miss, fetching from API for city: " + validatedCity);
            String encodedCity = URLEncoder.encode(validatedCity, StandardCharsets.UTF_8); // URL encoding per sicurezza
            String geoUrl = ApiConfig.getGeoCodingUrl() + "?name=" + encodedCity;

            // Crea richiesta HTTP con timeout per prevenire attacchi DoS
            HttpRequest geoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(geoUrl))
                    .timeout(Duration.ofSeconds(15)) // timeout API 15 secondi
                    .GET()
                    .build();

            // Esegui chiamata sincrona all'API di geocoding
            HttpResponse<String> geoResponse = client.send(geoRequest, HttpResponse.BodyHandlers.ofString());

            // 4.2 GESTIONE ERRORI GEOCODING: Verifica risposta API
            if (geoResponse.statusCode() != 200) {
                SecureLogger.logError("Geocoding API error - Status: " + geoResponse.statusCode());
                System.out.println(" Errore: Servizio di geolocalizzazione non disponibile. Riprovare pi tardi.");
                return Optional.empty();
            }

            // 4.3 PARSING RISPOSTA GEOCODING: Estrae coordinate dal JSON
            JsonNode geoJson = mapper.readTree(geoResponse.body());

            // Verifica presenza risultati nella risposta
            if (!geoJson.has("results") || geoJson.get("results").isEmpty()) {
                System.out.println(" Errore: Citta '" + validatedCity + "' non trovata. Verificare il nome e riprovare.");
                return Optional.empty();
            }

            // Estrae coordinate e nome normalizzato dalla prima risposta
            JsonNode firstResult = geoJson.get("results").get(0);
            double lat = firstResult.get("latitude").asDouble();
            double lon = firstResult.get("longitude").asDouble();
            String name = firstResult.get("name").asText();

            // 4.4 WEATHER API: Recupera dati meteo usando coordinate
            String weatherUrl = String.format(
                    Locale.US, // Usa locale US per formattazione numerica consistente
                    "%s?latitude=%f&longitude=%f&current_weather=true&units=%s",
                    ApiConfig.getWeatherUrl(), lat, lon, ApiConfig.getUnits()
            );

            // Crea richiesta per dati meteo con stesso timeout di sicurezza
            HttpRequest weatherRequest = HttpRequest.newBuilder()
                    .uri(URI.create(weatherUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            // Esegui chiamata sincrona all'API meteo
            HttpResponse<String> weatherResponse = client.send(weatherRequest, HttpResponse.BodyHandlers.ofString());

            // 4.5 GESTIONE ERRORI WEATHER API: Verifica risposta API meteo
            if (weatherResponse.statusCode() != 200) {
                SecureLogger.logError("Weather API error - Status: " + weatherResponse.statusCode());
                System.out.println(" Errore: Servizio meteo non disponibile. Riprovare pi tardi.");
                return Optional.empty();
            }

            // 4.6 PARSING RISPOSTA WEATHER: Estrae dati meteo dal JSON
            JsonNode weatherJson = mapper.readTree(weatherResponse.body());

            // Verifica presenza dati meteo nella risposta
            if (!weatherJson.has("current_weather")) {
                System.out.println("Errore: Dati meteo non disponibili per la citta'" + name + "'.");
                return Optional.empty();
            }

            // Estrae temperatura dai dati meteo attuali
            double temperature = weatherJson.get("current_weather").get("temperature").asDouble();

            // 5. SALVATAGGIO CACHE: Memorizza risultati per future richieste
            WeatherData weatherData = new WeatherData(name, temperature);
            cache.put(validatedCity, new CacheEntry(weatherData)); // Cache con timestamp automatico
            SecureLogger.logInfo("Weather data cached successfully for city: " + validatedCity);

            // 6. RESTITUZIONE RISULTATI: Successo - restituisce dati meteo
            return Optional.of(weatherData);
            
        } catch (IOException | InterruptedException e) {
            // GESTIONE ERRORI DI RETE: Problemi di connessione o timeout
            SecureLogger.logError("Network error: " + e.getMessage());
            System.out.println(" Errore: Problema di connessione. Riprovare pi tardi.");
            return Optional.empty();
        } catch (Exception e) {
            // GESTIONE ERRORI GENERICI: Qualsiasi altro errore imprevisto
            SecureLogger.logError("Unexpected error: " + e.getMessage());
            System.out.println(" Errore: Si è verificato un errore imprevisto. Riprovare pi tardi.");
            return Optional.empty();
        }
    }
}
