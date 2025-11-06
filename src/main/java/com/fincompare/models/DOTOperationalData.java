package com.fincompare.models;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * DOT Bureau of Transportation Statistics operational data (Form 41 T-100)
 * Monthly operational statistics for US airlines
 */
public class DOTOperationalData {
    private String airlineCode; // DOT carrier code
    private YearMonth reportPeriod;

    // Traffic Data
    private Long revenuePassengers;
    private BigDecimal revenuePassengerMiles; // RPM
    private BigDecimal availableSeatMiles; // ASM
    private BigDecimal passengerLoadFactor; // calculated: RPM/ASM * 100

    // Cargo Data
    private BigDecimal freightTonMiles;
    private BigDecimal mailTonMiles;
    private BigDecimal cargoTonMiles; // Freight + Mail

    // Operations
    private Integer departuresPerformed;
    private Integer departuresScheduled;
    private BigDecimal aircraftHours; // Block hours
    private BigDecimal aircraftDepartures;
    private BigDecimal availableTonMiles;

    // Revenue (reported to DOT)
    private BigDecimal passengerRevenue;
    private BigDecimal freightRevenue;
    private BigDecimal mailRevenue;

    // Service Class breakdown
    private Long domesticPassengers;
    private Long internationalPassengers;
    private BigDecimal domesticASM;
    private BigDecimal internationalASM;

    public DOTOperationalData() {}

    public DOTOperationalData(String airlineCode, YearMonth reportPeriod) {
        this.airlineCode = airlineCode;
        this.reportPeriod = reportPeriod;
    }

    // Calculate load factor if not provided
    public void calculateLoadFactor() {
        if (revenuePassengerMiles != null && availableSeatMiles != null
            && availableSeatMiles.compareTo(BigDecimal.ZERO) > 0) {
            this.passengerLoadFactor = revenuePassengerMiles
                .divide(availableSeatMiles, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        }
    }

    // Calculate cargo ton miles if not provided
    public void calculateCargoTonMiles() {
        if (freightTonMiles != null && mailTonMiles != null) {
            this.cargoTonMiles = freightTonMiles.add(mailTonMiles);
        } else if (freightTonMiles != null) {
            this.cargoTonMiles = freightTonMiles;
        }
    }

    // Getters and Setters
    public String getAirlineCode() { return airlineCode; }
    public void setAirlineCode(String airlineCode) { this.airlineCode = airlineCode; }

    public YearMonth getReportPeriod() { return reportPeriod; }
    public void setReportPeriod(YearMonth reportPeriod) { this.reportPeriod = reportPeriod; }

    public Long getRevenuePassengers() { return revenuePassengers; }
    public void setRevenuePassengers(Long revenuePassengers) { this.revenuePassengers = revenuePassengers; }

    public BigDecimal getRevenuePassengerMiles() { return revenuePassengerMiles; }
    public void setRevenuePassengerMiles(BigDecimal revenuePassengerMiles) {
        this.revenuePassengerMiles = revenuePassengerMiles;
    }

    public BigDecimal getAvailableSeatMiles() { return availableSeatMiles; }
    public void setAvailableSeatMiles(BigDecimal availableSeatMiles) {
        this.availableSeatMiles = availableSeatMiles;
    }

    public BigDecimal getPassengerLoadFactor() { return passengerLoadFactor; }
    public void setPassengerLoadFactor(BigDecimal passengerLoadFactor) {
        this.passengerLoadFactor = passengerLoadFactor;
    }

    public BigDecimal getFreightTonMiles() { return freightTonMiles; }
    public void setFreightTonMiles(BigDecimal freightTonMiles) { this.freightTonMiles = freightTonMiles; }

    public BigDecimal getMailTonMiles() { return mailTonMiles; }
    public void setMailTonMiles(BigDecimal mailTonMiles) { this.mailTonMiles = mailTonMiles; }

    public BigDecimal getCargoTonMiles() { return cargoTonMiles; }
    public void setCargoTonMiles(BigDecimal cargoTonMiles) { this.cargoTonMiles = cargoTonMiles; }

    public Integer getDeparturesPerformed() { return departuresPerformed; }
    public void setDeparturesPerformed(Integer departuresPerformed) {
        this.departuresPerformed = departuresPerformed;
    }

    public Integer getDeparturesScheduled() { return departuresScheduled; }
    public void setDeparturesScheduled(Integer departuresScheduled) {
        this.departuresScheduled = departuresScheduled;
    }

    public BigDecimal getAircraftHours() { return aircraftHours; }
    public void setAircraftHours(BigDecimal aircraftHours) { this.aircraftHours = aircraftHours; }

    public BigDecimal getAircraftDepartures() { return aircraftDepartures; }
    public void setAircraftDepartures(BigDecimal aircraftDepartures) {
        this.aircraftDepartures = aircraftDepartures;
    }

    public BigDecimal getAvailableTonMiles() { return availableTonMiles; }
    public void setAvailableTonMiles(BigDecimal availableTonMiles) {
        this.availableTonMiles = availableTonMiles;
    }

    public BigDecimal getPassengerRevenue() { return passengerRevenue; }
    public void setPassengerRevenue(BigDecimal passengerRevenue) {
        this.passengerRevenue = passengerRevenue;
    }

    public BigDecimal getFreightRevenue() { return freightRevenue; }
    public void setFreightRevenue(BigDecimal freightRevenue) { this.freightRevenue = freightRevenue; }

    public BigDecimal getMailRevenue() { return mailRevenue; }
    public void setMailRevenue(BigDecimal mailRevenue) { this.mailRevenue = mailRevenue; }

    public Long getDomesticPassengers() { return domesticPassengers; }
    public void setDomesticPassengers(Long domesticPassengers) {
        this.domesticPassengers = domesticPassengers;
    }

    public Long getInternationalPassengers() { return internationalPassengers; }
    public void setInternationalPassengers(Long internationalPassengers) {
        this.internationalPassengers = internationalPassengers;
    }

    public BigDecimal getDomesticASM() { return domesticASM; }
    public void setDomesticASM(BigDecimal domesticASM) { this.domesticASM = domesticASM; }

    public BigDecimal getInternationalASM() { return internationalASM; }
    public void setInternationalASM(BigDecimal internationalASM) {
        this.internationalASM = internationalASM;
    }
}
