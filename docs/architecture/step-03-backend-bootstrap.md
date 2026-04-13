# KIACMS Step 3 - Spring Boot Backend Bootstrap

## 1. What This Step Implements

This step turns the entity-focused backend from Step 2 into a runnable Spring Boot application foundation.

Included in this step:

- executable Spring Boot backend structure
- Gradle build configuration
- `application.yml` and profile-specific configuration examples
- common API response envelope
- global exception handling structure
- repository interfaces for the main domains
- a health-check API endpoint
- an application context smoke test

## 2. Package Structure

```text
backend/src/main/java/com/kiacms
|-- KiacmsApplication.java
|-- global/
|   |-- config/
|   |   `-- JpaAuditingConfig.java
|   |-- controller/
|   |   `-- HealthCheckController.java
|   |-- entity/
|   |   `-- BaseEntity.java
|   |-- exception/
|   |   |-- ErrorCode.java
|   |   |-- BusinessException.java
|   |   |-- ResourceNotFoundException.java
|   |   |-- ConflictException.java
|   |   |-- AccessDeniedBusinessException.java
|   |   `-- GlobalExceptionHandler.java
|   `-- response/
|       |-- ApiResponse.java
|       |-- ApiError.java
|       |-- FieldValidationError.java
|       `-- HealthCheckResponse.java
|-- approval/
|   |-- entity/
|   |-- enums/
|   `-- repository/
|-- ai/
|   |-- entity/
|   |-- enums/
|   `-- repository/
|-- course/
|   |-- entity/
|   |-- enums/
|   `-- repository/
|-- integration/
|   |-- entity/
|   |-- enums/
|   `-- repository/
|-- mentor/
|   |-- entity/
|   |-- enums/
|   `-- repository/
|-- note/
|   |-- entity/
|   `-- repository/
|-- notification/
|   |-- entity/
|   |-- enums/
|   `-- repository/
|-- project/
|   |-- entity/
|   |-- enums/
|   `-- repository/
`-- user/
    |-- entity/
    |-- enums/
    `-- repository/
```

## 3. Shared API Response Format

All API responses are designed to follow a consistent shape:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-04-13T00:00:00Z"
}
```

Validation and business errors use:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed.",
    "fieldErrors": [
      {
        "field": "email",
        "message": "must not be blank",
        "rejectedValue": ""
      }
    ]
  },
  "timestamp": "2026-04-13T00:00:00Z"
}
```

## 4. Exception Handling Structure

The global exception layer currently handles:

- custom business exceptions
- validation errors
- malformed request bodies
- unsupported HTTP methods
- database integrity violations
- uncaught internal server errors

This gives later domain APIs a single predictable error contract.

## 5. Configuration Files

### `application.yml`

- shared base configuration
- PostgreSQL datasource placeholders
- JPA defaults
- actuator health/info exposure
- logging baseline

### `application-local.yml`

- local development overrides
- intended for PostgreSQL-backed boot runs

### `application-test.yml`

- H2 in-memory test profile
- used by the Spring Boot context smoke test

## 6. Runtime Verification

Validated in this step:

- `.\gradlew.bat compileJava`
- `.\gradlew.bat build`

## 7. What Comes Next in Step 4

Step 4 will implement:

- signup/login/logout
- approval gating for `PENDING` users
- role-based authorization structure
- password change flow
- refresh-token strategy
- root approval endpoints
