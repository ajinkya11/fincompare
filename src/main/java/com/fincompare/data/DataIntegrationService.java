package com.fincompare.data;

import com.fincompare.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service to integrate data from multiple sources:
 * - SEC 10-K filings (via XBRLParser)
 * - DOT/BTS operational data (via DOTDataParser)
 *
 * Merges data with priority: XBRL > 10-K HTML > DOT/BTS
 */
@Service
public class DataIntegrationService {
    private static final Logger logger = LoggerFactory.getLogger(DataIntegrationService.class);

    @Value("${dot.bts.enabled:true}")
    private boolean dotBtsEnabled;

    private final DOTDataParser dotDataParser;

    public DataIntegrationService(DOTDataParser dotDataParser) {
        this.dotDataParser = dotDataParser;
    }

    /**
     * Enhance company financial data with DOT/BTS data
     * Fills in missing operational metrics from DOT sources
     */
    public void enhanceWithDOTData(CompanyFinancialData companyData, String ticker) {
        if (!dotBtsEnabled) {
            logger.info("DOT/BTS integration is disabled");
            return;
        }

        logger.info("Enhancing {} data with DOT/BTS sources", ticker);

        for (YearlyFinancialData yearlyData : companyData.getYearlyData()) {
            String fiscalYear = yearlyData.getFiscalYear();

            try {
                // Fetch aggregated DOT data for this fiscal year
                AggregatedDOTData dotData = dotDataParser.fetchAggregatedData(ticker, fiscalYear);

                if (dotData != null && dotData.getAnnualOperationalData() != null) {
                    // Merge operational data
                    mergeOperationalData(yearlyData, dotData);

                    // Add performance data (new metrics not in 10-K)
                    addPerformanceData(yearlyData, dotData);

                    // Enhance fleet data if needed
                    enhanceFleetData(yearlyData, dotData);

                    logger.info("Successfully enhanced FY{} with DOT/BTS data", fiscalYear);
                }

            } catch (Exception e) {
                logger.warn("Failed to fetch DOT/BTS data for FY{}: {}", fiscalYear, e.getMessage());
            }
        }
    }

    /**
     * Merge operational data from DOT with existing data
     * Priority: Existing (10-K) > DOT/BTS (only fill gaps)
     */
    private void mergeOperationalData(YearlyFinancialData yearlyData, AggregatedDOTData dotData) {
        AirlineOperationalData opData = yearlyData.getOperationalData();
        if (opData == null) {
            opData = new AirlineOperationalData();
            opData.setFiscalYear(yearlyData.getFiscalYear());
            yearlyData.setOperationalData(opData);
        }

        DOTOperationalData dotOpData = dotData.getAnnualOperationalData();

        // Fill in missing operational metrics from DOT
        if (opData.getAvailableSeatMiles() == null && dotOpData.getAvailableSeatMiles() != null) {
            opData.setAvailableSeatMiles(dotOpData.getAvailableSeatMiles());
            logger.debug("Filled ASM from DOT: {}", dotOpData.getAvailableSeatMiles());
        }

        if (opData.getRevenuePassengerMiles() == null && dotOpData.getRevenuePassengerMiles() != null) {
            opData.setRevenuePassengerMiles(dotOpData.getRevenuePassengerMiles());
            logger.debug("Filled RPM from DOT: {}", dotOpData.getRevenuePassengerMiles());
        }

        if (opData.getLoadFactor() == null && dotOpData.getPassengerLoadFactor() != null) {
            opData.setLoadFactor(dotOpData.getPassengerLoadFactor());
            logger.debug("Filled Load Factor from DOT: {}%", dotOpData.getPassengerLoadFactor());
        }

        if (opData.getPassengersCarried() == null && dotOpData.getRevenuePassengers() != null) {
            opData.setPassengersCarried(dotOpData.getRevenuePassengers());
            logger.debug("Filled Passengers from DOT: {}", dotOpData.getRevenuePassengers());
        }

        if (opData.getCargoTonMiles() == null && dotOpData.getCargoTonMiles() != null) {
            opData.setCargoTonMiles(dotOpData.getCargoTonMiles());
            logger.debug("Filled CTM from DOT: {}", dotOpData.getCargoTonMiles());
        }

        if (opData.getAvailableTonMiles() == null && dotOpData.getAvailableTonMiles() != null) {
            opData.setAvailableTonMiles(dotOpData.getAvailableTonMiles());
            logger.debug("Filled ATM from DOT: {}", dotOpData.getAvailableTonMiles());
        }

        if (opData.getDeparturesPerformed() == null && dotOpData.getDeparturesPerformed() != null) {
            opData.setDeparturesPerformed(new BigDecimal(dotOpData.getDeparturesPerformed()));
            logger.debug("Filled Departures from DOT: {}", dotOpData.getDeparturesPerformed());
        }

        // Store DOT data reference for detailed analysis
        if (yearlyData instanceof YearlyFinancialDataExtended) {
            ((YearlyFinancialDataExtended) yearlyData).setDotData(dotData);
        }
    }

