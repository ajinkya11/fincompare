package com.fincompare.models;

import java.time.LocalDate;

public class CompanyInfo {
    private String tickerSymbol;
    private String companyName;
    private String cik;
    private String fiscalYearEnd;
    private LocalDate filingDate;
    private String formType;

    public CompanyInfo() {}

    public CompanyInfo(String tickerSymbol, String companyName, String cik) {
        this.tickerSymbol = tickerSymbol;
        this.companyName = companyName;
        this.cik = cik;
    }

    // Getters and Setters
    public String getTickerSymbol() { return tickerSymbol; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCik() { return cik; }
    public void setCik(String cik) { this.cik = cik; }

    public String getFiscalYearEnd() { return fiscalYearEnd; }
    public void setFiscalYearEnd(String fiscalYearEnd) { this.fiscalYearEnd = fiscalYearEnd; }

    public LocalDate getFilingDate() { return filingDate; }
    public void setFilingDate(LocalDate filingDate) { this.filingDate = filingDate; }

    public String getFormType() { return formType; }
    public void setFormType(String formType) { this.formType = formType; }
}
