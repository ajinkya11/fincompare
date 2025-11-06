# DOT/BTS Data Integration

## Overview

This application now includes comprehensive integration with the **U.S. Department of Transportation Bureau of Transportation Statistics (DOT/BTS)**, providing extensive airline operational and performance data that supplements SEC 10-K filings.

## What Data is Included

### 1. T-100 Operational Data (Form 41)
Monthly operational statistics reported by airlines to the DOT:

**Traffic Metrics:**
- Available Seat Miles (ASM)
- Revenue Passenger Miles (RPM)
- Passenger Load Factor
- Passengers Carried
- Departures Performed

**Cargo Operations:**
- Freight Ton Miles
- Mail Ton Miles
- Cargo Ton Miles (combined)
- Available Ton Miles

**Geographic Breakdown:**
- Domestic vs International passengers
- Domestic vs International capacity (ASM)

**Flight Operations:**
- Departures Scheduled
- Departures Performed
- Aircraft Hours (Block Hours)

### 2. On-Time Performance Data
Comprehensive flight quality metrics:

**Performance Indicators:**
- Total Flights
- On-Time Flights (within 15 minutes of schedule)
- On-Time Performance Percentage
- Delayed Flights
- Cancelled Flights (with cancellation rate)
- Diverted Flights

**Delay Analysis by Cause:**
- Carrier Delay Minutes (airline-caused)
- Weather Delay Minutes
- NAS Delay Minutes (National Aviation System - ATC, airport ops)
- Security Delay Minutes
- Late Aircraft Delay Minutes (previous flight delays)

**Averages:**
- Average Arrival Delay (minutes)
- Average Departure Delay (minutes)
- Average Taxi-Out Time
- Average Taxi-In Time

**Delay Distribution:**
- Flights delayed 0-15 minutes
- Flights delayed 15-30 minutes
- Flights delayed 30-60 minutes
- Flights delayed 60-120 minutes
- Flights delayed 120+ minutes

### 3. Customer Service Metrics

**Baggage Handling:**
- Mishandled Baggage count
- Mishandled Baggage Rate (per 1,000 passengers)

**Customer Complaints:**
- Total Customer Complaints
- Complaint Rate (per 100,000 passengers)

### 4. Fleet Inventory (Schedule B-43)
Detailed aircraft information:
- Aircraft Registration (tail number)
- Aircraft Type and Model
- Manufacturer
- Seating Capacity
- Year Manufactured (for age calculation)
- Date Acquired
- Ownership Type (Owned/Leased)

## Data Integration Strategy

### Priority Hierarchy
The system uses a smart data merging strategy:

1. **XBRL Data** (highest priority) - Structured SEC company facts
2. **10-K HTML Data** (medium priority) - Extracted from 10-K documents
3. **DOT/BTS Data** (fills gaps) - Supplements missing metrics

### How It Works

```
┌─────────────────┐
│  SEC 10-K XBRL  │  Primary source for financial data
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  10-K HTML      │  Operational metrics extraction
│  Extraction     │  (enhanced parser)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  DOT/BTS API    │  Fills missing operational data
│  Integration    │  + adds performance metrics
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Data           │  Validates consistency
│  Validation     │  Cross-checks calculations
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Comprehensive  │  Complete airline profile
│  Airline Data   │
└─────────────────┘
```

### Merge Logic Example

For **Available Seat Miles (ASM)**:
1. Check if XBRL data has ASM (rare, custom taxonomy)
2. If not, check 10-K HTML extraction
3. If still missing, fetch from DOT T-100 data
4. Use the first available source

For **On-Time Performance** (unique to DOT):
- Always from DOT/BTS (not in SEC filings)
- New metrics added to the analysis

## New Command: `analyze`

### Usage

```bash
# Basic detailed analysis
fincompare analyze UAL

# With specific years
fincompare analyze UAL --years 3

# Include all detail sections
fincompare analyze UAL --all

# Specific sections
fincompare analyze JBLU --performance      # On-time performance
fincompare analyze DAL --monthly           # Monthly trends
fincompare analyze UAL --fleet             # Detailed fleet info

# Export to JSON
fincompare analyze UAL --all -o ual-report.json
```

### Command Options

