package com.veen.velocitylimits.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veen.velocitylimits.domain.LoadFund;
import com.veen.velocitylimits.exception.InvalidLoadFundException;

public class LoadFundParserTest {

    private LoadFundParser parser;

    @BeforeEach
    void setUp() {
        parser = new LoadFundParser(new ObjectMapper());
    }

    @Test
    void shouldParseValidLoadFundRequest() {
        String json = """
            {
                "id": "1234",
                "customer_id": "5678",
                "load_amount": "$123.45",
                "time": "2018-01-01T00:00:00Z"
            }
        """;

        LoadFund loadFund = parser.parse(json);

        assertEquals("1234", loadFund.loadId());
        assertEquals("5678", loadFund.customerId());
        assertEquals(new BigDecimal("123.45"), loadFund.loadAmount());
        assertEquals(
            Instant.parse("2018-01-01T00:00:00Z"),
            loadFund.loadTime()
        );
    }

    @Test
    void shouldRejectBlankInput() {
        InvalidLoadFundException exception = assertThrows(
            InvalidLoadFundException.class,
            () -> parser.parse(" ")
        );
        assertEquals("Input JSON cannot be empty", exception.getMessage());
    }

    @Test
    void sshouldRejectMalformedJson() {
        InvalidLoadFundException exception = assertThrows(
            InvalidLoadFundException.class,
            () -> parser.parse("""
                        {
                            "id": 
                        }
                    """)
        );
        assertEquals("Invalid JSON input", exception.getMessage());
    }

    @Test
    void shouldRejectMissingCustomerId() {
        String json = """
            {
                "id": "1234",
                "load_amount": "$123.45",
                "time": "2018-01-01T00:00:00Z"
            }
        """;
        InvalidLoadFundException exception = assertThrows(
            InvalidLoadFundException.class,
            () -> parser.parse(json)
        );
        assertEquals("customer_id is required", exception.getMessage());
    }

    @Test
    void shouldRejectMissingId() {
        String json = """
            {
                "customer_id": "5678",
                "load_amount": "$123.45",
                "time": "2018-01-01T00:00:00Z"
            }
        """;
        InvalidLoadFundException exception = assertThrows(
            InvalidLoadFundException.class,
            () -> parser.parse(json)
        );
        assertEquals("id is required", exception.getMessage());
    }

    @Test
    void shouldRejectMissingLoadAmount() {
        String json = """
            {
                "id": "1234",
                "customer_id": "5678",
                "time": "2018-01-01T00:00:00Z"
            }
        """;
        InvalidLoadFundException exception = assertThrows(
            InvalidLoadFundException.class,
            () -> parser.parse(json)
        );
        assertEquals("load_amount is required", exception.getMessage());
    }

    @Test
    void shouldRejectMissingTime() {
        String json = """
            {
                "id": "1234",
                "customer_id": "5678",
                "load_amount": "$123.45"
            }
        """;
        InvalidLoadFundException exception = assertThrows(
            InvalidLoadFundException.class,
            () -> parser.parse(json)
        );
        assertEquals("time is required", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidAmount() {
        String json = """
            {
                "id": "1234",
                "customer_id": "5678",
                "load_amount": "$invalid",
                "time": "2018-01-01T00:00:00Z"
            }
        """;
        InvalidLoadFundException exception = assertThrows(
                InvalidLoadFundException.class,
                () -> parser.parse(json)
        );
        assertEquals(
            "Invalid load_amount: $invalid",
            exception.getMessage()
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        String json = """
            {
                "id": "1234",
                "customer_id": "5678",
                "load_amount": "$0.00",
                "time": "2018-01-01T00:00:00Z"
            }
        """;
        InvalidLoadFundException exception = assertThrows(
            InvalidLoadFundException.class,
            () -> parser.parse(json)
        );
        assertEquals(
            "load_amount must be greater than zero",
            exception.getMessage()
        );
    }

    @Test

    void shouldRejectInvalidTime() {

        String json = """
            {
                "id": "1234",
                "customer_id": "5678",
                "load_amount": "$123.45",
                "time": "invalid-time"
            }
        """;

        InvalidLoadFundException exception = assertThrows(
            InvalidLoadFundException.class,
            () -> parser.parse(json)
        );

        assertEquals(
            "Invalid time: invalid-time",
            exception.getMessage()
        );
    }
}
