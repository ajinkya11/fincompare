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
            BigDecimal asm = extractFromTables(doc, "ASM");
            BigDecimal rpm = extractFromTables(doc, "RPM");
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
     * Extract metrics from tables (more reliable than free text)
     */
    private BigDecimal extractFromTables(Document doc, String metricType) {
        Elements tables = doc.select("table");
        logger.info("Searching {} tables for {} data", tables.size(), metricType);

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
                    // Extract the number from this row
                    Elements cells = row.select("td, th");
                    for (int i = 0; i < cells.size(); i++) {
                        String cellText = cells.get(i).text();
                        BigDecimal value = parseNumber(cellText);
                        if (value != null && value.compareTo(new BigDecimal("10")) > 0) {
                            // Found a reasonable number
                            logger.info("Found {} in table: {} from row: {}", metricType, value, rowText.substring(0, Math.min(100, rowText.length())));

                            // Determine if it's in millions or billions based on explicit unit markers
                            // Be careful: "(b)" might be a footnote, not "billions"
                            // Only convert if we see "billion" or "billions" as actual words
                            if (rowText.matches(".*\\bbillion\\b.*") || rowText.matches(".*\\bbillions\\b.*")) {
                                // Check it's not already labeled as millions
                                if (!rowText.contains("(millions)") && !rowText.contains("millions")) {
                                    value = value.multiply(new BigDecimal("1000"));
                                    logger.info("Converting from billions to millions: {}", value);
                                }
                            } else if (rowText.contains("million") || rowText.contains("(millions)") || value.compareTo(new BigDecimal("1000")) > 0) {
                                // Already in millions or large number assumed to be millions
                                logger.info("Value assumed to be in millions: {}", value);
                            }

                            return value;
                        }
                    }
                }
            }
        }

        logger.warn("Could not find {} in any table", metricType);
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
