# Testing Enhanced 10-K Extraction for UAL and JBLU

## Test Overview

This document outlines the testing plan for the enhanced 10-K extraction features with United Airlines (UAL) and JetBlue (JBLU).

## Enhanced Extraction Features

### 1. Revenue Breakdowns
The enhanced parser now extracts detailed revenue segmentation:
- **Passenger Revenue** - Core passenger transportation revenue
- **Cargo Revenue** - Freight and mail revenue
- **Other Revenue** - Ancillary services, loyalty programs, etc.

### 2. Additional Operational Metrics
New cargo and operational metrics extracted:
- **Cargo Ton Miles (CTM)** - Freight traffic volume
- **Available Ton Miles (ATM)** - Total freight capacity
- **Cargo Load Factor** - Freight capacity utilization percentage
- **Departures Performed** - Total number of flight operations
- **Block Hours** - Aircraft utilization time

### 3. Fleet Information
Detailed fleet data extraction:
- **Fleet Composition** - Breakdown by aircraft type (e.g., "150 Boeing 737-800, 50 Airbus A320")
- **Average Fleet Age** - Average age of aircraft in years
- **Fleet Size** - Total number of aircraft

## Test Commands

### Basic Test (1 Year)
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 1 --ops --data-quality
```

### Multi-Year Test (3 Years)
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 3 --ops --detail
```

