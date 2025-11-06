package com.fincompare.cli;

import com.fincompare.analysis.FinancialAnalysisService;
import com.fincompare.models.CompanyFinancialData;
import com.fincompare.reporting.DetailedReportGenerator;
import com.fincompare.reporting.JSONExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Command for detailed analysis of a single airline
 * Provides in-depth operational, financial, and performance metrics
 */
@Component
@Command(
        name = "analyze",
        description = "Perform detailed analysis of a single airline with comprehensive DOT/BTS data",
        aliases = {"detail"},
        mixinStandardHelpOptions = true
)
public class AnalyzeCommand implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzeCommand.class);

    @Parameters(index = "0", description = "Airline ticker symbol (e.g., UAL, JBLU, DAL)")
    private String ticker;

    @Option(names = {"-y", "--years"}, description = "Number of years to analyze (default: 3)", defaultValue = "3")
    private int years;

    @Option(names = {"-o", "--output"}, description = "Output file path for JSON export")
    private String outputFile;

    @Option(names = {"--performance"}, description = "Include detailed on-time performance analysis")
    private boolean includePerformance;

    @Option(names = {"--monthly"}, description = "Show monthly trend data (requires DOT/BTS)")
    private boolean showMonthly;

    @Option(names = {"--fleet"}, description = "Include detailed fleet composition and analysis")
    private boolean includeFleet;

    @Option(names = {"--all"}, description = "Include all available detail (performance, monthly, fleet)")
    private boolean includeAll;

    private final FinancialAnalysisService analysisService;
    private final DetailedReportGenerator detailReporter;
    private final JSONExporter jsonExporter;

    public AnalyzeCommand(FinancialAnalysisService analysisService,
                         DetailedReportGenerator detailReporter,
                         JSONExporter jsonExporter) {
        this.analysisService = analysisService;
        this.detailReporter = detailReporter;
        this.jsonExporter = jsonExporter;
    }

    @Override
    public Integer call() {
        try {
            System.out.println("\n🔍 Performing detailed analysis of " + ticker.toUpperCase() + "...");
            System.out.println("Fetching data from SEC 10-K filings and DOT/BTS databases...\n");

            // Fetch and analyze company data
            CompanyFinancialData companyData = analysisService.analyzeCompany(ticker.toUpperCase(), years);

            // Determine detail level
            boolean performanceDetail = includePerformance || includeAll;
            boolean monthlyTrends = showMonthly || includeAll;
            boolean fleetDetail = includeFleet || includeAll;

            // Generate detailed report
            detailReporter.generateDetailedReport(companyData, performanceDetail, monthlyTrends, fleetDetail);

            // Export to JSON if requested
            if (outputFile != null) {
                jsonExporter.exportCompanyData(companyData, outputFile);
                System.out.println("\n✅ Detailed analysis exported to: " + outputFile);
            }

            System.out.println("\n✅ Analysis complete!");
            return 0;

        } catch (Exception e) {
            logger.error("Error analyzing {}: {}", ticker, e.getMessage(), e);
            System.err.println("❌ Error: " + e.getMessage());
            return 1;
        }
    }
}
