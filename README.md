# HackHub Backend

Basic Spring Boot backend for the HackHub hackathon platform.

## Requirements

- Java 25
- Gradle Wrapper (`./gradlew`)

## Run in Dev Mode

`dev` mode activates the Spring profile `dev` (`spring.profiles.active=dev`).

In this project, dev mode enables:
- H2 console
- Swagger/OpenAPI docs
- Development in-memory database (`jdbc:h2:mem:hackhub-dev`)

Start the app in dev mode with:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Dev URLs

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console`

H2 (dev profile) database URL: `jdbc:h2:mem:hackhub-dev`  
Default username: `sa`  
Password: *(empty)*

## Run Tests

```bash
./gradlew test
```
