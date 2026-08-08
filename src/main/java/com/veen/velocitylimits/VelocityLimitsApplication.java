package com.veen.velocitylimits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.veen.velocitylimits.exception.CliException;

@SpringBootApplication
public class VelocityLimitsApplication {

	private static final Logger log = LoggerFactory.getLogger(VelocityLimitsApplication.class);

	public static void main(String[] args) {
		try {
			SpringApplication.run(VelocityLimitsApplication.class, args);
		} catch (RuntimeException e) {
			CliException cliException = findCause(e, CliException.class);

			if (cliException != null) {
				System.err.println("Error: " + cliException.getMessage());
				System.exit(cliException.exitCode());
			}

			log.error("Application failed", e);
			System.err.println("Error: Application failed. See logs for details.");
			System.exit(1);
		}
	}

	private static <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
		Throwable current = throwable;

		while (current != null) {
			if (type.isInstance(current)) {
				return type.cast(current);
			}
			current = current.getCause();
		}

		return null;
	}
}
