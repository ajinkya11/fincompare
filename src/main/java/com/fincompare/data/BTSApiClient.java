package com.fincompare.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Client for Bureau of Transportation Statistics (BTS) data
 * Downloads and parses CSV files from BTS PREZIP directory
 *
 * NOTE: BTS does not have a REST API. Data is accessed via:
 * 1. Direct CSV/ZIP downloads from /PREZIP/ directory (On-Time Performance)
 * 2. Web form submissions (T-100 Segment data)
 */
@Service
public class BTSApiClient {
    private static final Logger logger = LoggerFactory.getLogger(BTSApiClient.class);

    // BTS PREZIP directory for direct file downloads
    private static final String BTS_PREZIP_URL = "https://transtats.bts.gov/PREZIP";

    // File naming patterns
    private static final String ON_TIME_FILE_PATTERN = "On_Time_Reporting_Carrier_On_Time_Performance_1987_present_%d_%d.zip";

    // Cache directory for downloaded files
    private static final String CACHE_DIR = ".bts-cache";

    private final ObjectMapper objectMapper;
    private final CsvMapper csvMapper;
    private final Path cacheDirectory;

    @Value("${bts.cache.enabled:true}")
    private boolean cacheEnabled;

    public BTSApiClient() {
        this.objectMapper = new ObjectMapper();
        this.csvMapper = new CsvMapper();

        // Initialize cache directory
        this.cacheDirectory = Paths.get(System.getProperty("user.home"), CACHE_DIR);
        try {
            Files.createDirectories(cacheDirectory);
            logger.info("BTS cache directory: {}", cacheDirectory);
        } catch (IOException e) {
            logger.warn("Failed to create cache directory: {}", e.getMessage());
        }
    }

    /**
     * Fetch T-100 Domestic Segment data
     *
     * NOTE: T-100 data is not available via PREZIP URLs with predictable names.
     * It requires form submission to https://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=259
     *
     * For now, returning empty data. This needs to be implemented via form submission
     * or the user needs to manually download the data files.
     */
    public JsonNode fetchT100DomesticData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Fetching T-100 Domestic data for carrier {} - {}/{}", carrierCode, year, month);
        logger.warn("T-100 Domestic data requires manual download from BTS website. Returning empty data.");
        logger.info("Visit: https://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=259");
        logger.info("Select filters: Carrier={}, Year={}, Month={}", carrierCode, year, month);