| Option | Description |
|--------|-------------|
| `--years N` | Number of years to analyze (default: 3) |
| `--performance` | Include detailed on-time performance analysis |
| `--monthly` | Show monthly trend data (requires DOT/BTS) |
| `--fleet` | Include detailed fleet composition and analysis |
| `--all` | Include all available detail (performance + monthly + fleet) |
| `-o, --output FILE` | Export results to JSON file |

### Sample Output

```
================================================================================
                   COMPREHENSIVE AIRLINE ANALYSIS
================================================================================

Company: United Airlines Holdings, Inc.
Ticker:  UAL
CIK:     0001045810

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 FISCAL YEAR 2023
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

┌─ FINANCIAL STATEMENTS ─────────────────────────────────────────────────┐
│
│  INCOME STATEMENT
│  ───────────────────────────────────────────────────────────────────
│  Total Revenue:                              $53,716,000,000
│    - Passenger Revenue:                      $48,146,000,000
│    - Cargo Revenue:                           $2,045,000,000
│    - Other Revenue:                           $3,525,000,000
│  Operating Expenses:                         $50,123,000,000
│  Operating Income:                            $3,593,000,000
│  Net Income:                                  $2,517,000,000
│
│  BALANCE SHEET
│  ───────────────────────────────────────────────────────────────────
│  Total Assets:                               $76,456,000,000
│  Total Liabilities:                          $71,329,000,000
│  Total Equity:                                $5,127,000,000
│
└────────────────────────────────────────────────────────────────────────┘

┌─ OPERATIONAL METRICS ──────────────────────────────────────────────────┐
│
│  CAPACITY & TRAFFIC
│  ───────────────────────────────────────────────────────────────────
│  Available Seat Miles (ASM):                     257,941.00 millions
│  Revenue Passenger Miles (RPM):                  217,623.00 millions
│  Passenger Load Factor:                               84.37 %
│  Passengers Carried:                         148,256,000.00 passengers
│
│  CARGO OPERATIONS
│  ───────────────────────────────────────────────────────────────────
│  Cargo Ton Miles (CTM):                            3,845.00 millions
│  Available Ton Miles (ATM):                       17,236.00 millions
│  Cargo Load Factor:                                   22.31 %
│
└────────────────────────────────────────────────────────────────────────┘

┌─ ON-TIME PERFORMANCE (DOT/BTS) ────────────────────────────────────────┐
│
│  FLIGHT PERFORMANCE
│  ───────────────────────────────────────────────────────────────────
│  Total Flights:                                         486,234
│  On-Time Flights:                                       387,891
│  On-Time Performance:                                     79.76 %
│  Cancelled Flights:                                      12,456
│  Cancellation Rate:                                        2.56 %
│  Diverted Flights:                                        1,234
│
│  DELAY ANALYSIS
│  ───────────────────────────────────────────────────────────────────
│  Average Arrival Delay:                                  14.25 minutes
│  Average Departure Delay:                                12.34 minutes
│  Carrier Delay Minutes:                              2,456,789 total minutes
│  Weather Delay Minutes:                              1,234,567 total minutes
│  NAS Delay Minutes:                                    987,654 total minutes
│
└────────────────────────────────────────────────────────────────────────┘

┌─ MONTHLY TRENDS (DOT T-100) ───────────────────────────────────────────┐
│
│  Month          Passengers        ASM (M)    Load %     Departures
│  ───────────────────────────────────────────────────────────────────
│  2023-01        11,234,567      19,456.00     82.34        38,456
│  2023-02        10,987,654      18,234.00     83.12        36,234
│  2023-03        12,456,789      21,345.00     84.56        41,234
│  ...
│
└────────────────────────────────────────────────────────────────────────┘
```

## Configuration

### Enable/Disable DOT/BTS Integration

Edit `src/main/resources/application.properties`:

```properties
# Enable DOT/BTS data integration (default: true)
dot.bts.enabled=true

# Disable to use only SEC 10-K data
# dot.bts.enabled=false
```

### API Key (Optional)

Most DOT/BTS data is publicly accessible without authentication. However, if you encounter rate limits, you can obtain an API key:

1. Visit: https://www.transtats.bts.gov/
2. Register for an account
3. Request an API key
4. Add to `application.properties`:

