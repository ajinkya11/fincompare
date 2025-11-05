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
        printSectionHeader("Financial Metrics Comparison");

        String c1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String c2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        YearlyFinancialData c1Data = analysis.getCompany1().getLatestYearData();
        YearlyFinancialData c2Data = analysis.getCompany2().getLatestYearData();

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
            printComparisonRow("Operating Expenses", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getOperatingExpenses, true);
            printComparisonRow("Operating Income", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getOperatingIncome, true);
            printComparisonRow("Net Income", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getNetIncome, true);
            printComparisonRow("Diluted EPS", c1Data.getIncomeStatement(), c2Data.getIncomeStatement(),
                    IncomeStatement::getDilutedEPS, false);

            // Balance Sheet Metrics
            printComparisonSection("BALANCE SHEET");
            printComparisonRow("Total Assets", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getTotalAssets, true);
            printComparisonRow("Cash & Equivalents", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getCashAndEquivalents, true);
            printComparisonRow("Total Liabilities", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getTotalLiabilities, true);
            printComparisonRow("Total Equity", c1Data.getBalanceSheet(), c2Data.getBalanceSheet(),
                    BalanceSheet::getTotalEquity, true);

            // Cash Flow Metrics
            printComparisonSection("CASH FLOW");
            printComparisonRow("Operating Cash Flow", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getOperatingCashFlow, true);
            printComparisonRow("Free Cash Flow", c1Data.getCashFlowStatement(), c2Data.getCashFlowStatement(),
                    CashFlowStatement::getFreeCashFlow, true);

            // Financial Ratios
            printComparisonSection("FINANCIAL RATIOS");
            printMetricRow("Operating Margin %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getOperatingMargin, true);
            printMetricRow("Net Margin %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getNetMargin, true);
            printMetricRow("ROE %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getReturnOnEquity, true);
            printMetricRow("ROA %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getReturnOnAssets, true);
            printMetricRow("Current Ratio", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getCurrentRatio, true);
            printMetricRow("Debt-to-Equity", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getDebtToEquity, false);
            printMetricRow("Revenue Growth %", c1Data.getMetrics(), c2Data.getMetrics(),
                    FinancialMetrics::getRevenueGrowth, true);
        }

        System.out.println();
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
