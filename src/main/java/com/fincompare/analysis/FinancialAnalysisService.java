package com.fincompare.analysis;

import com.fincompare.data.SECEdgarClient;
import com.fincompare.data.XBRLParser;
import com.fincompare.metrics.AirlineMetricsCalculator;
import com.fincompare.metrics.FinancialMetricsCalculator;
import com.fincompare.models.CompanyFinancialData;
import com.fincompare.models.YearlyFinancialData;
import com.fincompare.util.FileCache;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FinancialAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(FinancialAnalysisService.class);

    private final SECEdgarClient secClient;
    private final XBRLParser xbrlParser;
    private final FinancialMetricsCalculator metricsCalculator;
    private final AirlineMetricsCalculator airlineMetricsCalculator;
    private final FileCache cache;

    public FinancialAnalysisService(SECEdgarClient secClient,
                                   XBRLParser xbrlParser,
                                   FinancialMetricsCalculator metricsCalculator,
                                   AirlineMetricsCalculator airlineMetricsCalculator,
                                   FileCache cache) {
        this.secClient = secClient;
        this.xbrlParser = xbrlParser;
        this.metricsCalculator = metricsCalculator;
        this.airlineMetricsCalculator = airlineMetricsCalculator;
        this.cache = cache;
    }

    /**
     * Fetch and analyze financial data for a company
     */
    public CompanyFinancialData analyzeCompany(String ticker, int years) throws IOException, ParseException {
        logger.info("Analyzing company: {} for {} years", ticker, years);

        // Look up CIK
        String cacheKey = "cik_" + ticker;
        String cik = cache.get(cacheKey);

        if (cik == null) {
            logger.info("Fetching CIK for ticker: {}", ticker);
            cik = secClient.getCIKFromTicker(ticker);
            cache.put(cacheKey, cik);
        }

        // Fetch company facts
        String factsCacheKey = "facts_" + cik;
        String companyFactsJson = cache.get(factsCacheKey);

        if (companyFactsJson == null) {
            logger.info("Fetching company facts from SEC for CIK: {}", cik);
            companyFactsJson = secClient.fetchCompanyFacts(cik);
            cache.put(factsCacheKey, companyFactsJson);
        } else {
            logger.info("Using cached company facts for CIK: {}", cik);
        }

        // Parse financial data
        CompanyFinancialData companyData = xbrlParser.parseCompanyFacts(companyFactsJson, ticker, years);

        // Calculate metrics for each year
        for (int i = 0; i < companyData.getYearlyData().size(); i++) {
            YearlyFinancialData currentYear = companyData.getYearlyData().get(i);
            YearlyFinancialData priorYear = null;

            if (i + 1 < companyData.getYearlyData().size()) {
                priorYear = companyData.getYearlyData().get(i + 1);
            }

            // Calculate financial metrics
            var metrics = metricsCalculator.calculateMetrics(currentYear, priorYear);
            currentYear.setMetrics(metrics);

            // Calculate airline-specific metrics
            if (currentYear.getOperationalData() != null && currentYear.getIncomeStatement() != null) {
                airlineMetricsCalculator.calculateAirlineMetrics(
                        currentYear.getOperationalData(),
                        currentYear.getIncomeStatement()
                );
            }
        }

        logger.info("Analysis completed for {}", ticker);
        return companyData;
    }
}
