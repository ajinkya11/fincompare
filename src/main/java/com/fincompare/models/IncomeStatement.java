package com.fincompare.models;

import java.math.BigDecimal;

public class IncomeStatement {
    private String fiscalYear;

    // Revenue
    private BigDecimal totalRevenue;
    private BigDecimal passengerRevenue;
    private BigDecimal cargoRevenue;
    private BigDecimal otherRevenue;

    // Costs and Expenses
    private BigDecimal costOfRevenue;
    private BigDecimal operatingExpenses;
    private BigDecimal fuelCosts;
    private BigDecimal laborCosts;
    private BigDecimal depreciationAmortization;
    private BigDecimal interestExpense;

    // Profitability
    private BigDecimal grossProfit;
    private BigDecimal operatingIncome;
    private BigDecimal ebitda;
    private BigDecimal netIncome;
    private BigDecimal incomeBeforeTax;
    private BigDecimal incomeTaxExpense;

    // Per Share Data
    private BigDecimal basicEPS;
    private BigDecimal dilutedEPS;
    private Long sharesOutstandingBasic;
    private Long sharesOutstandingDiluted;

    public IncomeStatement() {}

    // Getters and Setters
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getPassengerRevenue() { return passengerRevenue; }
    public void setPassengerRevenue(BigDecimal passengerRevenue) { this.passengerRevenue = passengerRevenue; }

    public BigDecimal getCargoRevenue() { return cargoRevenue; }
    public void setCargoRevenue(BigDecimal cargoRevenue) { this.cargoRevenue = cargoRevenue; }

    public BigDecimal getOtherRevenue() { return otherRevenue; }
    public void setOtherRevenue(BigDecimal otherRevenue) { this.otherRevenue = otherRevenue; }

    public BigDecimal getCostOfRevenue() { return costOfRevenue; }
    public void setCostOfRevenue(BigDecimal costOfRevenue) { this.costOfRevenue = costOfRevenue; }

    public BigDecimal getOperatingExpenses() { return operatingExpenses; }
    public void setOperatingExpenses(BigDecimal operatingExpenses) { this.operatingExpenses = operatingExpenses; }

    public BigDecimal getFuelCosts() { return fuelCosts; }
    public void setFuelCosts(BigDecimal fuelCosts) { this.fuelCosts = fuelCosts; }

    public BigDecimal getLaborCosts() { return laborCosts; }
    public void setLaborCosts(BigDecimal laborCosts) { this.laborCosts = laborCosts; }

    public BigDecimal getDepreciationAmortization() { return depreciationAmortization; }
    public void setDepreciationAmortization(BigDecimal depreciationAmortization) {
        this.depreciationAmortization = depreciationAmortization;
    }

    public BigDecimal getInterestExpense() { return interestExpense; }
    public void setInterestExpense(BigDecimal interestExpense) { this.interestExpense = interestExpense; }

    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = grossProfit; }

    public BigDecimal getOperatingIncome() { return operatingIncome; }
    public void setOperatingIncome(BigDecimal operatingIncome) { this.operatingIncome = operatingIncome; }

    public BigDecimal getEbitda() { return ebitda; }
    public void setEbitda(BigDecimal ebitda) { this.ebitda = ebitda; }

    public BigDecimal getNetIncome() { return netIncome; }
    public void setNetIncome(BigDecimal netIncome) { this.netIncome = netIncome; }

    public BigDecimal getIncomeBeforeTax() { return incomeBeforeTax; }
    public void setIncomeBeforeTax(BigDecimal incomeBeforeTax) { this.incomeBeforeTax = incomeBeforeTax; }

    public BigDecimal getIncomeTaxExpense() { return incomeTaxExpense; }
    public void setIncomeTaxExpense(BigDecimal incomeTaxExpense) { this.incomeTaxExpense = incomeTaxExpense; }

    public BigDecimal getBasicEPS() { return basicEPS; }
    public void setBasicEPS(BigDecimal basicEPS) { this.basicEPS = basicEPS; }

    public BigDecimal getDilutedEPS() { return dilutedEPS; }
    public void setDilutedEPS(BigDecimal dilutedEPS) { this.dilutedEPS = dilutedEPS; }

    public Long getSharesOutstandingBasic() { return sharesOutstandingBasic; }
    public void setSharesOutstandingBasic(Long sharesOutstandingBasic) {
        this.sharesOutstandingBasic = sharesOutstandingBasic;
    }

    public Long getSharesOutstandingDiluted() { return sharesOutstandingDiluted; }
    public void setSharesOutstandingDiluted(Long sharesOutstandingDiluted) {
        this.sharesOutstandingDiluted = sharesOutstandingDiluted;
    }
}
