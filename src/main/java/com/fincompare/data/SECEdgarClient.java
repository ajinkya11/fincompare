package com.fincompare.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SECEdgarClient {
    private static final Logger logger = LoggerFactory.getLogger(SECEdgarClient.class);

    private static final String SEC_BASE_URL = "https://data.sec.gov";
    private static final String SEC_COMPANY_TICKERS_URL = "https://www.sec.gov/files/company_tickers.json";
    private static final String USER_AGENT = "FinCompare/1.0 (Financial Analysis Tool; mailto:your-email@example.com)";

    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    public SECEdgarClient() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Lookup CIK (Central Index Key) from ticker symbol
     */
    public String getCIKFromTicker(String ticker) throws IOException, ParseException {
        logger.info("Looking up CIK for ticker: {}", ticker);

        HttpGet request = new HttpGet(SEC_COMPANY_TICKERS_URL);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                throw new IOException("Failed to fetch company tickers. Status: " + response.getCode());
            }

            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonNode root = objectMapper.readTree(jsonResponse);

            // The JSON structure is an object where keys are indices
            for (JsonNode node : root) {
                if (node.has("ticker") && ticker.equalsIgnoreCase(node.get("ticker").asText())) {
                    String cik = String.format("%010d", node.get("cik_str").asInt());
                    logger.info("Found CIK {} for ticker {}", cik, ticker);
                    return cik;
                }
            }

            throw new IOException("Ticker symbol not found: " + ticker);
        }
    }

    /**
     * Fetch 10-K filings for a company
     */
    public List<SECFilingMetadata> get10KFilings(String cik, int maxFilings) throws IOException, ParseException {
        logger.info("Fetching 10-K filings for CIK: {}", cik);

        // Ensure CIK is zero-padded to 10 digits for SEC API
        String paddedCik = padCik(cik);
        String url = String.format("%s/submissions/CIK%s.json", SEC_BASE_URL, paddedCik);

        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                throw new IOException("Failed to fetch filings for CIK " + cik + ". Status: " + response.getCode());
            }

            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonNode root = objectMapper.readTree(jsonResponse);

            List<SECFilingMetadata> filings = new ArrayList<>();
            JsonNode recentFilings = root.path("filings").path("recent");

            if (!recentFilings.isMissingNode()) {
                JsonNode accessionNumbers = recentFilings.path("accessionNumber");
                JsonNode filingDates = recentFilings.path("filingDate");
                JsonNode reportDates = recentFilings.path("reportDate");
                JsonNode forms = recentFilings.path("form");
                JsonNode primaryDocuments = recentFilings.path("primaryDocument");

                int count = 0;
                for (int i = 0; i < forms.size() && count < maxFilings; i++) {
                    String form = forms.get(i).asText();

                    if ("10-K".equals(form)) {
                        SECFilingMetadata filing = new SECFilingMetadata();
                        filing.setAccessionNumber(accessionNumbers.get(i).asText());
                        filing.setFilingDate(filingDates.get(i).asText());
                        filing.setReportDate(reportDates.get(i).asText());
                        filing.setFormType(form);

                        // Construct document URL
                        String accessionNoHyphens = filing.getAccessionNumber().replace("-", "");
                        String baseUrl = String.format(
                                "https://www.sec.gov/Archives/edgar/data/%s/%s",
                                cik.replaceFirst("^0+", ""),
                                accessionNoHyphens
                        );

                        filing.setDocumentUrl(baseUrl + "/" + primaryDocuments.get(i).asText());

                        // Extract fiscal year from report date
                        String reportDate = filing.getReportDate();
                        if (reportDate != null && reportDate.length() >= 4) {
                            filing.setFiscalYear(reportDate.substring(0, 4));
                        }

                        filings.add(filing);
                        count++;
                        logger.info("Found 10-K filing: {}", filing);
                    }
                }
            }

            logger.info("Found {} 10-K filings", filings.size());
            return filings;
        }
    }

    /**
     * Download XBRL filing document
     */
    public String downloadFiling(String url) throws IOException, ParseException {
        logger.info("Downloading filing from: {}", url);

        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept", "*/*");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                throw new IOException("Failed to download filing. Status: " + response.getCode());
            }

            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * Get XBRL instance document URL for a filing
     */
    public String getXBRLInstanceUrl(String cik, String accessionNumber) {
        String accessionNoHyphens = accessionNumber.replace("-", "");
        String cidNoLeadingZeros = cik.replaceFirst("^0+", "");

        // Common patterns for XBRL instance documents
        String baseUrl = String.format(
                "https://www.sec.gov/cgi-bin/viewer?action=view&cik=%s&accession_number=%s&xbrl_type=v",
                cidNoLeadingZeros,
                accessionNumber
        );

        return baseUrl;
    }

    /**
     * Fetch financial data API endpoint (Company Concept)
     */
    public String fetchCompanyConcept(String cik, String taxonomy, String tag) throws IOException, ParseException {
        String url = String.format("%s/api/xbrl/companyconcept/CIK%s/%s/%s.json",
                SEC_BASE_URL, cik, taxonomy, tag);

        logger.info("Fetching company concept: {}", url);

        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                logger.warn("Failed to fetch concept {}:{} for CIK {}. Status: {}",
                        taxonomy, tag, cik, response.getCode());
                return null;
            }

            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * Fetch company facts (all financial data)
     */
    public String fetchCompanyFacts(String cik) throws IOException, ParseException {
        // Ensure CIK is zero-padded to 10 digits for SEC API
        String paddedCik = padCik(cik);
        String url = String.format("%s/api/xbrl/companyfacts/CIK%s.json", SEC_BASE_URL, paddedCik);

        logger.info("Fetching company facts for CIK: {}", paddedCik);

        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                throw new IOException("Failed to fetch company facts for CIK " + cik +
                        ". Status: " + response.getCode());
            }

            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * Ensures CIK is zero-padded to 10 digits as required by SEC API
     * @param cik The CIK with or without leading zeros
     * @return Zero-padded CIK (10 digits)
     */
    private String padCik(String cik) {
        // Remove any existing leading zeros and "CIK" prefix if present
        String numericCik = cik.replaceAll("^(CIK)?0+", "");

        // Pad to 10 digits
        return String.format("%010d", Long.parseLong(numericCik));
    }

    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
