package com.fincompare.metrics;

import com.fincompare.models.AirlineOperationalData;
import com.fincompare.models.IncomeStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class AirlineMetricsCalculator {
    private static final Logger logger = LoggerFactory.getLogger(AirlineMetricsCalculator.class);
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * Calculate airline-specific operational metrics
     */
    public void calculateAirlineMetrics(AirlineOperationalData operationalData, IncomeStatement incomeStatement) {
        if (operationalData == null) {
            logger.warn("No operational data available for airline metrics calculation");
            return;
        }

        logger.info("Calculating airline-specific metrics for fiscal year: {}", operationalData.getFiscalYear());

        calculateLoadFactors(operationalData);
        calculateUnitMetrics(operationalData, incomeStatement);
        calculateYields(operationalData, incomeStatement);
        calculateBreakEvenLoadFactor(operationalData, incomeStatement);
    }

    /**
     * Calculate load factors (only if not already extracted from 10-K)
     */
    private void calculateLoadFactors(AirlineOperationalData data) {
        // Passenger Load Factor = RPM / ASM * 100
        // Only calculate if not already set (extracted from 10-K takes precedence)
        if (data.getPassengerLoadFactor() == null &&
                data.getRevenuePassengerMiles() != null && data.getAvailableSeatMiles() != null &&
                data.getAvailableSeatMiles().compareTo(ZERO) > 0) {
            BigDecimal loadFactor = data.getRevenuePassengerMiles()
                    .divide(data.getAvailableSeatMiles(), MC)
                    .multiply(HUNDRED);
            data.setPassengerLoadFactor(loadFactor);
            logger.debug("Calculated Passenger Load Factor: {}%", loadFactor);
        }

        // Cargo Load Factor (if cargo data available)
        if (data.getCargoTonMiles() != null && data.getAvailableTonMiles() != null &&
                data.getAvailableTonMiles().compareTo(ZERO) > 0) {
            BigDecimal cargoLoadFactor = data.getCargoTonMiles()
                    .divide(data.getAvailableTonMiles(), MC)
                    .multiply(HUNDRED);
            data.setCargoLoadFactor(cargoLoadFactor);
        }
    }

    /**
     * Calculate unit revenue and cost metrics (RASM, CASM)
     */
    private void calculateUnitMetrics(AirlineOperationalData data, IncomeStatement income) {
        if (data.getAvailableSeatMiles() == null || data.getAvailableSeatMiles().compareTo(ZERO) <= 0) {
            logger.warn("ASM not available or zero, cannot calculate unit metrics");
            return;
        }

        // ASM is stored in millions, need to convert to actual seat miles
        BigDecimal asmMillions = data.getAvailableSeatMiles();
        BigDecimal asmActual = asmMillions.multiply(new BigDecimal("1000000")); // Convert millions to actual

        if (income != null) {
            // RASM = Total Revenue / ASM (in cents)
            // Revenue is in dollars, ASM is in actual seat miles
            if (income.getTotalRevenue() != null) {
                BigDecimal rasm = income.getTotalRevenue()
                        .multiply(HUNDRED) // Convert to cents
                        .divide(asmActual, MC);
                data.setRasm(rasm);
                logger.debug("RASM: {} cents", rasm);
            }

            // CASM = Operating Expenses / ASM (in cents)
            if (income.getOperatingExpenses() != null) {
                BigDecimal casm = income.getOperatingExpenses()
                        .multiply(HUNDRED)
                        .divide(asmActual, MC);
                data.setCasm(casm);
                logger.debug("CASM: {} cents", casm);

                // CASM-ex (excluding fuel)
                if (income.getFuelCosts() != null) {
                    BigDecimal opExExFuel = income.getOperatingExpenses().subtract(income.getFuelCosts());
                    BigDecimal casmEx = opExExFuel
                            .multiply(HUNDRED)
                            .divide(asmActual, MC);
                    data.setCasmEx(casmEx);
                    logger.debug("CASM-ex (excluding fuel): {} cents", casmEx);
                }
            }
        }
    }

    /**
     * Calculate yield metrics
     */
    private void calculateYields(AirlineOperationalData data, IncomeStatement income) {
        if (income == null) {
            return;
        }

        // Passenger Yield = Passenger Revenue / RPM (in cents)
        // RPM is stored in millions, need to convert to actual
        if (income.getPassengerRevenue() != null && data.getRevenuePassengerMiles() != null &&
                data.getRevenuePassengerMiles().compareTo(ZERO) > 0) {
            BigDecimal rpmActual = data.getRevenuePassengerMiles().multiply(new BigDecimal("1000000"));
            BigDecimal passengerYield = income.getPassengerRevenue()
                    .multiply(HUNDRED)
                    .divide(rpmActual, MC);
            data.setPassengerYield(passengerYield);
            logger.debug("Passenger Yield: {} cents per RPM", passengerYield);
        }

        // Cargo Yield = Cargo Revenue / Cargo Ton Miles (in cents)
        // CargoTonMiles is also stored in millions
        if (income.getCargoRevenue() != null && data.getCargoTonMiles() != null &&
                data.getCargoTonMiles().compareTo(ZERO) > 0) {
            BigDecimal cargoTonMilesActual = data.getCargoTonMiles().multiply(new BigDecimal("1000000"));
            BigDecimal cargoYield = income.getCargoRevenue()
                    .multiply(HUNDRED)
                    .divide(cargoTonMilesActual, MC);
            data.setCargoYield(cargoYield);
        }
    }

    /**
     * Calculate break-even load factor
     * Break-even Load Factor = (Operating Expenses / (Passenger Yield * ASM)) * 100
     * Simplified: CASM / RASM * 100
     */
    private void calculateBreakEvenLoadFactor(AirlineOperationalData data, IncomeStatement income) {
        if (data.getCasm() != null && data.getRasm() != null && data.getRasm().compareTo(ZERO) > 0) {
            BigDecimal breakEven = data.getCasm()
                    .divide(data.getRasm(), MC)
                    .multiply(HUNDRED);
            data.setBreakEvenLoadFactor(breakEven);
            logger.debug("Break-even Load Factor: {}%", breakEven);
        }
    }

    /**
     * Calculate average stage length if departures and RPM are available
     */
    public void calculateAverageStageLength(AirlineOperationalData data) {
        if (data.getRevenuePassengerMiles() != null && data.getDeparturesPerformed() != null &&
                data.getDeparturesPerformed().compareTo(ZERO) > 0 && data.getPassengersCarried() != null &&
                data.getPassengersCarried() > 0) {
            // Average Stage Length = RPM / Passengers Carried
            BigDecimal avgStageLength = data.getRevenuePassengerMiles()
                    .divide(new BigDecimal(data.getPassengersCarried()), MC);
            data.setAverageStageLength(avgStageLength);
        }
    }

    /**
     * Validate calculated metrics for reasonableness
     */
    public boolean validateMetrics(AirlineOperationalData data) {
        boolean valid = true;

        // Load factor should be between 0 and 100
        if (data.getPassengerLoadFactor() != null) {
            BigDecimal lf = data.getPassengerLoadFactor();
            if (lf.compareTo(ZERO) < 0 || lf.compareTo(HUNDRED) > 0) {
                logger.warn("Invalid passenger load factor: {}%. Should be between 0 and 100.", lf);
                valid = false;
            }
        }

        // RASM should be positive and reasonable (typically 10-20 cents)
        if (data.getRasm() != null && data.getRasm().compareTo(ZERO) <= 0) {
            logger.warn("Invalid RASM: {}. Should be positive.", data.getRasm());
            valid = false;
        }

        // CASM should be positive and reasonable (typically 12-18 cents)
        if (data.getCasm() != null && data.getCasm().compareTo(ZERO) <= 0) {
            logger.warn("Invalid CASM: {}. Should be positive.", data.getCasm());
            valid = false;
        }

        // Break-even load factor should be reasonable (typically 70-95%)
        // Values >100% are possible (airline can't break even) but >150% likely indicates bad data
        if (data.getBreakEvenLoadFactor() != null) {
            BigDecimal belf = data.getBreakEvenLoadFactor();
            if (belf.compareTo(ZERO) < 0) {
                logger.warn("Invalid break-even load factor: {}%. Should be positive.", belf);
                valid = false;
            } else if (belf.compareTo(new BigDecimal("150")) > 0) {
                logger.warn("Suspicious break-even load factor: {}%. Values >150% likely indicate data quality issues.", belf);
                valid = false;
            } else if (belf.compareTo(HUNDRED) > 0) {
                logger.info("Break-even load factor: {}%. Value >100% means airline cannot break even at full capacity.", belf);
            }
        }

        return valid;
    }
}
