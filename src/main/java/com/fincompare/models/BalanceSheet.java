package com.fincompare.models;

import java.math.BigDecimal;

public class BalanceSheet {
    private String fiscalYear;

    // Assets
    private BigDecimal totalAssets;
    private BigDecimal currentAssets;
    private BigDecimal cashAndEquivalents;
    private BigDecimal shortTermInvestments;
    private BigDecimal accountsReceivable;
    private BigDecimal inventory;
    private BigDecimal otherCurrentAssets;

    private BigDecimal nonCurrentAssets;
    private BigDecimal propertyPlantEquipment;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netPPE;
    private BigDecimal intangibleAssets;
    private BigDecimal goodwill;
    private BigDecimal longTermInvestments;

    // Liabilities
    private BigDecimal totalLiabilities;
    private BigDecimal currentLiabilities;
    private BigDecimal accountsPayable;
    private BigDecimal shortTermDebt;
    private BigDecimal currentPortionLongTermDebt;
    private BigDecimal otherCurrentLiabilities;

    private BigDecimal longTermLiabilities;
    private BigDecimal longTermDebt;
    private BigDecimal deferredTaxLiabilities;
    private BigDecimal pensionLiabilities;
    private BigDecimal operatingLeaseLiabilities;
    private BigDecimal otherLongTermLiabilities;

    // Equity
    private BigDecimal totalEquity;
    private BigDecimal commonStock;
    private BigDecimal retainedEarnings;
    private BigDecimal treasuryStock;
    private BigDecimal additionalPaidInCapital;

    public BalanceSheet() {}

    // Getters and Setters
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public BigDecimal getTotalAssets() { return totalAssets; }
    public void setTotalAssets(BigDecimal totalAssets) { this.totalAssets = totalAssets; }

    public BigDecimal getCurrentAssets() { return currentAssets; }
    public void setCurrentAssets(BigDecimal currentAssets) { this.currentAssets = currentAssets; }

    public BigDecimal getCashAndEquivalents() { return cashAndEquivalents; }
    public void setCashAndEquivalents(BigDecimal cashAndEquivalents) {
        this.cashAndEquivalents = cashAndEquivalents;
    }

    public BigDecimal getShortTermInvestments() { return shortTermInvestments; }
    public void setShortTermInvestments(BigDecimal shortTermInvestments) {
        this.shortTermInvestments = shortTermInvestments;
    }

    public BigDecimal getAccountsReceivable() { return accountsReceivable; }
    public void setAccountsReceivable(BigDecimal accountsReceivable) {
        this.accountsReceivable = accountsReceivable;
    }

    public BigDecimal getInventory() { return inventory; }
    public void setInventory(BigDecimal inventory) { this.inventory = inventory; }

    public BigDecimal getOtherCurrentAssets() { return otherCurrentAssets; }
    public void setOtherCurrentAssets(BigDecimal otherCurrentAssets) {
        this.otherCurrentAssets = otherCurrentAssets;
    }

    public BigDecimal getNonCurrentAssets() { return nonCurrentAssets; }
    public void setNonCurrentAssets(BigDecimal nonCurrentAssets) {
        this.nonCurrentAssets = nonCurrentAssets;
    }

    public BigDecimal getPropertyPlantEquipment() { return propertyPlantEquipment; }
    public void setPropertyPlantEquipment(BigDecimal propertyPlantEquipment) {
        this.propertyPlantEquipment = propertyPlantEquipment;
    }

