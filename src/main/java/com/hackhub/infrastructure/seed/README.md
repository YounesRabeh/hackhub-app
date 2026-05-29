# Development Data Seeder

## Overview

The `DevDataSeeder` class is responsible for populating the database with demo data when the application starts in the **dev** profile.

Its purpose is to simplify local development and testing by automatically creating:

* Demo users for each system role.
* A sample hackathon.
* A known password for all demo accounts.

This avoids the need to manually create data every time the database is recreated.

---

## Activation

The seeder is only active when the application runs with the `dev` profile.

```java
@Profile("dev")
```

Example:

```bash
spring.profiles.active=dev
```

or

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The seeder is not executed in production environments.

---

## Startup Execution

The class implements `CommandLineRunner`.

```java
public class DevDataSeeder implements CommandLineRunner
```

Spring Boot automatically invokes the `run()` method once the application context has been initialized.

```java
@Override
public void run(String... args)
```

During startup the seeder:

1. Creates demo users if they do not already exist.
2. Creates a demo hackathon if no hackathons exist.

---

## Seeded Users

The following accounts are automatically created:

| Email                                                 | Role      |
| ----------------------------------------------------- | --------- |
| [organizer@example.com](mailto:organizer@example.com) | ORGANIZER |
| [judge@example.com](mailto:judge@example.com)         | JUDGE     |
| [mentor1@example.com](mailto:mentor1@example.com)     | MENTOR    |
| [mentor2@example.com](mailto:mentor2@example.com)     | MENTOR    |
| [user1@example.com](mailto:user1@example.com)         | USER      |
| [user2@example.com](mailto:user2@example.com)         | USER      |
| [user3@example.com](mailto:user3@example.com)         | USER      |

All accounts share the same password:

```text
Password123!
```

Passwords are stored using the configured `PasswordEncoder`.

---

## Seeded Hackathon

If no hackathons exist in the database, a sample hackathon is created.

### Properties

| Field       | Value                                                 |
| ----------- | ----------------------------------------------------- |
| Title       | HackHub Demo Hackathon                                |
| Description | Demo hackathon seeded for local development           |
| Status      | REGISTRATION_OPEN                                     |
| Prize       | 1000.00                                               |
| Organizer   | [organizer@example.com](mailto:organizer@example.com) |

### Dates

Dates are generated relative to the current application startup time:

| Field                 | Value         |
| --------------------- | ------------- |
| Registration Deadline | now + 10 days |
| Start Date            | now + 12 days |
| Submission Deadline   | now + 20 days |
| End Date              | now + 25 days |

This guarantees that the seeded hackathon is always valid regardless of when the application is started.

---

## Idempotency

The seeder is designed to be idempotent.

### User Creation

Before creating a user, the system checks:

```java
userRepository.existsByEmail(email)
```

If the user already exists, creation is skipped.

### Hackathon Creation

Before creating the demo hackathon, the system checks:

```java
hackathonRepository.count() > 0
```

If at least one hackathon already exists, the demo hackathon is not created.

This allows the application to restart multiple times without creating duplicate data.

---

## Extending the Seeder

Additional demo data can be added by:

1. Creating new helper methods.
2. Invoking them from `run()`.
3. Maintaining idempotency checks before inserting records.

Example:

```java
@Override
public void run(String... args) {
    seedUsers();
    seedHackathons();
    seedTeams();
    seedSubmissions();
}
```

When adding new seeders, ensure that existing records are checked before creation to avoid duplicates across application restarts.

---

## Important Notes

* This class is intended exclusively for local development.
* It must not contain production data.
* It must not be enabled outside the `dev` profile.
* Demo credentials should never be reused in production environments.
* All seeded data should remain deterministic and easy to understand for developers and testers.
