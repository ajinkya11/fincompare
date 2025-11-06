package com.fincompare.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fincompare.models.AggregatedDOTData;
import com.fincompare.models.DOTOnTimePerformance;
import com.fincompare.models.DOTOperationalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for DOT/BTS API responses
 * Converts JSON data into domain models
 */
@Service
public class DOTDataParser {
    private static final Logger logger = LoggerFactory.getLogger(DOTDataParser.class);

    private final BTSApiClient btsApiClient;

    public DOTDataParser(BTSApiClient btsApiClient) {
        this.btsApiClient = btsApiClient;
    }

    /**
     * Fetch and aggregate all DOT data for a specific airline and year
     */
    public AggregatedDOTData fetchAggregatedData(String ticker, String fiscalYear) {
        logger.info("Fetching aggregated DOT data for {} - {}", ticker, fiscalYear);

        String carrierCode = btsApiClient.getCarrierCodeFromTicker(ticker);
        int year = Integer.parseInt(fiscalYear);

        AggregatedDOTData aggregatedData = new AggregatedDOTData(carrierCode, fiscalYear);

        // Fetch monthly data for the entire year
        for (int month = 1; month <= 12; month++) {
            try {
                // Fetch T-100 operational data
                DOTOperationalData opData = parseT100Data(carrierCode, year, month);
                if (opData != null) {
                    aggregatedData.addMonthlyOperationalData(opData);
                }

                // Fetch on-time performance data
                DOTOnTimePerformance perfData = parseOnTimePerformanceData(carrierCode, year, month);
                if (perfData != null) {
                    aggregatedData.addMonthlyPerformanceData(perfData);
                }

            } catch (Exception e) {
                logger.warn("Failed to fetch DOT data for {}/{}: {}", year, month, e.getMessage());
            }
        }

        // Aggregate monthly data to annual
        aggregatedData.setAnnualOperationalData(aggregateOperationalData(aggregatedData.getMonthlyOperationalData()));
        aggregatedData.setAnnualPerformanceData(aggregatePerformanceData(aggregatedData.getMonthlyPerformanceData()));

        logger.info("Successfully fetched {} months of operational data and {} months of performance data",
                aggregatedData.getMonthlyOperationalData().size(),
                aggregatedData.getMonthlyPerformanceData().size());

        return aggregatedData;
    }