    public BigDecimal getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) {
        this.accumulatedDepreciation = accumulatedDepreciation;
    }

    public BigDecimal getNetPPE() { return netPPE; }
    public void setNetPPE(BigDecimal netPPE) { this.netPPE = netPPE; }

    public BigDecimal getIntangibleAssets() { return intangibleAssets; }
    public void setIntangibleAssets(BigDecimal intangibleAssets) {
        this.intangibleAssets = intangibleAssets;
    }

    public BigDecimal getGoodwill() { return goodwill; }
    public void setGoodwill(BigDecimal goodwill) { this.goodwill = goodwill; }

    public BigDecimal getLongTermInvestments() { return longTermInvestments; }
    public void setLongTermInvestments(BigDecimal longTermInvestments) {
        this.longTermInvestments = longTermInvestments;
    }

    public BigDecimal getTotalLiabilities() { return totalLiabilities; }
    public void setTotalLiabilities(BigDecimal totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    public BigDecimal getCurrentLiabilities() { return currentLiabilities; }
    public void setCurrentLiabilities(BigDecimal currentLiabilities) {
        this.currentLiabilities = currentLiabilities;
    }

    public BigDecimal getAccountsPayable() { return accountsPayable; }
    public void setAccountsPayable(BigDecimal accountsPayable) {
        this.accountsPayable = accountsPayable;
    }

    public BigDecimal getShortTermDebt() { return shortTermDebt; }
    public void setShortTermDebt(BigDecimal shortTermDebt) { this.shortTermDebt = shortTermDebt; }

    public BigDecimal getCurrentPortionLongTermDebt() { return currentPortionLongTermDebt; }
    public void setCurrentPortionLongTermDebt(BigDecimal currentPortionLongTermDebt) {
        this.currentPortionLongTermDebt = currentPortionLongTermDebt;
    }

    public BigDecimal getOtherCurrentLiabilities() { return otherCurrentLiabilities; }
    public void setOtherCurrentLiabilities(BigDecimal otherCurrentLiabilities) {
        this.otherCurrentLiabilities = otherCurrentLiabilities;
    }

    public BigDecimal getLongTermLiabilities() { return longTermLiabilities; }
    public void setLongTermLiabilities(BigDecimal longTermLiabilities) {
        this.longTermLiabilities = longTermLiabilities;
    }

    public BigDecimal getLongTermDebt() { return longTermDebt; }
    public void setLongTermDebt(BigDecimal longTermDebt) { this.longTermDebt = longTermDebt; }

    public BigDecimal getDeferredTaxLiabilities() { return deferredTaxLiabilities; }
    public void setDeferredTaxLiabilities(BigDecimal deferredTaxLiabilities) {
        this.deferredTaxLiabilities = deferredTaxLiabilities;
    }

    public BigDecimal getPensionLiabilities() { return pensionLiabilities; }
    public void setPensionLiabilities(BigDecimal pensionLiabilities) {
        this.pensionLiabilities = pensionLiabilities;
    }

    public BigDecimal getOperatingLeaseLiabilities() { return operatingLeaseLiabilities; }
    public void setOperatingLeaseLiabilities(BigDecimal operatingLeaseLiabilities) {
        this.operatingLeaseLiabilities = operatingLeaseLiabilities;
    }

    public BigDecimal getOtherLongTermLiabilities() { return otherLongTermLiabilities; }
    public void setOtherLongTermLiabilities(BigDecimal otherLongTermLiabilities) {
        this.otherLongTermLiabilities = otherLongTermLiabilities;
    }

    public BigDecimal getTotalEquity() { return totalEquity; }
    public void setTotalEquity(BigDecimal totalEquity) { this.totalEquity = totalEquity; }

    public BigDecimal getCommonStock() { return commonStock; }
    public void setCommonStock(BigDecimal commonStock) { this.commonStock = commonStock; }

    public BigDecimal getRetainedEarnings() { return retainedEarnings; }
    public void setRetainedEarnings(BigDecimal retainedEarnings) {
        this.retainedEarnings = retainedEarnings;
    }

    public BigDecimal getTreasuryStock() { return treasuryStock; }
    public void setTreasuryStock(BigDecimal treasuryStock) { this.treasuryStock = treasuryStock; }

    public BigDecimal getAdditionalPaidInCapital() { return additionalPaidInCapital; }
    public void setAdditionalPaidInCapital(BigDecimal additionalPaidInCapital) {
        this.additionalPaidInCapital = additionalPaidInCapital;
    }
}
