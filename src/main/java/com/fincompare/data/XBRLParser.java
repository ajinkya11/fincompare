package com.fincompare.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincompare.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class XBRLParser {
    private static final Logger logger = LoggerFactory.getLogger(XBRLParser.class);

    private final ObjectMapper objectMapper;

    // Common XBRL tags for financial data
    private static final Map<String, List<String>> XBRL_TAGS = new HashMap<>();

    static {
        // Revenue tags (in order of preference)
        XBRL_TAGS.put("revenue", Arrays.asList(
                "Revenues", "RevenueFromContractWithCustomerExcludingAssessedTax",
                "SalesRevenueNet", "OperatingRevenues", "RevenueNet"
        ));

        // Operating expenses
        XBRL_TAGS.put("operatingExpenses", Arrays.asList(
                "OperatingExpenses", "CostsAndExpenses", "OperatingCostsAndExpenses"
        ));

        // Operating income
        XBRL_TAGS.put("operatingIncome", Arrays.asList(
                "OperatingIncomeLoss", "OperatingIncome"
        ));

        // Net income
        XBRL_TAGS.put("netIncome", Arrays.asList(
                "NetIncomeLoss", "ProfitLoss", "NetIncomeLossAvailableToCommonStockholdersBasic"
        ));

        // Total assets
        XBRL_TAGS.put("totalAssets", Arrays.asList(
                "Assets"
        ));

        // Total liabilities
        XBRL_TAGS.put("totalLiabilities", Arrays.asList(
                "Liabilities", "LiabilitiesNoncurrent", "LiabilitiesCurrent"
        ));

        // Stockholders equity
        XBRL_TAGS.put("totalEquity", Arrays.asList(
                "StockholdersEquity", "StockholdersEquityIncludingPortionAttributableToNoncontrollingInterest"
        ));

        // Cash and equivalents
        XBRL_TAGS.put("cashAndEquivalents", Arrays.asList(
                "CashAndCashEquivalentsAtCarryingValue", "Cash"
        ));

        // Operating cash flow
        XBRL_TAGS.put("operatingCashFlow", Arrays.asList(
                "NetCashProvidedByUsedInOperatingActivities"
        ));

        // Investing cash flow
        XBRL_TAGS.put("investingCashFlow", Arrays.asList(
                "NetCashProvidedByUsedInInvestingActivities"
        ));

        // Financing cash flow
        XBRL_TAGS.put("financingCashFlow", Arrays.asList(
                "NetCashProvidedByUsedInFinancingActivities"
        ));
    }

    public XBRLParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Parse company facts JSON from SEC API
     */
    public CompanyFinancialData parseCompanyFacts(String jsonData, String ticker, int years) throws IOException {
        logger.info("Parsing company facts for ticker: {}", ticker);

        JsonNode root = objectMapper.readTree(jsonData);

        CompanyFinancialData companyData = new CompanyFinancialData();

        // Extract company info
        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setTickerSymbol(ticker);
        companyInfo.setCompanyName(root.path("entityName").asText());
        companyInfo.setCik(root.path("cik").asText());
        companyData.setCompanyInfo(companyInfo);

        // Extract financial facts
        JsonNode facts = root.path("facts");
        JsonNode usGaap = facts.path("us-gaap");

        // Get fiscal years from the data
        Set<String> fiscalYears = extractFiscalYears(usGaap, years);

        // Parse data for each year
        for (String year : fiscalYears) {
            YearlyFinancialData yearlyData = new YearlyFinancialData(year);

            // Parse income statement
            IncomeStatement incomeStatement = parseIncomeStatement(usGaap, year);
            yearlyData.setIncomeStatement(incomeStatement);

            // Parse balance sheet
            BalanceSheet balanceSheet = parseBalanceSheet(usGaap, year);
            yearlyData.setBalanceSheet(balanceSheet);

            // Parse cash flow statement
            CashFlowStatement cashFlow = parseCashFlowStatement(usGaap, year);
            yearlyData.setCashFlowStatement(cashFlow);

            // Parse airline operational data
            AirlineOperationalData operationalData = parseOperationalData(facts, year);
            yearlyData.setOperationalData(operationalData);

            companyData.addYearData(yearlyData);
        }

        logger.info("Successfully parsed financial data for {} years", fiscalYears.size());
        return companyData;
    }

    /**
     * Extract available fiscal years from the data
     */
    private Set<String> extractFiscalYears(JsonNode usGaap, int maxYears) {
        Set<String> allYears = new TreeSet<>(Collections.reverseOrder());

        // Check ALL revenue tags to find all available years (don't break after first tag)
        List<String> revenueTags = XBRL_TAGS.get("revenue");
        int tagsChecked = 0;
        int tagsWithData = 0;

        for (String tag : revenueTags) {
            if (usGaap.has(tag)) {
                tagsChecked++;
                JsonNode units = usGaap.get(tag).path("units");
                if (units.has("USD")) {
                    Set<String> yearsFromThisTag = new TreeSet<>(Collections.reverseOrder());
                    for (JsonNode entry : units.get("USD")) {
                        String fy = entry.path("fy").asText();
                        String form = entry.path("form").asText();
                        String frame = entry.path("frame").asText();
                        String fp = entry.path("fp").asText();

                        // Collect ALL 10-K years regardless of frame/fp
                        // Companies may use different formats in different years
                        if (!fy.isEmpty() && "10-K".equals(form)) {
                            allYears.add(fy);
                            yearsFromThisTag.add(fy);
                            logger.debug("Tag {}: Found fiscal year {} (frame: {}, fp: {})", tag, fy, frame, fp);
                        }
                    }

                    if (!yearsFromThisTag.isEmpty()) {
                        tagsWithData++;
                        logger.info("Tag '{}' has 10-K data for years: {}", tag, yearsFromThisTag);
                    }
                }
                // REMOVED: Don't break! Check ALL tags to get complete year coverage
            }
        }

        logger.info("Checked {} revenue tags, found data in {} tags", tagsChecked, tagsWithData);
        logger.info("Available fiscal years with 10-K data (combined from all tags): {}", allYears);

        // Return only the requested number of most recent years
        Set<String> result = new TreeSet<>(Collections.reverseOrder());
        int count = 0;
        for (String year : allYears) {
            if (count >= maxYears) break;
            result.add(year);
            count++;
        }

        logger.info("Selected fiscal years for analysis: {}", result);
        return result;
    }

    /**
     * Parse income statement data
     */
    private IncomeStatement parseIncomeStatement(JsonNode usGaap, String fiscalYear) {
        IncomeStatement stmt = new IncomeStatement();
        stmt.setFiscalYear(fiscalYear);

        stmt.setTotalRevenue(extractValue(usGaap, XBRL_TAGS.get("revenue"), fiscalYear));
        stmt.setOperatingExpenses(extractValue(usGaap, XBRL_TAGS.get("operatingExpenses"), fiscalYear));
        stmt.setOperatingIncome(extractValue(usGaap, XBRL_TAGS.get("operatingIncome"), fiscalYear));
        stmt.setNetIncome(extractValue(usGaap, XBRL_TAGS.get("netIncome"), fiscalYear));

        // Try to extract detailed line items
        stmt.setFuelCosts(extractValue(usGaap, Arrays.asList("FuelCosts", "CostOfGoodsAndServicesSold"), fiscalYear));
        stmt.setLaborCosts(extractValue(usGaap, Arrays.asList("LaborAndRelatedExpense", "SalariesAndWages"), fiscalYear));
        stmt.setDepreciationAmortization(extractValue(usGaap, Arrays.asList("DepreciationDepletionAndAmortization", "Depreciation"), fiscalYear));
        stmt.setInterestExpense(extractValue(usGaap, Arrays.asList("InterestExpense"), fiscalYear));

        // EPS
        stmt.setBasicEPS(extractValue(usGaap, Arrays.asList("EarningsPerShareBasic"), fiscalYear));
        stmt.setDilutedEPS(extractValue(usGaap, Arrays.asList("EarningsPerShareDiluted"), fiscalYear));

        // Calculate gross profit if not directly available
        if (stmt.getTotalRevenue() != null && stmt.getOperatingExpenses() != null) {
            stmt.setGrossProfit(stmt.getTotalRevenue().subtract(stmt.getOperatingExpenses()));
        }

        return stmt;
    }

    /**
     * Parse balance sheet data
     */
    private BalanceSheet parseBalanceSheet(JsonNode usGaap, String fiscalYear) {
        BalanceSheet bs = new BalanceSheet();
        bs.setFiscalYear(fiscalYear);

        // Assets
        bs.setTotalAssets(extractValue(usGaap, XBRL_TAGS.get("totalAssets"), fiscalYear));
        bs.setCurrentAssets(extractValue(usGaap, Arrays.asList("AssetsCurrent"), fiscalYear));
        bs.setCashAndEquivalents(extractValue(usGaap, XBRL_TAGS.get("cashAndEquivalents"), fiscalYear));
        bs.setAccountsReceivable(extractValue(usGaap, Arrays.asList("AccountsReceivableNetCurrent"), fiscalYear));
        bs.setInventory(extractValue(usGaap, Arrays.asList("InventoryNet"), fiscalYear));
        bs.setPropertyPlantEquipment(extractValue(usGaap, Arrays.asList("PropertyPlantAndEquipmentGross"), fiscalYear));
        bs.setNetPPE(extractValue(usGaap, Arrays.asList("PropertyPlantAndEquipmentNet"), fiscalYear));

        // Liabilities
        bs.setTotalLiabilities(extractValue(usGaap, XBRL_TAGS.get("totalLiabilities"), fiscalYear));
        bs.setCurrentLiabilities(extractValue(usGaap, Arrays.asList("LiabilitiesCurrent"), fiscalYear));
        bs.setLongTermDebt(extractValue(usGaap, Arrays.asList("LongTermDebtNoncurrent", "LongTermDebt"), fiscalYear));
        bs.setShortTermDebt(extractValue(usGaap, Arrays.asList("ShortTermBorrowings", "DebtCurrent"), fiscalYear));

        // Equity
        bs.setTotalEquity(extractValue(usGaap, XBRL_TAGS.get("totalEquity"), fiscalYear));
        bs.setRetainedEarnings(extractValue(usGaap, Arrays.asList("RetainedEarningsAccumulatedDeficit"), fiscalYear));

        return bs;
    }

    /**
     * Parse cash flow statement data
     */
    private CashFlowStatement parseCashFlowStatement(JsonNode usGaap, String fiscalYear) {
        CashFlowStatement cf = new CashFlowStatement();
        cf.setFiscalYear(fiscalYear);

        cf.setOperatingCashFlow(extractValue(usGaap, XBRL_TAGS.get("operatingCashFlow"), fiscalYear));
        cf.setInvestingCashFlow(extractValue(usGaap, XBRL_TAGS.get("investingCashFlow"), fiscalYear));
        cf.setFinancingCashFlow(extractValue(usGaap, XBRL_TAGS.get("financingCashFlow"), fiscalYear));

        cf.setCapitalExpenditures(extractValue(usGaap,
                Arrays.asList("PaymentsToAcquirePropertyPlantAndEquipment",
                            "PaymentsForCapitalImprovements",
                            "CapitalExpenditureIncurredButNotYetPaid"), fiscalYear));
        cf.setDepreciationAmortization(extractValue(usGaap,
                Arrays.asList("DepreciationDepletionAndAmortization",
                            "Depreciation",
                            "DepreciationAndAmortization"), fiscalYear));

        // Additional cash flow components
        cf.setNetIncome(extractValue(usGaap, XBRL_TAGS.get("netIncome"), fiscalYear));
        cf.setDebtIssuance(extractValue(usGaap,
                Arrays.asList("ProceedsFromIssuanceOfLongTermDebt",
                            "ProceedsFromDebtNetOfIssuanceCosts"), fiscalYear));
        cf.setDebtRepayment(extractValue(usGaap,
                Arrays.asList("RepaymentsOfLongTermDebt",
                            "RepaymentsOfDebt"), fiscalYear));

        // Calculate free cash flow
        if (cf.getOperatingCashFlow() != null && cf.getCapitalExpenditures() != null) {
            cf.setFreeCashFlow(cf.getOperatingCashFlow().subtract(cf.getCapitalExpenditures().abs()));
        }

        logger.debug("Cash flow data - Operating: {}, Investing: {}, Financing: {}, CapEx: {}, FCF: {}",
                cf.getOperatingCashFlow(), cf.getInvestingCashFlow(), cf.getFinancingCashFlow(),
                cf.getCapitalExpenditures(), cf.getFreeCashFlow());

        return cf;
    }

    /**
     * Parse airline operational data (if available)
     */
    private AirlineOperationalData parseOperationalData(JsonNode facts, String fiscalYear) {
        AirlineOperationalData data = new AirlineOperationalData();
        data.setFiscalYear(fiscalYear);

        logger.debug("Parsing operational data for fiscal year: {}", fiscalYear);
        logger.debug("Available taxonomies: {}", String.join(", ",
            facts.fieldNames().hasNext() ? iteratorToList(facts.fieldNames()) : Arrays.asList("none")));

        // Check for custom taxonomy (airline-specific metrics)
        for (Iterator<String> it = facts.fieldNames(); it.hasNext(); ) {
            String taxonomy = it.next();
            if (!taxonomy.equals("us-gaap") && !taxonomy.equals("dei")) {
                JsonNode customFacts = facts.get(taxonomy);

                // Log available fields in custom taxonomy
                List<String> fields = customFacts.fieldNames().hasNext() ?
                    iteratorToList(customFacts.fieldNames()) : new ArrayList<>();

                logger.info("Found custom taxonomy '{}' with {} fields", taxonomy, fields.size());

                if (!fields.isEmpty()) {
                    // Show first 10 fields as sample
                    int sampleSize = Math.min(10, fields.size());
                    logger.info("  Sample fields (first {}): {}", sampleSize,
                        String.join(", ", fields.subList(0, sampleSize)));
                }

                // Look for airline-specific operational metrics with various naming conventions
                data.setAvailableSeatMiles(extractValue(customFacts,
                        Arrays.asList("AvailableSeatMiles", "ASM", "AvailableSeatMilesASM",
                                    "ScheduledAvailableSeatMiles"), fiscalYear));
                data.setRevenuePassengerMiles(extractValue(customFacts,
                        Arrays.asList("RevenuePassengerMiles", "RPM", "RevenuePassengerMilesRPM",
                                    "ScheduledRevenuePassengerMiles"), fiscalYear));
                data.setPassengersCarried(extractLongValue(customFacts,
                        Arrays.asList("PassengersCarried", "NumberOfPassengers", "PassengersBoarded",
                                    "ScheduledPassengers"), fiscalYear));
                data.setCargoTonMiles(extractValue(customFacts,
                        Arrays.asList("CargoTonMiles", "FreightTonMiles"), fiscalYear));
                data.setAvailableTonMiles(extractValue(customFacts,
                        Arrays.asList("AvailableTonMiles", "ATM"), fiscalYear));
                data.setFleetSize(extractIntValue(customFacts,
                        Arrays.asList("NumberOfAircraft", "FleetCount", "AircraftInService"), fiscalYear));
            }
        }

        // Extract from us-gaap if available
        JsonNode usGaap = facts.path("us-gaap");
        data.setFullTimeEmployees(extractIntValue(usGaap, Arrays.asList("NumberOfEmployees"), fiscalYear));

        // Log what we found
        logger.info("Operational data extracted - ASM: {}, RPM: {}, Passengers: {}, Employees: {}",
                data.getAvailableSeatMiles(), data.getRevenuePassengerMiles(),
                data.getPassengersCarried(), data.getFullTimeEmployees());

        // Warn if key airline metrics are missing
        if (data.getAvailableSeatMiles() == null && data.getRevenuePassengerMiles() == null) {
            logger.warn("Airline operational metrics (ASM/RPM) not found in XBRL companyfacts. " +
                       "These metrics may only be available in the full 10-K filing documents, " +
                       "not in the structured companyfacts API endpoint.");
        }

        return data;
    }

    /**
     * Helper method to convert Iterator to List for logging
     */
    private List<String> iteratorToList(Iterator<String> iterator) {
        List<String> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Extract a BigDecimal value from XBRL data
     */
    private BigDecimal extractValue(JsonNode parent, List<String> possibleTags, String fiscalYear) {
        for (String tag : possibleTags) {
            if (parent.has(tag)) {
                JsonNode concept = parent.get(tag);
                JsonNode units = concept.path("units");

                // Try USD first
                if (units.has("USD")) {
                    BigDecimal value = findValueForFiscalYear(units.get("USD"), fiscalYear);
                    if (value != null) {
                        return value;
                    }
                }

                // Try other unit types
                for (Iterator<String> it = units.fieldNames(); it.hasNext(); ) {
                    String unitType = it.next();
                    BigDecimal value = findValueForFiscalYear(units.get(unitType), fiscalYear);
                    if (value != null) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extract an Integer value
     */
    private Integer extractIntValue(JsonNode parent, List<String> possibleTags, String fiscalYear) {
        BigDecimal value = extractValue(parent, possibleTags, fiscalYear);
        return value != null ? value.intValue() : null;
    }

    /**
     * Extract a Long value
     */
    private Long extractLongValue(JsonNode parent, List<String> possibleTags, String fiscalYear) {
        BigDecimal value = extractValue(parent, possibleTags, fiscalYear);
        return value != null ? value.longValue() : null;
    }

    /**
     * Find value for a specific fiscal year from an array of data points
     */
    private BigDecimal findValueForFiscalYear(JsonNode dataPoints, String fiscalYear) {
        BigDecimal preferredValue = null;
        BigDecimal fallbackValue = null;

        for (JsonNode point : dataPoints) {
            String fy = point.path("fy").asText();
            String form = point.path("form").asText();
            String frame = point.path("frame").asText();
            String fp = point.path("fp").asText();

            // Match fiscal year and look for 10-K filings
            if (fiscalYear.equals(fy) && "10-K".equals(form)) {
                if (point.has("val")) {
                    BigDecimal value = new BigDecimal(point.get("val").asText());

                    // Prefer CY (Calendar Year) frames or FY (Fiscal Year) period
                    if (frame.contains("CY") || "FY".equals(fp)) {
                        preferredValue = value;
                        // Keep looking for best match but remember this
                    } else if (fallbackValue == null) {
                        // Accept any 10-K data as fallback
                        fallbackValue = value;
                    }
                }
            }
        }

        // Return preferred value if found, otherwise fallback
        return preferredValue != null ? preferredValue : fallbackValue;
    }
}
