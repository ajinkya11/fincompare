package com.fincompare.metrics;

import com.fincompare.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class FinancialMetricsCalculator {
    private static final Logger logger = LoggerFactory.getLogger(FinancialMetricsCalculator.class);
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * Calculate all financial metrics for a yearly dataset
     */
    public FinancialMetrics calculateMetrics(YearlyFinancialData yearData, YearlyFinancialData priorYearData) {
        logger.info("Calculating financial metrics for fiscal year: {}", yearData.getFiscalYear());

        FinancialMetrics metrics = new FinancialMetrics();
        metrics.setFiscalYear(yearData.getFiscalYear());

        IncomeStatement income = yearData.getIncomeStatement();
        BalanceSheet balance = yearData.getBalanceSheet();
        CashFlowStatement cashFlow = yearData.getCashFlowStatement();

        if (income != null) {
            calculateMarginRatios(metrics, income);
        }

        if (balance != null) {
            calculateLiquidityRatios(metrics, balance);
            calculateLeverageRatios(metrics, balance, income);
        }

        if (income != null && balance != null) {
            calculateProfitabilityRatios(metrics, income, balance);
            calculateEfficiencyRatios(metrics, income, balance);
        }

        if (cashFlow != null && balance != null) {
            calculateCashFlowMetrics(metrics, cashFlow, balance, income);
        }

        if (priorYearData != null) {
            calculateGrowthRates(metrics, yearData, priorYearData);
        }

        // Airline-specific financial health metrics
        if (income != null) {
            calculateAirlineSpecificMetrics(metrics, income);
        }

        return metrics;
    }

    /**
     * Calculate margin ratios
     */
    private void calculateMarginRatios(FinancialMetrics metrics, IncomeStatement income) {
        BigDecimal revenue = income.getTotalRevenue();

        if (revenue != null && revenue.compareTo(ZERO) > 0) {
            // Gross Margin
            if (income.getGrossProfit() != null) {
                metrics.setGrossMargin(
                        income.getGrossProfit().divide(revenue, MC).multiply(HUNDRED)
                );
            }

            // Operating Margin
            if (income.getOperatingIncome() != null) {
                metrics.setOperatingMargin(
                        income.getOperatingIncome().divide(revenue, MC).multiply(HUNDRED)
                );
            }

            // Net Margin
            if (income.getNetIncome() != null) {
                metrics.setNetMargin(
                        income.getNetIncome().divide(revenue, MC).multiply(HUNDRED)
                );
            }

            // EBITDA Margin
            if (income.getEbitda() != null) {
                metrics.setEbitdaMargin(
                        income.getEbitda().divide(revenue, MC).multiply(HUNDRED)
                );
            } else if (income.getOperatingIncome() != null && income.getDepreciationAmortization() != null) {
                // Calculate EBITDA if not directly available
                BigDecimal ebitda = income.getOperatingIncome().add(income.getDepreciationAmortization());
                metrics.setEbitdaMargin(ebitda.divide(revenue, MC).multiply(HUNDRED));
            }
        }
    }

    /**
     * Calculate profitability ratios
     */
    private void calculateProfitabilityRatios(FinancialMetrics metrics, IncomeStatement income, BalanceSheet balance) {
        // Return on Assets (ROA)
        if (income.getNetIncome() != null && balance.getTotalAssets() != null &&
                balance.getTotalAssets().compareTo(ZERO) > 0) {
            metrics.setReturnOnAssets(
                    income.getNetIncome().divide(balance.getTotalAssets(), MC).multiply(HUNDRED)
            );
        }

        // Return on Equity (ROE)
        if (income.getNetIncome() != null && balance.getTotalEquity() != null &&
                balance.getTotalEquity().compareTo(ZERO) > 0) {
            metrics.setReturnOnEquity(
                    income.getNetIncome().divide(balance.getTotalEquity(), MC).multiply(HUNDRED)
            );
        }

        // Return on Invested Capital (ROIC)
        BigDecimal investedCapital = calculateInvestedCapital(balance);
        if (income.getOperatingIncome() != null && investedCapital != null &&
                investedCapital.compareTo(ZERO) > 0) {
            // ROIC = NOPAT / Invested Capital
            // Simplified: Operating Income * (1 - tax rate) / Invested Capital
            // Using operating income as approximation
            metrics.setReturnOnInvestedCapital(
                    income.getOperatingIncome().divide(investedCapital, MC).multiply(HUNDRED)
            );
        }
    }

    /**
     * Calculate liquidity ratios
     */
    private void calculateLiquidityRatios(FinancialMetrics metrics, BalanceSheet balance) {
        // Current Ratio
        if (balance.getCurrentAssets() != null && balance.getCurrentLiabilities() != null &&
                balance.getCurrentLiabilities().compareTo(ZERO) > 0) {
            metrics.setCurrentRatio(
                    balance.getCurrentAssets().divide(balance.getCurrentLiabilities(), MC)
            );
        }

        // Quick Ratio (Current Assets - Inventory) / Current Liabilities
        if (balance.getCurrentAssets() != null && balance.getCurrentLiabilities() != null &&
                balance.getCurrentLiabilities().compareTo(ZERO) > 0) {
            BigDecimal quickAssets = balance.getCurrentAssets();
            if (balance.getInventory() != null) {
                quickAssets = quickAssets.subtract(balance.getInventory());
            }
            metrics.setQuickRatio(
                    quickAssets.divide(balance.getCurrentLiabilities(), MC)
            );
        }

        // Cash Ratio
        if (balance.getCashAndEquivalents() != null && balance.getCurrentLiabilities() != null &&
                balance.getCurrentLiabilities().compareTo(ZERO) > 0) {
            metrics.setCashRatio(
                    balance.getCashAndEquivalents().divide(balance.getCurrentLiabilities(), MC)
            );
        }

        // Working Capital
        if (balance.getCurrentAssets() != null && balance.getCurrentLiabilities() != null) {
            metrics.setWorkingCapital(
                    balance.getCurrentAssets().subtract(balance.getCurrentLiabilities())
            );
        }
    }

    /**
     * Calculate leverage ratios
     */
    private void calculateLeverageRatios(FinancialMetrics metrics, BalanceSheet balance, IncomeStatement income) {
        // Total Debt
        BigDecimal totalDebt = calculateTotalDebt(balance);
        if (totalDebt != null) {
            metrics.setTotalDebt(totalDebt);

            // Net Debt = Total Debt - Cash
            if (balance.getCashAndEquivalents() != null) {
                metrics.setNetDebt(totalDebt.subtract(balance.getCashAndEquivalents()));
            }
        }

        // Debt to Equity
        if (totalDebt != null && balance.getTotalEquity() != null &&
                balance.getTotalEquity().compareTo(ZERO) > 0) {
            metrics.setDebtToEquity(
                    totalDebt.divide(balance.getTotalEquity(), MC)
            );
        }

        // Debt to Assets
        if (totalDebt != null && balance.getTotalAssets() != null &&
                balance.getTotalAssets().compareTo(ZERO) > 0) {
            metrics.setDebtToAssets(
                    totalDebt.divide(balance.getTotalAssets(), MC)
            );
        }

        // Equity Multiplier = Total Assets / Total Equity
        if (balance.getTotalAssets() != null && balance.getTotalEquity() != null &&
                balance.getTotalEquity().compareTo(ZERO) > 0) {
            metrics.setEquityMultiplier(
                    balance.getTotalAssets().divide(balance.getTotalEquity(), MC)
            );
        }

        // Interest Coverage = Operating Income / Interest Expense
        if (income != null && income.getOperatingIncome() != null &&
                income.getInterestExpense() != null &&
                income.getInterestExpense().compareTo(ZERO) > 0) {
            metrics.setInterestCoverage(
                    income.getOperatingIncome().divide(income.getInterestExpense(), MC)
            );
        }
    }

    /**
     * Calculate efficiency ratios
     */
    private void calculateEfficiencyRatios(FinancialMetrics metrics, IncomeStatement income, BalanceSheet balance) {
        // Asset Turnover = Revenue / Average Total Assets
        if (income.getTotalRevenue() != null && balance.getTotalAssets() != null &&
                balance.getTotalAssets().compareTo(ZERO) > 0) {
            metrics.setAssetTurnover(
                    income.getTotalRevenue().divide(balance.getTotalAssets(), MC)
            );
        }

        // Receivables Turnover = Revenue / Accounts Receivable
        if (income.getTotalRevenue() != null && balance.getAccountsReceivable() != null &&
                balance.getAccountsReceivable().compareTo(ZERO) > 0) {
            metrics.setReceivablesTurnover(
                    income.getTotalRevenue().divide(balance.getAccountsReceivable(), MC)
            );
        }

        // Inventory Turnover = COGS / Inventory
        if (income.getCostOfRevenue() != null && balance.getInventory() != null &&
                balance.getInventory().compareTo(ZERO) > 0) {
            metrics.setInventoryTurnover(
                    income.getCostOfRevenue().divide(balance.getInventory(), MC)
            );
        }
    }

    /**
     * Calculate cash flow metrics
     */
    private void calculateCashFlowMetrics(FinancialMetrics metrics, CashFlowStatement cashFlow,
                                          BalanceSheet balance, IncomeStatement income) {
        // Cash Flow to Debt
        BigDecimal totalDebt = metrics.getTotalDebt();
        if (cashFlow.getOperatingCashFlow() != null && totalDebt != null && totalDebt.compareTo(ZERO) > 0) {
            metrics.setCashFlowToDebt(
                    cashFlow.getOperatingCashFlow().divide(totalDebt, MC)
            );
        }

        // Operating Cash Flow Ratio = Operating CF / Current Liabilities
        if (cashFlow.getOperatingCashFlow() != null && balance.getCurrentLiabilities() != null &&
                balance.getCurrentLiabilities().compareTo(ZERO) > 0) {
            metrics.setOperatingCashFlowRatio(
                    cashFlow.getOperatingCashFlow().divide(balance.getCurrentLiabilities(), MC)
            );
        }

        // Cash Conversion Ratio = Operating CF / Net Income
        if (cashFlow.getOperatingCashFlow() != null && income != null &&
                income.getNetIncome() != null && income.getNetIncome().compareTo(ZERO) > 0) {
            metrics.setCashConversionRatio(
                    cashFlow.getOperatingCashFlow().divide(income.getNetIncome(), MC)
            );
        }

        // Book Value Per Share
        if (balance.getTotalEquity() != null && income != null && income.getSharesOutstandingBasic() != null &&
                income.getSharesOutstandingBasic() > 0) {
            metrics.setBookValuePerShare(
                    balance.getTotalEquity().divide(
                            new BigDecimal(income.getSharesOutstandingBasic()), MC)
            );
        }

        // Free Cash Flow Per Share
        if (cashFlow.getFreeCashFlow() != null && income != null &&
                income.getSharesOutstandingBasic() != null && income.getSharesOutstandingBasic() > 0) {
            metrics.setFreeCashFlowPerShare(
                    cashFlow.getFreeCashFlow().divide(
                            new BigDecimal(income.getSharesOutstandingBasic()), MC)
            );
        }
    }

    /**
     * Calculate growth rates (year-over-year)
     */
    private void calculateGrowthRates(FinancialMetrics metrics, YearlyFinancialData currentYear,
                                      YearlyFinancialData priorYear) {
        IncomeStatement currentIncome = currentYear.getIncomeStatement();
        IncomeStatement priorIncome = priorYear.getIncomeStatement();
        CashFlowStatement currentCF = currentYear.getCashFlowStatement();
        CashFlowStatement priorCF = priorYear.getCashFlowStatement();

        if (currentIncome != null && priorIncome != null) {
            // Revenue Growth
            metrics.setRevenueGrowth(
                    calculateGrowthRate(priorIncome.getTotalRevenue(), currentIncome.getTotalRevenue())
            );

            // Net Income Growth
            metrics.setNetIncomeGrowth(
                    calculateGrowthRate(priorIncome.getNetIncome(), currentIncome.getNetIncome())
            );

            // EPS Growth
            metrics.setEpsGrowth(
                    calculateGrowthRate(priorIncome.getDilutedEPS(), currentIncome.getDilutedEPS())
            );
        }

        if (currentCF != null && priorCF != null) {
            // Operating Cash Flow Growth
            metrics.setOperatingCashFlowGrowth(
                    calculateGrowthRate(priorCF.getOperatingCashFlow(), currentCF.getOperatingCashFlow())
            );
        }
    }

    /**
     * Calculate airline-specific financial health metrics
     */
    private void calculateAirlineSpecificMetrics(FinancialMetrics metrics, IncomeStatement income) {
        BigDecimal operatingExpenses = income.getOperatingExpenses();

        if (operatingExpenses != null && operatingExpenses.compareTo(ZERO) > 0) {
            // Fuel Costs as % of Operating Expenses
            if (income.getFuelCosts() != null) {
                metrics.setFuelCostPercentage(
                        income.getFuelCosts().divide(operatingExpenses, MC).multiply(HUNDRED)
                );
            }

            // Labor Costs as % of Operating Expenses
            if (income.getLaborCosts() != null) {
                metrics.setLaborCostPercentage(
                        income.getLaborCosts().divide(operatingExpenses, MC).multiply(HUNDRED)
                );
            }
        }
    }

    /**
     * Helper: Calculate growth rate
     */
    private BigDecimal calculateGrowthRate(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue == null || newValue == null || oldValue.compareTo(ZERO) == 0) {
            return null;
        }

        return newValue.subtract(oldValue)
                .divide(oldValue.abs(), MC)
                .multiply(HUNDRED);
    }

    /**
     * Helper: Calculate total debt
     */
    private BigDecimal calculateTotalDebt(BalanceSheet balance) {
        BigDecimal totalDebt = ZERO;
        boolean hasDebt = false;

        if (balance.getLongTermDebt() != null) {
            totalDebt = totalDebt.add(balance.getLongTermDebt());
            hasDebt = true;
        }

        if (balance.getShortTermDebt() != null) {
            totalDebt = totalDebt.add(balance.getShortTermDebt());
            hasDebt = true;
        }

        if (balance.getCurrentPortionLongTermDebt() != null) {
            totalDebt = totalDebt.add(balance.getCurrentPortionLongTermDebt());
            hasDebt = true;
        }

        return hasDebt ? totalDebt : null;
    }

    /**
     * Helper: Calculate invested capital
     */
    private BigDecimal calculateInvestedCapital(BalanceSheet balance) {
        if (balance.getTotalEquity() != null) {
            BigDecimal investedCapital = balance.getTotalEquity();
            BigDecimal totalDebt = calculateTotalDebt(balance);
            if (totalDebt != null) {
                investedCapital = investedCapital.add(totalDebt);
            }
            return investedCapital;
        }
        return null;
    }
}
