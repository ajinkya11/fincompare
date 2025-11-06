package com.fincompare.reporting;

import com.fincompare.models.*;
import org.fusesource.jansi.Ansi;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

@Service
public class ConsoleReportGenerator {

    private static final int LABEL_WIDTH = 35;
    private static final int VALUE_WIDTH = 20;

    /**
     * Generate and print comprehensive comparative analysis report
     */
    public void generateReport(ComparativeAnalysis analysis) {
        printHeader("AIRLINE FINANCIAL COMPARATIVE ANALYSIS");
        printExecutiveSummary(analysis);
        printSideBySideComparison(analysis);
        printStrengthsAndWeaknesses(analysis);
        printAirlineSpecificAnalysis(analysis);
        printRedFlags(analysis);
        printRecommendations(analysis);
        printFooter();
    }

    /**
     * Print executive summary
     */
    private void printExecutiveSummary(ComparativeAnalysis analysis) {
        printSectionHeader("Executive Summary");
        System.out.println(analysis.getExecutiveSummary());
        System.out.println();
    }

    /**
     * Print side-by-side financial comparison
     */
    private void printSideBySideComparison(ComparativeAnalysis analysis) {
        String c1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String c2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        List<YearlyFinancialData> c1AllYears = analysis.getCompany1().getYearlyData();
        List<YearlyFinancialData> c2AllYears = analysis.getCompany2().getYearlyData();

        // If multi-year data, print each year separately
        if (c1AllYears.size() > 1 || c2AllYears.size() > 1) {
            printSectionHeader("Financial Metrics Comparison - Multi-Year View");
            System.out.println("Showing detailed comparison for each fiscal year:\n");

            // Find the maximum number of years to display
            int maxYears = Math.max(c1AllYears.size(), c2AllYears.size());

            for (int i = 0; i < maxYears; i++) {
                YearlyFinancialData c1Data = i < c1AllYears.size() ? c1AllYears.get(i) : null;
                YearlyFinancialData c2Data = i < c2AllYears.size() ? c2AllYears.get(i) : null;

                String year1 = c1Data != null ? c1Data.getFiscalYear() : "N/A";
                String year2 = c2Data != null ? c2Data.getFiscalYear() : "N/A";

                if (i > 0) {
                    System.out.println(); // Add spacing between years
                }

                System.out.println(ansi().fg(Ansi.Color.CYAN).bold()
                        .a("=== Fiscal Year: " + c1Name + " " + year1 + " vs " + c2Name + " " + year2 + " ===")
                        .reset());

                // Check if fiscal years match and warn if not
                if (c1Data != null && c2Data != null && !year1.equals(year2)) {
                    System.out.println(ansi().fg(Ansi.Color.YELLOW).bold()
                            .a("⚠ WARNING: Comparing different fiscal years")
                            .reset());
                }

                printYearComparison(c1Name, c2Name, c1Data, c2Data);

                // Print year-over-year changes if not the most recent year
                // Data is ordered [2024, 2023, 2022], so previous year is at index i+1
                if (i > 0 && (i + 1) < c1AllYears.size() && (i + 1) < c2AllYears.size()) {
                    YearlyFinancialData c1PrevData = c1AllYears.get(i + 1);
                    YearlyFinancialData c2PrevData = c2AllYears.get(i + 1);
                    printYoYChanges(c1Name, c2Name, c1Data, c1PrevData, c2Data, c2PrevData);
                }
            }
        } else {
            // Single year view (original behavior)
            printSectionHeader("Financial Metrics Comparison");

            YearlyFinancialData c1Data = c1AllYears.isEmpty() ? null : c1AllYears.get(0);
            YearlyFinancialData c2Data = c2AllYears.isEmpty() ? null : c2AllYears.get(0);

            // Check if fiscal years match and warn if not
            if (c1Data != null && c2Data != null &&
                !c1Data.getFiscalYear().equals(c2Data.getFiscalYear())) {
                System.out.println(ansi().fg(Ansi.Color.YELLOW).bold()
                        .a("⚠ WARNING: Comparing different fiscal years - ")
                        .a(c1Name + ": " + c1Data.getFiscalYear() + ", ")
                        .a(c2Name + ": " + c2Data.getFiscalYear())
                        .reset());
                System.out.println();
            }

            printYearComparison(c1Name, c2Name, c1Data, c2Data);
        }
    }

