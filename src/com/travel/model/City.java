package com.travel.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Demonstrates: Encapsulation, Immutability principles, and Object Class Overrides (equals/hashCode/toString).
 * Represents a geographical destination and its transport connectivity profile.
 */
public class City implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String state;
    private final String tier;
    private final double latitude;
    private final double longitude;
    private final boolean hasAirport;
    private final boolean hasStation;
    private final ArrayList<String> touristPlaces;

    public City(int id, String name, String state, String tier, double latitude, double longitude,
                boolean hasAirport, boolean hasStation, List<String> touristPlaces) {
        this.id = id;
        this.name = (name != null) ? name.trim() : "";
        this.state = (state != null) ? state.trim() : "";
        this.tier = (tier != null) ? tier.trim() : "";
        this.latitude = latitude;
        this.longitude = longitude;
        this.hasAirport = hasAirport;
        this.hasStation = hasStation;
        this.touristPlaces = (touristPlaces != null) ? new ArrayList<>(touristPlaces) : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getTier() {
        return tier;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean hasAirport() {
        return hasAirport;
    }

    public boolean hasStation() {
        return hasStation;
    }

    /** Returns the city's tourist-place list as an unmodifiable view. */
    public List<String> getTouristPlaces() {
        return Collections.unmodifiableList(touristPlaces);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City)) return false;
        City city = (City) o;
        return id == city.id || name.equalsIgnoreCase(city.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("%s, %s (%s)", name, state, tier);
    }
}
