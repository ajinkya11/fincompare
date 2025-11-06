package com.fincompare.cli;

import com.fincompare.analysis.ComparativeAnalysisEngine;
import com.fincompare.analysis.FinancialAnalysisService;
import com.fincompare.models.ComparativeAnalysis;
import com.fincompare.models.CompanyFinancialData;
import com.fincompare.reporting.CSVExporter;
import com.fincompare.reporting.ConsoleReportGenerator;
import com.fincompare.reporting.JSONExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Component
@Command(
        name = "compare",
        description = "Compare two airline companies' financial performance",
        mixinStandardHelpOptions = true
)
public class CompareCommand implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CompareCommand.class);

    @Parameters(index = "0", description = "First company ticker symbol (e.g., UAL)")
    private String ticker1;

    @Parameters(index = "1", description = "Second company ticker symbol (e.g., JBLU)")
    private String ticker2;

    @Option(names = {"-y", "--years"}, description = "Number of years to analyze (default: 1)", defaultValue = "1")
    private int years;

    @Option(names = {"-o", "--output"}, description = "Output file path for CSV export")
    private String outputFile;

    @Option(names = {"--json"}, description = "Export results as JSON")
    private boolean jsonExport;

    @Option(names = {"--metrics"}, description = "Show only specific metrics (operational, profitability, liquidity)")
    private String metricsFilter;

    @Option(names = {"--detail"}, description = "Show detailed financial statements with 3-year trends")
    private boolean detailView;

    @Option(names = {"--ops"}, description = "Show operational deep-dive with unit economics and fleet details")
    private boolean opsView;

    @Option(names = {"--data-quality"}, description = "Show data quality report with sources and validation")
    private boolean dataQualityView;

    private final FinancialAnalysisService analysisService;
    private final ComparativeAnalysisEngine comparisonEngine;
    private final ConsoleReportGenerator consoleReporter;
    private final CSVExporter csvExporter;
    private final JSONExporter jsonExporter;

    public CompareCommand(FinancialAnalysisService analysisService,
                         ComparativeAnalysisEngine comparisonEngine,
                         ConsoleReportGenerator consoleReporter,
                         CSVExporter csvExporter,
                         JSONExporter jsonExporter) {
        this.analysisService = analysisService;
        this.comparisonEngine = comparisonEngine;
        this.consoleReporter = consoleReporter;
        this.csvExporter = csvExporter;
        this.jsonExporter = jsonExporter;
    }

    @Override
    public Integer call() {
        try {
            System.out.println("\n🔍 Fetching financial data for " + ticker1.toUpperCase() + "...");
            CompanyFinancialData company1Data = analysisService.analyzeCompany(ticker1.toUpperCase(), years);

            System.out.println("🔍 Fetching financial data for " + ticker2.toUpperCase() + "...");
            CompanyFinancialData company2Data = analysisService.analyzeCompany(ticker2.toUpperCase(), years);

            System.out.println("📊 Performing comparative analysis...");
            ComparativeAnalysis analysis = comparisonEngine.compare(company1Data, company2Data);

            // Generate console report with appropriate detail level
            consoleReporter.generateReport(analysis, detailView, opsView, dataQualityView);

            // Export to file if requested
            if (outputFile != null) {
                if (jsonExport || outputFile.endsWith(".json")) {
                    jsonExporter.exportToJSON(analysis, outputFile);
                    System.out.println("✅ Analysis exported to JSON: " + outputFile);
                } else {
                    csvExporter.exportToCSV(analysis, outputFile);
                    System.out.println("✅ Analysis exported to CSV: " + outputFile);
                }
            }

            return 0;

        } catch (Exception e) {
            logger.error("Error performing comparative analysis", e);
            System.err.println("❌ Error: " + e.getMessage());
            System.err.println("\nPlease check:");
            System.err.println("  • Ticker symbols are valid");
            System.err.println("  • You have an active internet connection");
            System.err.println("  • The companies have filed 10-K reports with the SEC");
            return 1;
        }
    }
}
