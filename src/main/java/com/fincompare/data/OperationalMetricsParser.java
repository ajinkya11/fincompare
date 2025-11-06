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
    // ASM patterns (Available Seat Miles)
    private static final Pattern[] ASM_PATTERNS = {
            Pattern.compile("available\\s+seat\\s+miles.*?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|billion)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ASMs?.*?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|billion)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("capacity.*?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|billion)?.*?ASM", Pattern.CASE_INSENSITIVE)
    };

    // RPM patterns (Revenue Passenger Miles)
    private static final Pattern[] RPM_PATTERNS = {
            Pattern.compile("revenue\\s+passenger\\s+miles.*?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|billion)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("RPMs?.*?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|billion)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("traffic.*?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?)\\s*(million|billion)?.*?RPM", Pattern.CASE_INSENSITIVE)
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
     */
    private BigDecimal extractMetric(String text, Pattern[] patterns, String metricName) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    String numberStr = matcher.group(1).replace(",", "");
                    BigDecimal value = new BigDecimal(numberStr);

                    // Check for unit multiplier
                    if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                        String unit = matcher.group(2).toLowerCase();
                        if (unit.contains("billion")) {
                            value = value.multiply(new BigDecimal("1000")); // Convert to millions
                        } else if (unit.contains("thousand")) {
                            value = value.divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP); // Convert to millions
                        }
                    }

                    logger.debug("Found {} value: {}", metricName, value);
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
