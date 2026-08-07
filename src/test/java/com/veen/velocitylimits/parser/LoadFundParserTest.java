package com.veen.velocitylimits.parser;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veen.velocitylimits.parser.LoadFundParser;

public class LoadFundParserTest {

    private LoadFundParser parser;

    @BeforeEach
    void setUp() {
        parser = new LoadFundParser(new ObjectMapper());
    }
}
