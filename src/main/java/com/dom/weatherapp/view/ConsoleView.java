package com.dom.weatherapp.view;

import com.dom.weatherapp.model.WeatherData;

import java.util.Scanner;

public class ConsoleView {
    private final Scanner scanner = new Scanner(System.in);

    public String askCity() {
        while (true) {
            System.out.print("Inserisci una citta: ");
            String city = scanner.nextLine().trim();

            if (city.isEmpty()) {
                System.out.println("Errore: Il nome della città non può essere vuoto. Riprova.");
            } else if (city.length() > 100) {
                System.out.println("Errore: Il nome della città è troppo lungo. Massimo 100 caratteri.");
            } else {
                return city;
            }
        }
    }

    public void showWeather(WeatherData data) {
        System.out.println("\nMeteo per " + data.getCity());
        System.out.println("Temperatura: " + data.getTemperature() + "°C");
    }

    public boolean askToContinue() {
        while (true) {
            System.out.print("\nVuoi cercare un'altra città? (s/n): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("s") || response.equals("si") || response.equals("y") || response.equals("yes")) {
                return true;
            } else if (response.equals("n") || response.equals("no")) {
                return false;
            } else {
                System.out.println("Input non valido. Per favore inserisci 's' per continuare o 'n' per terminare.");
            }
        }
    }
}
