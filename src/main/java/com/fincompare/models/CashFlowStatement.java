package com.fincompare.models;

import java.math.BigDecimal;

public class CashFlowStatement {
    private String fiscalYear;

    // Operating Activities
    private BigDecimal operatingCashFlow;
    private BigDecimal netIncome;
    private BigDecimal depreciationAmortization;
    private BigDecimal deferredIncomeTax;
    private BigDecimal stockBasedCompensation;
    private BigDecimal changesInWorkingCapital;
    private BigDecimal changesInAccountsReceivable;
    private BigDecimal changesInInventory;
    private BigDecimal changesInAccountsPayable;

    // Investing Activities
    private BigDecimal investingCashFlow;
    private BigDecimal capitalExpenditures;
    private BigDecimal investmentPurchases;
    private BigDecimal investmentSales;
    private BigDecimal acquisitions;
    private BigDecimal assetSales;

    // Financing Activities
    private BigDecimal financingCashFlow;
    private BigDecimal debtIssuance;
    private BigDecimal debtRepayment;
    private BigDecimal equityIssuance;
    private BigDecimal equityRepurchase;
    private BigDecimal dividendsPaid;

    // Summary
    private BigDecimal netChangeInCash;
    private BigDecimal beginningCash;
    private BigDecimal endingCash;
    private BigDecimal freeCashFlow; // Operating CF - CapEx

    public CashFlowStatement() {}

    // Getters and Setters
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public BigDecimal getOperatingCashFlow() { return operatingCashFlow; }
    public void setOperatingCashFlow(BigDecimal operatingCashFlow) {
        this.operatingCashFlow = operatingCashFlow;
    }

    public BigDecimal getNetIncome() { return netIncome; }
    public void setNetIncome(BigDecimal netIncome) { this.netIncome = netIncome; }

    public BigDecimal getDepreciationAmortization() { return depreciationAmortization; }
    public void setDepreciationAmortization(BigDecimal depreciationAmortization) {
        this.depreciationAmortization = depreciationAmortization;
    }

    public BigDecimal getDeferredIncomeTax() { return deferredIncomeTax; }
    public void setDeferredIncomeTax(BigDecimal deferredIncomeTax) {
        this.deferredIncomeTax = deferredIncomeTax;
    }

    public BigDecimal getStockBasedCompensation() { return stockBasedCompensation; }
    public void setStockBasedCompensation(BigDecimal stockBasedCompensation) {
        this.stockBasedCompensation = stockBasedCompensation;
    }

    public BigDecimal getChangesInWorkingCapital() { return changesInWorkingCapital; }
    public void setChangesInWorkingCapital(BigDecimal changesInWorkingCapital) {
        this.changesInWorkingCapital = changesInWorkingCapital;
    }

    public BigDecimal getChangesInAccountsReceivable() { return changesInAccountsReceivable; }
    public void setChangesInAccountsReceivable(BigDecimal changesInAccountsReceivable) {
        this.changesInAccountsReceivable = changesInAccountsReceivable;
    }

    public BigDecimal getChangesInInventory() { return changesInInventory; }
    public void setChangesInInventory(BigDecimal changesInInventory) {
        this.changesInInventory = changesInInventory;
    }

    public BigDecimal getChangesInAccountsPayable() { return changesInAccountsPayable; }
    public void setChangesInAccountsPayable(BigDecimal changesInAccountsPayable) {
        this.changesInAccountsPayable = changesInAccountsPayable;
    }

    public BigDecimal getInvestingCashFlow() { return investingCashFlow; }
    public void setInvestingCashFlow(BigDecimal investingCashFlow) {
        this.investingCashFlow = investingCashFlow;
    }

    public BigDecimal getCapitalExpenditures() { return capitalExpenditures; }
    public void setCapitalExpenditures(BigDecimal capitalExpenditures) {
        this.capitalExpenditures = capitalExpenditures;
    }

    public BigDecimal getInvestmentPurchases() { return investmentPurchases; }
    public void setInvestmentPurchases(BigDecimal investmentPurchases) {
        this.investmentPurchases = investmentPurchases;
    }

    public BigDecimal getInvestmentSales() { return investmentSales; }
    public void setInvestmentSales(BigDecimal investmentSales) {
        this.investmentSales = investmentSales;
    }

    public BigDecimal getAcquisitions() { return acquisitions; }
    public void setAcquisitions(BigDecimal acquisitions) { this.acquisitions = acquisitions; }

    public BigDecimal getAssetSales() { return assetSales; }
    public void setAssetSales(BigDecimal assetSales) { this.assetSales = assetSales; }

    public BigDecimal getFinancingCashFlow() { return financingCashFlow; }
    public void setFinancingCashFlow(BigDecimal financingCashFlow) {
        this.financingCashFlow = financingCashFlow;
    }

    public BigDecimal getDebtIssuance() { return debtIssuance; }
    public void setDebtIssuance(BigDecimal debtIssuance) { this.debtIssuance = debtIssuance; }

    public BigDecimal getDebtRepayment() { return debtRepayment; }
    public void setDebtRepayment(BigDecimal debtRepayment) { this.debtRepayment = debtRepayment; }

    public BigDecimal getEquityIssuance() { return equityIssuance; }
    public void setEquityIssuance(BigDecimal equityIssuance) { this.equityIssuance = equityIssuance; }

    public BigDecimal getEquityRepurchase() { return equityRepurchase; }
    public void setEquityRepurchase(BigDecimal equityRepurchase) {
        this.equityRepurchase = equityRepurchase;
    }

    public BigDecimal getDividendsPaid() { return dividendsPaid; }
    public void setDividendsPaid(BigDecimal dividendsPaid) { this.dividendsPaid = dividendsPaid; }

    public BigDecimal getNetChangeInCash() { return netChangeInCash; }
    public void setNetChangeInCash(BigDecimal netChangeInCash) {
        this.netChangeInCash = netChangeInCash;
    }

    public BigDecimal getBeginningCash() { return beginningCash; }
    public void setBeginningCash(BigDecimal beginningCash) { this.beginningCash = beginningCash; }

    public BigDecimal getEndingCash() { return endingCash; }
    public void setEndingCash(BigDecimal endingCash) { this.endingCash = endingCash; }

    public BigDecimal getFreeCashFlow() { return freeCashFlow; }
    public void setFreeCashFlow(BigDecimal freeCashFlow) { this.freeCashFlow = freeCashFlow; }
}
