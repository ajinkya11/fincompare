# BTS T-100 Data Integration Guide

## Overview
This application automatically fetches domestic and international departure counts for airlines using the Bureau of Transportation Statistics (BTS) T-100 database via the Socrata Open Data API (SODA).

## Automated Data Fetching
The application is configured to automatically download T-100 data when you run comparative analysis. **No manual data entry required!**

## Setup Instructions

### Step 1: Find the Correct Dataset IDs
The BTS T-100 datasets are available on the Department of Transportation's open data portal at https://datahub.transportation.gov/

To find the current dataset IDs:

1. Visit **https://datahub.transportation.gov/**
2. Navigate to **Aviation** section
3. Search for "T-100 Domestic Segment" and "T-100 International Segment"
4. Click on each dataset
5. Click the **Export** button (top right)
6. Click **API**
7. Copy the dataset ID from the API endpoint URL (it's the 4-character code like `abc1-def2` in the URL)

Example API endpoint format:
```
https://data.transportation.gov/resource/abc1-def2.json
                                              ^^^^^^^^^
                                              This is the dataset ID
```

### Step 2: Update Configuration
Edit `src/main/resources/application.properties` and update the dataset IDs:

```properties
# BTS T-100 Data API Configuration
bts.t100.api.base-url=https://data.transportation.gov/resource
bts.t100.api.domestic-dataset-id=abc1-def2        # Update with actual domestic dataset ID
bts.t100.api.international-dataset-id=xyz7-uvw8  # Update with actual international dataset ID
bts.t100.api.timeout-seconds=30
bts.t100.api.enabled=true
```

### Step 3: Rebuild and Run
```bash
mvn clean package
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 3 --detail
```

The application will automatically:
- Query the BTS T-100 API
- Aggregate departure counts by carrier and year
- Display the data in your comparative analysis report

## How It Works

### API Queries
The application uses Socrata's SODA API to query T-100 data with filters:
- **Carrier**: Filters by airline name (e.g., "United Air Lines Inc.")
- **Year**: Filters by calendar year (e.g., 2024)
- **Aggregation**: Sums all `DEPARTURES_PERFORMED` for that carrier/year

Example API query:
```
https://data.transportation.gov/resource/abc1-def2.json
  ?$where=year=2024&unique_carrier_name='United Air Lines Inc.'
  &$select=sum(departures_performed) as total
```

### Supported Airlines
The application automatically maps ticker symbols to BTS carrier names:
- **UAL** → "United Air Lines Inc."
- **JBLU** → "JetBlue Airways"
- **DAL** → "Delta Air Lines Inc."
- **AAL** → "American Airlines Inc."
- **LUV** → "Southwest Airlines Co."

To add more airlines, update the `CARRIER_NAMES` map in `BTST100Client.java`.

## Data Availability
- **Domestic Data**: Publicly available immediately
- **International Data**: May be restricted for 6 months from the data date
- **Update Frequency**: Monthly (typically 2-3 months lag)
- **Historical Data**: Available back to 1990

## Disabling API Fetching
If you want to disable automatic T-100 data fetching, set:
```properties
bts.t100.api.enabled=false
```

## Troubleshooting

### No Departure Data Appears
1. **Check Dataset IDs**: Visit datahub.transportation.gov and verify you have the correct dataset IDs
2. **Check Logs**: Look for error messages starting with "Error fetching domestic/international T-100 data"
3. **Test API Access**: Try the API URL directly in your browser:
   ```
   https://data.transportation.gov/resource/YOUR-DATASET-ID.json?$limit=1
   ```
4. **Verify Carrier Names**: Ensure carrier names in `BTST100Client.java` match BTS naming conventions

### HTTP Errors
- **403 Forbidden**: The API endpoint or dataset ID is incorrect
- **404 Not Found**: The dataset ID doesn't exist
- **500 Server Error**: BTS API is temporarily unavailable

### Data Looks Wrong
- T-100 reports "performed" departures, not "scheduled" departures
- Data is by segment (route), not market
- Regional operations may be reported separately from mainline

## API Rate Limits
The Socrata Open Data API has rate limits:
- **Unauthenticated**: 1,000 requests per rolling 24-hour period
- **Authenticated**: 10,000 requests per rolling 24-hour period

For higher limits, you can register for an app token at https://dev.socrata.com/

To use an app token, add this header in `BTST100Client.java`:
```java
.header("X-App-Token", "YOUR_APP_TOKEN")
```

## References
- **BTS Open Data Portal**: https://datahub.transportation.gov/
- **SODA API Documentation**: https://dev.socrata.com/
- **T-100 Data Dictionary**: https://www.transtats.bts.gov/Fields.asp?gnoyr_VQ=FIL
- **BTS Contact**: ritainfo@dot.gov

## Future Enhancements
Potential improvements:
- Caching API responses to reduce redundant queries
- Support for more granular data (monthly instead of yearly)
- Automatic dataset ID discovery
- Fallback to other data sources if BTS API is unavailable
