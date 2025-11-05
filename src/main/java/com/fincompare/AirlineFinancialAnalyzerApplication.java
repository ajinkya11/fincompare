package com.fincompare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Airline Financial Analyzer CLI tool
 */
@SpringBootApplication
public class AirlineFinancialAnalyzerApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
                SpringApplication.run(AirlineFinancialAnalyzerApplication.class, args)
        ));
    }
}
