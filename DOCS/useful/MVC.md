# MVC Structure

```text
Model -> domain
         domain/model
         domain/enums
         domain/state

Controller -> api/controller

View -> api/dto
        api/dto/request
        api/dto/response

Service / Business Logic -> application/service

Mapper -> application/mapper

Persistence / Repository -> infrastructure/repository

External Services -> infrastructure/external

Security -> security

Configuration / Startup -> HackHubApplication.java
                           api/OpenApiConfig.java
                           application.yml
```

Note: Since this is a REST API, there is no traditional HTML View layer.
The View is represented by the JSON DTOs returned by the controllers,
mainly `api/dto/response`.

# Package Interactions

```text
api/controller
    -> application/service
    -> api/dto/request
    -> api/dto/response

application/service
    -> domain/model
    -> domain/enums
    -> domain/state
    -> infrastructure/repository
    -> infrastructure/external
    -> application/mapper

application/mapper
    -> domain/model
    -> api/dto/response

infrastructure/repository
    -> domain/model

infrastructure/external
    -> application/service

security
    -> infrastructure/repository
    -> domain/model
    -> application/service / api/controller indirectly

api/exception
    -> api/controller
    -> application/service
```

# Typical Request Flow

```text
Client
  -> api/controller
  -> application/service
  -> infrastructure/repository
  -> domain/model
  -> application/mapper
  -> api/dto/response
  -> Client
```

# Example

```text
HackathonController
  -> HackathonService
  -> HackathonRepository
  -> Hackathon domain model
  -> HackathonMapper
  -> HackathonResponse
```
