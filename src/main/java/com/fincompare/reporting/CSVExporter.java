package com.fincompare.reporting;

import com.fincompare.models.*;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CSVExporter {
    private static final Logger logger = LoggerFactory.getLogger(CSVExporter.class);

    /**
     * Export comparative analysis to CSV format
     */
    public void exportToCSV(ComparativeAnalysis analysis, String outputPath) throws IOException {
        logger.info("Exporting comparative analysis to CSV: {}", outputPath);

        try (FileWriter writer = new FileWriter(outputPath)) {
            String c1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
            String c2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

            YearlyFinancialData c1Data = analysis.getCompany1().getLatestYearData();
            YearlyFinancialData c2Data = analysis.getCompany2().getLatestYearData();

            // Write header
            writer.write("Metric,Category," + c1Name + "," + c2Name + "\n");

            if (c1Data != null && c2Data != null) {
                // Income Statement
                writeCSVSection(writer, "Income Statement");
                writeCSVRow(writer, "Total Revenue", "Income Statement",
                        c1Data.getIncomeStatement().getTotalRevenue(),
                        c2Data.getIncomeStatement().getTotalRevenue());
                writeCSVRow(writer, "Operating Expenses", "Income Statement",
                        c1Data.getIncomeStatement().getOperatingExpenses(),
                        c2Data.getIncomeStatement().getOperatingExpenses());
                writeCSVRow(writer, "Operating Income", "Income Statement",
                        c1Data.getIncomeStatement().getOperatingIncome(),
                        c2Data.getIncomeStatement().getOperatingIncome());
                writeCSVRow(writer, "Net Income", "Income Statement",
                        c1Data.getIncomeStatement().getNetIncome(),
                        c2Data.getIncomeStatement().getNetIncome());

                // Balance Sheet
                writeCSVSection(writer, "Balance Sheet");
                writeCSVRow(writer, "Total Assets", "Balance Sheet",
                        c1Data.getBalanceSheet().getTotalAssets(),
                        c2Data.getBalanceSheet().getTotalAssets());
                writeCSVRow(writer, "Total Liabilities", "Balance Sheet",
                        c1Data.getBalanceSheet().getTotalLiabilities(),
                        c2Data.getBalanceSheet().getTotalLiabilities());
                writeCSVRow(writer, "Total Equity", "Balance Sheet",
                        c1Data.getBalanceSheet().getTotalEquity(),
                        c2Data.getBalanceSheet().getTotalEquity());

                // Financial Metrics
                writeCSVSection(writer, "Financial Ratios");
                if (c1Data.getMetrics() != null && c2Data.getMetrics() != null) {
                    writeCSVRow(writer, "Operating Margin %", "Financial Ratios",
                            c1Data.getMetrics().getOperatingMargin(),
                            c2Data.getMetrics().getOperatingMargin());
                    writeCSVRow(writer, "Net Margin %", "Financial Ratios",
                            c1Data.getMetrics().getNetMargin(),
                            c2Data.getMetrics().getNetMargin());
                    writeCSVRow(writer, "ROE %", "Financial Ratios",
                            c1Data.getMetrics().getReturnOnEquity(),
                            c2Data.getMetrics().getReturnOnEquity());
                    writeCSVRow(writer, "Debt-to-Equity", "Financial Ratios",
                            c1Data.getMetrics().getDebtToEquity(),
                            c2Data.getMetrics().getDebtToEquity());
                }

                // Airline Metrics
                if (c1Data.getOperationalData() != null && c2Data.getOperationalData() != null) {
                    writeCSVSection(writer, "Airline Operational Metrics");
                    writeCSVRow(writer, "Load Factor %", "Airline Metrics",
                            c1Data.getOperationalData().getPassengerLoadFactor(),
                            c2Data.getOperationalData().getPassengerLoadFactor());
                    writeCSVRow(writer, "RASM (cents)", "Airline Metrics",
                            c1Data.getOperationalData().getRasm(),
                            c2Data.getOperationalData().getRasm());
                    writeCSVRow(writer, "CASM (cents)", "Airline Metrics",
                            c1Data.getOperationalData().getCasm(),
                            c2Data.getOperationalData().getCasm());
                }
            }

            logger.info("CSV export completed successfully");
        }
    }

    private void writeCSVSection(FileWriter writer, String sectionName) throws IOException {
        writer.write("\n" + sectionName + ",,\n");
    }

    private void writeCSVRow(FileWriter writer, String metric, String category,
                             BigDecimal val1, BigDecimal val2) throws IOException {
        writer.write(String.format("%s,%s,%s,%s\n",
                metric,
                category,
                val1 != null ? val1.toString() : "N/A",
                val2 != null ? val2.toString() : "N/A"));
    }
}