        // Return empty JSON structure
        ObjectNode root = objectMapper.createObjectNode();
        root.set("data", objectMapper.createArrayNode());
        return root;
    }

    /**
     * Fetch T-100 International Segment data
     * Similar to domestic - requires form submission
     */
    public JsonNode fetchT100InternationalData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Fetching T-100 International data for carrier {} - {}/{}", carrierCode, year, month);
        logger.warn("T-100 International data requires manual download from BTS website. Returning empty data.");
        logger.info("Visit: https://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=260");

        // Return empty JSON structure
        ObjectNode root = objectMapper.createObjectNode();
        root.set("data", objectMapper.createArrayNode());
        return root;
    }

    /**
     * Fetch On-Time Performance data
     * Downloads CSV from PREZIP directory and parses it
     */
    public JsonNode fetchOnTimePerformanceData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Fetching On-Time Performance data for carrier {} - {}/{}", carrierCode, year, month);

        try {
            // Download and extract CSV
            String csvContent = downloadOnTimePerformanceCsv(year, month);

            // Parse CSV and filter by carrier
            return parseOnTimePerformanceCsv(csvContent, carrierCode);

        } catch (IOException e) {
            logger.error("Failed to fetch On-Time Performance data: {}", e.getMessage());

            // Return empty JSON structure on error
            ObjectNode root = objectMapper.createObjectNode();
            root.set("data", objectMapper.createArrayNode());
            return root;
        }
    }

    /**
     * Download On-Time Performance CSV file
     */
    private String downloadOnTimePerformanceCsv(int year, int month) throws IOException {
        String fileName = String.format(ON_TIME_FILE_PATTERN, year, month);
        String fileUrl = BTS_PREZIP_URL + "/" + fileName;

        // Check cache first
        Path cachedFile = cacheDirectory.resolve(fileName.replace(".zip", ".csv"));
        if (cacheEnabled && Files.exists(cachedFile)) {
            logger.debug("Using cached file: {}", cachedFile);
            return Files.readString(cachedFile);
        }

        logger.info("Downloading: {}", fileUrl);

        // Download ZIP file
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "FinCompare-Airline-Analyzer/1.0");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Failed to download file: HTTP " + responseCode + " for URL: " + fileUrl);
        }

        // Extract CSV from ZIP
        String csvContent;
        try (ZipInputStream zis = new ZipInputStream(connection.getInputStream())) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                throw new IOException("No files found in ZIP archive");
            }

            logger.debug("Extracting: {}", entry.getName());

            // Read CSV content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            csvContent = baos.toString("UTF-8");
        }

        // Cache the CSV file
        if (cacheEnabled) {
            try {
                Files.writeString(cachedFile, csvContent);
                logger.debug("Cached CSV to: {}", cachedFile);
            } catch (IOException e) {
                logger.warn("Failed to cache CSV file: {}", e.getMessage());
            }
        }

        return csvContent;
    }

    /**
     * Parse On-Time Performance CSV and convert to JSON format expected by DOTDataParser
     *
     * Expected CSV columns (sample):
     * YEAR, MONTH, CARRIER, ORIGIN, DEST, ARR_FLIGHTS, ARR_DEL15, CANCELLED, DIVERTED,
     * CARRIER_DELAY, WEATHER_DELAY, NAS_DELAY, SECURITY_DELAY, LATE_AIRCRAFT_DELAY,
     * ARR_DELAY, DEP_DELAY, ...
     */
    private JsonNode parseOnTimePerformanceCsv(String csvContent, String carrierCode) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode dataArray = objectMapper.createArrayNode();

        // Parse CSV with header detection
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        try (Reader reader = new StringReader(csvContent)) {
            var it = csvMapper.readerFor(Map.class)
                .with(schema)
                .readValues(reader);

            int totalRows = 0;
            int matchedRows = 0;
            boolean headersLogged = false;
            Set<String> uniqueCarriers = new HashSet<>();

            while (it.hasNext()) {
                Map<String, String> row = (Map<String, String>) it.next();
                totalRows++;

                // Log column headers on first row for debugging
                if (!headersLogged) {
                    logger.info("CSV columns available: {}", row.keySet());
                    headersLogged = true;
                }

                // Try multiple possible carrier code column names
                String rowCarrier = null;
                String[] possibleCarrierColumns = {
                    "Reporting_Airline",           // BTS On-Time Performance CSV (2024+)
                    "UNIQUE_CARRIER",              // Legacy BTS format
                    "OP_UNIQUE_CARRIER",           // Alternative format
                    "IATA_CODE_Reporting_Airline", // Alternative in same files
                    "OP_CARRIER",
                    "CARRIER",
                    "Unique_Carrier",
                    "Carrier"
                };

                for (String columnName : possibleCarrierColumns) {
                    rowCarrier = row.get(columnName);
                    if (rowCarrier != null && !rowCarrier.trim().isEmpty()) {
                        break;
                    }
                }

                // Collect unique carriers for debugging (first 100 rows only)
                if (totalRows <= 100 && rowCarrier != null) {
                    uniqueCarriers.add(rowCarrier);
                }

                if (rowCarrier == null || !rowCarrier.equals(carrierCode)) {
                    continue;
                }

                matchedRows++;

                // Convert CSV row to JSON object with fields expected by DOTDataParser
                ObjectNode record = objectMapper.createObjectNode();

                // Flight counts (note: actual CSV uses "Flights" column, value is 1 per row)
                // We aggregate by counting rows, so just mark each row as 1 flight
                record.put("ARR_FLIGHTS", parseIntOrZero(row.get("Flights")));
                record.put("ARR_DEL15", parseIntOrZero(row.get("ArrDel15")));  // Delayed 15+ min
                record.put("CANCELLED", parseIntOrZero(row.get("Cancelled")));
                record.put("DIVERTED", parseIntOrZero(row.get("Diverted")));

                // Delay minutes by category (CSV uses CamelCase without underscores)
                record.put("CARRIER_DELAY", parseDoubleOrZero(row.get("CarrierDelay")));
                record.put("WEATHER_DELAY", parseDoubleOrZero(row.get("WeatherDelay")));
                record.put("NAS_DELAY", parseDoubleOrZero(row.get("NASDelay")));
                record.put("SECURITY_DELAY", parseDoubleOrZero(row.get("SecurityDelay")));
                record.put("LATE_AIRCRAFT_DELAY", parseDoubleOrZero(row.get("LateAircraftDelay")));

                // Average delays (CSV uses CamelCase without underscores)
                record.put("ARR_DELAY", parseDoubleOrZero(row.get("ArrDelay")));
                record.put("DEP_DELAY", parseDoubleOrZero(row.get("DepDelay")));

                dataArray.add(record);
            }

            // Log debugging info about carriers found
            if (matchedRows == 0) {
                logger.warn("No rows matched carrier code '{}'. Sample carriers found in first 100 rows: {}",
                    carrierCode, uniqueCarriers);
            }

            logger.info("Parsed {} total rows, found {} rows for carrier {}", totalRows, matchedRows, carrierCode);
        }

        root.set("data", dataArray);
        return root;
    }

    /**
     * Fetch airline financial data from Form 41
     * Not implemented - financial data is better sourced from 10-K/10-Q filings
     */
    public JsonNode fetchFinancialData(String carrierCode, int year, int quarter) throws IOException, InterruptedException {
        logger.info("Fetching Financial data for carrier {} - {} Q{}", carrierCode, year, quarter);
        logger.info("Financial data is better sourced from SEC 10-K/10-Q filings");

        ObjectNode root = objectMapper.createObjectNode();
        root.set("data", objectMapper.createArrayNode());
        return root;
    }

    /**
     * Fetch baggage handling data
     * Not implemented - requires form submission
     */
    public JsonNode fetchBaggageData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Baggage data requires manual download from BTS website");

        ObjectNode root = objectMapper.createObjectNode();
        root.set("data", objectMapper.createArrayNode());
        return root;
    }

    /**
     * Fetch consumer complaints data
     * Not implemented - requires form submission
     */
    public JsonNode fetchComplaintsData(String carrierCode, int year, int month) throws IOException, InterruptedException {
        logger.info("Complaints data requires manual download from BTS website");

        ObjectNode root = objectMapper.createObjectNode();
        root.set("data", objectMapper.createArrayNode());
        return root;
    }

    /**
     * Lookup DOT carrier code from ticker symbol
     */
    public String getCarrierCodeFromTicker(String ticker) {
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
     * Test connection to BTS
     */
    public boolean testConnection() {
        try {
            // Try to download a recent month's data
            fetchOnTimePerformanceData("UA", 2023, 1);
            return true;
        } catch (Exception e) {
            logger.error("BTS connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clear the local cache directory
     */
    public void clearCache() {
        try {
            Files.walk(cacheDirectory)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.debug("Deleted cached file: {}", path);
                    } catch (IOException e) {
                        logger.warn("Failed to delete cached file: {}", e.getMessage());
                    }
                });
            logger.info("Cache cleared successfully");
        } catch (IOException e) {
            logger.error("Failed to clear cache: {}", e.getMessage());
        }
    }

    // Helper methods for parsing CSV values

    private int parseIntOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
