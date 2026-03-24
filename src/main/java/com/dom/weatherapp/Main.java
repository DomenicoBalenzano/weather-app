package com.dom.weatherapp;
import com.dom.weatherapp.controller.WeatherController;

public class Main {
    public static void main(String[] args) {
        WeatherController controller = new WeatherController();
        controller.run();
    }
}
