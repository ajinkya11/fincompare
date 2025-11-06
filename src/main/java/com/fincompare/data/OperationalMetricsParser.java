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

            // Extract text from the document (focusing on tables and paragraphs)
            String text = doc.body().text();

            // Also extract from tables which often contain operational data
            Elements tables = doc.select("table");
            StringBuilder tableText = new StringBuilder();
            for (Element table : tables) {
                tableText.append(table.text()).append(" ");
            }
            String fullText = text + " " + tableText.toString();

            // Extract metrics using patterns
            data.setAvailableSeatMiles(extractMetric(fullText, ASM_PATTERNS, "ASM"));
            data.setRevenuePassengerMiles(extractMetric(fullText, RPM_PATTERNS, "RPM"));
            data.setLoadFactor(extractLoadFactor(fullText));
            data.setPassengersCarried(extractPassengers(fullText));

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
