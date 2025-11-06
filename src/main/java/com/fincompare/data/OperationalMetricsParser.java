package com.fincompare.data;

import com.fincompare.models.AirlineOperationalData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
            BigDecimal loadFactor = extractLoadFactorFromTables(doc);
            Long passengers = extractPassengersFromTables(doc);

            // Strategy 2: Fallback to text extraction if table extraction failed
            if (asm == null || rpm == null) {
                logger.info("Table extraction incomplete, falling back to text pattern matching");
                String fullText = doc.body().text();

                if (asm == null) {
                    asm = extractMetric(fullText, ASM_PATTERNS, "ASM");
                }
                if (rpm == null) {
                    rpm = extractMetric(fullText, RPM_PATTERNS, "RPM");
                }
                if (loadFactor == null) {
                    loadFactor = extractLoadFactor(fullText);
                }
                if (passengers == null) {
                    passengers = extractPassengers(fullText);
                }
            }

            data.setAvailableSeatMiles(asm);
            data.setRevenuePassengerMiles(rpm);
            data.setLoadFactor(loadFactor);
            data.setPassengersCarried(passengers);

            // Log what we found
            logger.info("Extracted metrics - ASM: {}, RPM: {}, Load Factor: {}, Passengers: {}",
                    data.getAvailableSeatMiles(), data.getRevenuePassengerMiles(),
                    data.getLoadFactor(), data.getPassengersCarried());

            return data;

        } catch (Exception e) {
            logger.error("Error parsing operational metrics", e);
            return data;
        }
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
            searchTerms = new String[]{"available seat miles", "asms (millions)"};
            exclusionTerms = new String[]{"per asm", "revenue per asm", "/ asm"};
        } else {
            searchTerms = new String[]{"revenue passenger miles", "rpms (millions)"};
            exclusionTerms = new String[]{"per rpm", "/ rpm"};
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
    private BigDecimal extractLoadFactorFromTables(Document doc) {
        Elements tables = doc.select("table");

        for (Element table : tables) {
            String tableText = table.text().toLowerCase();
            if (!tableText.contains("load factor")) {
                continue;
            }

            Elements rows = table.select("tr");
            for (Element row : rows) {
                String rowText = row.text().toLowerCase();
                if (rowText.contains("load factor") || rowText.contains("passenger load")) {
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
}
