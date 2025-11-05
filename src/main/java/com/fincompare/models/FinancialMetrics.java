package com.fincompare.models;

import java.math.BigDecimal;

public class FinancialMetrics {
    private String fiscalYear;

    // Margin Ratios
    private BigDecimal grossMargin;
    private BigDecimal operatingMargin;
    private BigDecimal netMargin;
    private BigDecimal ebitdaMargin;

    // Profitability Ratios
    private BigDecimal returnOnAssets; // ROA
    private BigDecimal returnOnEquity; // ROE
    private BigDecimal returnOnInvestedCapital; // ROIC

    // Liquidity Ratios
    private BigDecimal currentRatio;
    private BigDecimal quickRatio;
    private BigDecimal cashRatio;
    private BigDecimal workingCapital;

    // Leverage Ratios
    private BigDecimal debtToEquity;
    private BigDecimal debtToAssets;
    private BigDecimal equityMultiplier;
    private BigDecimal interestCoverage;
    private BigDecimal totalDebt;
    private BigDecimal netDebt;

    // Efficiency Ratios
    private BigDecimal assetTurnover;
    private BigDecimal receivablesTurnover;
    private BigDecimal inventoryTurnover;
    private BigDecimal cashConversionCycle;

    // Cash Flow Metrics
    private BigDecimal cashFlowToDebt;
    private BigDecimal operatingCashFlowRatio;
    private BigDecimal freeCashFlowYield;
    private BigDecimal cashConversionRatio;

    // Per Share Metrics
    private BigDecimal bookValuePerShare;
    private BigDecimal freeCashFlowPerShare;

    // Growth Rates (YoY)
    private BigDecimal revenueGrowth;
    private BigDecimal netIncomeGrowth;
    private BigDecimal epsGrowth;
    private BigDecimal operatingCashFlowGrowth;

    // Airline-Specific Financial Health
    private BigDecimal fuelCostPercentage;
    private BigDecimal laborCostPercentage;
    private BigDecimal liquidityRatio;

    public FinancialMetrics() {}

    // Getters and Setters
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public BigDecimal getGrossMargin() { return grossMargin; }
    public void setGrossMargin(BigDecimal grossMargin) { this.grossMargin = grossMargin; }

    public BigDecimal getOperatingMargin() { return operatingMargin; }
    public void setOperatingMargin(BigDecimal operatingMargin) {
        this.operatingMargin = operatingMargin;
    }

    public BigDecimal getNetMargin() { return netMargin; }
    public void setNetMargin(BigDecimal netMargin) { this.netMargin = netMargin; }

    public BigDecimal getEbitdaMargin() { return ebitdaMargin; }
    public void setEbitdaMargin(BigDecimal ebitdaMargin) { this.ebitdaMargin = ebitdaMargin; }

    public BigDecimal getReturnOnAssets() { return returnOnAssets; }
    public void setReturnOnAssets(BigDecimal returnOnAssets) { this.returnOnAssets = returnOnAssets; }

    public BigDecimal getReturnOnEquity() { return returnOnEquity; }
    public void setReturnOnEquity(BigDecimal returnOnEquity) { this.returnOnEquity = returnOnEquity; }

    public BigDecimal getReturnOnInvestedCapital() { return returnOnInvestedCapital; }
    public void setReturnOnInvestedCapital(BigDecimal returnOnInvestedCapital) {
        this.returnOnInvestedCapital = returnOnInvestedCapital;
    }

    public BigDecimal getCurrentRatio() { return currentRatio; }
    public void setCurrentRatio(BigDecimal currentRatio) { this.currentRatio = currentRatio; }

    public BigDecimal getQuickRatio() { return quickRatio; }
    public void setQuickRatio(BigDecimal quickRatio) { this.quickRatio = quickRatio; }

    public BigDecimal getCashRatio() { return cashRatio; }
    public void setCashRatio(BigDecimal cashRatio) { this.cashRatio = cashRatio; }

    public BigDecimal getWorkingCapital() { return workingCapital; }
    public void setWorkingCapital(BigDecimal workingCapital) { this.workingCapital = workingCapital; }

    public BigDecimal getDebtToEquity() { return debtToEquity; }
    public void setDebtToEquity(BigDecimal debtToEquity) { this.debtToEquity = debtToEquity; }

    public BigDecimal getDebtToAssets() { return debtToAssets; }
    public void setDebtToAssets(BigDecimal debtToAssets) { this.debtToAssets = debtToAssets; }

