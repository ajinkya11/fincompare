package com.fincompare.data;

import com.fincompare.models.AirlineOperationalData;
import com.fincompare.models.IncomeStatement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for extracting operational metrics from 10-K HTML documents
 */
@Service
public class OperationalMetricsParser {
    private static final Logger logger = LoggerFactory.getLogger(OperationalMetricsParser.class);

    // Regex patterns for extracting operational metrics
    // ASM patterns (Available Seat Miles) - improved to match full numbers
    private static final Pattern[] ASM_PATTERNS = {
            // Match "Available seat miles (millions)    339,534" or "ASM    174.5 billion"
            Pattern.compile("(?:available\\s+seat\\s+miles|ASMs?)\\s*(?:\\(millions?\\))?[\\s:]*[\\-–—]*\\s*(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d+\\.\\d+)\\s*(million|billion|thousands?)?", Pattern.CASE_INSENSITIVE),
            // Fallback: number followed by million/billion then ASM/available seat miles
            Pattern.compile("(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d+\\.\\d+)\\s*(million|billion)\\s+(?:available\\s+seat\\s+miles|ASMs?)", Pattern.CASE_INSENSITIVE),
            // In tables: ASM label in one cell, number in adjacent cell
            Pattern.compile("(?:ASMs?|available\\s+seat\\s+miles)[^\\d]{0,50}(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d{3,})", Pattern.CASE_INSENSITIVE)
    };

    // RPM patterns (Revenue Passenger Miles) - improved to match full numbers
    private static final Pattern[] RPM_PATTERNS = {
            Pattern.compile("(?:revenue\\s+passenger\\s+miles|RPMs?)\\s*(?:\\(millions?\\))?[\\s:]*[\\-–—]*\\s*(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d+\\.\\d+)\\s*(million|billion|thousands?)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d+\\.\\d+)\\s*(million|billion)\\s+(?:revenue\\s+passenger\\s+miles|RPMs?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:RPMs?|revenue\\s+passenger\\s+miles)[^\\d]{0,50}(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d{3,})", Pattern.CASE_INSENSITIVE)
    };

    // Load Factor patterns
    private static final Pattern[] LOAD_FACTOR_PATTERNS = {
            Pattern.compile("load\\s+factor.*?(\\d{1,3}\\.\\d{1,2})\\s*%", Pattern.CASE_INSENSITIVE),
            Pattern.compile("passenger\\s+load\\s+factor.*?(\\d{1,3}\\.\\d{1,2})\\s*%", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d{1,3}\\.\\d{1,2})\\s*%.*?load\\s+factor", Pattern.CASE_INSENSITIVE)
    };