    /**
     * Print comparison for a single year
     */
    private void printYearComparison(String c1Name, String c2Name, YearlyFinancialData c1Data, YearlyFinancialData c2Data) {
        // Print table header with fiscal years
        String header1 = c1Name + (c1Data != null ? " (FY" + c1Data.getFiscalYear() + ")" : "");
        String header2 = c2Name + (c2Data != null ? " (FY" + c2Data.getFiscalYear() + ")" : "");
        System.out.println(String.format("%-" + LABEL_WIDTH + "s | %" + VALUE_WIDTH + "s | %" + VALUE_WIDTH + "s",
                "Metric", header1, header2));
        System.out.println(repeat("-", LABEL_WIDTH + VALUE_WIDTH * 2 + 6));

        if (c1Data != null && c2Data != null) {
            // Income Statement Metrics
            printComparisonSection("INCOME STATEMENT");
            printComparisonRow("Total Revenue", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getTotalRevenue, true);
            printComparisonRow("Gross Profit", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getGrossProfit, true);
            printComparisonRow("Operating Expenses", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getOperatingExpenses, true);
            printComparisonRow("  - Fuel Costs", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getFuelCosts, true);
            printComparisonRow("  - Labor Costs", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getLaborCosts, true);
            printComparisonRow("  - Depreciation & Amort.", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getDepreciationAmortization, true);
            printComparisonRow("Operating Income", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getOperatingIncome, true);
            printComparisonRow("Interest Expense", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getInterestExpense, true);
            printComparisonRow("Net Income", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getNetIncome, true);
            printComparisonRow("Basic EPS", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getBasicEPS, false);
            printComparisonRow("Diluted EPS", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getDilutedEPS, false);

            // Balance Sheet Metrics
            printComparisonSection("BALANCE SHEET - ASSETS");
            printComparisonRow("Total Assets", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getTotalAssets, true);
            printComparisonRow("Current Assets", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getCurrentAssets, true);
            printComparisonRow("  - Cash & Equivalents", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getCashAndEquivalents, true);
            printComparisonRow("  - Accounts Receivable", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getAccountsReceivable, true);
            printComparisonRow("  - Inventory", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getInventory, true);
            printComparisonRow("PP&E (Gross)", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getPropertyPlantEquipment, true);
            printComparisonRow("PP&E (Net)", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getNetPPE, true);

            printComparisonSection("BALANCE SHEET - LIABILITIES & EQUITY");
            printComparisonRow("Total Liabilities", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getTotalLiabilities, true);
            printComparisonRow("Current Liabilities", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getCurrentLiabilities, true);
            printComparisonRow("Short Term Debt", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getShortTermDebt, true);
            printComparisonRow("Long Term Debt", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getLongTermDebt, true);
            printComparisonRow("Total Equity", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getTotalEquity, true);
            printComparisonRow("Retained Earnings", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getRetainedEarnings, true);

            // Cash Flow Metrics
            printComparisonSection("CASH FLOW STATEMENT");
            printComparisonRow("Operating Cash Flow", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getOperatingCashFlow, true);
            printComparisonRow("Investing Cash Flow", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getInvestingCashFlow, true);
            printComparisonRow("  - Capital Expenditures", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getCapitalExpenditures, true);
            printComparisonRow("Financing Cash Flow", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getFinancingCashFlow, true);
            printComparisonRow("  - Debt Issuance", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getDebtIssuance, true);
            printComparisonRow("  - Debt Repayment", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getDebtRepayment, true);
            printComparisonRow("Free Cash Flow", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getFreeCashFlow, true);

            // Financial Ratios
            printComparisonSection("PROFITABILITY RATIOS");
            printMetricRow("Gross Margin %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getGrossMargin, true);
            printMetricRow("Operating Margin %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getOperatingMargin, true);
            printMetricRow("Net Margin %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getNetMargin, true);
            printMetricRow("ROE %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getReturnOnEquity, true);
            printMetricRow("ROA %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getReturnOnAssets, true);

            printComparisonSection("LIQUIDITY & LEVERAGE RATIOS");
            printMetricRow("Current Ratio", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getCurrentRatio, true);
            printMetricRow("Quick Ratio", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getQuickRatio, true);
            printMetricRow("Debt-to-Equity", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getDebtToEquity, true);
            printMetricRow("Debt-to-Assets", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getDebtToAssets, true);
            printMetricRow("Interest Coverage", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getInterestCoverage, true);

            printComparisonSection("EFFICIENCY RATIOS");
            printMetricRow("Asset Turnover", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getAssetTurnover, true);
            printMetricRow("Revenue Growth %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getRevenueGrowth, true);
        }

        System.out.println();
    }

    /**
     * Print year-over-year changes to show trends
     */
    private void printYoYChanges(String c1Name, String c2Name,
                                  YearlyFinancialData c1Current, YearlyFinancialData c1Prev,
                                  YearlyFinancialData c2Current, YearlyFinancialData c2Prev) {
        System.out.println();
        System.out.println(ansi().fg(Ansi.Color.MAGENTA).bold()
                .a("⟳ YEAR-OVER-YEAR TRENDS")
                .reset());

        String header1 = c1Name + " YoY Change";
        String header2 = c2Name + " YoY Change";
        System.out.println(String.format("%-" + LABEL_WIDTH + "s | %" + VALUE_WIDTH + "s | %" + VALUE_WIDTH + "s",
                "Metric", header1, header2));
        System.out.println(repeat("-", LABEL_WIDTH + VALUE_WIDTH * 2 + 6));

        // Key metrics to show trends
        if (c1Current != null && c1Prev != null && c2Current != null && c2Prev != null) {
            printYoYRow("Revenue Growth",
                    c1Prev.getIncomeStatement().getTotalRevenue(),
                    c1Current.getIncomeStatement().getTotalRevenue(),
                    c2Prev.getIncomeStatement().getTotalRevenue(),
                    c2Current.getIncomeStatement().getTotalRevenue());

            printYoYRow("Op. Income Growth",
                    c1Prev.getIncomeStatement().getOperatingIncome(),
                    c1Current.getIncomeStatement().getOperatingIncome(),
                    c2Prev.getIncomeStatement().getOperatingIncome(),
                    c2Current.getIncomeStatement().getOperatingIncome());

            printYoYRow("Net Income Growth",
                    c1Prev.getIncomeStatement().getNetIncome(),
                    c1Current.getIncomeStatement().getNetIncome(),
                    c2Prev.getIncomeStatement().getNetIncome(),
                    c2Current.getIncomeStatement().getNetIncome());

            printYoYRow("Total Assets Growth",
                    c1Prev.getBalanceSheet().getTotalAssets(),
                    c1Current.getBalanceSheet().getTotalAssets(),
                    c2Prev.getBalanceSheet().getTotalAssets(),
                    c2Current.getBalanceSheet().getTotalAssets());

            printYoYRow("Cash Growth",
                    c1Prev.getBalanceSheet().getCashAndEquivalents(),
                    c1Current.getBalanceSheet().getCashAndEquivalents(),
                    c2Prev.getBalanceSheet().getCashAndEquivalents(),
                    c2Current.getBalanceSheet().getCashAndEquivalents());
        }
    }

    /**
     * Print a single year-over-year comparison row
     */
    private void printYoYRow(String label, BigDecimal c1Prev, BigDecimal c1Current,
                              BigDecimal c2Prev, BigDecimal c2Current) {
        String c1Change = calculateYoYChange(c1Prev, c1Current);
        String c2Change = calculateYoYChange(c2Prev, c2Current);

        System.out.println(String.format("%-" + LABEL_WIDTH + "s | %" + VALUE_WIDTH + "s | %" + VALUE_WIDTH + "s",
                label, c1Change, c2Change));
    }

    /**
     * Calculate year-over-year percentage change with color coding
     */
    private String calculateYoYChange(BigDecimal prev, BigDecimal current) {
        if (prev == null || current == null || prev.compareTo(BigDecimal.ZERO) == 0) {
            return "N/A";
        }

        BigDecimal change = current.subtract(prev).divide(prev, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        String formatted = String.format("%+.2f%%", change.doubleValue());

        // Color code: green for positive, red for negative
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            return ansi().fg(Ansi.Color.GREEN).a("↑ " + formatted).reset().toString();
        } else if (change.compareTo(BigDecimal.ZERO) < 0) {
            return ansi().fg(Ansi.Color.RED).a("↓ " + formatted).reset().toString();
        } else {
            return formatted;
        }
    }

    /**
     * Print airline-specific operational analysis
     */
    private void printAirlineSpecificAnalysis(ComparativeAnalysis analysis) {
        printSectionHeader("Airline Operational Metrics");

        String c1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String c2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        YearlyFinancialData c1Data = analysis.getCompany1().getLatestYearData();
        YearlyFinancialData c2Data = analysis.getCompany2().getLatestYearData();

        if (c1Data != null && c2Data != null) {
            AirlineOperationalData op1 = c1Data.getOperationalData();
            AirlineOperationalData op2 = c2Data.getOperationalData();

            if (op1 != null && op2 != null) {
                // Print table header with fiscal years
                String header1 = c1Name + " (FY" + c1Data.getFiscalYear() + ")";
                String header2 = c2Name + " (FY" + c2Data.getFiscalYear() + ")";
                System.out.println(String.format("%-" + LABEL_WIDTH + "s | %" + VALUE_WIDTH + "s | %" + VALUE_WIDTH + "s",
                        "Metric", header1, header2));
                System.out.println(repeat("-", LABEL_WIDTH + VALUE_WIDTH * 2 + 6));

                printOperationalRow("Available Seat Miles (M)", op1, op2,
                        AirlineOperationalData::getAvailableSeatMiles, false);
                printOperationalRow("Revenue Passenger Miles (M)", op1, op2,
                        AirlineOperationalData::getRevenuePassengerMiles, false);
                printOperationalRow("Load Factor %", op1, op2,
                        AirlineOperationalData::getPassengerLoadFactor, true);
                printOperationalRow("RASM (cents)", op1, op2,
                        AirlineOperationalData::getRasm, true);
                printOperationalRow("CASM (cents)", op1, op2,
                        AirlineOperationalData::getCasm, false);
                printOperationalRow("CASM-ex (cents)", op1, op2,
                        AirlineOperationalData::getCasmEx, false);
                printOperationalRow("Break-even Load Factor %", op1, op2,
                        AirlineOperationalData::getBreakEvenLoadFactor, false);
                printOperationalRow("Passenger Yield (cents)", op1, op2,
                        AirlineOperationalData::getPassengerYield, true);
            } else {
                System.out.println("Operational data not available for one or both airlines.");
            }
        }

        System.out.println();
    }

    /**
     * Print strengths and weaknesses
     */
    private void printStrengthsAndWeaknesses(ComparativeAnalysis analysis) {
        String c1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String c2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        printSectionHeader(c1Name + " - Strengths");
        printBulletList(analysis.getCompany1Strengths(), Ansi.Color.GREEN);

        printSectionHeader(c1Name + " - Weaknesses");
        printBulletList(analysis.getCompany1Weaknesses(), Ansi.Color.YELLOW);

        printSectionHeader(c2Name + " - Strengths");
        printBulletList(analysis.getCompany2Strengths(), Ansi.Color.GREEN);

        printSectionHeader(c2Name + " - Weaknesses");
        printBulletList(analysis.getCompany2Weaknesses(), Ansi.Color.YELLOW);
    }

    /**
     * Print red flags
     */
    private void printRedFlags(ComparativeAnalysis analysis) {
        if (!analysis.getRedFlags().isEmpty()) {
            printSectionHeader("Red Flags & Concerns");
            printBulletList(analysis.getRedFlags(), Ansi.Color.RED);
        }
    }

    /**
     * Print recommendations
     */
    private void printRecommendations(ComparativeAnalysis analysis) {
        printSectionHeader("Investment Recommendations");
        printBulletList(analysis.getRecommendations(), Ansi.Color.CYAN);
    }

    // Helper methods

    private void printHeader(String title) {
        System.out.println();
        System.out.println(ansi().fg(Ansi.Color.CYAN).bold().a(repeat("=", 80)).reset());
        System.out.println(ansi().fg(Ansi.Color.CYAN).bold().a(centerText(title, 80)).reset());
        System.out.println(ansi().fg(Ansi.Color.CYAN).bold().a(repeat("=", 80)).reset());
        System.out.println();
    }

    private void printSectionHeader(String title) {
        System.out.println(ansi().fg(Ansi.Color.YELLOW).bold().a("\n" + title).reset());
        System.out.println(repeat("-", title.length()));
    }

    private void printComparisonSection(String title) {
        System.out.println(ansi().fg(Ansi.Color.BLUE).bold().a("\n" + title).reset());
    }

    private <T> void printComparisonRow(String label, T obj1, T obj2,
                                        java.util.function.Function<T, BigDecimal> getter,
                                        boolean higherIsBetter) {
        if (obj1 == null || obj2 == null) return;

        BigDecimal val1 = getter.apply(obj1);
        BigDecimal val2 = getter.apply(obj2);

        String val1Str = formatLargeNumber(val1);
        String val2Str = formatLargeNumber(val2);

        // Color code based on comparison
        if (val1 != null && val2 != null) {
            int comparison = val1.compareTo(val2);
            if (comparison > 0) {
                if (higherIsBetter) {
                    val1Str = colorize(val1Str, Ansi.Color.GREEN);
                    val2Str = colorize(val2Str, Ansi.Color.RED);
                } else {
                    val1Str = colorize(val1Str, Ansi.Color.RED);
                    val2Str = colorize(val2Str, Ansi.Color.GREEN);
                }
            } else if (comparison < 0) {
                if (higherIsBetter) {
                    val1Str = colorize(val1Str, Ansi.Color.RED);
                    val2Str = colorize(val2Str, Ansi.Color.GREEN);
                } else {
                    val1Str = colorize(val1Str, Ansi.Color.GREEN);
                    val2Str = colorize(val2Str, Ansi.Color.RED);
                }
            }
        }

        System.out.println(String.format("%-" + LABEL_WIDTH + "s | %" + VALUE_WIDTH + "s | %" + VALUE_WIDTH + "s",
                label, val1Str, val2Str));
    }

    private void printMetricRow(String label, FinancialMetrics m1, FinancialMetrics m2,
                                java.util.function.Function<FinancialMetrics, BigDecimal> getter,
                                boolean higherIsBetter) {
        if (m1 == null || m2 == null) return;
        printComparisonRow(label, m1, m2, getter, higherIsBetter);
    }

    private void printOperationalRow(String label, AirlineOperationalData op1, AirlineOperationalData op2,
                                     java.util.function.Function<AirlineOperationalData, BigDecimal> getter,
                                     boolean higherIsBetter) {
        if (op1 == null || op2 == null) return;
        printComparisonRow(label, op1, op2, getter, higherIsBetter);
    }

    private void printBulletList(List<String> items, Ansi.Color color) {
        if (items.isEmpty()) {
            System.out.println("  None identified");
        } else {
            for (String item : items) {
                System.out.println(ansi().fg(color).a("  • ").reset().a(item));
            }
        }
        System.out.println();
    }

    private String formatLargeNumber(BigDecimal value) {
        if (value == null) return "N/A";

        // Format large numbers with appropriate suffixes
        BigDecimal abs = value.abs();
        if (abs.compareTo(new BigDecimal("1000000000")) >= 0) {
            return String.format("%.2fB", value.divide(new BigDecimal("1000000000")).doubleValue());
        } else if (abs.compareTo(new BigDecimal("1000000")) >= 0) {
            return String.format("%.2fM", value.divide(new BigDecimal("1000000")).doubleValue());
        } else if (abs.compareTo(new BigDecimal("1000")) >= 0) {
            return String.format("%.2fK", value.divide(new BigDecimal("1000")).doubleValue());
        } else {
            return String.format("%.2f", value);
        }
    }

    private String colorize(String text, Ansi.Color color) {
        return ansi().fg(color).a(text).reset().toString();
    }

    private String repeat(String str, int count) {
        return new String(new char[count]).replace("\0", str);
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return repeat(" ", padding) + text;
    }

    private void printFooter() {
        System.out.println(ansi().fg(Ansi.Color.CYAN).a(repeat("=", 80)).reset());
        System.out.println();
    }
}
