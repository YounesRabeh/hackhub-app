# HackHub App

Basic Spring Boot backend for the HackHub hackathon platform.

## Requirements

- Java 25
- Gradle Wrapper (`./gradlew`)

## Run in Dev Mode

`bootRun` activates the Spring profile `dev` automatically.

In this project, dev mode enables:
- H2 console
- Swagger/OpenAPI docs
- Development in-memory database (`jdbc:h2:mem:hackhub-dev`)

Start the app in dev mode with:

```bash
./gradlew bootRun
```

## Dev URLs

- API base:   `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console`

H2 (dev profile) database URL: `jdbc:h2:mem:hackhub-dev`  
Default username: `admin`  
Password: *(empty)*

## Run Tests

```bash
./gradlew test
```

## Endpoint Coverage Check

This project includes an integration test that tracks which API endpoints are hit and validates coverage against a threshold.

Run only endpoint coverage test:

```bash
./gradlew cov
```

This command prints a summary like:
`Endpoint coverage: X/Y (Z%) - threshold N%`

Set custom threshold (default is `80`):

```bash
./gradlew cov -Dendpoint.coverage.threshold=90
```

How to check the result:
- The test logs a line like: `Endpoint coverage: X/Y (Z%) - threshold N%`
- Detailed test result file:
  - `build/test-results/test/TEST-com.hackhub.integration.coverage.EndpointCoverageIntegrationTest.xml`
- Quick terminal check:

```bash
rg "Endpoint coverage:" build/test-results/test/TEST-com.hackhub.integration.coverage.EndpointCoverageIntegrationTest.xml
```
