package com.fincompare.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Provides BTS T-100 data (domestic and international departures) for airlines.
 * Automatically fetches data from DOT's Socrata Open Data API (SODA).
 *
 * Data source: https://datahub.transportation.gov/
 * API documentation: https://dev.socrata.com/
 */
@Service
public class T100DataProvider {
    private static final Logger logger = LoggerFactory.getLogger(T100DataProvider.class);
    private final BTST100Client btst100Client;

    @Autowired
    public T100DataProvider(BTST100Client btst100Client) {
        this.btst100Client = btst100Client;
        logger.info("T100DataProvider initialized with automated BTS T-100 API client");
    }

    /**
     * Get domestic departures for a carrier and fiscal year from BTS T-100 data
     *
     * @param carrierCode ICAO carrier code (e.g., "UAL", "JBLU")
     * @param fiscalYear Fiscal year (e.g., "2024", "2023")
     * @return Number of domestic departures, or null if not available
     */
    public BigDecimal getDomesticDepartures(String carrierCode, String fiscalYear) {
        try {
            return btst100Client.getDomesticDepartures(carrierCode, fiscalYear);
        } catch (Exception e) {
            logger.error("Error fetching domestic departures for {} FY{}: {}",
                carrierCode, fiscalYear, e.getMessage());
            return null;
        }
    }

    /**
     * Get international departures for a carrier and fiscal year from BTS T-100 data
     *
     * @param carrierCode ICAO carrier code (e.g., "UAL", "JBLU")
     * @param fiscalYear Fiscal year (e.g., "2024", "2023")
     * @return Number of international departures, or null if not available
     */
    public BigDecimal getInternationalDepartures(String carrierCode, String fiscalYear) {
        try {
            return btst100Client.getInternationalDepartures(carrierCode, fiscalYear);
        } catch (Exception e) {
            logger.error("Error fetching international departures for {} FY{}: {}",
                carrierCode, fiscalYear, e.getMessage());
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

    /**
     * Check if the BTS T-100 API is accessible
     */
    public boolean isApiAvailable() {
        return btst100Client.isAvailable();
    }
}
