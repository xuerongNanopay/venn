package com.veen.velocitylimits.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final LoadFundParser loadFundParser;
    private final VelocityLimitsService velocityLimitsService;

    public LoadFundRunner(
        LoadFundParser loadFundParser,
        VelocityLimitsService velocityLimitsService
    ) {
        this.loadFundParser = loadFundParser;
        this.velocityLimitsService = velocityLimitsService;
    }
    
    /**
     * Entry point of Velocity Limits CLI application.
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

        log.info("Processing load fund input file: {}", inputPath);
        ObjectMapper mapper = new ObjectMapper();


        try (var lines = Files.lines(inputPath)) {
            AtomicInteger lineNumber = new AtomicInteger();
            lines.forEach(line -> {
                int currentLineNumber = lineNumber.incrementAndGet();

                try {
                    // Parse string record to LoadFund.
                    LoadFund loadFund = loadFundParser.parse(line);

                    // Process LoadFund.
                    Optional<LoadFundResult> result = velocityLimitsService.processLoadFund(loadFund);

                    
                    switch (result) {
                        case Optional<LoadFundResult> ret when ret.isPresent() -> {
                            LoadFundResult r = ret.get();
                            LoadFundResponse loadFundResponse = new LoadFundResponse(r.loadId(), r.customerId(), r.accepted());

                            try {
                                System.out.println(mapper.writeValueAsString(loadFundResponse));
                            } catch (Throwable t) {
                                log.error("Failed to serialize load fund response for load id {}", r.loadId(), t);
                            }

                        }
                        case Optional<LoadFundResult> _ -> {
                            // case: a load ID is observed more than once for a particular user
                            log.info("Ignoring duplicate load fund at line {}", currentLineNumber);
                            log.debug("Duplicate load fund at line {}: {}", currentLineNumber, line);
                        }
                    }
                } catch (InvalidLoadFundException e) {
                    log.warn(
                        "Skipping invalid load fund at line {}: {}",
                        currentLineNumber,
                        e.getMessage()
                    );
                    log.debug("Invalid load fund at line {}: {}", currentLineNumber, line);
                }
            });
        }
    }
}