    // Passengers carried patterns
    private static final Pattern[] PASSENGERS_PATTERNS = {
            Pattern.compile("passengers\\s+carried.*?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|thousand)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("revenue\\s+passengers.*?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|thousand)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|thousand)?\\s+passengers", Pattern.CASE_INSENSITIVE)
    };

    // Cargo Ton Miles patterns
    private static final Pattern[] CTM_PATTERNS = {
            Pattern.compile("(?:cargo\\s+ton\\s+miles|CTMs?|freight\\s+ton\\s+miles)\\s*(?:\\(millions?\\))?[\\s:]*[\\-–—]*\\s*(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d+\\.\\d+)\\s*(million|billion)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:CTMs?|cargo\\s+ton\\s+miles)[^\\d]{0,50}(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d{3,})", Pattern.CASE_INSENSITIVE)
    };

    // Available Ton Miles patterns
    private static final Pattern[] ATM_PATTERNS = {
            Pattern.compile("(?:available\\s+ton\\s+miles|ATMs?)\\s*(?:\\(millions?\\))?[\\s:]*[\\-–—]*\\s*(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d+\\.\\d+)\\s*(million|billion)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:ATMs?|available\\s+ton\\s+miles)[^\\d]{0,50}(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d{3,})", Pattern.CASE_INSENSITIVE)
    };

    // Departures patterns
    private static final Pattern[] DEPARTURES_PATTERNS = {
            Pattern.compile("(?:departures|flights\\s+operated).*?(\\d{1,3}(?:,\\d{3})+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d{1,3}(?:,\\d{3})+)\\s+(?:departures|flights)", Pattern.CASE_INSENSITIVE)
    };

    // Block Hours patterns
    private static final Pattern[] BLOCK_HOURS_PATTERNS = {
            Pattern.compile("block\\s+hours.*?(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d+\\.\\d+)\\s*(million|thousand)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d{1,3}(?:,\\d{3})+(?:\\.\\d+)?|\\d+\\.\\d+)\\s*(million|thousand)?\\s+block\\s+hours", Pattern.CASE_INSENSITIVE)
    };

    // Cargo Load Factor patterns
    private static final Pattern[] CARGO_LOAD_FACTOR_PATTERNS = {
            Pattern.compile("cargo\\s+load\\s+factor.*?(\\d{1,3}\\.\\d{1,2})\\s*%", Pattern.CASE_INSENSITIVE),
            Pattern.compile("freight\\s+load\\s+factor.*?(\\d{1,3}\\.\\d{1,2})\\s*%", Pattern.CASE_INSENSITIVE)
    };

    /**
     * Parse operational metrics from 10-K HTML content
     */
    public AirlineOperationalData parseOperationalMetrics(String htmlContent, String fiscalYear) {
        logger.info("Parsing operational metrics from 10-K document for fiscal year: {}", fiscalYear);

        AirlineOperationalData data = new AirlineOperationalData();
        data.setFiscalYear(fiscalYear);

        try {
            Document doc = Jsoup.parse(htmlContent);

            // Strategy 1: Look for operational statistics tables
            BigDecimal asm = extractFromTables(doc, "ASM", fiscalYear);
            BigDecimal rpm = extractFromTables(doc, "RPM", fiscalYear);
            BigDecimal ctm = extractFromTables(doc, "CTM", fiscalYear);
            BigDecimal atm = extractFromTables(doc, "ATM", fiscalYear);
            BigDecimal loadFactor = extractLoadFactorFromTables(doc, fiscalYear);
            BigDecimal cargoLoadFactor = extractCargoLoadFactorFromTables(doc, fiscalYear);
            BigDecimal departures = extractDeparturesFromTables(doc, fiscalYear);
            BigDecimal blockHours = extractBlockHoursFromTables(doc, fiscalYear);
            Long passengers = extractPassengersFromTables(doc);

            // Strategy 2: Fallback to text extraction if table extraction failed
            String fullText = doc.body().text();
            if (asm == null) {
                asm = extractMetric(fullText, ASM_PATTERNS, "ASM");
            }
            if (rpm == null) {
                rpm = extractMetric(fullText, RPM_PATTERNS, "RPM");
            }
            if (ctm == null) {
                ctm = extractMetric(fullText, CTM_PATTERNS, "CTM");
            }
            if (atm == null) {
                atm = extractMetric(fullText, ATM_PATTERNS, "ATM");
            }
            if (loadFactor == null) {
                loadFactor = extractLoadFactor(fullText);
            }
            if (cargoLoadFactor == null) {
                cargoLoadFactor = extractCargoLoadFactor(fullText);
            }
            if (departures == null) {
                departures = extractDepartures(fullText);
            }
            if (blockHours == null) {
                blockHours = extractBlockHours(fullText);
            }
            if (passengers == null) {
                passengers = extractPassengers(fullText);
            }

            // Set basic operational metrics
            data.setAvailableSeatMiles(asm);
            data.setRevenuePassengerMiles(rpm);
            data.setCargoTonMiles(ctm);
            data.setAvailableTonMiles(atm);
            data.setLoadFactor(loadFactor);
            data.setCargoLoadFactor(cargoLoadFactor);
            data.setDeparturesPerformed(departures);
            data.setBlockHours(blockHours);
            data.setPassengersCarried(passengers);

            // Extract fleet information
            extractFleetInformation(doc, data);

            // Log what we found
            logger.info("Extracted metrics - ASM: {}, RPM: {}, CTM: {}, ATM: {}, Load Factor: {}, Cargo LF: {}, Departures: {}, Block Hours: {}, Passengers: {}",
                    data.getAvailableSeatMiles(), data.getRevenuePassengerMiles(),
                    data.getCargoTonMiles(), data.getAvailableTonMiles(),
                    data.getLoadFactor(), data.getCargoLoadFactor(),
                    data.getDeparturesPerformed(), data.getBlockHours(),
                    data.getPassengersCarried());

            return data;

        } catch (Exception e) {
            logger.error("Error parsing operational metrics", e);
            return data;
        }
    }

    /**
     * Extract revenue breakdowns from 10-K HTML (passenger, cargo, other)
     */
    public Map<String, BigDecimal> extractRevenueBreakdowns(String htmlContent, String fiscalYear) {
        logger.info("Extracting revenue breakdowns from 10-K HTML for fiscal year: {}", fiscalYear);

        Map<String, BigDecimal> revenueBreakdowns = new HashMap<>();

        try {
            Document doc = Jsoup.parse(htmlContent);
            Elements tables = doc.select("table");

            // Search for Revenue breakdown tables (typically in Segment or Revenue note sections)
            for (Element table : tables) {
                String tableText = table.text().toLowerCase();

                // Look for revenue breakdown indicators
                if (!tableText.contains("revenue") && !tableText.contains("operating income")) {
                    continue;
                }

                // Skip tables that are clearly not revenue breakdowns
                if (tableText.contains("balance sheet") || tableText.contains("cash flow")) {
                    continue;
                }

                Elements rows = table.select("tr");
                for (Element row : rows) {
                    String rowText = row.text().toLowerCase();

                    // Look for passenger revenue
                    if ((rowText.contains("passenger revenue") || rowText.contains("passenger") && rowText.contains("revenue")) &&
                        !rowText.contains("per ") && !rowText.contains("yield")) {

                        BigDecimal passengerRevenue = extractRevenueValueFromRow(row, fiscalYear, table);
                        if (passengerRevenue != null) {
                            revenueBreakdowns.put("passenger", passengerRevenue);
                            logger.info("Found passenger revenue: {} million", passengerRevenue);
                        }
                    }

                    // Look for cargo/freight revenue
                    if ((rowText.contains("cargo revenue") || rowText.contains("cargo") && rowText.contains("revenue") ||
                         rowText.contains("freight revenue") || rowText.contains("freight") && rowText.contains("revenue")) &&
                        !rowText.contains("per ")) {

                        BigDecimal cargoRevenue = extractRevenueValueFromRow(row, fiscalYear, table);
                        if (cargoRevenue != null) {
                            revenueBreakdowns.put("cargo", cargoRevenue);
                            logger.info("Found cargo revenue: {} million", cargoRevenue);
                        }
                    }

                    // Look for other revenue
                    if ((rowText.contains("other revenue") || rowText.contains("other operating revenue")) &&
                        !rowText.contains("per ")) {

                        BigDecimal otherRevenue = extractRevenueValueFromRow(row, fiscalYear, table);
                        if (otherRevenue != null) {
                            revenueBreakdowns.put("other", otherRevenue);
                            logger.info("Found other revenue: {} million", otherRevenue);
                        }
                    }

                    // Look for domestic revenue (various patterns)
                    if ((rowText.contains("domestic") || rowText.contains("mainline domestic")) &&
                        !rowText.contains("per ") && !rowText.contains("yield") &&
                        !rowText.contains("asm") && !rowText.contains("rpm")) {

                        BigDecimal domesticRevenue = extractRevenueValueFromRow(row, fiscalYear, table);
                        if (domesticRevenue != null && domesticRevenue.compareTo(new BigDecimal("100")) > 0) {
                            revenueBreakdowns.put("domestic", domesticRevenue);
                            logger.info("Found domestic revenue: {} million", domesticRevenue);
                        }
                    }

                    // Look for international revenue (various patterns including regional breakdowns)
                    if ((rowText.contains("international") ||
                         rowText.contains("atlantic") ||
                         rowText.contains("pacific") ||
                         rowText.contains("latin") ||
                         rowText.contains("caribbean")) &&
                        !rowText.contains("per ") && !rowText.contains("yield") &&
                        !rowText.contains("asm") && !rowText.contains("rpm")) {

                        BigDecimal internationalRevenue = extractRevenueValueFromRow(row, fiscalYear, table);
                        if (internationalRevenue != null && internationalRevenue.compareTo(new BigDecimal("100")) > 0) {
                            // If we already have international revenue, add to it (for airlines that break down by region)
                            if (revenueBreakdowns.containsKey("international")) {
                                BigDecimal existing = revenueBreakdowns.get("international");
                                internationalRevenue = existing.add(internationalRevenue);
                            }
                            revenueBreakdowns.put("international", internationalRevenue);
                            logger.info("Found international revenue: {} million (regional: {})", internationalRevenue, rowText.contains("atlantic") || rowText.contains("pacific") || rowText.contains("latin") || rowText.contains("caribbean"));
                        }
                    }
                }
            }

            logger.info("Revenue breakdown extraction complete. Found {} revenue categories", revenueBreakdowns.size());
            return revenueBreakdowns;

        } catch (Exception e) {
            logger.error("Error extracting revenue breakdowns from HTML", e);
            return revenueBreakdowns;
        }
    }

    /**
     * Helper method to extract revenue value from a table row for a specific fiscal year
     */
    private BigDecimal extractRevenueValueFromRow(Element row, String fiscalYear, Element table) {
        Elements cells = row.select("td, th");
        java.util.List<BigDecimal> numbersFound = new java.util.ArrayList<>();
        java.util.List<Integer> numberIndices = new java.util.ArrayList<>();

        // Collect all valid numbers (revenue is typically in millions or billions)
        for (int i = 0; i < cells.size(); i++) {
            String cellText = cells.get(i).text();
            BigDecimal value = parseNumber(cellText);
            // Revenue should be at least in tens of millions
            if (value != null && value.compareTo(new BigDecimal("10")) > 0) {
                numbersFound.add(value);
                numberIndices.add(i);
            }
        }

        if (!numbersFound.isEmpty()) {
            // Try to find which number corresponds to the fiscal year
            BigDecimal selectedValue = selectValueForFiscalYear(
                cells, numbersFound, numberIndices, fiscalYear, table);

            if (selectedValue != null) {
                // Revenue is typically in millions in the 10-K
                // Convert to actual dollars (millions * 1,000,000)
                return selectedValue.multiply(new BigDecimal("1000000"));
            }
        }

        return null;
    }

    /**
     * Extract revenue from 10-K HTML to validate XBRL data
     * Some years (especially COVID-era) have incorrect XBRL revenue values
     */
    public BigDecimal extractRevenueFromHTML(String htmlContent, String fiscalYear) {
        logger.info("Extracting revenue from 10-K HTML for fiscal year: {}", fiscalYear);

        try {
            Document doc = Jsoup.parse(htmlContent);
            Elements tables = doc.select("table");

            // Search for Consolidated Statements of Operations or Income Statement
            for (Element table : tables) {
                String tableText = table.text().toLowerCase();

                // Look for income statement indicators
                if (!tableText.contains("statement") && !tableText.contains("operations") &&
                    !tableText.contains("income") && !tableText.contains("revenue")) {
                    continue;
                }

                // Skip tables that are clearly not income statements
                if (tableText.contains("balance sheet") || tableText.contains("cash flow") ||
                    tableText.contains("stockholders") || tableText.contains("segment")) {
                    continue;
                }

                Elements rows = table.select("tr");
                for (Element row : rows) {
                    String rowText = row.text().toLowerCase();

                    // Look for revenue rows (but exclude per-unit metrics)
                    if ((rowText.contains("total operating revenue") ||
                         rowText.contains("total revenue") ||
                         rowText.contains("operating revenues") ||
                         rowText.contains("total revenues") ||
                         rowText.contains("operating revenue")) &&
                        !rowText.contains("per ") &&
                        !rowText.contains("average") &&
                        !rowText.contains("yield")) {

                        Elements cells = row.select("td, th");
                        java.util.List<BigDecimal> numbersFound = new java.util.ArrayList<>();
                        java.util.List<Integer> numberIndices = new java.util.ArrayList<>();

                        // Collect all valid numbers (revenue is typically in millions or billions)
                        for (int i = 0; i < cells.size(); i++) {
                            String cellText = cells.get(i).text();
                            BigDecimal value = parseNumber(cellText);
                            // Revenue should be at least in hundreds of millions
                            if (value != null && value.compareTo(new BigDecimal("100")) > 0) {
                                numbersFound.add(value);
                                numberIndices.add(i);
                            }
                        }

                        if (!numbersFound.isEmpty()) {
                            // Try to find which number corresponds to the fiscal year
                            BigDecimal selectedValue = selectValueForFiscalYear(
                                cells, numbersFound, numberIndices, fiscalYear, table);

                            if (selectedValue != null) {
                                // Revenue is typically in millions in the 10-K
                                // Convert to actual dollars (millions * 1,000,000)
                                BigDecimal revenue = selectedValue.multiply(new BigDecimal("1000000"));
                                logger.info("Found revenue in table: {} million = {} from row: {}",
                                    selectedValue, revenue, rowText.substring(0, Math.min(80, rowText.length())));
                                return revenue;
                            }
                        }
                    }
                }
            }

            logger.warn("Could not extract revenue from 10-K HTML");
            return null;

        } catch (Exception e) {
            logger.error("Error extracting revenue from HTML", e);
            return null;
        }
    }

    /**
     * Extract metrics from tables (more reliable than free text)
     */
    private BigDecimal extractFromTables(Document doc, String metricType, String fiscalYear) {
        Elements tables = doc.select("table");
        logger.info("Searching {} tables for {} data (fiscal year: {})", tables.size(), metricType, fiscalYear);

        // Define search terms - be specific to avoid false matches
        String[] searchTerms;
        String[] exclusionTerms;

        if (metricType.equals("ASM")) {
            searchTerms = new String[]{"available seat miles", "asms (millions)", "asm"};
            exclusionTerms = new String[]{"per asm", "revenue per asm", "/ asm"};
        } else if (metricType.equals("RPM")) {
            searchTerms = new String[]{"revenue passenger miles", "rpms (millions)", "rpm"};
            exclusionTerms = new String[]{"per rpm", "/ rpm"};
        } else if (metricType.equals("CTM")) {
            searchTerms = new String[]{"cargo ton miles", "freight ton miles", "ctm"};
            exclusionTerms = new String[]{"per ctm", "/ ctm"};
        } else if (metricType.equals("ATM")) {
            searchTerms = new String[]{"available ton miles", "atm"};
            exclusionTerms = new String[]{"per atm", "/ atm"};
        } else {
            return null;
        }

        for (Element table : tables) {
            // Check if this table contains operational statistics
            String tableText = table.text().toLowerCase();
            if (!tableText.contains("seat miles") && !tableText.contains("operational")) {
                continue;
            }

            // Search table rows for the metric
            Elements rows = table.select("tr");
            for (Element row : rows) {
                String rowText = row.text().toLowerCase();

                // Check if this row should be excluded (per-unit metrics)
                boolean shouldExclude = false;
                for (String exclusion : exclusionTerms) {
                    if (rowText.contains(exclusion)) {
                        shouldExclude = true;
                        break;
                    }
                }
                if (shouldExclude) {
                    continue;
                }

                // Check if this row contains our metric
                boolean matches = false;
                for (String term : searchTerms) {
                    if (rowText.contains(term)) {
                        matches = true;
                        break;
                    }
                }

                if (matches) {
                    // Extract numbers from this row - may contain multiple years
                    Elements cells = row.select("td, th");
                    java.util.List<BigDecimal> numbersFound = new java.util.ArrayList<>();
                    java.util.List<Integer> numberIndices = new java.util.ArrayList<>();

                    // Collect all valid numbers in the row
                    for (int i = 0; i < cells.size(); i++) {
                        String cellText = cells.get(i).text();
                        BigDecimal value = parseNumber(cellText);
                        if (value != null && value.compareTo(new BigDecimal("10")) > 0) {
                            numbersFound.add(value);
                            numberIndices.add(i);
                        }
                    }

                    if (!numbersFound.isEmpty()) {
                        // Try to find which number corresponds to the fiscal year we're parsing
                        BigDecimal selectedValue = selectValueForFiscalYear(
                            cells, numbersFound, numberIndices, fiscalYear, table);

                        if (selectedValue != null) {
                            logger.info("Found {} in table: {} from row: {} (selected from {} candidates)",
                                metricType, selectedValue, rowText.substring(0, Math.min(100, rowText.length())), numbersFound.size());

                            // Determine if it's in millions or billions based on explicit unit markers
                            if (rowText.matches(".*\\bbillion\\b.*") || rowText.matches(".*\\bbillions\\b.*")) {
                                if (!rowText.contains("(millions)") && !rowText.contains("millions")) {
                                    selectedValue = selectedValue.multiply(new BigDecimal("1000"));
                                    logger.info("Converting from billions to millions: {}", selectedValue);
                                }
                            } else if (rowText.contains("million") || rowText.contains("(millions)") || selectedValue.compareTo(new BigDecimal("1000")) > 0) {
                                logger.info("Value assumed to be in millions: {}", selectedValue);
                            }

                            return selectedValue;
                        }
                    }
                }
            }
        }

        logger.warn("Could not find {} in any table", metricType);
        return null;
    }

    /**
     * Select the appropriate value for the fiscal year from multi-year table data
     */
    private BigDecimal selectValueForFiscalYear(Elements cells, java.util.List<BigDecimal> numbersFound,
                                                 java.util.List<Integer> numberIndices, String fiscalYear,
                                                 Element table) {
        if (numbersFound.isEmpty()) {
            return null;
        }

        // If only one number, return it
        if (numbersFound.size() == 1) {
            return numbersFound.get(0);
        }

        // Strategy 1: Look for fiscal year in table header rows (most reliable)
        Elements allRows = table.select("tr");
        for (int rowIdx = 0; rowIdx < Math.min(3, allRows.size()); rowIdx++) {
            Element headerRow = allRows.get(rowIdx);
            Elements headerCells = headerRow.select("th, td");

            for (int cellIdx = 0; cellIdx < headerCells.size(); cellIdx++) {
                String cellText = headerCells.get(cellIdx).text();
                // Look for the fiscal year in this header cell
                if (cellText.contains(fiscalYear)) {
                    // Found the year! Now map to data column
                    // The data row might have different structure, so look for the Nth numeric value
                    if (cellIdx > 0 && cellIdx - 1 < numbersFound.size()) {
                        logger.info("Selected value at index {} based on header row {} column match for year {}",
                            cellIdx - 1, rowIdx, fiscalYear);
                        return numbersFound.get(cellIdx - 1);
                    } else if (cellIdx < numbersFound.size()) {
                        logger.info("Selected value at index {} based on header row {} direct column match for year {}",
                            cellIdx, rowIdx, fiscalYear);
                        return numbersFound.get(cellIdx);
                    }
                }
            }
        }

        // Strategy 2: Look for fiscal year in the same row (in adjacent cells)
        for (int i = 0; i < numberIndices.size(); i++) {
            int cellIndex = numberIndices.get(i);
            // Check cells around the number
            for (int offset = -2; offset <= 2; offset++) {
                int checkIdx = cellIndex + offset;
                if (checkIdx >= 0 && checkIdx < cells.size()) {
                    String cellText = cells.get(checkIdx).text();
                    if (cellText.contains(fiscalYear)) {
                        logger.info("Selected value at index {} based on adjacent cell containing year {}", i, fiscalYear);
                        return numbersFound.get(i);
                    }
                }
            }
        }

        // Strategy 3: Search the entire table for year context
        String tableHtml = table.html().toLowerCase();
        // Look for patterns like "2024 2023 2022" in headers
        java.util.regex.Pattern yearSequencePattern = java.util.regex.Pattern.compile(
            "(\\d{4})\\s+(\\d{4})\\s+(\\d{4})"
        );
        java.util.regex.Matcher matcher = yearSequencePattern.matcher(table.text());
        if (matcher.find()) {
            // Found a sequence of years - determine position of our target year
            for (int groupIdx = 1; groupIdx <= matcher.groupCount(); groupIdx++) {
                if (matcher.group(groupIdx).equals(fiscalYear)) {
                    int yearPosition = groupIdx - 1; // 0-indexed position
                    if (yearPosition < numbersFound.size()) {
                        logger.info("Selected value at index {} based on year sequence pattern match", yearPosition);
                        return numbersFound.get(yearPosition);
                    }
                }
            }
        }

        // Strategy 4: If we can't find the year, return null instead of guessing
        // This is better than returning wrong data
        logger.warn("Could not match fiscal year {} to any table column. Found {} candidate values but cannot determine which is correct. Returning null to avoid incorrect data.",
            fiscalYear, numbersFound.size());
        return null;
    }

    /**
     * Extract load factor from tables
     */
    private BigDecimal extractLoadFactorFromTables(Document doc, String fiscalYear) {
        Elements tables = doc.select("table");

        for (Element table : tables) {
            String tableText = table.text().toLowerCase();
            if (!tableText.contains("load factor")) {
                continue;
            }

            Elements rows = table.select("tr");
            for (Element row : rows) {
                String rowText = row.text().toLowerCase();
                if ((rowText.contains("load factor") || rowText.contains("passenger load")) &&
                    !rowText.contains("cargo") && !rowText.contains("freight")) {
                    // Look for percentage in this row
                    Pattern percentPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,2})\\s*%");
                    Matcher matcher = percentPattern.matcher(row.text());
                    if (matcher.find()) {
                        BigDecimal value = new BigDecimal(matcher.group(1));
                        logger.info("Found Load Factor in table: {}%", value);
                        return value;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extract cargo load factor from tables
     */
    private BigDecimal extractCargoLoadFactorFromTables(Document doc, String fiscalYear) {
        Elements tables = doc.select("table");

        for (Element table : tables) {
            String tableText = table.text().toLowerCase();
            if (!tableText.contains("load factor") && !tableText.contains("cargo")) {
                continue;
            }

            Elements rows = table.select("tr");
            for (Element row : rows) {
                String rowText = row.text().toLowerCase();
                if ((rowText.contains("cargo") && rowText.contains("load factor")) ||
                    (rowText.contains("freight") && rowText.contains("load factor"))) {
                    // Look for percentage in this row
                    Pattern percentPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,2})\\s*%");
                    Matcher matcher = percentPattern.matcher(row.text());
                    if (matcher.find()) {
                        BigDecimal value = new BigDecimal(matcher.group(1));
                        logger.info("Found Cargo Load Factor in table: {}%", value);
                        return value;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extract departures from tables
     */
    private BigDecimal extractDeparturesFromTables(Document doc, String fiscalYear) {
        Elements tables = doc.select("table");

        for (Element table : tables) {
            String tableText = table.text().toLowerCase();
            if (!tableText.contains("departure") && !tableText.contains("flight")) {
                continue;
            }

            Elements rows = table.select("tr");
            for (Element row : rows) {
                String rowText = row.text().toLowerCase();
                if (rowText.contains("departure") || rowText.contains("flights operated")) {
                    Elements cells = row.select("td, th");
                    for (Element cell : cells) {
                        String cellText = cell.text().replace(",", "").replace(" ", "");
                        try {
                            Pattern numberPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
                            Matcher matcher = numberPattern.matcher(cellText);
                            if (matcher.find()) {
                                BigDecimal value = new BigDecimal(matcher.group(1));
                                if (value.compareTo(new BigDecimal("100")) > 0 &&
                                    value.compareTo(new BigDecimal("10000000")) < 0) {
                                    logger.info("Found Departures in table: {}", value);
                                    return value;
                                }
                            }
                        } catch (Exception e) {
                            // Continue searching
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extract block hours from tables
     */
    private BigDecimal extractBlockHoursFromTables(Document doc, String fiscalYear) {
        Elements tables = doc.select("table");

        for (Element table : tables) {
            String tableText = table.text().toLowerCase();
            if (!tableText.contains("block hour")) {
                continue;
            }

            Elements rows = table.select("tr");
            for (Element row : rows) {
                String rowText = row.text().toLowerCase();
                if (rowText.contains("block hour")) {
                    Elements cells = row.select("td, th");
                    for (Element cell : cells) {
                        String cellText = cell.text().replace(",", "").replace(" ", "");
                        try {
                            Pattern numberPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
                            Matcher matcher = numberPattern.matcher(cellText);
                            if (matcher.find()) {
                                BigDecimal value = new BigDecimal(matcher.group(1));
                                // Block hours are typically in thousands or millions
                                if (value.compareTo(new BigDecimal("100")) > 0) {
                                    logger.info("Found Block Hours in table: {}", value);
                                    return value;
                                }
                            }
                        } catch (Exception e) {
                            // Continue searching
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extract passengers from tables
     */
    private Long extractPassengersFromTables(Document doc) {
        Elements tables = doc.select("table");

        for (Element table : tables) {
            String tableText = table.text().toLowerCase();
            if (!tableText.contains("passenger")) {
                continue;
            }

            Elements rows = table.select("tr");
            for (Element row : rows) {
                String rowText = row.text().toLowerCase();
                if (rowText.contains("passengers") && (rowText.contains("carried") || rowText.contains("enplaned"))) {
                    Elements cells = row.select("td, th");
                    for (Element cell : cells) {
                        String cellText = cell.text().replace(",", "").replace(" ", "");
                        try {
                            // Look for numbers in millions
                            Pattern numberPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
                            Matcher matcher = numberPattern.matcher(cellText);
                            if (matcher.find()) {
                                double value = Double.parseDouble(matcher.group(1));
                                if (value > 1 && value < 1000) {
                                    // Likely in millions
                                    return (long)(value * 1_000_000);
                                }
                            }
                        } catch (Exception e) {
                            // Continue searching
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Parse a number from text, handling commas and decimals
     */
    private BigDecimal parseNumber(String text) {
        try {
            // Remove everything except digits, commas, and decimal points
            String cleaned = text.replaceAll("[^0-9,.]", "");
            if (cleaned.isEmpty()) {
                return null;
            }

            // Remove commas
            cleaned = cleaned.replace(",", "");

            // Parse
            BigDecimal value = new BigDecimal(cleaned);

            // Sanity check - must be positive and reasonable
            if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(new BigDecimal("1000000")) < 0) {
                return value;
            }
        } catch (Exception e) {
            // Not a valid number
        }
        return null;
    }

    /**
     * Extract a metric using multiple regex patterns
     * Returns value in millions (standard unit for ASM/RPM)
     */
    private BigDecimal extractMetric(String text, Pattern[] patterns, String metricName) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String numberStr = matcher.group(1).replace(",", "").replace(" ", "");
                    BigDecimal value = new BigDecimal(numberStr);

                    // Check for unit multiplier and convert to millions
                    if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                        String unit = matcher.group(2).toLowerCase();
                        if (unit.contains("billion")) {
                            value = value.multiply(new BigDecimal("1000")); // Convert billions to millions
                        } else if (unit.contains("thousand")) {
                            value = value.divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP); // Convert thousands to millions
                        }
                        // If unit is "million" or no unit, value is already in millions
                    } else {
                        // No explicit unit - if the number is > 1000, assume it's already in millions
                        // If < 1000 and we're looking for ASM/RPM, it might be in billions
                        if (value.compareTo(new BigDecimal("1000")) < 0 && (metricName.equals("ASM") || metricName.equals("RPM"))) {
                            // Small number like 174.5 without unit likely means billions
                            logger.debug("Assuming {} value {} is in billions, converting to millions", metricName, value);
                            value = value.multiply(new BigDecimal("1000"));
                        }
                    }

                    logger.debug("Found {} value: {} million", metricName, value);
                    return value;
                } catch (Exception e) {
                    logger.debug("Error parsing {} value from match: {}", metricName, matcher.group(), e);
                }
            }
        }
        return null;
    }

    /**
     * Extract load factor percentage
     */
    private BigDecimal extractLoadFactor(String text) {
        for (Pattern pattern : LOAD_FACTOR_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String percentStr = matcher.group(1);
                    BigDecimal value = new BigDecimal(percentStr);
                    logger.debug("Found Load Factor: {}%", value);
                    return value;
                } catch (Exception e) {
                    logger.debug("Error parsing load factor from match: {}", matcher.group(), e);
                }
            }
        }
        return null;
    }

    /**
     * Extract passengers carried
     */
    private Long extractPassengers(String text) {
        for (Pattern pattern : PASSENGERS_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String numberStr = matcher.group(1).replace(",", "");
                    long value = Long.parseLong(numberStr);

                    // Check for unit multiplier
                    if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                        String unit = matcher.group(2).toLowerCase();
                        if (unit.contains("million")) {
                            value = value * 1_000_000;
                        } else if (unit.contains("thousand")) {
                            value = value * 1_000;
                        }
                    }

                    logger.debug("Found Passengers: {}", value);
                    return value;
                } catch (Exception e) {
                    logger.debug("Error parsing passengers from match: {}", matcher.group(), e);
                }
            }
        }
        return null;
    }

    /**
     * Extract cargo load factor percentage from text
     */
    private BigDecimal extractCargoLoadFactor(String text) {
        for (Pattern pattern : CARGO_LOAD_FACTOR_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String percentStr = matcher.group(1);
                    BigDecimal value = new BigDecimal(percentStr);
                    logger.debug("Found Cargo Load Factor: {}%", value);
                    return value;
                } catch (Exception e) {
                    logger.debug("Error parsing cargo load factor from match: {}", matcher.group(), e);
                }
            }
        }
        return null;
    }

    /**
     * Extract departures from text
     */
    private BigDecimal extractDepartures(String text) {
        for (Pattern pattern : DEPARTURES_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String numberStr = matcher.group(1).replace(",", "");
                    BigDecimal value = new BigDecimal(numberStr);
                    logger.debug("Found Departures: {}", value);
                    return value;
                } catch (Exception e) {
                    logger.debug("Error parsing departures from match: {}", matcher.group(), e);
                }
            }
        }
        return null;
    }

    /**
     * Extract block hours from text
     */
    private BigDecimal extractBlockHours(String text) {
        for (Pattern pattern : BLOCK_HOURS_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String numberStr = matcher.group(1).replace(",", "");
                    BigDecimal value = new BigDecimal(numberStr);

                    // Check for unit multiplier
                    if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                        String unit = matcher.group(2).toLowerCase();
                        if (unit.contains("million")) {
                            value = value.multiply(new BigDecimal("1000000"));
                        } else if (unit.contains("thousand")) {
                            value = value.multiply(new BigDecimal("1000"));
                        }
                    }

                    logger.debug("Found Block Hours: {}", value);
                    return value;
                } catch (Exception e) {
                    logger.debug("Error parsing block hours from match: {}", matcher.group(), e);
                }
            }
        }
        return null;
    }

    /**
     * Extract fleet information from 10-K document
     */
    private void extractFleetInformation(Document doc, AirlineOperationalData data) {
        logger.debug("Attempting to extract fleet information");

        try {
            // Look for fleet information in tables or text
            Elements tables = doc.select("table");

            for (Element table : tables) {
                String tableText = table.text().toLowerCase();

                // Look for fleet-related tables
                if (tableText.contains("fleet") || tableText.contains("aircraft")) {

                    // Try to extract fleet composition
                    StringBuilder fleetComposition = new StringBuilder();
                    int aircraftTypesFound = 0;

                    Elements rows = table.select("tr");
                    for (Element row : rows) {
                        String rowText = row.text();

                        // Look for aircraft types (Boeing 737, Airbus A320, etc.)
                        Pattern aircraftPattern = Pattern.compile("(Boeing|Airbus|McDonnell|Embraer|Bombardier)\\s+([A-Z]?\\d{3}[A-Z]?(?:-\\d+)?)", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = aircraftPattern.matcher(rowText);

                        if (matcher.find()) {
                            String aircraftType = matcher.group(1) + " " + matcher.group(2);

                            // Try to find the count for this aircraft type
                            Elements cells = row.select("td, th");
                            for (Element cell : cells) {
                                try {
                                    String cellText = cell.text().trim().replace(",", "");
                                    int count = Integer.parseInt(cellText);
                                    if (count > 0 && count < 2000) { // Sanity check
                                        if (fleetComposition.length() > 0) {
                                            fleetComposition.append(", ");
                                        }
                                        fleetComposition.append(count).append(" ").append(aircraftType);
                                        aircraftTypesFound++;
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    // Not a number, continue
                                }
                            }
                        }
                    }

                    if (fleetComposition.length() > 0) {
                        data.setFleetComposition(fleetComposition.toString());
                        logger.info("Found fleet composition: {}", fleetComposition.toString());
                    }

                    // Try to extract average fleet age
                    for (Element row : rows) {
                        String rowText = row.text().toLowerCase();
                        if (rowText.contains("average age") || rowText.contains("weighted average age")) {
                            Pattern agePattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:years?)?");
                            Matcher ageMatcher = agePattern.matcher(row.text());
                            if (ageMatcher.find()) {
                                try {
                                    double age = Double.parseDouble(ageMatcher.group(1));
                                    if (age > 0 && age < 50) { // Sanity check
                                        data.setAverageFleetAge((int) Math.round(age));
                                        logger.info("Found average fleet age: {} years", (int) Math.round(age));
                                    }
                                } catch (Exception e) {
                                    logger.debug("Error parsing fleet age", e);
                                }
                            }
                        }
                    }
                }
            }

            // If we didn't find fleet composition in tables, try text search
            if (data.getFleetComposition() == null) {
                String fullText = doc.body().text();

                // Look for fleet size mentions
                Pattern fleetSizePattern = Pattern.compile("fleet\\s+of\\s+(\\d+)\\s+aircraft", Pattern.CASE_INSENSITIVE);
                Matcher fleetMatcher = fleetSizePattern.matcher(fullText);
                if (fleetMatcher.find()) {
                    try {
                        int fleetSize = Integer.parseInt(fleetMatcher.group(1));
                        if (data.getFleetSize() == null && fleetSize > 0 && fleetSize < 2000) {
                            data.setFleetSize(fleetSize);
                            logger.info("Found fleet size from text: {}", fleetSize);
                        }
                    } catch (Exception e) {
                        logger.debug("Error parsing fleet size from text", e);
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Error extracting fleet information: {}", e.getMessage());
        }
    }
}
