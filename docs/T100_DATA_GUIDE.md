# BTS T-100 Data Integration Guide

## Overview
This application can display domestic and international departure counts for airlines using data from the Bureau of Transportation Statistics (BTS) T-100 database.

## Data Source
- **Website**: https://www.transtats.bts.gov/
- **Database**: T-100 Domestic Segment Data & T-100 International Segment Data
- **Update Frequency**: Monthly (data is typically released 2-3 months after the reporting period)

## How to Add T-100 Data

### Step 1: Access BTS TranStats
1. Visit https://www.transtats.bts.gov/
2. Click on "Aviation" → "Air Carriers"
3. Look for "T-100 Domestic Segment (All Carriers)" or "T-100 International Segment (All Carriers)"

### Step 2: Download the Data
1. Select the appropriate dataset:
   - For domestic departures: "T-100 Domestic Segment (All Carriers)"
   - For international departures: "T-100 International Segment (All Carriers)"

2. Filter by:
   - **Carrier**: Select the airline (e.g., United Airlines, JetBlue Airways)
   - **Time Period**: Select the fiscal year (e.g., 2024, 2023, 2022)
   - **Fields**: Make sure to include `DEPARTURES_PERFORMED`

3. Download the data as CSV

### Step 3: Calculate Total Departures
The T-100 data contains segment-level (route-level) information. You need to sum the `DEPARTURES_PERFORMED` column for each carrier and year.

Example using Excel/Google Sheets:
```
1. Open the CSV file
2. Create a pivot table or use =SUM() to add all DEPARTURES_PERFORMED values
3. Result = Total departures for that carrier and year
```

Example using command line (Linux/Mac):
```bash
# Extract and sum DEPARTURES_PERFORMED column
awk -F',' 'NR>1 {sum+=$COLUMN_NUM} END {print sum}' t100_data.csv
```

### Step 4: Update the Configuration File
Edit the file: `src/main/resources/t100-data.properties`

Add your calculated totals:
```properties
# United Airlines (UAL)
UAL.2024.domestic.departures=520000
UAL.2024.international.departures=89000
UAL.2023.domestic.departures=510000
UAL.2023.international.departures=85000
UAL.2022.domestic.departures=490000
UAL.2022.international.departures=82000

# JetBlue Airways (JBLU)
JBLU.2024.domestic.departures=215000
JBLU.2024.international.departures=28000
JBLU.2023.domestic.departures=220000
JBLU.2023.international.departures=25000
JBLU.2022.domestic.departures=210000
JBLU.2022.international.departures=22000
```

**Note**: The values above are examples only. You must replace them with actual data from BTS.

### Step 5: Rebuild and Run
```bash
mvn clean package
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 3 --detail
```

## Carrier Codes
- **UAL** = United Airlines (BTS Carrier ID: 19977)
- **JBLU** = JetBlue Airways (BTS Carrier ID: 20409)
- **DAL** = Delta Air Lines (BTS Carrier ID: 19790)
- **AAL** = American Airlines (BTS Carrier ID: 19805)
- **LUV** = Southwest Airlines (BTS Carrier ID: 19393)

You can find carrier codes on the BTS website or in the T-100 data downloads.

## Data Availability
- **Domestic Data**: Publicly available immediately
- **International Data**: Restricted for 6 months from the data date for confidentiality reasons
- **Fiscal Year**: Most airlines report on calendar year basis (January-December)

## Troubleshooting

### No Data Appears in Report
- Check that you've edited the correct file: `src/main/resources/t100-data.properties`
- Verify the carrier code matches (UAL, not UA)
- Verify the fiscal year is exactly 4 digits (2024, not FY2024)
- Ensure you rebuilt the application after editing the properties file

### Data Doesn't Match Expectations
- T-100 reports "performed" departures, not "scheduled" departures
- Make sure you're summing all segments (not just mainline)
- Some airlines report regional operations separately

## Future Enhancements
Potential improvements for this feature:
- Automated CSV parsing from downloaded T-100 files
- Direct API integration if BTS provides one
- Automatic data updates from BTS FTP server
- Historical trend analysis using multi-year T-100 data

## References
- BTS TranStats: https://www.transtats.bts.gov/
- T-100 Data Dictionary: https://www.transtats.bts.gov/Fields.asp?gnoyr_VQ=FIL
- BTS Contact: ritainfo@dot.gov
