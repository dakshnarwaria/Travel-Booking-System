package com.travel.repository;

import com.travel.model.City;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates: Singleton Pattern, File I/O, Robust Error Handling,
 * Encapsulation, and Collections.
 * Manages the persistent geographical database of Indian cities from CSV.
 */
public class CityRepository {

    private static final String DEFAULT_CSV_PATH = "data/cities.csv";
    private static CityRepository instance;

    private final Map<Integer, City> citiesById = new LinkedHashMap<>();
    private final Map<String, City> citiesByName = new LinkedHashMap<>();

    private CityRepository() {
        loadFromCsv(DEFAULT_CSV_PATH);
    }

    public static synchronized CityRepository getInstance() {
        if (instance == null) {
            instance = new CityRepository();
        }
        return instance;
    }

    public void loadFromCsv(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Warning: City database file not found at " + file.getAbsolutePath());
            return;
        }

        citiesById.clear();
        citiesByName.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // skip header: id,name,state,tier,latitude,longitude,hasAirport,hasStation,touristPlaces
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 9) {
                    System.err.printf("Warning: Malformed CSV row at line %d (expected 9 columns, found %d): %s\n",
                            lineNumber, parts.length, line);
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String state = parts[2].trim();
                    String tier = parts[3].trim();
                    double lat = Double.parseDouble(parts[4].trim());
                    double lon = Double.parseDouble(parts[5].trim());
                    boolean airport = Boolean.parseBoolean(parts[6].trim());
                    boolean station = Boolean.parseBoolean(parts[7].trim());

                    List<String> places = new ArrayList<>();
                    if (!parts[8].trim().isEmpty()) {
                        String[] placeArr = parts[8].trim().split("\\|");
                        for (String p : placeArr) {
                            if (!p.trim().isEmpty()) places.add(p.trim());
                        }
                    }

                    City city = new City(id, name, state, tier, lat, lon, airport, station, places);
                    citiesById.put(id, city);
                    citiesByName.put(name.toLowerCase(), city);
                } catch (NumberFormatException e) {
                    System.err.printf("Warning: Invalid number format at line %d: %s\n", lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading city data from " + filePath + ": " + e.getMessage());
        }
    }

    public List<City> getAllCities() {
        return new ArrayList<>(citiesById.values());
    }

    public City getCityById(int id) {
        return citiesById.get(id);
    }

    public City getCityByName(String name) {
        if (name == null) return null;
        return citiesByName.get(name.trim().toLowerCase());
    }

    public List<City> getCitiesByTier(String tier) {
        List<City> filtered = new ArrayList<>();
        if (tier == null) return filtered;
        for (City city : citiesById.values()) {
            if (city.getTier().equalsIgnoreCase(tier.trim())) {
                filtered.add(city);
            }
        }
        return filtered;
    }

    public List<City> searchByNamePrefix(String prefix) {
        List<City> results = new ArrayList<>();
        if (prefix == null || prefix.trim().isEmpty()) return results;
        String lowerPrefix = prefix.trim().toLowerCase();
        for (City city : citiesById.values()) {
            if (city.getName().toLowerCase().startsWith(lowerPrefix)) {
                results.add(city);
            }
        }
        return results;
    }
}
