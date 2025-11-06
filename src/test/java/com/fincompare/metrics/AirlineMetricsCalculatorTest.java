package com.fincompare.metrics;

import com.fincompare.models.AirlineOperationalData;
import com.fincompare.models.IncomeStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AirlineMetricsCalculatorTest {

    private AirlineMetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AirlineMetricsCalculator();
    }

    @Test
    void testCalculateLoadFactor() {
        // Arrange
        AirlineOperationalData data = new AirlineOperationalData();
        // ASM stored in millions: 100,000 million = 100 billion actual seat miles
        data.setAvailableSeatMiles(new BigDecimal("100000")); // 100,000 million ASM
        data.setRevenuePassengerMiles(new BigDecimal("85000")); // 85,000 million RPM

        // Act
        calculator.calculateAirlineMetrics(data, null);

        // Assert
        assertNotNull(data.getPassengerLoadFactor());
        assertEquals(new BigDecimal("85.00"), data.getPassengerLoadFactor().setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void testCalculateRASM() {
        // Arrange
        AirlineOperationalData data = new AirlineOperationalData();
        // ASM stored in millions: 100,000 million = 100 billion actual
        data.setAvailableSeatMiles(new BigDecimal("100000")); // 100,000 million ASM

        IncomeStatement income = new IncomeStatement();
        income.setTotalRevenue(new BigDecimal("15000000000")); // $15B revenue

        // Act
        calculator.calculateAirlineMetrics(data, income);

        // Assert
        assertNotNull(data.getRasm());
        // RASM = (Revenue * 100) / (ASM_millions * 1,000,000)
        // RASM = (15,000,000,000 * 100) / (100,000 * 1,000,000) = 1,500,000,000,000 / 100,000,000,000 = 15 cents
        assertEquals(new BigDecimal("15.00"), data.getRasm().setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void testCalculateCASM() {
        // Arrange
        AirlineOperationalData data = new AirlineOperationalData();
        // ASM stored in millions: 100,000 million = 100 billion actual
        data.setAvailableSeatMiles(new BigDecimal("100000")); // 100,000 million ASM

        IncomeStatement income = new IncomeStatement();
        income.setOperatingExpenses(new BigDecimal("14000000000")); // $14B expenses
        income.setFuelCosts(new BigDecimal("4000000000")); // $4B fuel

        // Act
        calculator.calculateAirlineMetrics(data, income);

        // Assert
        assertNotNull(data.getCasm());
        // CASM = (Expenses * 100) / (ASM_millions * 1,000,000) = (14B * 100) / 100B = 14 cents
        assertEquals(new BigDecimal("14.00"), data.getCasm().setScale(2, BigDecimal.ROUND_HALF_UP));

        // CASM-ex = ((Expenses - Fuel) * 100) / (ASM_millions * 1,000,000) = (10B * 100) / 100B = 10 cents
        assertNotNull(data.getCasmEx());
        assertEquals(new BigDecimal("10.00"), data.getCasmEx().setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void testCalculateBreakEvenLoadFactor() {
        // Arrange
        AirlineOperationalData data = new AirlineOperationalData();
        // ASM stored in millions: 100,000 million = 100 billion actual
        data.setAvailableSeatMiles(new BigDecimal("100000")); // 100,000 million ASM

        IncomeStatement income = new IncomeStatement();
        income.setTotalRevenue(new BigDecimal("15000000000")); // RASM = 15 cents
        income.setOperatingExpenses(new BigDecimal("12000000000")); // CASM = 12 cents

        // Act
        calculator.calculateAirlineMetrics(data, income);

        // Assert
        assertNotNull(data.getBreakEvenLoadFactor());
        // Break-even = (CASM / RASM) * 100 = (12 / 15) * 100 = 80%
        assertEquals(new BigDecimal("80.00"), data.getBreakEvenLoadFactor().setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    void testValidateMetrics() {
        // Arrange - Valid metrics
        AirlineOperationalData validData = new AirlineOperationalData();
        validData.setPassengerLoadFactor(new BigDecimal("85.0"));
        validData.setRasm(new BigDecimal("15.0"));
        validData.setCasm(new BigDecimal("12.0"));
        validData.setBreakEvenLoadFactor(new BigDecimal("80.0"));

        // Act & Assert
        assertTrue(calculator.validateMetrics(validData));

        // Arrange - Invalid load factor (>150% - extreme value indicating data quality issues)
        AirlineOperationalData invalidData = new AirlineOperationalData();
        invalidData.setBreakEvenLoadFactor(new BigDecimal("200.0")); // >150% triggers validation failure

        // Act & Assert
        assertFalse(calculator.validateMetrics(invalidData));

        // Arrange - Load factor slightly >100% is valid (airline can't break even but data is OK)
        AirlineOperationalData concerningButValidData = new AirlineOperationalData();
        concerningButValidData.setBreakEvenLoadFactor(new BigDecimal("105.0")); // >100% but <150% is valid

        // Act & Assert
        assertTrue(calculator.validateMetrics(concerningButValidData));
    }

    @Test
    void testHandleNullValues() {
        // Arrange
        AirlineOperationalData data = new AirlineOperationalData();
        data.setAvailableSeatMiles(null); // No ASM data

        // Act - should not throw exception
        assertDoesNotThrow(() -> calculator.calculateAirlineMetrics(data, null));

        // Assert - metrics should remain null
        assertNull(data.getPassengerLoadFactor());
        assertNull(data.getRasm());
    }
}
