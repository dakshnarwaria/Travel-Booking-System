package com.travel.model;

import com.travel.exception.InvalidInputException;

import java.io.Serializable;
import java.util.Objects;

/**
 * Demonstrates: Encapsulation and Data Validation.
 * Represents an individual passenger traveling on a reserved seat.
 */
public class Passenger implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
    private String gender;
    private String seatNumber;

    public Passenger(String name, int age, String gender) {
        this.name = normalizeName(name);
        this.age = validateAge(age);
        this.gender = normalizeGender(gender);
    }

    public Passenger(String name, int age, String gender, String seatNumber) {
        this(name, age, gender);
        this.seatNumber = seatNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeName(name);
    }

    private static String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Passenger name cannot be blank.");
        }
        return name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = validateAge(age);
    }

    private static int validateAge(int age) {
        if (age < 1 || age > 125) {
            throw new InvalidInputException("Passenger age must be between 1 and 125. Provided: " + age);
        }
        return age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = normalizeGender(gender);
    }

    private static String normalizeGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new InvalidInputException("Passenger gender cannot be blank.");
        }
        return gender.trim();
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Passenger)) return false;
        Passenger that = (Passenger) o;
        return age == that.age &&
                Objects.equals(name, that.name) &&
                Objects.equals(gender, that.gender) &&
                Objects.equals(seatNumber, that.seatNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, gender, seatNumber);
    }

    @Override
    public String toString() {
        return String.format("%s (Age: %d, %s) Seat: %s",
                name, age, gender, (seatNumber != null ? seatNumber : "Unassigned"));
    }
}
