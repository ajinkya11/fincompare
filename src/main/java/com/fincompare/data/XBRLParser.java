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
        Set<String> years = new TreeSet<>(Collections.reverseOrder());

        // Look at revenue data to find available years
        List<String> revenueTags = XBRL_TAGS.get("revenue");
        for (String tag : revenueTags) {
            if (usGaap.has(tag)) {
                JsonNode units = usGaap.get(tag).path("units");
                if (units.has("USD")) {
                    for (JsonNode entry : units.get("USD")) {
                        String fy = entry.path("fy").asText();
                        String form = entry.path("form").asText();
                        // Only include 10-K filings (annual reports)
                        if (!fy.isEmpty() && "10-K".equals(form)) {
                            years.add(fy);
                        }
                    }
                }
                break;
            }
        }

        // Return only the requested number of most recent years
        Set<String> result = new TreeSet<>(Collections.reverseOrder());
        int count = 0;
        for (String year : years) {
            if (count >= maxYears) break;
            result.add(year);
            count++;
        }

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

        cf.setCapitalExpenditures(extractValue(usGaap, Arrays.asList("PaymentsToAcquirePropertyPlantAndEquipment"), fiscalYear));
        cf.setDepreciationAmortization(extractValue(usGaap, Arrays.asList("DepreciationDepletionAndAmortization"), fiscalYear));

        // Calculate free cash flow
        if (cf.getOperatingCashFlow() != null && cf.getCapitalExpenditures() != null) {
            cf.setFreeCashFlow(cf.getOperatingCashFlow().subtract(cf.getCapitalExpenditures().abs()));
        }

        return cf;
    }

    /**
     * Parse airline operational data (if available)
     */
    private AirlineOperationalData parseOperationalData(JsonNode facts, String fiscalYear) {
        AirlineOperationalData data = new AirlineOperationalData();
        data.setFiscalYear(fiscalYear);

        // Check for custom taxonomy (airline-specific metrics)
        for (Iterator<String> it = facts.fieldNames(); it.hasNext(); ) {
            String taxonomy = it.next();
            if (!taxonomy.equals("us-gaap") && !taxonomy.equals("dei")) {
                JsonNode customFacts = facts.get(taxonomy);

                // Look for airline-specific operational metrics
                data.setAvailableSeatMiles(extractValue(customFacts,
                        Arrays.asList("AvailableSeatMiles", "ASM"), fiscalYear));
                data.setRevenuePassengerMiles(extractValue(customFacts,
                        Arrays.asList("RevenuePassengerMiles", "RPM"), fiscalYear));
                data.setPassengersCarried(extractLongValue(customFacts,
                        Arrays.asList("PassengersCarried", "NumberOfPassengers"), fiscalYear));
            }
        }

        // Extract from us-gaap if available
        JsonNode usGaap = facts.path("us-gaap");
        data.setFullTimeEmployees(extractIntValue(usGaap, Arrays.asList("NumberOfEmployees"), fiscalYear));

        return data;
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
        for (JsonNode point : dataPoints) {
            String fy = point.path("fy").asText();
            String form = point.path("form").asText();

            // Match fiscal year and prefer 10-K filings
            if (fiscalYear.equals(fy) && "10-K".equals(form)) {
                if (point.has("val")) {
                    return new BigDecimal(point.get("val").asText());
                }
            }
        }
        return null;
    }
}