    /**
     * Add performance data from DOT (not typically in 10-K)
     * This is NEW data, not merging
     */
    private void addPerformanceData(YearlyFinancialData yearlyData, AggregatedDOTData dotData) {
        DOTOnTimePerformance perfData = dotData.getAnnualPerformanceData();

        if (perfData == null) {
            logger.debug("No performance data available from DOT");
            return;
        }

        // Store performance data in extended model
        if (yearlyData instanceof YearlyFinancialDataExtended) {
            ((YearlyFinancialDataExtended) yearlyData).setPerformanceData(perfData);
            logger.info("Added on-time performance data: {}% on-time, {}% cancelled",
                    perfData.getOnTimePercentage(), perfData.getCancellationRate());
        } else {
            logger.warn("YearlyFinancialData not extended, cannot store performance data");
        }
    }

    /**
     * Enhance fleet data with DOT fleet inventory
     */
    private void enhanceFleetData(YearlyFinancialData yearlyData, AggregatedDOTData dotData) {
        AirlineOperationalData opData = yearlyData.getOperationalData();
        if (opData == null) {
            return;
        }

        int fleetSize = dotData.getFleetSize();
        if (fleetSize > 0) {
            if (opData.getFleetSize() == null) {
                opData.setFleetSize(fleetSize);
                logger.debug("Filled Fleet Size from DOT: {}", fleetSize);
            }

            if (opData.getFleetComposition() == null) {
                String composition = dotData.getFleetCompositionString();
                if (composition != null && !composition.isEmpty()) {
                    opData.setFleetComposition(composition);
                    logger.debug("Filled Fleet Composition from DOT: {}", composition);
                }
            }

            if (opData.getAverageFleetAge() == null) {
                Double avgAge = dotData.getAverageFleetAge();
                if (avgAge != null && avgAge > 0) {
                    opData.setAverageFleetAge(avgAge.intValue());
                    logger.debug("Filled Average Fleet Age from DOT: {} years", avgAge.intValue());
                }
            }
        }
    }

    /**
     * Validate and cross-check data between sources
     * Logs warnings if significant discrepancies are found
     */
    public void validateDataConsistency(CompanyFinancialData companyData) {
        logger.info("Validating data consistency across sources");

        for (YearlyFinancialData yearlyData : companyData.getYearlyData()) {
            AirlineOperationalData opData = yearlyData.getOperationalData();
            if (opData == null) {
                continue;
            }

            // Validate load factor calculation
            if (opData.getLoadFactor() != null && opData.getAvailableSeatMiles() != null
                && opData.getRevenuePassengerMiles() != null) {

                BigDecimal calculatedLF = opData.getRevenuePassengerMiles()
                    .divide(opData.getAvailableSeatMiles(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));

                BigDecimal reportedLF = opData.getLoadFactor();
                BigDecimal diff = calculatedLF.subtract(reportedLF).abs();

                if (diff.compareTo(new BigDecimal("2.0")) > 0) {
                    logger.warn("FY{}: Load factor discrepancy - Calculated: {}%, Reported: {}%",
                            yearlyData.getFiscalYear(), calculatedLF.setScale(2, BigDecimal.ROUND_HALF_UP),
                            reportedLF.setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }

            // Validate revenue vs passengers (basic sanity check)
            IncomeStatement income = yearlyData.getIncomeStatement();
            if (income != null && income.getTotalRevenue() != null
                && opData.getPassengersCarried() != null && opData.getPassengersCarried() > 0) {

                // Average revenue per passenger should be reasonable ($50-$500)
                BigDecimal revenuePerPax = income.getTotalRevenue()
                    .divide(new BigDecimal(opData.getPassengersCarried()), 2, BigDecimal.ROUND_HALF_UP);

                if (revenuePerPax.compareTo(new BigDecimal("50")) < 0
                    || revenuePerPax.compareTo(new BigDecimal("1000")) > 0) {
                    logger.warn("FY{}: Unusual revenue per passenger: ${} (total revenue: {}, passengers: {})",
                            yearlyData.getFiscalYear(), revenuePerPax,
                            income.getTotalRevenue(), opData.getPassengersCarried());
                }
            }
        }
    }
}