    public BigDecimal getEquityMultiplier() { return equityMultiplier; }
    public void setEquityMultiplier(BigDecimal equityMultiplier) {
        this.equityMultiplier = equityMultiplier;
    }

    public BigDecimal getInterestCoverage() { return interestCoverage; }
    public void setInterestCoverage(BigDecimal interestCoverage) {
        this.interestCoverage = interestCoverage;
    }

    public BigDecimal getTotalDebt() { return totalDebt; }
    public void setTotalDebt(BigDecimal totalDebt) { this.totalDebt = totalDebt; }

    public BigDecimal getNetDebt() { return netDebt; }
    public void setNetDebt(BigDecimal netDebt) { this.netDebt = netDebt; }

    public BigDecimal getAssetTurnover() { return assetTurnover; }
    public void setAssetTurnover(BigDecimal assetTurnover) { this.assetTurnover = assetTurnover; }

    public BigDecimal getReceivablesTurnover() { return receivablesTurnover; }
    public void setReceivablesTurnover(BigDecimal receivablesTurnover) {
        this.receivablesTurnover = receivablesTurnover;
    }

    public BigDecimal getInventoryTurnover() { return inventoryTurnover; }
    public void setInventoryTurnover(BigDecimal inventoryTurnover) {
        this.inventoryTurnover = inventoryTurnover;
    }

    public BigDecimal getCashConversionCycle() { return cashConversionCycle; }
    public void setCashConversionCycle(BigDecimal cashConversionCycle) {
        this.cashConversionCycle = cashConversionCycle;
    }

    public BigDecimal getCashFlowToDebt() { return cashFlowToDebt; }
    public void setCashFlowToDebt(BigDecimal cashFlowToDebt) {
        this.cashFlowToDebt = cashFlowToDebt;
    }

    public BigDecimal getOperatingCashFlowRatio() { return operatingCashFlowRatio; }
    public void setOperatingCashFlowRatio(BigDecimal operatingCashFlowRatio) {
        this.operatingCashFlowRatio = operatingCashFlowRatio;
    }

    public BigDecimal getFreeCashFlowYield() { return freeCashFlowYield; }
    public void setFreeCashFlowYield(BigDecimal freeCashFlowYield) {
        this.freeCashFlowYield = freeCashFlowYield;
    }

    public BigDecimal getCashConversionRatio() { return cashConversionRatio; }
    public void setCashConversionRatio(BigDecimal cashConversionRatio) {
        this.cashConversionRatio = cashConversionRatio;
    }

    public BigDecimal getBookValuePerShare() { return bookValuePerShare; }
    public void setBookValuePerShare(BigDecimal bookValuePerShare) {
        this.bookValuePerShare = bookValuePerShare;
    }

    public BigDecimal getFreeCashFlowPerShare() { return freeCashFlowPerShare; }
    public void setFreeCashFlowPerShare(BigDecimal freeCashFlowPerShare) {
        this.freeCashFlowPerShare = freeCashFlowPerShare;
    }

    public BigDecimal getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(BigDecimal revenueGrowth) { this.revenueGrowth = revenueGrowth; }

    public BigDecimal getNetIncomeGrowth() { return netIncomeGrowth; }
    public void setNetIncomeGrowth(BigDecimal netIncomeGrowth) {
        this.netIncomeGrowth = netIncomeGrowth;
    }

    public BigDecimal getEpsGrowth() { return epsGrowth; }
    public void setEpsGrowth(BigDecimal epsGrowth) { this.epsGrowth = epsGrowth; }

    public BigDecimal getOperatingCashFlowGrowth() { return operatingCashFlowGrowth; }
    public void setOperatingCashFlowGrowth(BigDecimal operatingCashFlowGrowth) {
        this.operatingCashFlowGrowth = operatingCashFlowGrowth;
    }

    public BigDecimal getFuelCostPercentage() { return fuelCostPercentage; }
    public void setFuelCostPercentage(BigDecimal fuelCostPercentage) {
        this.fuelCostPercentage = fuelCostPercentage;
    }

    public BigDecimal getLaborCostPercentage() { return laborCostPercentage; }
    public void setLaborCostPercentage(BigDecimal laborCostPercentage) {
        this.laborCostPercentage = laborCostPercentage;
    }

    public BigDecimal getLiquidityRatio() { return liquidityRatio; }
    public void setLiquidityRatio(BigDecimal liquidityRatio) {
        this.liquidityRatio = liquidityRatio;
    }
}
