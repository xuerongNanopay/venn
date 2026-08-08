package com.veen.velocitylimits.parser;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veen.velocitylimits.domain.LoadFund;
import com.veen.velocitylimits.dto.LoadFundRequest;
import com.veen.velocitylimits.exception.InvalidLoadFundException;

@Component
public class LoadFundParser {
    
    private final ObjectMapper objectMapper;

    public LoadFundParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Deserialize string to LoadFund
     * 
     * @throws InvalidLoadFundException if the format is invalid
     */
    public LoadFund parse(String line) throws InvalidLoadFundException {

        if (line == null || line.isBlank()) {
            throw new InvalidLoadFundException("Input JSON cannot be empty");
        }

        try {
            LoadFundRequest request = objectMapper.readValue(line, LoadFundRequest.class);
            validateLoadRequest(request);

            return new LoadFund(
                request.loadId(),
                request.customerId(),
                parseAmount(request.loadAmount()),
                parseTime(request.loadTime())
            );
            
        } catch (JsonProcessingException e) {
            throw new InvalidLoadFundException(
                "Invalid JSON input", e
            );
        }
    }

    /**
     * Parses an amount string to BigDecimal
     * 
     * @param value The fund amount string
     *                  Example: {@code $7777777.77}
     * @throws InvalidLoadFundException if the value format is invalid
     */
    private BigDecimal parseAmount(String value) {
        try {
            String normalized = value.trim().replace("$", "");

            BigDecimal amount = new BigDecimal(normalized);

            if (amount.signum() <= 0) {
                throw new InvalidLoadFundException(
                    "load_amount must be greater than zero"
                );
            }

            return amount;

        } catch (NumberFormatException e) {
            throw new InvalidLoadFundException("Invalid load_amount: " + value, e);
        }
    }

    /**
     * Parses a standard ISO 8610 string with UTC offsets to Instant
     * 
     * @param timestamp The ISO 8601 date-time string to parse
     *                  Example: {@code 2018-01-01T00:00:00Z}
     * @throws InvalidLoadFundException if the date format is invalid
     */
    private Instant parseTime(String timestamp) {
        try {
            return Instant.parse(timestamp);
        } catch (DateTimeParseException e) {
            throw new InvalidLoadFundException("Invalid time: " + timestamp, e);
        }
    }

    /**
     * Validate LoadRequest
     * 
     * @throws InvalidLoadFundException if any field is invalid
     */
    private void validateLoadRequest(LoadFundRequest request) {
        if (isBlank(request.loadId())) {
            throw new InvalidLoadFundException("id is required");
        }

        if (isBlank(request.customerId())) {
            throw new InvalidLoadFundException("customer_id is required");
        }
    
        if (isBlank(request.loadAmount())) {
            throw new InvalidLoadFundException("load_amount is required");
        }

        if (isBlank(request.loadTime())) {
            throw new InvalidLoadFundException("time is required");
        }
    }

    /**
     * Safe utility method to check if String is empty.
     */
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    } 
}
