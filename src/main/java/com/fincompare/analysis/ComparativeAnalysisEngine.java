package com.fincompare.analysis;

import com.fincompare.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComparativeAnalysisEngine {
    private static final Logger logger = LoggerFactory.getLogger(ComparativeAnalysisEngine.class);

    /**
     * Perform comparative analysis between two companies
     */
    public ComparativeAnalysis compare(CompanyFinancialData company1, CompanyFinancialData company2) {
        logger.info("Performing comparative analysis between {} and {}",
                company1.getCompanyInfo().getTickerSymbol(),
                company2.getCompanyInfo().getTickerSymbol());

        ComparativeAnalysis analysis = new ComparativeAnalysis();
        analysis.setCompany1(company1);
        analysis.setCompany2(company2);
        analysis.setAnalysisDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Get latest year data for both companies
        YearlyFinancialData c1Data = company1.getLatestYearData();
        YearlyFinancialData c2Data = company2.getLatestYearData();

        if (c1Data == null || c2Data == null) {
            logger.error("Missing financial data for one or both companies");
            return analysis;
        }

        // Analyze different aspects
        analyzeProfitability(analysis, c1Data, c2Data);
        analyzeLiquidity(analysis, c1Data, c2Data);
        analyzeLeverage(analysis, c1Data, c2Data);
        analyzeGrowth(analysis, company1, company2);
        analyzeAirlineOperations(analysis, c1Data, c2Data);

        // Generate executive summary
        generateExecutiveSummary(analysis);

        // Generate recommendations
        generateRecommendations(analysis);

        logger.info("Comparative analysis completed");
        return analysis;
    }

    /**
     * Analyze profitability metrics
     */
    private void analyzeProfitability(ComparativeAnalysis analysis, YearlyFinancialData c1, YearlyFinancialData c2) {
        FinancialMetrics m1 = c1.getMetrics();
        FinancialMetrics m2 = c2.getMetrics();

        String company1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String company2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        if (m1 != null && m2 != null) {
            // Compare Operating Margin
            if (compare(m1.getOperatingMargin(), m2.getOperatingMargin()) > 0) {
                analysis.getCompany1Strengths().add(
                        String.format("Higher operating margin (%.2f%% vs %.2f%%)",
                                m1.getOperatingMargin(), m2.getOperatingMargin())
                );
            } else if (compare(m1.getOperatingMargin(), m2.getOperatingMargin()) < 0) {
                analysis.getCompany2Strengths().add(
                        String.format("Higher operating margin (%.2f%% vs %.2f%%)",
                                m2.getOperatingMargin(), m1.getOperatingMargin())
                );
            }

            // Compare Net Margin
            if (compare(m1.getNetMargin(), m2.getNetMargin()) > 0) {
                analysis.getCompany1Strengths().add(
                        String.format("Higher net profit margin (%.2f%% vs %.2f%%)",
                                m1.getNetMargin(), m2.getNetMargin())
                );
            } else if (compare(m1.getNetMargin(), m2.getNetMargin()) < 0) {
                analysis.getCompany2Strengths().add(
                        String.format("Higher net profit margin (%.2f%% vs %.2f%%)",
                                m2.getNetMargin(), m1.getNetMargin())
                );
            }

            // Compare ROE
            if (compare(m1.getReturnOnEquity(), m2.getReturnOnEquity()) > 0) {
                analysis.getCompany1Strengths().add(
                        String.format("Higher return on equity (%.2f%% vs %.2f%%)",
                                m1.getReturnOnEquity(), m2.getReturnOnEquity())
                );
            } else if (compare(m1.getReturnOnEquity(), m2.getReturnOnEquity()) < 0) {
                analysis.getCompany2Strengths().add(
                        String.format("Higher return on equity (%.2f%% vs %.2f%%)",
                                m2.getReturnOnEquity(), m1.getReturnOnEquity())
                );
            }

            // Flag negative margins as red flags
            if (m1.getNetMargin() != null && m1.getNetMargin().compareTo(BigDecimal.ZERO) < 0) {
                analysis.getRedFlags().add(company1Name + " has negative net margin");
            }
            if (m2.getNetMargin() != null && m2.getNetMargin().compareTo(BigDecimal.ZERO) < 0) {
                analysis.getRedFlags().add(company2Name + " has negative net margin");
            }
        }
    }

    /**
     * Analyze liquidity metrics
     */
    private void analyzeLiquidity(ComparativeAnalysis analysis, YearlyFinancialData c1, YearlyFinancialData c2) {
        FinancialMetrics m1 = c1.getMetrics();
        FinancialMetrics m2 = c2.getMetrics();

        String company1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String company2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        if (m1 != null && m2 != null) {
            // Compare Current Ratio
            if (compare(m1.getCurrentRatio(), m2.getCurrentRatio()) > 0) {
                analysis.getCompany1Strengths().add(
                        String.format("Better liquidity (current ratio: %.2f vs %.2f)",
                                m1.getCurrentRatio(), m2.getCurrentRatio())
                );
            } else if (compare(m1.getCurrentRatio(), m2.getCurrentRatio()) < 0) {
                analysis.getCompany2Strengths().add(
                        String.format("Better liquidity (current ratio: %.2f vs %.2f)",
                                m2.getCurrentRatio(), m1.getCurrentRatio())
                );
            }

            // Flag low current ratio as concern
            if (m1.getCurrentRatio() != null && m1.getCurrentRatio().compareTo(new BigDecimal("1.0")) < 0) {
                analysis.getRedFlags().add(company1Name + " has current ratio below 1.0 - potential liquidity concerns");
            }
            if (m2.getCurrentRatio() != null && m2.getCurrentRatio().compareTo(new BigDecimal("1.0")) < 0) {
                analysis.getRedFlags().add(company2Name + " has current ratio below 1.0 - potential liquidity concerns");
            }
        }
    }

    /**
     * Analyze leverage metrics
     */
    private void analyzeLeverage(ComparativeAnalysis analysis, YearlyFinancialData c1, YearlyFinancialData c2) {
        FinancialMetrics m1 = c1.getMetrics();
        FinancialMetrics m2 = c2.getMetrics();

        String company1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String company2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        if (m1 != null && m2 != null) {
            // Compare Debt to Equity (lower is better)
            if (compare(m2.getDebtToEquity(), m1.getDebtToEquity()) > 0) {
                analysis.getCompany1Strengths().add(
                        String.format("Lower debt-to-equity ratio (%.2f vs %.2f)",
                                m1.getDebtToEquity(), m2.getDebtToEquity())
                );
            } else if (compare(m2.getDebtToEquity(), m1.getDebtToEquity()) < 0) {
                analysis.getCompany2Strengths().add(
                        String.format("Lower debt-to-equity ratio (%.2f vs %.2f)",
                                m2.getDebtToEquity(), m1.getDebtToEquity())
                );
            }

            // Flag high debt levels
            if (m1.getDebtToEquity() != null && m1.getDebtToEquity().compareTo(new BigDecimal("2.0")) > 0) {
                analysis.getRedFlags().add(company1Name + " has high debt-to-equity ratio (>2.0)");
            }
            if (m2.getDebtToEquity() != null && m2.getDebtToEquity().compareTo(new BigDecimal("2.0")) > 0) {
                analysis.getRedFlags().add(company2Name + " has high debt-to-equity ratio (>2.0)");
            }

            // Check interest coverage
            if (m1.getInterestCoverage() != null && m1.getInterestCoverage().compareTo(new BigDecimal("2.0")) < 0) {
                analysis.getRedFlags().add(company1Name + " has low interest coverage (<2.0x)");
            }
            if (m2.getInterestCoverage() != null && m2.getInterestCoverage().compareTo(new BigDecimal("2.0")) < 0) {
                analysis.getRedFlags().add(company2Name + " has low interest coverage (<2.0x)");
            }
        }
    }

    /**
     * Analyze growth metrics
     */
    private void analyzeGrowth(ComparativeAnalysis analysis, CompanyFinancialData c1, CompanyFinancialData c2) {
        if (c1.getYearlyData().isEmpty() || c2.getYearlyData().isEmpty()) {
            return;
        }

        YearlyFinancialData c1Latest = c1.getLatestYearData();
        YearlyFinancialData c2Latest = c2.getLatestYearData();

        if (c1Latest.getMetrics() != null && c2Latest.getMetrics() != null) {
            FinancialMetrics m1 = c1Latest.getMetrics();
            FinancialMetrics m2 = c2Latest.getMetrics();

            // Compare revenue growth
            if (compare(m1.getRevenueGrowth(), m2.getRevenueGrowth()) > 0) {
                analysis.getCompany1Strengths().add(
                        String.format("Higher revenue growth (%.2f%% vs %.2f%%)",
                                m1.getRevenueGrowth(), m2.getRevenueGrowth())
                );
            } else if (compare(m1.getRevenueGrowth(), m2.getRevenueGrowth()) < 0) {
                analysis.getCompany2Strengths().add(
                        String.format("Higher revenue growth (%.2f%% vs %.2f%%)",
                                m2.getRevenueGrowth(), m1.getRevenueGrowth())
                );
            }

            // Flag negative growth
            if (m1.getRevenueGrowth() != null && m1.getRevenueGrowth().compareTo(BigDecimal.ZERO) < 0) {
                analysis.getCompany1Weaknesses().add("Declining revenue");
            }
            if (m2.getRevenueGrowth() != null && m2.getRevenueGrowth().compareTo(BigDecimal.ZERO) < 0) {
                analysis.getCompany2Weaknesses().add("Declining revenue");
            }
        }
    }

    /**
     * Analyze airline-specific operational metrics
     */
    private void analyzeAirlineOperations(ComparativeAnalysis analysis, YearlyFinancialData c1, YearlyFinancialData c2) {
        AirlineOperationalData op1 = c1.getOperationalData();
        AirlineOperationalData op2 = c2.getOperationalData();

        if (op1 == null || op2 == null) {
            return;
        }

        // Compare RASM (higher is better)
        if (compare(op1.getRasm(), op2.getRasm()) > 0) {
            analysis.getCompany1Strengths().add(
                    String.format("Higher RASM - better revenue generation (%.2f¢ vs %.2f¢)",
                            op1.getRasm(), op2.getRasm())
            );
        } else if (compare(op1.getRasm(), op2.getRasm()) < 0) {
            analysis.getCompany2Strengths().add(
                    String.format("Higher RASM - better revenue generation (%.2f¢ vs %.2f¢)",
                            op2.getRasm(), op1.getRasm())
            );
        }

        // Compare CASM (lower is better)
        if (compare(op2.getCasm(), op1.getCasm()) > 0) {
            analysis.getCompany1Strengths().add(
                    String.format("Lower CASM - more cost efficient (%.2f¢ vs %.2f¢)",
                            op1.getCasm(), op2.getCasm())
            );
        } else if (compare(op2.getCasm(), op1.getCasm()) < 0) {
            analysis.getCompany2Strengths().add(
                    String.format("Lower CASM - more cost efficient (%.2f¢ vs %.2f¢)",
                            op2.getCasm(), op1.getCasm())
            );
        }

        // Compare Load Factor (higher is better)
        if (compare(op1.getPassengerLoadFactor(), op2.getPassengerLoadFactor()) > 0) {
            analysis.getCompany1Strengths().add(
                    String.format("Higher load factor - better capacity utilization (%.2f%% vs %.2f%%)",
                            op1.getPassengerLoadFactor(), op2.getPassengerLoadFactor())
            );
        } else if (compare(op1.getPassengerLoadFactor(), op2.getPassengerLoadFactor()) < 0) {
            analysis.getCompany2Strengths().add(
                    String.format("Higher load factor - better capacity utilization (%.2f%% vs %.2f%%)",
                            op2.getPassengerLoadFactor(), op1.getPassengerLoadFactor())
            );
        }
    }

    /**
     * Generate executive summary
     */
    private void generateExecutiveSummary(ComparativeAnalysis analysis) {
        StringBuilder summary = new StringBuilder();
        String c1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String c2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        summary.append(String.format("Comparative Analysis: %s vs %s\n\n", c1Name, c2Name));

        if (!analysis.getCompany1Strengths().isEmpty() || !analysis.getCompany2Strengths().isEmpty()) {
            summary.append(String.format("%s has %d key strengths, while %s has %d key strengths. ",
                    c1Name, analysis.getCompany1Strengths().size(),
                    c2Name, analysis.getCompany2Strengths().size()));
        }

        if (!analysis.getRedFlags().isEmpty()) {
            summary.append(String.format("Analysis identified %d potential concerns that require attention. ",
                    analysis.getRedFlags().size()));
        }

        analysis.setExecutiveSummary(summary.toString());
    }

    /**
     * Generate investment recommendations
     */
    private void generateRecommendations(ComparativeAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();

        String c1Name = analysis.getCompany1().getCompanyInfo().getTickerSymbol();
        String c2Name = analysis.getCompany2().getCompanyInfo().getTickerSymbol();

        // Determine which company has more strengths
        int c1Score = analysis.getCompany1Strengths().size() - analysis.getCompany1Weaknesses().size();
        int c2Score = analysis.getCompany2Strengths().size() - analysis.getCompany2Weaknesses().size();

        if (c1Score > c2Score) {
            recommendations.add(String.format("%s appears to have stronger overall financial position", c1Name));
        } else if (c2Score > c1Score) {
            recommendations.add(String.format("%s appears to have stronger overall financial position", c2Name));
        } else {
            recommendations.add("Both companies show similar overall financial strength");
        }

        // Add specific recommendations based on analysis
        if (!analysis.getRedFlags().isEmpty()) {
            recommendations.add("Investors should carefully review the identified red flags before making decisions");
        }

        recommendations.add("Consider diversification by investing in both airlines if seeking sector exposure");
        recommendations.add("Monitor fuel costs and operational efficiency metrics for both companies");
        recommendations.add("Review quarterly earnings for trend confirmation");

        analysis.setRecommendations(recommendations);
    }

    /**
     * Helper: Compare two BigDecimal values safely
     */
    private int compare(BigDecimal v1, BigDecimal v2) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return -1;
        if (v2 == null) return 1;
        return v1.compareTo(v2);
    }
}
