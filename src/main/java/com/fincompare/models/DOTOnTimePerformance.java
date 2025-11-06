package com.fincompare.models;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * DOT On-Time Performance data
 * Flight-level operational quality metrics
 */
public class DOTOnTimePerformance {
    private String airlineCode;
    private YearMonth reportPeriod;

    // Flight Counts
    private Integer totalFlights;
    private Integer onTimeFlights; // Arrived within 15 minutes of schedule
    private Integer delayedFlights;
    private Integer cancelledFlights;
    private Integer divertedFlights;

    // Performance Percentages
    private BigDecimal onTimePercentage;
    private BigDecimal cancellationRate;
    private BigDecimal diversionRate;

    // Delay Categories (minutes)
    private BigDecimal carrierDelayMinutes; // Airline-caused delays
    private BigDecimal weatherDelayMinutes; // Weather delays
    private BigDecimal nasDelayMinutes; // National Aviation System delays
    private BigDecimal securityDelayMinutes; // Security delays
    private BigDecimal lateAircraftDelayMinutes; // Previous flight delays

    // Averages
    private BigDecimal averageArrivalDelay; // Minutes
    private BigDecimal averageDepartureDelay; // Minutes
    private BigDecimal averageTaxiOut; // Minutes
    private BigDecimal averageTaxiIn; // Minutes

    // Counts by delay magnitude
    private Integer flights0to15MinDelay;
    private Integer flights15to30MinDelay;
    private Integer flights30to60MinDelay;
    private Integer flights60to120MinDelay;
    private Integer flights120PlusMinDelay;

    // Baggage Issues
    private Integer mishandledBaggage;
    private BigDecimal mishandledBaggageRate; // Per 1,000 passengers

    // Customer Complaints
    private Integer customerComplaints;
    private BigDecimal complaintRate; // Per 100,000 passengers

    public DOTOnTimePerformance() {}

    public DOTOnTimePerformance(String airlineCode, YearMonth reportPeriod) {
        this.airlineCode = airlineCode;
        this.reportPeriod = reportPeriod;
    }

