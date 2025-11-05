package com.fincompare.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results of a comparative analysis between two companies
 */
public class ComparativeAnalysis {
    private CompanyFinancialData company1;
    private CompanyFinancialData company2;
    private String analysisDate;

    private List<String> company1Strengths;
    private List<String> company1Weaknesses;
    private List<String> company2Strengths;
    private List<String> company2Weaknesses;

    private List<String> keyHighlights;
    private List<String> redFlags;
    private List<String> recommendations;

    private String executiveSummary;

    public ComparativeAnalysis() {
        this.company1Strengths = new ArrayList<>();
        this.company1Weaknesses = new ArrayList<>();
        this.company2Strengths = new ArrayList<>();
        this.company2Weaknesses = new ArrayList<>();
        this.keyHighlights = new ArrayList<>();
        this.redFlags = new ArrayList<>();
        this.recommendations = new ArrayList<>();
    }

    // Getters and Setters
    public CompanyFinancialData getCompany1() { return company1; }
    public void setCompany1(CompanyFinancialData company1) { this.company1 = company1; }

    public CompanyFinancialData getCompany2() { return company2; }
    public void setCompany2(CompanyFinancialData company2) { this.company2 = company2; }

    public String getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(String analysisDate) { this.analysisDate = analysisDate; }

    public List<String> getCompany1Strengths() { return company1Strengths; }
    public void setCompany1Strengths(List<String> company1Strengths) {
        this.company1Strengths = company1Strengths;
    }

    public List<String> getCompany1Weaknesses() { return company1Weaknesses; }
    public void setCompany1Weaknesses(List<String> company1Weaknesses) {
        this.company1Weaknesses = company1Weaknesses;
    }

    public List<String> getCompany2Strengths() { return company2Strengths; }
    public void setCompany2Strengths(List<String> company2Strengths) {
        this.company2Strengths = company2Strengths;
    }

    public List<String> getCompany2Weaknesses() { return company2Weaknesses; }
    public void setCompany2Weaknesses(List<String> company2Weaknesses) {
        this.company2Weaknesses = company2Weaknesses;
    }

    public List<String> getKeyHighlights() { return keyHighlights; }
    public void setKeyHighlights(List<String> keyHighlights) { this.keyHighlights = keyHighlights; }

    public List<String> getRedFlags() { return redFlags; }
    public void setRedFlags(List<String> redFlags) { this.redFlags = redFlags; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }
}
