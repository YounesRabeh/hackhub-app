# Security Module

## Overview

This package contains the security infrastructure of the HackHub application.

Its responsibilities include:

* User authentication.
* Password hashing.
* JWT generation and validation.
* Request authentication through security filters.
* Authorization configuration for protected endpoints.

The application uses **Spring Security** together with **JSON Web Tokens (JWT)** to implement stateless authentication.

---

## Authentication Flow

The authentication process follows these steps:

```text
User Login
     |
     v
Auth Service validates credentials
     |
     v
JwtService generates JWT
     |
     v
Client stores token
     |
     v
>>> Client sends:
Authorization: Bearer <token>
     |
     v
JwtAuthenticationFilter validates token
     |
     v
Spring Security Context populated
     |
     v
Protected endpoint accessed
```

---

## Components

### SecurityConfig

Central Spring Security configuration.

Responsibilities:

* Configures authorization rules.
* Defines public and protected endpoints.
* Enables stateless authentication.
* Registers the JWT authentication filter.
* Provides security-related beans.

---

### JwtService

Utility service responsible for JWT operations.

Responsibilities:

* Generate tokens.
* Extract user information from tokens.
* Validate token integrity and expiration.
* Verify token signatures.

The service relies on a configurable signing secret and expiration time.

---

### JwtAuthenticationFilter

Spring Security filter executed for every incoming request.

Responsibilities:

* Read the `Authorization` header.
* Extract Bearer tokens.
* Validate JWTs.
* Load authenticated users.
* Populate the Spring Security context.

If authentication fails, the request continues as anonymous.

---

### CustomUserDetailsService

Bridge between the application's user model and Spring Security.

Responsibilities:

* Load users from the database.
* Convert application users into Spring Security `UserDetails`.
* Provide user roles and credentials during authentication.

---

## Stateless Authentication

The application does not use HTTP sessions.

```text
Session Authentication
Server stores session state

JWT Authentication
Client stores authentication state
```

Because authentication information is stored inside JWTs, the server remains stateless and scalable.

---

## Development Features

When the `dev` profile is active, additional endpoints are exposed:

* H2 Database Console
* Swagger/OpenAPI documentation

These endpoints are disabled in production environments.

---

## Security Principles

This module follows several security best practices:

* Passwords are hashed using BCrypt.
* Authentication is stateless.
* JWTs are signed and validated.
* Protected endpoints require authentication.
* Security logic is centralized in a dedicated module.

The rest of the application should rely on Spring Security's authentication context rather than performing manual credential checks.
