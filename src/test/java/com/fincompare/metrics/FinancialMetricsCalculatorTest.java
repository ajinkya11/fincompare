package com.fincompare.metrics;

import com.fincompare.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FinancialMetricsCalculatorTest {

    private FinancialMetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FinancialMetricsCalculator();
    }

    @Test
    void testCalculateMarginRatios() {
        // Arrange
        YearlyFinancialData yearData = new YearlyFinancialData("2023");
        IncomeStatement income = new IncomeStatement();
        income.setTotalRevenue(new BigDecimal("10000000000")); // $10B
        income.setOperatingIncome(new BigDecimal("1000000000")); // $1B
        income.setNetIncome(new BigDecimal("500000000")); // $500M
        income.setGrossProfit(new BigDecimal("3000000000")); // $3B

        yearData.setIncomeStatement(income);

        // Act
        FinancialMetrics metrics = calculator.calculateMetrics(yearData, null);

        // Assert
        assertNotNull(metrics);
        assertEquals(new BigDecimal("10.00"), metrics.getOperatingMargin().setScale(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("5.00"), metrics.getNetMargin().setScale(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("30.00"), metrics.getGrossMargin().setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void testCalculateLiquidityRatios() {
        // Arrange
        YearlyFinancialData yearData = new YearlyFinancialData("2023");
        BalanceSheet balance = new BalanceSheet();
        balance.setCurrentAssets(new BigDecimal("5000000000")); // $5B
        balance.setCurrentLiabilities(new BigDecimal("2500000000")); // $2.5B
        balance.setCashAndEquivalents(new BigDecimal("2000000000")); // $2B
        balance.setInventory(new BigDecimal("500000000")); // $500M

        yearData.setBalanceSheet(balance);

        // Act
        FinancialMetrics metrics = calculator.calculateMetrics(yearData, null);

        // Assert
        assertNotNull(metrics);
        assertEquals(new BigDecimal("2.00"), metrics.getCurrentRatio().setScale(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("0.80"), metrics.getCashRatio().setScale(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("2500000000"), metrics.getWorkingCapital());
    }

    @Test
    void testCalculateProfitabilityRatios() {
        // Arrange
        YearlyFinancialData yearData = new YearlyFinancialData("2023");

        IncomeStatement income = new IncomeStatement();
        income.setNetIncome(new BigDecimal("500000000")); // $500M
        income.setOperatingIncome(new BigDecimal("1000000000")); // $1B

        BalanceSheet balance = new BalanceSheet();
        balance.setTotalAssets(new BigDecimal("20000000000")); // $20B
        balance.setTotalEquity(new BigDecimal("5000000000")); // $5B

        yearData.setIncomeStatement(income);
        yearData.setBalanceSheet(balance);

        // Act
        FinancialMetrics metrics = calculator.calculateMetrics(yearData, null);

        // Assert
        assertNotNull(metrics);
        assertEquals(new BigDecimal("2.50"), metrics.getReturnOnAssets().setScale(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("10.00"), metrics.getReturnOnEquity().setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void testCalculateGrowthRates() {
        // Arrange current year
        YearlyFinancialData currentYear = new YearlyFinancialData("2023");
        IncomeStatement currentIncome = new IncomeStatement();
        currentIncome.setTotalRevenue(new BigDecimal("11000000000")); // $11B
        currentIncome.setNetIncome(new BigDecimal("600000000")); // $600M
        currentYear.setIncomeStatement(currentIncome);

        // Arrange prior year
        YearlyFinancialData priorYear = new YearlyFinancialData("2022");
        IncomeStatement priorIncome = new IncomeStatement();
        priorIncome.setTotalRevenue(new BigDecimal("10000000000")); // $10B
        priorIncome.setNetIncome(new BigDecimal("500000000")); // $500M
        priorYear.setIncomeStatement(priorIncome);

        // Act
        FinancialMetrics metrics = calculator.calculateMetrics(currentYear, priorYear);

        // Assert
        assertNotNull(metrics);
        assertEquals(new BigDecimal("10.00"), metrics.getRevenueGrowth().setScale(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("20.00"), metrics.getNetIncomeGrowth().setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void testHandleNullValues() {
        // Arrange
        YearlyFinancialData yearData = new YearlyFinancialData("2023");
        IncomeStatement income = new IncomeStatement();
        income.setTotalRevenue(null); // No revenue data

        yearData.setIncomeStatement(income);

        // Act
        FinancialMetrics metrics = calculator.calculateMetrics(yearData, null);

        // Assert - should not throw exception and return null metrics
        assertNotNull(metrics);
        assertNull(metrics.getOperatingMargin());
        assertNull(metrics.getNetMargin());
    }
}