    /**
     * Parse T-100 data (both domestic and international combined)
     */
    public DOTOperationalData parseT100Data(String carrierCode, int year, int month) {
        logger.debug("Parsing T-100 data for {} - {}/{}", carrierCode, year, month);

        DOTOperationalData opData = new DOTOperationalData(carrierCode, YearMonth.of(year, month));

        try {
            // Fetch domestic data
            JsonNode domesticData = btsApiClient.fetchT100DomesticData(carrierCode, year, month);
            parseT100Response(domesticData, opData, true);

            // Fetch international data
            JsonNode intlData = btsApiClient.fetchT100InternationalData(carrierCode, year, month);
            parseT100Response(intlData, opData, false);

            // Calculate derived metrics
            opData.calculateLoadFactor();
            opData.calculateCargoTonMiles();

            return opData;

        } catch (Exception e) {
            logger.error("Error parsing T-100 data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parse T-100 JSON response
     */
    private void parseT100Response(JsonNode response, DOTOperationalData opData, boolean isDomestic) {
        if (response == null || response.isEmpty()) {
            logger.debug("Empty T-100 response for {} market", isDomestic ? "domestic" : "international");
            return;
        }

        // BTS API returns data in "data" array
        JsonNode dataArray = response.path("data");
        if (dataArray.isEmpty()) {
            logger.debug("No data array in T-100 response");
            return;
        }

        // Aggregate data across all records (routes/segments)
        long totalPassengers = 0;
        double totalRPM = 0;
        double totalASM = 0;
        double totalFreightTonMiles = 0;
        double totalMailTonMiles = 0;
        int totalDepartures = 0;

        for (JsonNode record : dataArray) {
            // Aggregate passengers
            totalPassengers += record.path("PASSENGERS").asLong(0);

            // Aggregate RPM and ASM (in millions typically)
            totalRPM += record.path("RPM").asDouble(0);
            totalASM += record.path("ASM").asDouble(0);

            // Aggregate cargo
            totalFreightTonMiles += record.path("FREIGHT_TON_MILES").asDouble(0);
            totalMailTonMiles += record.path("MAIL_TON_MILES").asDouble(0);

            // Aggregate departures
            totalDepartures += record.path("DEPARTURES_PERFORMED").asInt(0);
        }

        // Add to operational data (combine domestic and international)
        if (opData.getRevenuePassengers() == null) {
            opData.setRevenuePassengers(totalPassengers);
        } else {
            opData.setRevenuePassengers(opData.getRevenuePassengers() + totalPassengers);
        }

        if (opData.getRevenuePassengerMiles() == null) {
            opData.setRevenuePassengerMiles(BigDecimal.valueOf(totalRPM));
        } else {
            opData.setRevenuePassengerMiles(
                opData.getRevenuePassengerMiles().add(BigDecimal.valueOf(totalRPM))
            );
        }

        if (opData.getAvailableSeatMiles() == null) {
            opData.setAvailableSeatMiles(BigDecimal.valueOf(totalASM));
        } else {
            opData.setAvailableSeatMiles(
                opData.getAvailableSeatMiles().add(BigDecimal.valueOf(totalASM))
            );
        }

        if (opData.getFreightTonMiles() == null) {
            opData.setFreightTonMiles(BigDecimal.valueOf(totalFreightTonMiles));
        } else {
            opData.setFreightTonMiles(
                opData.getFreightTonMiles().add(BigDecimal.valueOf(totalFreightTonMiles))
            );
        }

        if (opData.getMailTonMiles() == null) {
            opData.setMailTonMiles(BigDecimal.valueOf(totalMailTonMiles));
        } else {
            opData.setMailTonMiles(
                opData.getMailTonMiles().add(BigDecimal.valueOf(totalMailTonMiles))
            );
        }

        if (opData.getDeparturesPerformed() == null) {
            opData.setDeparturesPerformed(totalDepartures);
        } else {
            opData.setDeparturesPerformed(opData.getDeparturesPerformed() + totalDepartures);
        }

        // Store domestic/international breakdowns
        if (isDomestic) {
            opData.setDomesticPassengers(totalPassengers);
            opData.setDomesticASM(BigDecimal.valueOf(totalASM));
        } else {
            opData.setInternationalPassengers(totalPassengers);
            opData.setInternationalASM(BigDecimal.valueOf(totalASM));
        }

        logger.debug("Parsed {} T-100 data - Passengers: {}, ASM: {}, RPM: {}",
                isDomestic ? "domestic" : "international", totalPassengers, totalASM, totalRPM);
    }

    /**
     * Parse On-Time Performance data
     */
    public DOTOnTimePerformance parseOnTimePerformanceData(String carrierCode, int year, int month) {
        logger.debug("Parsing On-Time Performance data for {} - {}/{}", carrierCode, year, month);

        DOTOnTimePerformance perfData = new DOTOnTimePerformance(carrierCode, YearMonth.of(year, month));

        try {
            JsonNode response = btsApiClient.fetchOnTimePerformanceData(carrierCode, year, month);

            if (response == null || response.isEmpty()) {
                logger.debug("Empty On-Time Performance response");
                return null;
            }

            JsonNode dataArray = response.path("data");
            if (dataArray.isEmpty()) {
                logger.debug("No data array in On-Time Performance response");
                return null;
            }

            // Aggregate across all records (airports/routes)
            int totalFlights = 0;
            int onTimeFlights = 0;
            int delayed = 0;
            int cancelled = 0;
            int diverted = 0;

            double totalCarrierDelay = 0;
            double totalWeatherDelay = 0;
            double totalNASDelay = 0;
            double totalSecurityDelay = 0;
            double totalLateAircraftDelay = 0;

            double totalArrDelay = 0;
            double totalDepDelay = 0;

            for (JsonNode record : dataArray) {
                totalFlights += record.path("ARR_FLIGHTS").asInt(0);
                onTimeFlights += record.path("ARR_DEL15").asInt(0); // Flights delayed 15+ min
                cancelled += record.path("CANCELLED").asInt(0);
                diverted += record.path("DIVERTED").asInt(0);

                totalCarrierDelay += record.path("CARRIER_DELAY").asDouble(0);
                totalWeatherDelay += record.path("WEATHER_DELAY").asDouble(0);
                totalNASDelay += record.path("NAS_DELAY").asDouble(0);
                totalSecurityDelay += record.path("SECURITY_DELAY").asDouble(0);
                totalLateAircraftDelay += record.path("LATE_AIRCRAFT_DELAY").asDouble(0);

                totalArrDelay += record.path("ARR_DELAY").asDouble(0);
                totalDepDelay += record.path("DEP_DELAY").asDouble(0);
            }

            // On-time = total - delayed (ARR_DEL15 is delayed 15+ min)
            perfData.setTotalFlights(totalFlights);
            perfData.setOnTimeFlights(totalFlights - onTimeFlights);
            perfData.setDelayedFlights(onTimeFlights);
            perfData.setCancelledFlights(cancelled);
            perfData.setDivertedFlights(diverted);

            perfData.setCarrierDelayMinutes(BigDecimal.valueOf(totalCarrierDelay));
            perfData.setWeatherDelayMinutes(BigDecimal.valueOf(totalWeatherDelay));
            perfData.setNasDelayMinutes(BigDecimal.valueOf(totalNASDelay));
            perfData.setSecurityDelayMinutes(BigDecimal.valueOf(totalSecurityDelay));
            perfData.setLateAircraftDelayMinutes(BigDecimal.valueOf(totalLateAircraftDelay));

            if (totalFlights > 0) {
                perfData.setAverageArrivalDelay(BigDecimal.valueOf(totalArrDelay / totalFlights));
                perfData.setAverageDepartureDelay(BigDecimal.valueOf(totalDepDelay / totalFlights));
            }

            // Calculate performance metrics
            perfData.calculatePerformanceMetrics();

            logger.debug("Parsed On-Time Performance - Flights: {}, On-Time %: {}",
                    totalFlights, perfData.getOnTimePercentage());

            return perfData;

        } catch (Exception e) {
            logger.error("Error parsing On-Time Performance data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Aggregate monthly operational data to annual
     */
    private DOTOperationalData aggregateOperationalData(List<DOTOperationalData> monthlyData) {
        if (monthlyData.isEmpty()) {
            return null;
        }

        DOTOperationalData annual = new DOTOperationalData();
        annual.setAirlineCode(monthlyData.get(0).getAirlineCode());

        long totalPassengers = 0;
        BigDecimal totalRPM = BigDecimal.ZERO;
        BigDecimal totalASM = BigDecimal.ZERO;
        BigDecimal totalFreightTM = BigDecimal.ZERO;
        BigDecimal totalMailTM = BigDecimal.ZERO;
        int totalDepartures = 0;

        for (DOTOperationalData monthly : monthlyData) {
            if (monthly.getRevenuePassengers() != null) {
                totalPassengers += monthly.getRevenuePassengers();
            }
            if (monthly.getRevenuePassengerMiles() != null) {
                totalRPM = totalRPM.add(monthly.getRevenuePassengerMiles());
            }
            if (monthly.getAvailableSeatMiles() != null) {
                totalASM = totalASM.add(monthly.getAvailableSeatMiles());
            }
            if (monthly.getFreightTonMiles() != null) {
                totalFreightTM = totalFreightTM.add(monthly.getFreightTonMiles());
            }
            if (monthly.getMailTonMiles() != null) {
                totalMailTM = totalMailTM.add(monthly.getMailTonMiles());
            }
            if (monthly.getDeparturesPerformed() != null) {
                totalDepartures += monthly.getDeparturesPerformed();
            }
        }

        annual.setRevenuePassengers(totalPassengers);
        annual.setRevenuePassengerMiles(totalRPM);
        annual.setAvailableSeatMiles(totalASM);
        annual.setFreightTonMiles(totalFreightTM);
        annual.setMailTonMiles(totalMailTM);
        annual.setDeparturesPerformed(totalDepartures);

        annual.calculateLoadFactor();
        annual.calculateCargoTonMiles();

        return annual;
    }

    /**
     * Aggregate monthly performance data to annual
     */
    private DOTOnTimePerformance aggregatePerformanceData(List<DOTOnTimePerformance> monthlyData) {
        if (monthlyData.isEmpty()) {
            return null;
        }

        DOTOnTimePerformance annual = new DOTOnTimePerformance();
        annual.setAirlineCode(monthlyData.get(0).getAirlineCode());

        int totalFlights = 0;
        int onTimeFlights = 0;
        int cancelled = 0;
        int diverted = 0;
        BigDecimal totalCarrierDelay = BigDecimal.ZERO;
        BigDecimal totalWeatherDelay = BigDecimal.ZERO;
        BigDecimal totalNASDelay = BigDecimal.ZERO;

        for (DOTOnTimePerformance monthly : monthlyData) {
            if (monthly.getTotalFlights() != null) {
                totalFlights += monthly.getTotalFlights();
            }
            if (monthly.getOnTimeFlights() != null) {
                onTimeFlights += monthly.getOnTimeFlights();
            }
            if (monthly.getCancelledFlights() != null) {
                cancelled += monthly.getCancelledFlights();
            }
            if (monthly.getDivertedFlights() != null) {
                diverted += monthly.getDivertedFlights();
            }
            if (monthly.getCarrierDelayMinutes() != null) {
                totalCarrierDelay = totalCarrierDelay.add(monthly.getCarrierDelayMinutes());
            }
            if (monthly.getWeatherDelayMinutes() != null) {
                totalWeatherDelay = totalWeatherDelay.add(monthly.getWeatherDelayMinutes());
            }
            if (monthly.getNasDelayMinutes() != null) {
                totalNASDelay = totalNASDelay.add(monthly.getNasDelayMinutes());
            }
        }

        annual.setTotalFlights(totalFlights);
        annual.setOnTimeFlights(onTimeFlights);
        annual.setCancelledFlights(cancelled);
        annual.setDivertedFlights(diverted);
        annual.setCarrierDelayMinutes(totalCarrierDelay);
        annual.setWeatherDelayMinutes(totalWeatherDelay);
        annual.setNasDelayMinutes(totalNASDelay);

        annual.calculatePerformanceMetrics();

        return annual;
    }
}
