package com.fincompare.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregated DOT/BTS data for a specific time period
 * Combines operational, performance, and fleet data
 */
public class AggregatedDOTData {
    private String airlineCode;
    private String fiscalYear;

    // Monthly operational data (aggregated to annual)
    private List<DOTOperationalData> monthlyOperationalData = new ArrayList<>();
    private DOTOperationalData annualOperationalData;

    // Monthly on-time performance data
    private List<DOTOnTimePerformance> monthlyPerformanceData = new ArrayList<>();
    private DOTOnTimePerformance annualPerformanceData;

    // Fleet inventory (point-in-time)
    private List<DOTFleetInventory> fleetInventory = new ArrayList<>();

    public AggregatedDOTData() {}

    public AggregatedDOTData(String airlineCode, String fiscalYear) {
        this.airlineCode = airlineCode;
        this.fiscalYear = fiscalYear;
    }

    /**
     * Add monthly operational data
     */
    public void addMonthlyOperationalData(DOTOperationalData data) {
        this.monthlyOperationalData.add(data);
    }

    /**
     * Add monthly performance data
     */
    public void addMonthlyPerformanceData(DOTOnTimePerformance data) {
        this.monthlyPerformanceData.add(data);
    }

    /**
     * Add fleet aircraft
     */
    public void addFleetAircraft(DOTFleetInventory aircraft) {
        this.fleetInventory.add(aircraft);
    }

    /**
     * Get fleet size
     */
    public int getFleetSize() {
        return fleetInventory.size();
    }

    /**
     * Get fleet composition summary
     */
    public Map<String, Long> getFleetCompositionMap() {
        return fleetInventory.stream()
            .collect(Collectors.groupingBy(
                DOTFleetInventory::getAircraftType,
                Collectors.counting()
            ));
    }

    /**
     * Get fleet composition as formatted string
     */
    public String getFleetCompositionString() {
        Map<String, Long> composition = getFleetCompositionMap();
        return composition.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // Sort by count descending
            .map(e -> e.getValue() + " " + e.getKey())
            .collect(Collectors.joining(", "));
    }

    /**
     * Calculate average fleet age
     */
    public Double getAverageFleetAge() {
        return fleetInventory.stream()
            .map(DOTFleetInventory::getAircraftAge)
            .filter(age -> age != null)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }

    // Getters and Setters
    public String getAirlineCode() { return airlineCode; }
    public void setAirlineCode(String airlineCode) { this.airlineCode = airlineCode; }

    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public List<DOTOperationalData> getMonthlyOperationalData() { return monthlyOperationalData; }
    public void setMonthlyOperationalData(List<DOTOperationalData> monthlyOperationalData) {
        this.monthlyOperationalData = monthlyOperationalData;
    }

    public DOTOperationalData getAnnualOperationalData() { return annualOperationalData; }
    public void setAnnualOperationalData(DOTOperationalData annualOperationalData) {
        this.annualOperationalData = annualOperationalData;
    }

    public List<DOTOnTimePerformance> getMonthlyPerformanceData() { return monthlyPerformanceData; }
    public void setMonthlyPerformanceData(List<DOTOnTimePerformance> monthlyPerformanceData) {
        this.monthlyPerformanceData = monthlyPerformanceData;
    }

    public DOTOnTimePerformance getAnnualPerformanceData() { return annualPerformanceData; }
    public void setAnnualPerformanceData(DOTOnTimePerformance annualPerformanceData) {
        this.annualPerformanceData = annualPerformanceData;
    }

    public List<DOTFleetInventory> getFleetInventory() { return fleetInventory; }
    public void setFleetInventory(List<DOTFleetInventory> fleetInventory) {
        this.fleetInventory = fleetInventory;
    }
}
