package com.fincompare.models;

/**
 * Contains all financial data for a single fiscal year
 */
public class YearlyFinancialData {
    private String fiscalYear;
    private IncomeStatement incomeStatement;
    private BalanceSheet balanceSheet;
    private CashFlowStatement cashFlowStatement;
    private AirlineOperationalData operationalData;
    private FinancialMetrics metrics;

    public YearlyFinancialData() {}

    public YearlyFinancialData(String fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    // Getters and Setters
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public IncomeStatement getIncomeStatement() { return incomeStatement; }
    public void setIncomeStatement(IncomeStatement incomeStatement) {
        this.incomeStatement = incomeStatement;
    }

    public BalanceSheet getBalanceSheet() { return balanceSheet; }
    public void setBalanceSheet(BalanceSheet balanceSheet) { this.balanceSheet = balanceSheet; }

    public CashFlowStatement getCashFlowStatement() { return cashFlowStatement; }
    public void setCashFlowStatement(CashFlowStatement cashFlowStatement) {
        this.cashFlowStatement = cashFlowStatement;
    }

    public AirlineOperationalData getOperationalData() { return operationalData; }
    public void setOperationalData(AirlineOperationalData operationalData) {
        this.operationalData = operationalData;
    }

    public FinancialMetrics getMetrics() { return metrics; }
    public void setMetrics(FinancialMetrics metrics) { this.metrics = metrics; }
}
