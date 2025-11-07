package com.fincompare.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Provides BTS T-100 data (domestic and international departures) for airlines.
 * Data is loaded from t100-data.properties file which should be manually populated
 * with data downloaded from https://www.transtats.bts.gov/
 */
@Service
public class T100DataProvider {
    private static final Logger logger = LoggerFactory.getLogger(T100DataProvider.class);
    private final Properties t100Data;

    public T100DataProvider() {
        this.t100Data = new Properties();
        loadT100Data();
    }

    private void loadT100Data() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("t100-data.properties")) {
            if (input == null) {
                logger.warn("t100-data.properties file not found. T-100 departure data will not be available.");
                return;
            }
            t100Data.load(input);
            logger.info("Loaded T-100 data properties");
        } catch (IOException e) {
            logger.error("Error loading T-100 data: {}", e.getMessage());
        }
    }

    /**
     * Get domestic departures for a carrier and fiscal year from BTS T-100 data
     * @param carrierCode ICAO carrier code (e.g., "UAL", "JBLU")
     * @param fiscalYear Fiscal year (e.g., "2024", "2023")
     * @return Number of domestic departures, or null if not available
     */
    public BigDecimal getDomesticDepartures(String carrierCode, String fiscalYear) {
        String key = String.format("%s.%s.domestic.departures", carrierCode, fiscalYear);
        return getValueAsBigDecimal(key);
    }

    /**
     * Get international departures for a carrier and fiscal year from BTS T-100 data
     * @param carrierCode ICAO carrier code (e.g., "UAL", "JBLU")
     * @param fiscalYear Fiscal year (e.g., "2024", "2023")
     * @return Number of international departures, or null if not available
     */
    public BigDecimal getInternationalDepartures(String carrierCode, String fiscalYear) {
        String key = String.format("%s.%s.international.departures", carrierCode, fiscalYear);
        return getValueAsBigDecimal(key);
    }

    private BigDecimal getValueAsBigDecimal(String key) {
        String value = t100Data.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            logger.debug("No T-100 data found for key: {}", key);
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            logger.error("Invalid number format for key {}: {}", key, value);
            return null;
        }
    }

    /**
     * Check if T-100 data is available for a carrier and year
     */
    public boolean hasData(String carrierCode, String fiscalYear) {
        return getDomesticDepartures(carrierCode, fiscalYear) != null ||
               getInternationalDepartures(carrierCode, fiscalYear) != null;
    }
}
