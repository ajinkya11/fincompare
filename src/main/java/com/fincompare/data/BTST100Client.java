package com.fincompare.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for fetching BTS T-100 data from DOT's Socrata Open Data API (SODA)
 *
 * NOTE: Currently configured with placeholder dataset IDs. To enable:
 * 1. Visit https://datahub.transportation.gov/ -> Aviation section
 * 2. Find current T-100 Domestic and International Segment datasets
 * 3. Click Export -> API to get the dataset ID (4-character code like "a1b2-c3d4")
 * 4. Update application.properties with the correct dataset IDs
 *
 * Data source: https://datahub.transportation.gov/
 * API documentation: https://dev.socrata.com/
 */
@Service
public class BTST100Client {
    private static final Logger logger = LoggerFactory.getLogger(BTST100Client.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${bts.t100.api.base-url}")
    private String baseUrl;

    @Value("${bts.t100.api.domestic-dataset-id}")
    private String domesticDatasetId;

    @Value("${bts.t100.api.international-dataset-id}")
    private String internationalDatasetId;

    @Value("${bts.t100.api.timeout-seconds}")
    private int timeoutSeconds;

    @Value("${bts.t100.api.enabled:true}")
    private boolean apiEnabled;

    // Carrier code mapping (IATA/ICAO to carrier names in BTS data)
    private static final Map<String, String> CARRIER_NAMES = new HashMap<>();
    static {
        CARRIER_NAMES.put("UAL", "United Air Lines Inc.");
        CARRIER_NAMES.put("JBLU", "JetBlue Airways");
        CARRIER_NAMES.put("DAL", "Delta Air Lines Inc.");
        CARRIER_NAMES.put("AAL", "American Airlines Inc.");
        CARRIER_NAMES.put("LUV", "Southwest Airlines Co.");
    }

    public BTST100Client() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch domestic departures for a carrier and year from BTS T-100 data
     *
     * @param carrierCode ICAO carrier code (e.g., "UAL", "JBLU")
     * @param year Calendar year (e.g., "2024")
     * @return Total domestic departures performed, or null if unavailable
     */
    public BigDecimal getDomesticDepartures(String carrierCode, String year) {
        if (!apiEnabled) {
            logger.debug("BTS T-100 API is disabled in configuration");
            return null;
        }

        String carrierName = CARRIER_NAMES.get(carrierCode);
        if (carrierName == null) {
            logger.warn("Unknown carrier code: {}. Add mapping to CARRIER_NAMES.", carrierCode);
            return null;
        }

        try {
            // Build SODA query to sum departures_performed for the carrier and year
            // Query format: ?carrier_name=...&year=...&$select=sum(departures_performed)
            String query = String.format(
                "?$where=year=%s&unique_carrier_name='%s'&$select=sum(departures_performed) as total",
                year,
                URLEncoder.encode(carrierName, StandardCharsets.UTF_8)
            );

            String url = String.format("%s/%s.json%s", baseUrl, domesticDatasetId, query);

            logger.info("Fetching domestic T-100 data from BTS for {} year {}", carrierCode, year);
            logger.debug("Query URL: {}", url);

            String response = fetchData(url);
            return parseDeparturesSum(response);

        } catch (Exception e) {
            logger.error("Error fetching domestic T-100 data for {} year {}: {}",
                carrierCode, year, e.getMessage());
            return null;
        }
    }

    /**
     * Fetch international departures for a carrier and year from BTS T-100 data
     */
    public BigDecimal getInternationalDepartures(String carrierCode, String year) {
        if (!apiEnabled) {
            logger.debug("BTS T-100 API is disabled in configuration");
            return null;
        }

        String carrierName = CARRIER_NAMES.get(carrierCode);
        if (carrierName == null) {
            logger.warn("Unknown carrier code: {}", carrierCode);
            return null;
        }

        try {
            String query = String.format(
                "?$where=year=%s&unique_carrier_name='%s'&$select=sum(departures_performed) as total",
                year,
                URLEncoder.encode(carrierName, StandardCharsets.UTF_8)
            );

            String url = String.format("%s/%s.json%s", baseUrl, internationalDatasetId, query);

            logger.info("Fetching international T-100 data from BTS for {} year {}", carrierCode, year);
            logger.debug("Query URL: {}", url);

            String response = fetchData(url);
            return parseDeparturesSum(response);

        } catch (Exception e) {
            logger.error("Error fetching international T-100 data for {} year {}: {}",
                carrierCode, year, e.getMessage());
            return null;
        }
    }

    /**
     * Fetch data from the SODA API endpoint
     */
    private String fetchData(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("User-Agent", "FinCompare/1.0 (Airline Financial Analysis Tool)")
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    /**
     * Parse the sum of departures from SODA API JSON response
     * Expected format: [{"total": "12345"}]
     */
    private BigDecimal parseDeparturesSum(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            if (root.isArray() && root.size() > 0) {
                JsonNode firstResult = root.get(0);
                if (firstResult.has("total")) {
                    String totalStr = firstResult.get("total").asText();
                    return new BigDecimal(totalStr);
                }
            }

            logger.warn("No departure total found in response: {}", jsonResponse);
            return null;

        } catch (Exception e) {
            logger.error("Error parsing T-100 response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if BTS T-100 API is reachable (health check)
     */
    public boolean isAvailable() {
        try {
            // Try a simple query to check if the API is reachable
            String testUrl = "https://data.transportation.gov/api/id/";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(testUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            logger.debug("BTS T-100 API not reachable: {}", e.getMessage());
            return false;
        }
    }
}
