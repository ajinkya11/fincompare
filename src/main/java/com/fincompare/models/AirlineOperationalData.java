package com.fincompare.models;

import java.math.BigDecimal;

public class AirlineOperationalData {
    private String fiscalYear;

    // Capacity and Traffic
    private BigDecimal availableSeatMiles; // ASM
    private BigDecimal revenuePassengerMiles; // RPM
    private BigDecimal cargoTonMiles;
    private BigDecimal availableTonMiles;

    // Fleet
    private Integer fleetSize;
    private String fleetComposition; // e.g., "150 Boeing 737, 50 Airbus A320"
    private Integer averageFleetAge;

    // Operations
    private Long passengersCarried;
    private BigDecimal averageStageLength; // miles
    private BigDecimal departuresPerformed;
    private BigDecimal blockHours;

    // Load Factors
    private BigDecimal passengerLoadFactor; // %
    private BigDecimal cargoLoadFactor; // %

    // Yield and Unit Metrics (calculated)
    private BigDecimal passengerYield; // revenue per RPM
    private BigDecimal cargoYield;
    private BigDecimal rasm; // Revenue per ASM
    private BigDecimal casm; // Cost per ASM
    private BigDecimal casmEx; // CASM excluding fuel
    private BigDecimal breakEvenLoadFactor; // %

    // Employee Data
    private Integer fullTimeEmployees;
    private BigDecimal averageSalary;

    public AirlineOperationalData() {}

    // Getters and Setters
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public BigDecimal getAvailableSeatMiles() { return availableSeatMiles; }
    public void setAvailableSeatMiles(BigDecimal availableSeatMiles) {
        this.availableSeatMiles = availableSeatMiles;
    }

    public BigDecimal getRevenuePassengerMiles() { return revenuePassengerMiles; }
    public void setRevenuePassengerMiles(BigDecimal revenuePassengerMiles) {
        this.revenuePassengerMiles = revenuePassengerMiles;
    }

    public BigDecimal getCargoTonMiles() { return cargoTonMiles; }
    public void setCargoTonMiles(BigDecimal cargoTonMiles) { this.cargoTonMiles = cargoTonMiles; }

    public BigDecimal getAvailableTonMiles() { return availableTonMiles; }
    public void setAvailableTonMiles(BigDecimal availableTonMiles) {
        this.availableTonMiles = availableTonMiles;
    }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public String getFleetComposition() { return fleetComposition; }
    public void setFleetComposition(String fleetComposition) {
        this.fleetComposition = fleetComposition;
    }

    public Integer getAverageFleetAge() { return averageFleetAge; }
    public void setAverageFleetAge(Integer averageFleetAge) {
        this.averageFleetAge = averageFleetAge;
    }

    public Long getPassengersCarried() { return passengersCarried; }
    public void setPassengersCarried(Long passengersCarried) {
        this.passengersCarried = passengersCarried;
    }

    public BigDecimal getAverageStageLength() { return averageStageLength; }
    public void setAverageStageLength(BigDecimal averageStageLength) {
        this.averageStageLength = averageStageLength;
    }

    public BigDecimal getDeparturesPerformed() { return departuresPerformed; }
    public void setDeparturesPerformed(BigDecimal departuresPerformed) {
        this.departuresPerformed = departuresPerformed;
    }

    public BigDecimal getBlockHours() { return blockHours; }
    public void setBlockHours(BigDecimal blockHours) { this.blockHours = blockHours; }

    public BigDecimal getPassengerLoadFactor() { return passengerLoadFactor; }
    public void setPassengerLoadFactor(BigDecimal passengerLoadFactor) {
        this.passengerLoadFactor = passengerLoadFactor;
    }

    public BigDecimal getCargoLoadFactor() { return cargoLoadFactor; }
    public void setCargoLoadFactor(BigDecimal cargoLoadFactor) {
        this.cargoLoadFactor = cargoLoadFactor;
    }

    public BigDecimal getPassengerYield() { return passengerYield; }
    public void setPassengerYield(BigDecimal passengerYield) {
        this.passengerYield = passengerYield;
    }

    public BigDecimal getCargoYield() { return cargoYield; }
    public void setCargoYield(BigDecimal cargoYield) { this.cargoYield = cargoYield; }

    public BigDecimal getRasm() { return rasm; }
    public void setRasm(BigDecimal rasm) { this.rasm = rasm; }

    public BigDecimal getCasm() { return casm; }
    public void setCasm(BigDecimal casm) { this.casm = casm; }

    public BigDecimal getCasmEx() { return casmEx; }
    public void setCasmEx(BigDecimal casmEx) { this.casmEx = casmEx; }

    public BigDecimal getBreakEvenLoadFactor() { return breakEvenLoadFactor; }
    public void setBreakEvenLoadFactor(BigDecimal breakEvenLoadFactor) {
        this.breakEvenLoadFactor = breakEvenLoadFactor;
    }

    public Integer getFullTimeEmployees() { return fullTimeEmployees; }
    public void setFullTimeEmployees(Integer fullTimeEmployees) {
        this.fullTimeEmployees = fullTimeEmployees;
    }

    public BigDecimal getAverageSalary() { return averageSalary; }
    public void setAverageSalary(BigDecimal averageSalary) { this.averageSalary = averageSalary; }

    // Convenience methods for load factor (maps to passenger load factor)
    public BigDecimal getLoadFactor() { return passengerLoadFactor; }
    public void setLoadFactor(BigDecimal loadFactor) { this.passengerLoadFactor = loadFactor; }
}
