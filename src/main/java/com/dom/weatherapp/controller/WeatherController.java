package com.dom.weatherapp.controller;

import com.dom.weatherapp.model.WeatherData;
import com.dom.weatherapp.service.WeatherService;
import com.dom.weatherapp.util.SecureLogger;
import com.dom.weatherapp.view.ConsoleView;

import java.util.Optional;

/**
 * Controller principale dell'applicazione che implementa il pattern MVC.
 * 
 * Responsabilità del Controller:
 * - Gestire il flusso principale dell'applicazione
 * - Coordinare le interazioni tra View e Service
 * - Gestire il ciclo di vita dell'applicazione
 * - Implementare la logica di controllo errori a livello UI
 */
public class WeatherController {
    // View per l'interfaccia utente a riga di comando
    private final ConsoleView view = new ConsoleView();
    // Service layer per la logica di business
    private final WeatherService service = new WeatherService();

    /**
     * Metodo principale che avvia e gestisce il ciclo di vita dell'applicazione.
     * 
     * Flusso di esecuzione:
     * 1. Mostra messaggio di benvenuto
     * 2. Avvia ciclo interattivo principale
     * 3. Per ogni iterazione:
     *    - Richiede input città all'utente
     *    - Delega richiesta al service layer
     *    - Gestisce risposta (successo/errore)
     *    - Mostra risultati appropriati
     *    - Chiede se continuare
     * 4. Gestisce uscita graceful
     */
    public void run() {
        // Logging di avvio per monitoraggio e debug
        SecureLogger.logInfo("Weather application started");
        System.out.println("=== Weather App ===");
        System.out.println("Benvenuto nell'applicazione meteo con cache!");

        // Ciclo principale dell'applicazione
        boolean continueRunning = true;

        while (continueRunning) {
            // 1. INPUT UTENTE: Richiede nome città tramite view
            String city = view.askCity();

            try {
                // 2. ELABORAZIONE: Delega richiesta al service layer
                // Il service gestisce cache, API calls e business logic
                Optional<WeatherData> optionalData = service.getWeatherByCity(city);
                
                if (optionalData.isPresent()) {
                    // 3. SUCCESSO: Mostra dati meteo recuperati
                    WeatherData data = optionalData.get();
                    view.showWeather(data);
                    SecureLogger.logInfo("Weather data retrieved successfully for city: " + city);
                }
                // Nota: il service gestisce già i casi di Optional.empty()
                // quindi non serve else clause qui
                
            } catch (IllegalArgumentException e) {
                // 4. ERRORE INPUT: Gestisce input non validi (es. città vuota)
                SecureLogger.logWarning("Invalid input provided: " + e.getMessage());
                System.out.println("Input non valido: " + e.getMessage());
            } catch (Exception e) {
                // 5. ERRORE SISTEMA: Gestisce errori imprevisti a livello controller
                SecureLogger.logError("Error retrieving weather data: " + e.getMessage());
                System.out.println("Errore imprevisto. Riprova più tardi.");
            }

            // 6. CONTINUITÀ: Chiede all'utente se continuare
            continueRunning = view.askToContinue();
            System.out.println(); // Spazio per leggibilità
        }

        // 7. USCITA: Logging di terminazione e messaggio di arrivederci
        SecureLogger.logInfo("Weather application terminated");
        System.out.println("Grazie per aver usato Weather App. Arrivederci!");
    }
}