    // Calculate performance percentages
    public void calculatePerformanceMetrics() {
        if (totalFlights != null && totalFlights > 0) {
            if (onTimeFlights != null) {
                this.onTimePercentage = new BigDecimal(onTimeFlights)
                    .divide(new BigDecimal(totalFlights), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            }
            if (cancelledFlights != null) {
                this.cancellationRate = new BigDecimal(cancelledFlights)
                    .divide(new BigDecimal(totalFlights), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            }
            if (divertedFlights != null) {
                this.diversionRate = new BigDecimal(divertedFlights)
                    .divide(new BigDecimal(totalFlights), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            }
        }
    }

    // Getters and Setters
    public String getAirlineCode() { return airlineCode; }
    public void setAirlineCode(String airlineCode) { this.airlineCode = airlineCode; }

    public YearMonth getReportPeriod() { return reportPeriod; }
    public void setReportPeriod(YearMonth reportPeriod) { this.reportPeriod = reportPeriod; }

    public Integer getTotalFlights() { return totalFlights; }
    public void setTotalFlights(Integer totalFlights) { this.totalFlights = totalFlights; }

    public Integer getOnTimeFlights() { return onTimeFlights; }
    public void setOnTimeFlights(Integer onTimeFlights) { this.onTimeFlights = onTimeFlights; }

    public Integer getDelayedFlights() { return delayedFlights; }
    public void setDelayedFlights(Integer delayedFlights) { this.delayedFlights = delayedFlights; }

    public Integer getCancelledFlights() { return cancelledFlights; }
    public void setCancelledFlights(Integer cancelledFlights) { this.cancelledFlights = cancelledFlights; }

    public Integer getDivertedFlights() { return divertedFlights; }
    public void setDivertedFlights(Integer divertedFlights) { this.divertedFlights = divertedFlights; }

    public BigDecimal getOnTimePercentage() { return onTimePercentage; }
    public void setOnTimePercentage(BigDecimal onTimePercentage) {
        this.onTimePercentage = onTimePercentage;
    }

    public BigDecimal getCancellationRate() { return cancellationRate; }
    public void setCancellationRate(BigDecimal cancellationRate) {
        this.cancellationRate = cancellationRate;
    }

    public BigDecimal getDiversionRate() { return diversionRate; }
    public void setDiversionRate(BigDecimal diversionRate) { this.diversionRate = diversionRate; }

    public BigDecimal getCarrierDelayMinutes() { return carrierDelayMinutes; }
    public void setCarrierDelayMinutes(BigDecimal carrierDelayMinutes) {
        this.carrierDelayMinutes = carrierDelayMinutes;
    }

    public BigDecimal getWeatherDelayMinutes() { return weatherDelayMinutes; }
    public void setWeatherDelayMinutes(BigDecimal weatherDelayMinutes) {
        this.weatherDelayMinutes = weatherDelayMinutes;
    }

    public BigDecimal getNasDelayMinutes() { return nasDelayMinutes; }
    public void setNasDelayMinutes(BigDecimal nasDelayMinutes) { this.nasDelayMinutes = nasDelayMinutes; }

    public BigDecimal getSecurityDelayMinutes() { return securityDelayMinutes; }
    public void setSecurityDelayMinutes(BigDecimal securityDelayMinutes) {
        this.securityDelayMinutes = securityDelayMinutes;
    }

    public BigDecimal getLateAircraftDelayMinutes() { return lateAircraftDelayMinutes; }
    public void setLateAircraftDelayMinutes(BigDecimal lateAircraftDelayMinutes) {
        this.lateAircraftDelayMinutes = lateAircraftDelayMinutes;
    }

    public BigDecimal getAverageArrivalDelay() { return averageArrivalDelay; }
    public void setAverageArrivalDelay(BigDecimal averageArrivalDelay) {
        this.averageArrivalDelay = averageArrivalDelay;
    }

    public BigDecimal getAverageDepartureDelay() { return averageDepartureDelay; }
    public void setAverageDepartureDelay(BigDecimal averageDepartureDelay) {
        this.averageDepartureDelay = averageDepartureDelay;
    }

    public BigDecimal getAverageTaxiOut() { return averageTaxiOut; }
    public void setAverageTaxiOut(BigDecimal averageTaxiOut) { this.averageTaxiOut = averageTaxiOut; }

    public BigDecimal getAverageTaxiIn() { return averageTaxiIn; }
    public void setAverageTaxiIn(BigDecimal averageTaxiIn) { this.averageTaxiIn = averageTaxiIn; }

    public Integer getFlights0to15MinDelay() { return flights0to15MinDelay; }
    public void setFlights0to15MinDelay(Integer flights0to15MinDelay) {
        this.flights0to15MinDelay = flights0to15MinDelay;
    }

    public Integer getFlights15to30MinDelay() { return flights15to30MinDelay; }
    public void setFlights15to30MinDelay(Integer flights15to30MinDelay) {
        this.flights15to30MinDelay = flights15to30MinDelay;
    }

    public Integer getFlights30to60MinDelay() { return flights30to60MinDelay; }
    public void setFlights30to60MinDelay(Integer flights30to60MinDelay) {
        this.flights30to60MinDelay = flights30to60MinDelay;
    }

    public Integer getFlights60to120MinDelay() { return flights60to120MinDelay; }
    public void setFlights60to120MinDelay(Integer flights60to120MinDelay) {
        this.flights60to120MinDelay = flights60to120MinDelay;
    }

    public Integer getFlights120PlusMinDelay() { return flights120PlusMinDelay; }
    public void setFlights120PlusMinDelay(Integer flights120PlusMinDelay) {
        this.flights120PlusMinDelay = flights120PlusMinDelay;
    }

    public Integer getMishandledBaggage() { return mishandledBaggage; }
    public void setMishandledBaggage(Integer mishandledBaggage) {
        this.mishandledBaggage = mishandledBaggage;
    }

    public BigDecimal getMishandledBaggageRate() { return mishandledBaggageRate; }
    public void setMishandledBaggageRate(BigDecimal mishandledBaggageRate) {
        this.mishandledBaggageRate = mishandledBaggageRate;
    }

    public Integer getCustomerComplaints() { return customerComplaints; }
    public void setCustomerComplaints(Integer customerComplaints) {
        this.customerComplaints = customerComplaints;
    }

    public BigDecimal getComplaintRate() { return complaintRate; }
    public void setComplaintRate(BigDecimal complaintRate) { this.complaintRate = complaintRate; }
}