### Export Test Results
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 1 --ops --output test-results.json --json
```

## Expected Results for UAL (United Airlines)

### Revenue Breakdown (FY2023)
Based on typical UAL 10-K structure:
- **Total Revenue**: ~$53B
- **Passenger Revenue**: ~$48B (90-91% of total)
- **Cargo Revenue**: ~$2B (3-4% of total)
- **Other Revenue**: ~$3B (5-6% of total - ancillary, MileagePlus, etc.)

### Operational Metrics (FY2023)
Expected to extract from 10-K:
- **ASM**: ~258 billion miles
- **RPM**: ~218 billion miles
- **Passenger Load Factor**: ~84-85%
- **Passengers Carried**: ~148 million
- **CTM**: ~3.5-4 billion ton miles
- **ATM**: ~16-18 billion ton miles
- **Cargo Load Factor**: ~20-25%
- **Departures**: ~1.5 million
- **Block Hours**: ~2.5-2.8 million

### Fleet Information (FY2023)
UAL typically has detailed fleet tables in Section describing "Fleet":
- **Fleet Size**: ~1,300 aircraft
- **Fleet Composition**: Expected extraction like:
  - "361 Boeing 737-800, 155 Boeing 737-900, 136 Boeing 737 MAX,
     98 Boeing 777-200, 55 Boeing 777-300, 55 Boeing 787-8,
     67 Boeing 787-9, 45 Boeing 787-10,
     145 Airbus A319, 263 Airbus A320, 103 Airbus A321, etc."
- **Average Fleet Age**: ~16-17 years

### Data Quality Indicators
Look for these in logs:
```
[INFO] Found passenger revenue: 47856000000.0
[INFO] Found cargo revenue: 2045000000.0
[INFO] Found other revenue: 3079000000.0
[INFO] Found ASM in table: 257941
[INFO] Found RPM in table: 217623
[INFO] Found Cargo Ton Miles in table: 3845
[INFO] Found fleet composition: 361 Boeing 737, 155 Boeing 737-900, ...
[INFO] Found average fleet age: 16 years
```

## Expected Results for JBLU (JetBlue)

### Revenue Breakdown (FY2023)
JetBlue typically reports:
- **Total Revenue**: ~$9.7B
- **Passenger Revenue**: ~$9.1B (93-94% of total)
- **Cargo Revenue**: ~$50-100M (0.5-1% - minimal cargo operations)
- **Other Revenue**: ~$500-600M (5-6% - TrueBlue, baggage fees, etc.)

### Operational Metrics (FY2023)
Expected extraction:
- **ASM**: ~65-67 billion miles
- **RPM**: ~54-56 billion miles
- **Passenger Load Factor**: ~83-85%
- **Passengers Carried**: ~42-44 million
- **CTM**: May not be available (minimal cargo)
- **ATM**: May not be available
- **Cargo Load Factor**: Likely not reported (minimal operations)
- **Departures**: ~350,000-400,000
- **Block Hours**: ~800,000-900,000

### Fleet Information (FY2023)
JetBlue is all-Airbus for mainline:
- **Fleet Size**: ~290 aircraft
- **Fleet Composition**: Expected:
  - "130 Airbus A320, 130 Airbus A321, 60 Airbus A220-300, 5 Embraer E190"
- **Average Fleet Age**: ~11-12 years (younger than UAL)

### Key Differences Between UAL and JBLU

| Metric | UAL | JBLU |
|--------|-----|------|
| Network Type | Global network carrier | Point-to-point LCC |
| Cargo Operations | Significant (widebody fleet) | Minimal (narrowbody only) |
| Fleet Diversity | Multiple manufacturers, 10+ types | Primarily Airbus, 3-4 types |
| International Ops | Extensive long-haul | Limited Caribbean/Latin America |

## Verification Checklist

### For Each Airline, Verify:

#### ✅ Revenue Extraction
- [ ] Total revenue matches 10-K income statement
- [ ] Passenger revenue extracted and reasonable (>85% of total)
- [ ] Cargo revenue present for UAL (should be substantial)
- [ ] Cargo revenue minimal or absent for JBLU (as expected)
- [ ] Other revenue extracted (ancillary services)
- [ ] Revenue components sum to total (with reasonable tolerance)

#### ✅ Operational Metrics Extraction
- [ ] ASM extracted and in reasonable range (UAL > JBLU by ~4x)
- [ ] RPM extracted and consistent with ASM
- [ ] Load factor calculated correctly (RPM/ASM * 100)
- [ ] Passengers carried extracted
- [ ] CTM extracted for UAL (significant cargo operations)
- [ ] CTM absent for JBLU or very small (minimal cargo)
- [ ] Departures extracted and reasonable
- [ ] Block hours extracted

#### ✅ Fleet Information Extraction
- [ ] Fleet size extracted
- [ ] Fleet composition includes aircraft types and counts
- [ ] Boeing types present for UAL
- [ ] Primarily Airbus types for JBLU
- [ ] Aircraft counts sum to total fleet size
- [ ] Average fleet age extracted and reasonable (10-20 years)

#### ✅ Data Quality
- [ ] Fiscal year matching works correctly
- [ ] No duplicate entries in fleet composition
- [ ] Values in expected ranges (no parsing errors)
- [ ] Logs show successful extraction for most fields
- [ ] HTML revenue validation passes (within 20% tolerance)

## Common Extraction Challenges

### UAL Specific:
1. **Multiple Fleet Tables**: UAL may have separate tables for mainline and regional
   - Parser should handle both
2. **Complex Revenue Breakdown**: Many revenue categories
   - Parser needs to aggregate correctly
3. **Cargo Emphasis**: Strong cargo business
   - CTM/ATM should be prominent

### JBLU Specific:
1. **Simpler Fleet**: Fewer aircraft types
   - Should extract all Airbus variants correctly
2. **Minimal Cargo**: May not report CTM/ATM
   - Parser should handle absence gracefully
3. **Focus on Ancillary Revenue**: Strong "Other Revenue"
   - Should capture ancillary services properly

## Log Analysis

### Success Indicators
Look for these log patterns indicating successful extraction:

```
[INFO] Extracting revenue breakdowns from 10-K HTML for fiscal year: 2023
[INFO] Found passenger revenue: <value> million
[INFO] Found cargo revenue: <value> million
[INFO] Found other revenue: <value> million
[INFO] Revenue breakdown extraction complete. Found 3 revenue categories

