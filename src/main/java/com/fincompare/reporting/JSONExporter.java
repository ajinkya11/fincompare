package com.fincompare.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fincompare.models.ComparativeAnalysis;
import com.fincompare.models.CompanyFinancialData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class JSONExporter {
    private static final Logger logger = LoggerFactory.getLogger(JSONExporter.class);

    private final ObjectMapper objectMapper;

    public JSONExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Export comparative analysis to JSON format
     */
    public void exportToJSON(ComparativeAnalysis analysis, String outputPath) throws IOException {
        logger.info("Exporting comparative analysis to JSON: {}", outputPath);

        objectMapper.writeValue(new File(outputPath), analysis);

        logger.info("JSON export completed successfully");
    }

    /**
     * Export company financial data to JSON format
     */
    public void exportCompanyData(CompanyFinancialData companyData, String outputPath) throws IOException {
        logger.info("Exporting company data to JSON: {}", outputPath);

        objectMapper.writeValue(new File(outputPath), companyData);

        logger.info("JSON export completed successfully");
    }
}
