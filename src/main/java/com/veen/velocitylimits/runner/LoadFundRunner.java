package com.veen.velocitylimits.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veen.velocitylimits.domain.LoadFund;
import com.veen.velocitylimits.domain.LoadFundResult;
import com.veen.velocitylimits.dto.LoadFundResponse;
import com.veen.velocitylimits.parser.LoadFundParser;
import com.veen.velocitylimits.service.VelocityLimitsService;

@Component
@ConditionalOnProperty(name = "velocitylimits.runner.enabled", havingValue = "true", matchIfMissing = true)
public class LoadFundRunner implements CommandLineRunner {

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

        ObjectMapper mapper = new ObjectMapper();


        try (var lines = Files.lines(inputPath)) {
            lines.forEach(line -> {
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
                            t.printStackTrace();
                        }

                    }
                    case Optional<LoadFundResult> _ -> {
                        // case: a load ID is observed more than once for a particular user
                        //TODO: log
                    }
                    case null -> {
                        //TODO: unexpected error
                    }
                }
            });
        }
    }
}
