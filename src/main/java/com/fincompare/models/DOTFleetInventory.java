package com.fincompare.models;

import java.time.LocalDate;

/**
 * DOT Fleet Inventory (Schedule B-43)
 * Individual aircraft details from Form 41
 */
public class DOTFleetInventory {
    private String airlineCode;
    private String tailNumber; // Aircraft registration number
    private String aircraftType; // e.g., "Boeing 737-800"
    private String manufacturer;
    private String model;
    private Integer seats; // Total seating capacity
    private Integer yearManufactured;
    private LocalDate dateAcquired;
    private String ownershipType; // Owned, Leased, etc.

    public DOTFleetInventory() {}

    // Calculate aircraft age
    public Integer getAircraftAge() {
        if (yearManufactured != null) {
            return LocalDate.now().getYear() - yearManufactured;
        }
        return null;
    }

    // Getters and Setters
    public String getAirlineCode() { return airlineCode; }
    public void setAirlineCode(String airlineCode) { this.airlineCode = airlineCode; }

    public String getTailNumber() { return tailNumber; }
    public void setTailNumber(String tailNumber) { this.tailNumber = tailNumber; }

    public String getAircraftType() { return aircraftType; }
    public void setAircraftType(String aircraftType) { this.aircraftType = aircraftType; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }

    public Integer getYearManufactured() { return yearManufactured; }
    public void setYearManufactured(Integer yearManufactured) {
        this.yearManufactured = yearManufactured;
    }

    public LocalDate getDateAcquired() { return dateAcquired; }
    public void setDateAcquired(LocalDate dateAcquired) { this.dateAcquired = dateAcquired; }

    public String getOwnershipType() { return ownershipType; }
    public void setOwnershipType(String ownershipType) { this.ownershipType = ownershipType; }
}
