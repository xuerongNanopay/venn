package com.veen.velocitylimits.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veen.velocitylimits.domain.LoadFund;
import com.veen.velocitylimits.domain.LoadFundResult;
import com.veen.velocitylimits.dto.LoadFundResponse;
import com.veen.velocitylimits.exception.InvalidLoadFundException;
import com.veen.velocitylimits.parser.LoadFundParser;
import com.veen.velocitylimits.service.VelocityLimitsService;

@Component
@ConditionalOnProperty(name = "velocitylimits.runner.enabled", havingValue = "true", matchIfMissing = true)
public class LoadFundRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadFundRunner.class);
    private static final String USAGE = "Usage: java -jar app.jar <input_file> [output_file]";

    private final ObjectMapper objectMapper;
    private final LoadFundParser loadFundParser;
    private final VelocityLimitsService velocityLimitsService;

    public LoadFundRunner(
        ObjectMapper objectMapper,
        LoadFundParser loadFundParser,
        VelocityLimitsService velocityLimitsService
    ) {
        this.objectMapper = objectMapper;
        this.loadFundParser = loadFundParser;
        this.velocityLimitsService = velocityLimitsService;
    }
    
    /**
     * Entry point of the Velocity Limits CLI application.
     * Reads load fund records from the input file, writes non-duplicate
     */
    @Override
    public void run(String... args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("Expected at least 1 argument. " + USAGE);
        }

        String inputFile = args[0];
        Path inputPath = Path.of(inputFile);
        if ( !Files.isReadable(inputPath) ) {
            throw new IllegalArgumentException("Input file is unreadable: " + inputFile);
        }

        Path outputPath = args.length > 1 ? Path.of(args[1]) : Path.of("output.txt");

        log.info("Processing load fund input file: {}", inputPath);
        log.info("Writing load fund output file: {}", outputPath);

        try (
            var reader = Files.newBufferedReader(inputPath);
            var writer = Files.newBufferedWriter(outputPath)
        ) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    // Parse string record to LoadFund.
                    LoadFund loadFund = loadFundParser.parse(line);

                    // Process LoadFund.
                    Optional<LoadFundResult> result = velocityLimitsService.processLoadFund(loadFund);

                    
                    switch (result) {
                        case Optional<LoadFundResult> ret when ret.isPresent() -> {
                            LoadFundResult r = ret.get();
                            LoadFundResponse loadFundResponse = new LoadFundResponse(r.loadId(), r.customerId(), r.accepted());
                            writer.write(objectMapper.writeValueAsString(loadFundResponse));
                            writer.newLine();

                        }
                        case Optional<LoadFundResult> _ -> {
                            // case: a load ID is observed more than once for a particular user
                            log.info("Ignoring duplicate load fund at line {}", lineNumber);
                            log.debug("Duplicate load fund at line {}: {}", lineNumber, line);
                        }
                    }
                } catch (InvalidLoadFundException e) {
                    log.warn(
                        "Skipping invalid load fund at line {}: {}",
                        lineNumber,
                        e.getMessage()
                    );
                    log.debug("Invalid load fund at line {}: {}", lineNumber, line);
                }
            }
        }
    }
}
