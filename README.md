# Airline Financial Analyzer CLI

A comprehensive command-line tool for comparative financial analysis of airline companies using SEC 10-K filings.

## Features

- **Automated Data Fetching**: Retrieves financial data directly from SEC EDGAR database
- **Comprehensive Financial Metrics**: Analyzes income statements, balance sheets, and cash flow statements
- **Airline-Specific Metrics**: Calculates RASM, CASM, load factors, and other industry-specific KPIs
- **Comparative Analysis**: Side-by-side comparison of two airline companies
- **Multiple Output Formats**: Console reports, CSV, and JSON exports
- **Intelligent Caching**: Reduces API calls by caching downloaded SEC filings
- **Color-Coded Output**: Visual indicators for better/worse metrics

## Financial Metrics Analyzed

### Income Statement Metrics
- Revenue, Operating Expenses, Operating Income, Net Income
- Gross Margin %, Operating Margin %, Net Margin %, EBITDA Margin %
- EPS (Basic & Diluted)
- Year-over-year growth rates

### Balance Sheet Metrics
- Total Assets, Current Assets, Total Liabilities, Total Equity
- Working Capital, Current Ratio, Quick Ratio
- Debt-to-Equity Ratio, Book Value per Share

### Cash Flow Metrics
- Operating Cash Flow, Free Cash Flow
- Cash Flow from Investing/Financing Activities
- Cash Conversion Ratio

### Profitability Ratios
- Return on Assets (ROA)
- Return on Equity (ROE)
- Return on Invested Capital (ROIC)

### Airline-Specific Metrics
- **RASM**: Revenue per Available Seat Mile
- **CASM**: Cost per Available Seat Mile
- **CASM-ex**: CASM excluding fuel costs
- **Load Factor**: Passenger seat utilization percentage
- **Yield**: Revenue per Revenue Passenger Mile
- **Break-even Load Factor**: Minimum load factor for profitability

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Active internet connection (for fetching SEC data)

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd fincompare
```

2. Build the project:
```bash
mvn clean package
```

3. Create an executable script (optional):
```bash
# On Linux/Mac
echo '#!/bin/bash' > fincompare
echo 'java -jar target/airline-financial-analyzer-1.0.0.jar "$@"' >> fincompare
chmod +x fincompare

# On Windows
echo 'java -jar target/airline-financial-analyzer-1.0.0.jar %*' > fincompare.bat
```

## Usage

### Basic Comparison

Compare two airline companies:

```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU
```

Or using the script:
```bash
./fincompare compare UAL JBLU
```

### Multi-Year Analysis

Analyze the last 3 fiscal years:

```bash
./fincompare compare UAL JBLU --years 3
```

### Export Results

Export to CSV:
```bash
./fincompare compare UAL JBLU --output results.csv
```

Export to JSON:
```bash
./fincompare compare UAL JBLU --output results.json --json
```

### View Help

```bash
./fincompare compare --help
```

## Example Output

```
================================================================================
          AIRLINE FINANCIAL COMPARATIVE ANALYSIS
================================================================================

Executive Summary
-----------------
Comparative Analysis: UAL vs JBLU

UAL has 5 key strengths, while JBLU has 4 key strengths.

Financial Metrics Comparison
-----------------------------

INCOME STATEMENT
Metric                              |                  UAL |                 JBLU
-------------------------------------------------------------------------------
Total Revenue                       |              $50.98B |              $9.74B
Operating Expenses                  |              $47.39B |              $9.38B
Operating Income                    |               $3.59B |             $364.00M
Net Income                          |               $2.52B |             $151.00M

FINANCIAL RATIOS
Operating Margin %                  |                 7.05 |                 3.74
Net Margin %                        |                 4.94 |                 1.55
ROE %                               |                45.67 |                 8.92
Current Ratio                       |                 0.89 |                 0.67

AIRLINE OPERATIONAL METRICS
Load Factor %                       |                84.50 |                83.20
RASM (cents)                        |                15.67 |                12.34
CASM (cents)                        |                14.56 |                11.89
```

## Common Airline Tickers

- **UAL**: United Airlines
- **DAL**: Delta Air Lines
- **AAL**: American Airlines
- **LUV**: Southwest Airlines
- **JBLU**: JetBlue Airways
- **ALK**: Alaska Air Group
- **SAVE**: Spirit Airlines

## Architecture

The application is built with a modular architecture:

```
com.fincompare/
├── cli/              # Command-line interface (Picocli)
├── data/             # SEC EDGAR client and XBRL parser
├── models/           # Financial data POJOs
├── metrics/          # Financial calculation engines
├── analysis/         # Comparative analysis logic
├── reporting/        # Report generation (Console, CSV, JSON)
└── util/             # Utilities (caching, etc.)
```

## Data Source

All financial data is fetched from the SEC EDGAR database using the official SEC API:
- Company Facts API: `https://data.sec.gov/api/xbrl/companyfacts/`
- Company Tickers: `https://www.sec.gov/files/company_tickers.json`

## Caching

Downloaded SEC filings are cached in your system's temporary directory to improve performance:
- Location: `{system-temp-dir}/fincompare-cache/`
- Cache keys are MD5-hashed for consistency
- Clear cache by deleting the cache directory

## Error Handling

The tool includes comprehensive error handling for:
- Invalid ticker symbols
- Network failures (with retry logic)
- Missing financial data
- Parsing errors

## Testing

Run the test suite:

```bash
mvn test
```

Run with coverage:

```bash
mvn test jacoco:report
```

## Logging

Logs are written to:
- Console: INFO level
- File: `logs/fincompare.log` with daily rotation

Configure logging in `src/main/resources/logback.xml`

## Important Notes

### SEC API Compliance
- The tool includes a proper User-Agent header as required by SEC
- Be respectful of SEC's API rate limits
- Data is cached to minimize API calls

### Data Accuracy
- Revenue and other financial metrics are extracted from aggregate XBRL concepts
- The tool implements validation checks to prevent double-counting
- All calculations use `BigDecimal` for precision
- Metrics are validated for reasonableness (e.g., margins should be < 100%)

### Airline-Specific Data
- Some airline-specific operational metrics (ASM, RPM) may not be available for all companies
- These metrics are typically found in company-specific XBRL taxonomies
- If unavailable, the tool gracefully handles missing data

## Troubleshooting

### "Ticker symbol not found"
- Verify the ticker symbol is correct
- Ensure the company files with the SEC
- Try using the full company name search

### "Failed to fetch company facts"
- Check your internet connection
- The SEC API may be temporarily unavailable
- Try again after a few minutes

### Missing operational metrics
- Some companies don't include operational metrics in their XBRL filings
- Check the company's 10-K filing directly for this data
- The tool will still calculate standard financial metrics

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is provided for educational and research purposes.

## Disclaimer

This tool is for informational purposes only and should not be considered financial advice. Always verify data with official SEC filings and consult with financial professionals before making investment decisions.

## Author

Built with Spring Boot, Picocli, and the SEC EDGAR API.

## Version History

### v1.0.0 (2025)
- Initial release
- Support for basic comparative analysis
- Income statement, balance sheet, and cash flow metrics
- Airline-specific operational metrics
- CSV and JSON export capabilities
- Caching mechanism