package com.dom.weatherapp.client;

import com.dom.weatherapp.model.CacheEntry;
import com.dom.weatherapp.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WeatherApiClientTest {
    private WeatherApiClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new WeatherApiClient();
        // Pulisco la cache prima di ogni test
        Field cacheField = WeatherApiClient.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, CacheEntry> cache =
                (java.util.concurrent.ConcurrentHashMap<String, CacheEntry>) cacheField.get(client);
        cache.clear();
    }

    @Test
    void testFetchWeather_ValidCity() {
        Optional<WeatherData> result = client.fetchWeather("Roma");

        assertTrue(result.isPresent());
        WeatherData weatherData = result.get();
        assertNotNull(weatherData.getCity());
        assertTrue(weatherData.getTemperature() > -100 && weatherData.getTemperature() < 100);
    }

    @Test
    void testFetchWeather_CityWithSpaces() {
        Optional<WeatherData> result = client.fetchWeather("New York");

        assertTrue(result.isPresent());
        WeatherData weatherData = result.get();
        assertNotNull(weatherData.getCity());
        assertTrue(weatherData.getTemperature() > -100 && weatherData.getTemperature() < 100);
    }

    @Test
    void testFetchWeather_NonExistentCity() {
        Optional<WeatherData> result = client.fetchWeather("CittàInesistente123456789");

        assertFalse(result.isPresent());
    }

    @Test
    void testFetchWeather_EmptyCity() {
        Optional<WeatherData> result = client.fetchWeather("");

        assertFalse(result.isPresent());
    }

    @Test
    void testFetchWeather_NullCity() {
        Optional<WeatherData> result = client.fetchWeather(null);

        assertFalse(result.isPresent());
    }

    @Test
    void testFetchWeather_VeryLongCityName() {
        String longCityName = "a".repeat(200);

        Optional<WeatherData> result = client.fetchWeather(longCityName);

        assertFalse(result.isPresent());
    }

    @Test
    void testFetchWeather_CityWithNumbers() {
        Optional<WeatherData> result = client.fetchWeather("City123");

        assertFalse(result.isPresent());
    }

    @Test
    void testFetchWeather_CityWithOnlySpaces() {
        Optional<WeatherData> result = client.fetchWeather("   ");

        assertFalse(result.isPresent());
    }

    @Test
    void testFetchWeather_MultipleValidCities() {
        String[] cities = {"Roma", "Milano", "Napoli", "Torino", "Firenze"};

        for (String city : cities) {
            Optional<WeatherData> result = client.fetchWeather(city);
            assertTrue(result.isPresent());
            WeatherData weatherData = result.get();
            assertNotNull(weatherData.getCity());
            assertTrue(weatherData.getTemperature() > -100 && weatherData.getTemperature() < 100);
        }
    }

    @Test
    void testFetchWeather_CaseInsensitive() {
        Optional<WeatherData> result1 = client.fetchWeather("roma");
        Optional<WeatherData> result2 = client.fetchWeather("ROMA");
        Optional<WeatherData> result3 = client.fetchWeather("Roma");

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());

        assertEquals(result1.get().getCity(), result2.get().getCity());
        assertEquals(result2.get().getCity(), result3.get().getCity());
    }

    @Test
    void testFetchWeather_CacheHit_ReturnsCachedData() {
        // Prima chiamata per popolare la cache
        Optional<WeatherData> firstResult = client.fetchWeather("Roma");
        assertTrue(firstResult.isPresent());

        // Seconda chiamata dovrebbe usare la cache
        Optional<WeatherData> secondResult = client.fetchWeather("Roma");
        assertTrue(secondResult.isPresent());

        // I dati dovrebbero essere identici (stesso oggetto in cache)
        assertEquals(firstResult.get().getCity(), secondResult.get().getCity());
        assertEquals(firstResult.get().getTemperature(), secondResult.get().getTemperature());
        assertEquals(firstResult.get().getTimestamp(), secondResult.get().getTimestamp());
    }

    @Test
    void testFetchWeather_CacheMiss_DifferentCities() {
        // Prima chiamata per Roma
        Optional<WeatherData> romaResult = client.fetchWeather("Roma");
        assertTrue(romaResult.isPresent());

        // Chiamata per Milano (cache miss per città diversa)
        Optional<WeatherData> milanoResult = client.fetchWeather("Milano");
        assertTrue(milanoResult.isPresent());

        // I dati dovrebbero essere diversi (città diverse)
        assertNotEquals(romaResult.get().getCity(), milanoResult.get().getCity());
        // Timestamp possono essere diversi perché sono chiamate API separate
    }

    @Test
    void testFetchWeather_CacheExpired_FetchesNewData() throws Exception {
        // Prima chiamata per popolare la cache
        Optional<WeatherData> firstResult = client.fetchWeather("Roma");
        assertTrue(firstResult.isPresent());
        Instant firstTimestamp = firstResult.get().getTimestamp();

        // Simulo lo scadere della cache manipolando direttamente il CacheEntry
        Field cacheField = WeatherApiClient.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, CacheEntry> cache =
                (java.util.concurrent.ConcurrentHashMap<String, CacheEntry>) cacheField.get(client);

        // Creo un CacheEntry scaduto (31 minuti fa) usando reflection
        Instant expiredTime = Instant.now().minusSeconds(31 * 60);

        // Creo un nuovo CacheEntry con timestamp scaduto usando reflection
        CacheEntry expiredEntry = new CacheEntry(firstResult.get());
        Field timestampField = CacheEntry.class.getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        timestampField.set(expiredEntry, expiredTime);

        // Sostituisco la cache con dati scaduti
        cache.put("roma", expiredEntry);

        // Seconda chiamata dovrebbe rilevare cache scaduta e chiamare API
        Optional<WeatherData> secondResult = client.fetchWeather("Roma");
        assertTrue(secondResult.isPresent());

        // I dati dovrebbero essere aggiornati (timestamp più recente)
        assertTrue(secondResult.get().getTimestamp().isAfter(firstTimestamp));
        assertEquals(firstResult.get().getCity(), secondResult.get().getCity()); // La città rimane uguale
    }

    @Test
    void testFetchWeather_CacheCaseInsensitive() {
        // Prima chiamata con "roma" (lowercase)
        Optional<WeatherData> result1 = client.fetchWeather("roma");
        assertTrue(result1.isPresent());

        // Seconda chiamata con "ROMA" (uppercase) dovrebbe usare cache
        Optional<WeatherData> result2 = client.fetchWeather("ROMA");
        assertTrue(result2.isPresent());

        // Terza chiamata con "Roma" (mixed case) dovrebbe usare cache
        Optional<WeatherData> result3 = client.fetchWeather("Roma");
        assertTrue(result3.isPresent());

        // Tutti dovrebbero restituire gli stessi dati (stesso oggetto in cache)
        assertEquals(result1.get().getCity(), result2.get().getCity());
        assertEquals(result2.get().getCity(), result3.get().getCity());
        assertEquals(result1.get().getTemperature(), result2.get().getTemperature());
        assertEquals(result2.get().getTemperature(), result3.get().getTemperature());
        assertEquals(result1.get().getTimestamp(), result2.get().getTimestamp());
        assertEquals(result2.get().getTimestamp(), result3.get().getTimestamp());
    }

    @Test
    void testFetchWeather_TimestampFunctionality() {
        Optional<WeatherData> result = client.fetchWeather("Roma");

        assertTrue(result.isPresent());
        WeatherData weatherData = result.get();
        assertNotNull(weatherData.getTimestamp());

        // Il timestamp dovrebbe essere recente (entro ultimi 60 secondi)
        Instant now = Instant.now();
        assertTrue(weatherData.getTimestamp().isBefore(now.plusSeconds(60)));
        assertTrue(weatherData.getTimestamp().isAfter(now.minusSeconds(60)));

        // Il timestamp non dovrebbe essere nel futuro
        assertFalse(weatherData.getTimestamp().isAfter(now.plusSeconds(1)));
    }

    @Test
    void testFetchWeather_CacheTimestampConsistency() throws Exception {
        // Prima chiamata
        Optional<WeatherData> firstCall = client.fetchWeather("Roma");
        assertTrue(firstCall.isPresent());
        Instant firstTimestamp = firstCall.get().getTimestamp();

        // Attendi un momento per assicurarsi che i timestamp siano diversi
        Thread.sleep(10);

        // Seconda chiamata dovrebbe usare cache (stesso timestamp)
        Optional<WeatherData> secondCall = client.fetchWeather("Roma");
        assertTrue(secondCall.isPresent());
        assertEquals(firstTimestamp, secondCall.get().getTimestamp());

        // Dopo che la cache scade (simulato), il timestamp dovrebbe cambiare
        Field cacheField = WeatherApiClient.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, CacheEntry> cache =
                (java.util.concurrent.ConcurrentHashMap<String, CacheEntry>) cacheField.get(client);
        cache.clear(); // Pulisco cache per forzare nuova chiamata API

        Thread.sleep(10); // Breve pausa

        Optional<WeatherData> thirdCall = client.fetchWeather("Roma");
        assertTrue(thirdCall.isPresent());
        assertTrue(thirdCall.get().getTimestamp().isAfter(firstTimestamp));
    }
}
