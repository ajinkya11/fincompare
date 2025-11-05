package com.fincompare.data;

import java.time.LocalDate;

public class SECFilingMetadata {
    private String accessionNumber;
    private String filingDate;
    private String reportDate;
    private String formType;
    private String documentUrl;
    private String xbrlUrl;
    private String fiscalYear;
    private String fiscalPeriod;

    public SECFilingMetadata() {}

    // Getters and Setters
    public String getAccessionNumber() { return accessionNumber; }
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getFilingDate() { return filingDate; }
    public void setFilingDate(String filingDate) { this.filingDate = filingDate; }

    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }

    public String getFormType() { return formType; }
    public void setFormType(String formType) { this.formType = formType; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public String getXbrlUrl() { return xbrlUrl; }
    public void setXbrlUrl(String xbrlUrl) { this.xbrlUrl = xbrlUrl; }

    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public String getFiscalPeriod() { return fiscalPeriod; }
    public void setFiscalPeriod(String fiscalPeriod) { this.fiscalPeriod = fiscalPeriod; }

    @Override
    public String toString() {
        return "SECFilingMetadata{" +
                "accessionNumber='" + accessionNumber + '\'' +
                ", filingDate='" + filingDate + '\'' +
                ", formType='" + formType + '\'' +
                ", fiscalYear='" + fiscalYear + '\'' +
                '}';
    }
}
