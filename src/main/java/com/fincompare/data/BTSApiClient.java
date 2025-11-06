package com.fincompare.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
 * Client for Bureau of Transportation Statistics (BTS) API
 * Fetches airline operational and performance data from DOT databases
 */
@Service
public class BTSApiClient {
    private static final Logger logger = LoggerFactory.getLogger(BTSApiClient.class);

    private static final String BTS_BASE_URL = "https://www.transtats.bts.gov/api/v1";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${bts.api.key:}")
    private String apiKey; // Optional API key (BTS doesn't require one for most data)

    public BTSApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch T-100 Domestic Segment data (Form 41)
     * Contains monthly operational statistics for domestic routes
     *
     * @param carrierCode DOT carrier code (e.g., "UA" for United, "B6" for JetBlue)
     * @param year Year to fetch data for
     * @param month Month to fetch data for (1-12)
     * @return JSON response containing T-100 data
     */
    public JsonNode fetchT100DomesticData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Fetching T-100 Domestic data for carrier {} - {}/{}", carrierCode, year, month);

        Map<String, String> params = new HashMap<>();
        params.put("CARRIER", carrierCode);
        params.put("YEAR", String.valueOf(year));
        params.put("MONTH", String.valueOf(month));

        // T-100 Domestic Segment (All Carriers)
        // Database ID: T-100 Domestic Segment (All Carriers)
        String endpoint = "/Table_ID/293/Data"; // T-100 Domestic Market

        return makeApiRequest(endpoint, params);
    }

    /**
     * Fetch T-100 International Segment data
     * Contains monthly operational statistics for international routes
     */
    public JsonNode fetchT100InternationalData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Fetching T-100 International data for carrier {} - {}/{}", carrierCode, year, month);

        Map<String, String> params = new HashMap<>();
        params.put("CARRIER", carrierCode);
        params.put("YEAR", String.valueOf(year));
        params.put("MONTH", String.valueOf(month));

        String endpoint = "/Table_ID/259/Data"; // T-100 International Market

        return makeApiRequest(endpoint, params);
    }

    /**
     * Fetch On-Time Performance data
     * Contains arrival/departure delays, cancellations, etc.
     */
    public JsonNode fetchOnTimePerformanceData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Fetching On-Time Performance data for carrier {} - {}/{}", carrierCode, year, month);

        Map<String, String> params = new HashMap<>();
        params.put("CARRIER", carrierCode);
        params.put("YEAR", String.valueOf(year));
        params.put("MONTH", String.valueOf(month));

        String endpoint = "/Table_ID/236/Data"; // On-Time Performance

        return makeApiRequest(endpoint, params);
    }

    /**
     * Fetch airline financial data from Form 41
     * Contains revenue, expenses, and other financial metrics
     */
    public JsonNode fetchFinancialData(String carrierCode, int year, int quarter) throws IOException, InterruptedException {
        logger.info("Fetching Financial data for carrier {} - {} Q{}", carrierCode, year, quarter);

        Map<String, String> params = new HashMap<>();
        params.put("CARRIER", carrierCode);
        params.put("YEAR", String.valueOf(year));
        params.put("QUARTER", String.valueOf(quarter));

        String endpoint = "/Table_ID/298/Data"; // Air Carrier Financial Reports

        return makeApiRequest(endpoint, params);
    }

    /**
     * Fetch baggage handling data
     * Contains mishandled baggage reports
     */
    public JsonNode fetchBaggageData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Fetching Baggage data for carrier {} - {}/{}", carrierCode, year, month);

        Map<String, String> params = new HashMap<>();
        params.put("CARRIER", carrierCode);
        params.put("YEAR", String.valueOf(year));
        params.put("MONTH", String.valueOf(month));

        String endpoint = "/Table_ID/278/Data"; // Baggage Complaints

        return makeApiRequest(endpoint, params);
    }

    /**
     * Fetch consumer complaints data
     * Contains customer complaint statistics
     */
    public JsonNode fetchComplaintsData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Fetching Complaints data for carrier {} - {}/{}", carrierCode, year, month);

        Map<String, String> params = new HashMap<>();
        params.put("CARRIER", carrierCode);
        params.put("YEAR", String.valueOf(year));
        params.put("MONTH", String.valueOf(month));

        String endpoint = "/Table_ID/278/Data"; // Consumer Complaints

        return makeApiRequest(endpoint, params);
    }

    /**
     * Generic method to make API requests to BTS
     */
    private JsonNode makeApiRequest(String endpoint, Map<String, String> params) throws IOException, InterruptedException {
        // Build query string
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            queryString.append("=");
            queryString.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        // Add API key if available
        if (apiKey != null && !apiKey.isEmpty()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append("api_key=").append(apiKey);
        }

        String url = BTS_BASE_URL + endpoint + "?" + queryString;

        logger.debug("Making BTS API request: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("User-Agent", "FinCompare-Airline-Analyzer/1.0")
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readTree(response.body());
            } else if (response.statusCode() == 404) {
                logger.warn("No data found at BTS API: {}", url);
                return objectMapper.createObjectNode();
            } else if (response.statusCode() == 429) {
                logger.error("Rate limit exceeded for BTS API");
                throw new IOException("Rate limit exceeded");
            } else {
                logger.error("BTS API request failed with status {}: {}", response.statusCode(), response.body());
                throw new IOException("BTS API request failed with status " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error fetching data from BTS API: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Lookup DOT carrier code from ticker symbol
     * This is a mapping function as ticker symbols differ from DOT codes
     */
    public String getCarrierCodeFromTicker(String ticker) {
        // Common airline ticker to DOT carrier code mappings
        Map<String, String> tickerToCarrier = Map.ofEntries(
                Map.entry("UAL", "UA"),   // United Airlines
                Map.entry("AAL", "AA"),   // American Airlines
                Map.entry("DAL", "DL"),   // Delta Air Lines
                Map.entry("LUV", "WN"),   // Southwest Airlines
                Map.entry("JBLU", "B6"),  // JetBlue Airways
                Map.entry("ALK", "AS"),   // Alaska Airlines
                Map.entry("SKYW", "OO"),  // SkyWest Airlines
                Map.entry("HA", "HA"),    // Hawaiian Airlines
                Map.entry("SAVE", "F9"),  // Frontier Airlines
                Map.entry("ALGT", "G4"),  // Allegiant Air
                Map.entry("MESA", "YV"),  // Mesa Airlines
                Map.entry("SNCY", "G7")   // Sun Country Airlines
        );

        String carrierCode = tickerToCarrier.get(ticker.toUpperCase());
        if (carrierCode == null) {
            logger.warn("Unknown ticker symbol {}, using as-is for carrier code", ticker);
            return ticker;
        }

        return carrierCode;
    }

    /**
     * Test connection to BTS API
     */
    public boolean testConnection() {
        try {
            // Make a simple request to check connectivity
            fetchT100DomesticData("UA", 2023, 1);
            return true;
        } catch (Exception e) {
            logger.error("BTS API connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