```properties
bts.api.key=your-api-key-here
```

## Data Sources & Attribution

### Bureau of Transportation Statistics (BTS)
- **URL**: https://www.transtats.bts.gov/
- **T-100 Data**: Monthly operational statistics (Form 41)
- **On-Time Performance**: https://www.transtats.bts.gov/ONTIME/
- **Consumer Complaints**: https://www.transportation.gov/airconsumer

### SEC EDGAR
- **URL**: https://www.sec.gov/edgar
- **10-K Filings**: Annual reports with financial statements
- **XBRL Data**: Structured financial data via Company Facts API

## Ticker to DOT Carrier Code Mapping

The system automatically maps stock ticker symbols to DOT carrier codes:

| Ticker | Carrier Code | Airline Name |
|--------|--------------|--------------|
| UAL | UA | United Airlines |
| AAL | AA | American Airlines |
| DAL | DL | Delta Air Lines |
| LUV | WN | Southwest Airlines |
| JBLU | B6 | JetBlue Airways |
| ALK | AS | Alaska Airlines |
| SKYW | OO | SkyWest Airlines |
| HA | HA | Hawaiian Airlines |
| SAVE | F9 | Frontier Airlines |
| ALGT | G4 | Allegiant Air |

## Troubleshooting

### "No DOT/BTS data available"

**Possible causes:**
1. Network connectivity issues accessing BTS API
2. BTS API temporarily unavailable
3. No data reported for that airline/time period

**Solution:**
The application will continue with 10-K data only. Check logs for details.

### "Rate limit exceeded"

**Cause:** Too many requests to BTS API

**Solution:**
1. Wait a few minutes and retry
2. Obtain and configure an API key (see Configuration above)

### "Data inconsistency warnings"

**Cause:** Discrepancies between SEC 10-K and DOT/BTS data sources

**Note:** This is normal and expected. The system logs warnings but continues processing. Common reasons:
- Different reporting periods (fiscal year vs calendar year)
- Different calculation methodologies
- Revised data in one source but not the other

## Technical Details

### API Endpoints Used

1. **T-100 Domestic Market**: `/Table_ID/293/Data`
2. **T-100 International Market**: `/Table_ID/259/Data`
3. **On-Time Performance**: `/Table_ID/236/Data`
4. **Air Carrier Financial**: `/Table_ID/298/Data`
5. **Baggage Complaints**: `/Table_ID/278/Data`

### Data Aggregation

Monthly DOT data is aggregated to annual totals:
- **Summed**: Passengers, ASM, RPM, Departures, Delay Minutes
- **Weighted Average**: Load Factors, On-Time Percentage
- **Calculated**: Cargo Ton Miles (Freight + Mail), Annual Metrics

### Caching

DOT/BTS data is fetched in real-time (not currently cached). Future enhancement may add caching similar to SEC data caching.

## Benefits of DOT/BTS Integration

### 1. Complete Data Coverage
- Fills gaps in 10-K extraction
- Provides metrics not in SEC filings

### 2. Granular Monthly Data
- See seasonal trends
- Identify month-over-month changes
- Track recovery/decline patterns

### 3. Performance Quality Metrics
- On-time performance (competitive metric)
- Delay cause analysis
- Customer service quality (baggage, complaints)

### 4. Detailed Fleet Information
- Aircraft-level detail (tail numbers)
- Exact seating configurations
- Ownership vs leased aircraft

### 5. Data Validation
- Cross-check SEC vs DOT data
- Identify discrepancies
- Improve data quality

## Future Enhancements

Potential additions:
- **Route-level data**: Origin-destination pair analysis
- **Fare data**: Average fares by market
- **Fuel consumption**: Gallons consumed per month
- **Employment data**: Detailed labor statistics
- **Code-share analysis**: Partner airline operations

## References

- [BTS Data Inventory](https://www.transtats.bts.gov/DataIndex.asp)
- [T-100 Data Documentation](https://www.transtats.bts.gov/DatabaseInfo.asp?DB_ID=111)
- [On-Time Performance Documentation](https://www.transtats.bts.gov/DatabaseInfo.asp?DB_ID=120)
- [Form 41 Financial Data](https://www.transtats.bts.gov/DatabaseInfo.asp?DB_ID=135)
