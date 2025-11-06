package com.fincompare.reporting;

import com.fincompare.models.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Generates detailed analysis reports for a single airline
 * Includes comprehensive DOT/BTS performance data
 */
@Service
public class DetailedReportGenerator {

    private final NumberFormat currencyFormat;
    private final NumberFormat numberFormat;
    private final NumberFormat percentFormat;

    public DetailedReportGenerator() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        this.numberFormat = NumberFormat.getNumberInstance(Locale.US);
        this.percentFormat = NumberFormat.getPercentInstance(Locale.US);
        percentFormat.setMinimumFractionDigits(2);
        percentFormat.setMaximumFractionDigits(2);
    }

    public void generateDetailedReport(CompanyFinancialData companyData,
                                      boolean includePerformance,
                                      boolean includeMonthly,
                                      boolean includeFleet) {

        printHeader(companyData.getCompanyInfo());

        for (YearlyFinancialData yearlyData : companyData.getYearlyData()) {
            printYearSection(yearlyData, includePerformance, includeMonthly, includeFleet);
        }

        printFooter();
    }

    private void printHeader(CompanyInfo companyInfo) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println(" ".repeat(30) + "COMPREHENSIVE AIRLINE ANALYSIS");
        System.out.println("=".repeat(100));
        System.out.println();
        System.out.println("Company: " + companyInfo.getCompanyName());
        System.out.println("Ticker:  " + companyInfo.getTickerSymbol());
        System.out.println("CIK:     " + companyInfo.getCik());
        System.out.println();
    }

    private void printYearSection(YearlyFinancialData yearlyData,
                                 boolean includePerformance,
                                 boolean includeMonthly,
                                 boolean includeFleet) {

        System.out.println("━".repeat(100));
        System.out.println(" FISCAL YEAR " + yearlyData.getFiscalYear());
        System.out.println("━".repeat(100));
        System.out.println();

        // Financial Statements
        printFinancialStatements(yearlyData);

        // Operational Metrics
        printOperationalMetrics(yearlyData.getOperationalData());

        // Airline-specific Unit Economics
        printUnitEconomics(yearlyData);

        // DOT/BTS Performance Data (if available)
        if (includePerformance && yearlyData instanceof YearlyFinancialDataExtended) {
            YearlyFinancialDataExtended extended = (YearlyFinancialDataExtended) yearlyData;
            if (extended.hasPerformanceData()) {
                printPerformanceMetrics(extended.getPerformanceData());
            }
        }

        // Monthly trends (if requested)
        if (includeMonthly && yearlyData instanceof YearlyFinancialDataExtended) {
            YearlyFinancialDataExtended extended = (YearlyFinancialDataExtended) yearlyData;
            if (extended.hasDOTData()) {
                printMonthlyTrends(extended.getDotData());
            }
        }

        // Fleet Details (if requested)
        if (includeFleet && yearlyData.getOperationalData() != null) {
            printFleetDetails(yearlyData.getOperationalData());
            if (yearlyData instanceof YearlyFinancialDataExtended) {
                YearlyFinancialDataExtended extended = (YearlyFinancialDataExtended) yearlyData;
                if (extended.hasDOTData() && !extended.getDotData().getFleetInventory().isEmpty()) {
                    printDOTFleetInventory(extended.getDotData());
                }
            }
        }

        System.out.println();
    }

    private void printFinancialStatements(YearlyFinancialData yearlyData) {
        System.out.println("┌─ FINANCIAL STATEMENTS ─────────────────────────────────────────────────────────┐");
        System.out.println("│");

        // Income Statement
        IncomeStatement income = yearlyData.getIncomeStatement();
        if (income != null) {
            System.out.println("│  INCOME STATEMENT");
            System.out.println("│  " + "─".repeat(75));
            printFinancialLine("Total Revenue", income.getTotalRevenue());
            if (income.getPassengerRevenue() != null) {
                printFinancialLine("  - Passenger Revenue", income.getPassengerRevenue());
            }
            if (income.getCargoRevenue() != null) {
                printFinancialLine("  - Cargo Revenue", income.getCargoRevenue());
            }
            if (income.getOtherRevenue() != null) {
                printFinancialLine("  - Other Revenue", income.getOtherRevenue());
            }
            printFinancialLine("Operating Expenses", income.getOperatingExpenses());
            if (income.getFuelCosts() != null) {
                printFinancialLine("  - Fuel Costs", income.getFuelCosts());
            }
            if (income.getLaborCosts() != null) {
                printFinancialLine("  - Labor Costs", income.getLaborCosts());
            }
            printFinancialLine("Operating Income", income.getOperatingIncome());
            printFinancialLine("Net Income", income.getNetIncome());
            System.out.println("│");
        }

        // Balance Sheet
        BalanceSheet balance = yearlyData.getBalanceSheet();
        if (balance != null) {
            System.out.println("│  BALANCE SHEET");
            System.out.println("│  " + "─".repeat(75));
            printFinancialLine("Total Assets", balance.getTotalAssets());
            printFinancialLine("Total Liabilities", balance.getTotalLiabilities());
            printFinancialLine("Total Equity", balance.getTotalEquity());
            printFinancialLine("Long-term Debt", balance.getLongTermDebt());
            System.out.println("│");
        }

        // Cash Flow
        CashFlowStatement cashFlow = yearlyData.getCashFlowStatement();
        if (cashFlow != null) {
            System.out.println("│  CASH FLOW STATEMENT");
            System.out.println("│  " + "─".repeat(75));
            printFinancialLine("Operating Cash Flow", cashFlow.getOperatingCashFlow());
            printFinancialLine("Free Cash Flow", cashFlow.getFreeCashFlow());
            printFinancialLine("Capital Expenditures", cashFlow.getCapitalExpenditures());
            System.out.println("│");
        }

        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void printOperationalMetrics(AirlineOperationalData opData) {
        if (opData == null) {
            return;
        }

        System.out.println("┌─ OPERATIONAL METRICS ──────────────────────────────────────────────────────────┐");
        System.out.println("│");
        System.out.println("│  CAPACITY & TRAFFIC");
        System.out.println("│  " + "─".repeat(75));
        printOperationalLine("Available Seat Miles (ASM)", opData.getAvailableSeatMiles(), "millions");
        printOperationalLine("Revenue Passenger Miles (RPM)", opData.getRevenuePassengerMiles(), "millions");
        printOperationalLine("Passenger Load Factor", opData.getLoadFactor(), "%");
        printOperationalLine("Passengers Carried", opData.getPassengersCarried() != null ?
                new BigDecimal(opData.getPassengersCarried()) : null, "passengers");
        System.out.println("│");

        // Cargo metrics
        if (opData.getCargoTonMiles() != null || opData.getAvailableTonMiles() != null) {
            System.out.println("│  CARGO OPERATIONS");
            System.out.println("│  " + "─".repeat(75));
            printOperationalLine("Cargo Ton Miles (CTM)", opData.getCargoTonMiles(), "millions");
            printOperationalLine("Available Ton Miles (ATM)", opData.getAvailableTonMiles(), "millions");
            printOperationalLine("Cargo Load Factor", opData.getCargoLoadFactor(), "%");
            System.out.println("│");
        }

        // Flight operations
        if (opData.getDeparturesPerformed() != null || opData.getBlockHours() != null) {
            System.out.println("│  FLIGHT OPERATIONS");
            System.out.println("│  " + "─".repeat(75));
            printOperationalLine("Departures Performed", opData.getDeparturesPerformed(), "flights");
            printOperationalLine("Block Hours", opData.getBlockHours(), "hours");
            System.out.println("│");
        }

        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void printUnitEconomics(YearlyFinancialData yearlyData) {
        AirlineOperationalData opData = yearlyData.getOperationalData();
        if (opData == null) {
            return;
        }

        System.out.println("┌─ UNIT ECONOMICS ───────────────────────────────────────────────────────────────┐");
        System.out.println("│");
        printOperationalLine("RASM (Revenue per ASM)", opData.getRasm(), "cents");
        printOperationalLine("CASM (Cost per ASM)", opData.getCasm(), "cents");
        printOperationalLine("CASM-ex (CASM excluding fuel)", opData.getCasmEx(), "cents");
        printOperationalLine("Passenger Yield", opData.getPassengerYield(), "cents per RPM");
        if (opData.getCargoYield() != null) {
            printOperationalLine("Cargo Yield", opData.getCargoYield(), "cents per CTM");
        }
        printOperationalLine("Break-even Load Factor", opData.getBreakEvenLoadFactor(), "%");
        System.out.println("│");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void printPerformanceMetrics(DOTOnTimePerformance perfData) {
        System.out.println("┌─ ON-TIME PERFORMANCE (DOT/BTS) ────────────────────────────────────────────────┐");
        System.out.println("│");
        System.out.println("│  FLIGHT PERFORMANCE");
        System.out.println("│  " + "─".repeat(75));
        printPerformanceLine("Total Flights", perfData.getTotalFlights());
        printPerformanceLine("On-Time Flights", perfData.getOnTimeFlights());
        printPerformanceLine("On-Time Performance", perfData.getOnTimePercentage(), "%");
        printPerformanceLine("Cancelled Flights", perfData.getCancelledFlights());
        printPerformanceLine("Cancellation Rate", perfData.getCancellationRate(), "%");
        printPerformanceLine("Diverted Flights", perfData.getDivertedFlights());
        System.out.println("│");

        System.out.println("│  DELAY ANALYSIS");
        System.out.println("│  " + "─".repeat(75));
        printPerformanceLine("Average Arrival Delay", perfData.getAverageArrivalDelay(), "minutes");
        printPerformanceLine("Average Departure Delay", perfData.getAverageDepartureDelay(), "minutes");
        printPerformanceLine("Carrier Delay Minutes", perfData.getCarrierDelayMinutes(), "total minutes");
        printPerformanceLine("Weather Delay Minutes", perfData.getWeatherDelayMinutes(), "total minutes");
        printPerformanceLine("NAS Delay Minutes", perfData.getNasDelayMinutes(), "total minutes");
        System.out.println("│");

        if (perfData.getMishandledBaggage() != null) {
            System.out.println("│  BAGGAGE & COMPLAINTS");
            System.out.println("│  " + "─".repeat(75));
            printPerformanceLine("Mishandled Baggage", perfData.getMishandledBaggage());
            printPerformanceLine("Mishandled Baggage Rate", perfData.getMishandledBaggageRate(), "per 1,000 pax");
            if (perfData.getCustomerComplaints() != null) {
                printPerformanceLine("Customer Complaints", perfData.getCustomerComplaints());
                printPerformanceLine("Complaint Rate", perfData.getComplaintRate(), "per 100K pax");
            }
            System.out.println("│");
        }

        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void printMonthlyTrends(AggregatedDOTData dotData) {
        if (dotData.getMonthlyOperationalData().isEmpty()) {
            return;
        }

        System.out.println("┌─ MONTHLY TRENDS (DOT T-100) ───────────────────────────────────────────────────┐");
        System.out.println("│");
        System.out.printf("│  %-10s %15s %15s %10s %15s%n",
                "Month", "Passengers", "ASM (M)", "Load %", "Departures");
        System.out.println("│  " + "─".repeat(75));

        for (DOTOperationalData monthly : dotData.getMonthlyOperationalData()) {
            System.out.printf("│  %-10s %,15d %,15.0f %,10.2f %,15d%n",
                    monthly.getReportPeriod().toString(),
                    monthly.getRevenuePassengers() != null ? monthly.getRevenuePassengers() : 0,
                    monthly.getAvailableSeatMiles() != null ? monthly.getAvailableSeatMiles().doubleValue() : 0.0,
                    monthly.getPassengerLoadFactor() != null ? monthly.getPassengerLoadFactor().doubleValue() : 0.0,
                    monthly.getDeparturesPerformed() != null ? monthly.getDeparturesPerformed() : 0
            );
        }

        System.out.println("│");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void printFleetDetails(AirlineOperationalData opData) {
        if (opData.getFleetSize() == null && opData.getFleetComposition() == null) {
            return;
        }

        System.out.println("┌─ FLEET INFORMATION ────────────────────────────────────────────────────────────┐");
        System.out.println("│");
        printOperationalLine("Fleet Size", opData.getFleetSize() != null ?
                new BigDecimal(opData.getFleetSize()) : null, "aircraft");
        printOperationalLine("Average Fleet Age", opData.getAverageFleetAge() != null ?
                new BigDecimal(opData.getAverageFleetAge()) : null, "years");

        if (opData.getFleetComposition() != null) {
            System.out.println("│  Fleet Composition:");
            String[] aircraftTypes = opData.getFleetComposition().split(", ");
            for (String type : aircraftTypes) {
                System.out.println("│    - " + type);
            }
        }

        System.out.println("│");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void printDOTFleetInventory(AggregatedDOTData dotData) {
        System.out.println("┌─ DETAILED FLEET INVENTORY (DOT) ───────────────────────────────────────────────┐");
        System.out.println("│");
        System.out.printf("│  %-15s %,10s %,10s%n",
                "Aircraft Type", "Count", "Avg Age");
        System.out.println("│  " + "─".repeat(75));

        dotData.getFleetCompositionMap().forEach((type, count) -> {
            System.out.printf("│  %-15s %,10d%n", type, count);
        });

        System.out.println("│");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void printFooter() {
        System.out.println("=".repeat(100));
        System.out.println();
        System.out.println("Data Sources:");
        System.out.println("  - SEC 10-K Filings (XBRL & HTML extraction)");
        System.out.println("  - DOT Bureau of Transportation Statistics (T-100, On-Time Performance)");
        System.out.println();
    }

    // Helper methods for formatting
    private void printFinancialLine(String label, BigDecimal value) {
        if (value != null) {
            System.out.printf("│  %-40s %20s%n", label + ":", currencyFormat.format(value));
        }
    }

    private void printOperationalLine(String label, BigDecimal value, String unit) {
        if (value != null) {
            System.out.printf("│  %-40s %,20.2f %s%n", label + ":", value.doubleValue(), unit);
        }
    }

    private void printOperationalLine(String label, Integer value, String unit) {
        if (value != null) {
            System.out.printf("│  %-40s %,20d %s%n", label + ":", value, unit);
        }
    }

    private void printPerformanceLine(String label, Integer value) {
        if (value != null) {
            System.out.printf("│  %-40s %,20d%n", label + ":", value);
        }
    }

    private void printPerformanceLine(String label, BigDecimal value, String unit) {
        if (value != null) {
            System.out.printf("│  %-40s %,20.2f %s%n", label + ":", value.doubleValue(), unit);
        }
    }
}
