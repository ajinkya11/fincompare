package com.fincompare.models;

/**
 * Extended version of YearlyFinancialData that includes DOT/BTS data
 * Used when DOT integration is enabled
 */
public class YearlyFinancialDataExtended extends YearlyFinancialData {

    // DOT/BTS specific data
    private AggregatedDOTData dotData;
    private DOTOnTimePerformance performanceData;

    public YearlyFinancialDataExtended() {
        super();
    }

    public YearlyFinancialDataExtended(String fiscalYear) {
        super(fiscalYear);
    }

    // Getters and Setters
    public AggregatedDOTData getDotData() { return dotData; }
    public void setDotData(AggregatedDOTData dotData) { this.dotData = dotData; }

    public DOTOnTimePerformance getPerformanceData() { return performanceData; }
    public void setPerformanceData(DOTOnTimePerformance performanceData) {
        this.performanceData = performanceData;
    }

    /**
     * Check if this yearly data has DOT/BTS enhancements
     */
    public boolean hasDOTData() {
        return dotData != null && dotData.getAnnualOperationalData() != null;
    }

    /**
     * Check if performance data is available
     */
    public boolean hasPerformanceData() {
        return performanceData != null;
    }
}
