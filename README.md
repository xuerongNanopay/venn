## Velocity Limits

Velocity Limits is a Spring Boot CLI application that processes load-fund records from a line-delimited JSON input file.

## Notes

- The application creates a new customer record when a customer does not already exist. In a production system, customers would normally be created before they are allowed to load funds.
- The application uses H2 for demo purposes, but the service still accounts for race conditions that could occur in a production database.

## Requirements

- Java 25
- Gradle 9.5.1

The project includes the Gradle wrapper, so you can use `./gradlew` without installing Gradle separately.

## Project Structure

```text
src/main/java/com/veen/velocitylimits/
├── VelocityLimitsApplication.java      # Spring Boot entry point
├── config/                             # Application configuration
├── domain/                             # Internal domain records
├── dto/                                # JSON request/response records
├── entity/                             # JPA entities
├── exception/                          # Application exceptions
├── parser/                             # Input JSON parsing
├── repository/                         # Spring Data JPA repositories
├── runner/                             # CLI file processing
└── service/                            # Velocity limit business logic
```

## Test

```bash
./gradlew test
```

## Build

```bash
./gradlew build
```

## Usage

Using Gradle:

```bash
./gradlew bootRun --args="example_input.txt output.txt"
```

Using Java:

```bash
./gradlew build
java -jar build/libs/velocity-0.0.0-RELEASE.jar example_input.txt output.txt
```

The first argument is the input file. The second argument is optional.

- If provided, results are written to that file.
- If omitted, results are written to `output.txt`.

Input records should be one JSON object per line:

```json
{"id":"15887","customer_id":"528","load_amount":"$3318.47","time":"2000-01-01T00:00:00Z"}
```

Output records are also one JSON object per line:

```json
{"id":"15887","customer_id":"528","accepted":true}
```

Duplicate loads for the same customer are ignored and do not produce output.