[INFO] Extracted metrics - ASM: <value>, RPM: <value>, CTM: <value>, ATM: <value>
[INFO] Found Load Factor in table: <value>%
[INFO] Found Cargo Load Factor in table: <value>%

[INFO] Found fleet composition: <aircraft details>
[INFO] Found average fleet age: <value> years
```

### Warning Indicators
These are OK and expected:

```
[WARN] Could not extract revenue from 10-K HTML
[WARN] Could not find CTM in any table  (Expected for JBLU)
[WARN] Error extracting fleet information: <reason>
```

### Error Indicators
These need investigation:

```
[ERROR] Error parsing operational metrics
[ERROR] Error extracting revenue breakdowns from HTML
[ERROR] Failed to process 10-K filing for FY<year>
```

## Output Analysis

### Console Report Sections to Review

1. **Executive Summary**
   - Should mention newly available metrics

2. **Income Statement**
   - Look for passenger/cargo/other revenue breakdown
   - Verify total revenue calculation

3. **Operational Deep-Dive (--ops flag)**
   - Should show ASM, RPM, CTM, ATM
   - Should display load factors (passenger and cargo)
   - Should show departures and block hours
   - Should include fleet composition and age

4. **Data Quality Report (--data-quality flag)**
   - Should indicate which metrics were extracted from XBRL vs HTML
   - Should show any validation warnings

## Manual Verification

If extraction looks incomplete, manually verify by:

1. **Download the 10-K**: Visit SEC EDGAR (www.sec.gov/edgar)
2. **Search for ticker**: UAL or JBLU
3. **Find latest 10-K**: Look for Form 10-K filings
4. **Check sections**:
   - Part II, Item 6: "Operating Statistics" table
   - Part II, Item 7: "Management's Discussion and Analysis"
   - Part II, Item 8: "Financial Statements" - Revenue note
   - Part I, Item 2: "Properties" - Fleet table

## Success Criteria

### Minimum Viable Extraction
- [x] Revenue breakdowns extracted for at least one airline
- [x] At least 5 of 8 new operational metrics extracted
- [x] Fleet size extracted for both airlines

### Ideal Extraction
- [x] All revenue breakdowns for both airlines
- [x] 7-8 of 8 operational metrics for UAL
- [x] 5-6 of 8 operational metrics for JBLU (cargo may be absent)
- [x] Fleet composition with aircraft types
- [x] Average fleet age

## Next Steps After Testing

### If Extraction is Successful (>70% metrics captured):
✅ **10-K extraction is sufficient** - Continue enhancing patterns and edge cases

### If Extraction is Insufficient (<70% metrics captured):
⚠️ **Consider DOT/BTS integration** as supplementary data source:
- DOT Form 41 (T-100 data) - Monthly operational stats
- DOT Schedule P-5.2 - Aircraft inventory
- BTS Airline On-Time Performance Data

## Building and Running

### Build Application
```bash
cd /home/user/fincompare
mvn clean package -DskipTests
```

### Run Tests
```bash
# Test UAL vs JBLU with operational deep-dive
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 1 --ops

# Test with data quality report
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 1 --ops --data-quality

# Export results for analysis
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 1 --ops --output test-results.json --json
```

## Troubleshooting

### Issue: No operational metrics extracted
**Solution**: Check if 10-K HTML was downloaded successfully. Look for logs like "Downloading 10-K filing for FY<year>"

### Issue: Fleet composition empty
**Solution**: Fleet tables vary by airline. May need to add additional patterns for specific carriers.

### Issue: Revenue breakdowns not found
**Solution**: Some airlines use different table formats. Check the 10-K manually and adjust regex patterns.

### Issue: Cargo metrics missing for JBLU
**Expected**: JetBlue has minimal cargo operations. This is normal.

---

**Note**: Due to current network connectivity issues preventing Maven builds, actual testing will need to be performed once the environment has network access or in a different environment.
