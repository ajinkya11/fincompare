package com.fincompare.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates all financial data for a company across multiple years
 */
public class CompanyFinancialData {
    private CompanyInfo companyInfo;
    private List<YearlyFinancialData> yearlyData;

    public CompanyFinancialData() {
        this.yearlyData = new ArrayList<>();
    }

    public CompanyFinancialData(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
        this.yearlyData = new ArrayList<>();
    }

    public void addYearData(YearlyFinancialData data) {
        this.yearlyData.add(data);
    }

    // Getters and Setters
    public CompanyInfo getCompanyInfo() { return companyInfo; }
    public void setCompanyInfo(CompanyInfo companyInfo) { this.companyInfo = companyInfo; }

    public List<YearlyFinancialData> getYearlyData() { return yearlyData; }
    public void setYearlyData(List<YearlyFinancialData> yearlyData) { this.yearlyData = yearlyData; }

    /**
     * Get the most recent year's financial data
     */
    public YearlyFinancialData getLatestYearData() {
        if (yearlyData.isEmpty()) {
            return null;
        }
        return yearlyData.get(0); // Assuming sorted with most recent first
    }
}
