# Chubb Insurance — Policy Overview Dashboard API

A Backend-for-Frontend (BFF) service that aggregates and transforms insurance policy
data from the core policy database into a frontend-friendly format for the **Policy
Overview Dashboard** (Angular UI).

> Ticket: **APAC-2847** — Policy Overview Dashboard, BFF Layer.
> Regions in scope: Singapore, Hong Kong, Australia, India, Japan.

## Features

- **Paginated policy list** — server-side pagination compatible with Angular Material tables.
- **Display-ready transforms** — status codes (`ACTIVE` → `Active`) and region codes
  (`SG` → `Singapore`) are mapped to human-readable values.
- **Expiry indicator** — `isExpiringSoon` flags policies ending within 30 days.
- **Policy detail** lookup by id.
- **Flag for review** — mark a batch of policies for review.
- **Summary statistics** — aggregate counts by status and premium by line of business.
- **Robust error handling** — readable errors, no leaked stack traces; `503` when the
  datastore is unreachable.
- **Correlation IDs** — every request is traced via a correlation id in logs.

## Tech Stack

| Technology   | Version            |
| ------------ | ------------------ |
| Java (JDK)   | 21 (LTS)           |
| Spring Boot  | 3.4.x              |
| Build tool   | Maven 3.9.x (wrapper committed) |
| Database     | PostgreSQL 16.x (prod) / H2 in-memory (local & test) |
| API docs     | springdoc-openapi 2.7.0 (Swagger UI) |
| Testing      | JUnit 5 (Jupiter)  |

## Architecture

The codebase follows a **layered architecture with a domain core**. Dependencies point
inward: `api → service → domain`, and `infrastructure → domain`. DTOs never leave the
`api` layer, persistence entities never leave `infrastructure`, and only domain models
cross layer boundaries (via dedicated mappers).

```
com.chubb.assessment
├── api
│   ├── controller        REST controllers (thin, delegate to service)
│   ├── dto/{request,response}
│   ├── mapper            RequestDtoToDomain, DomainToResponseDto
│   └── exception         @RestControllerAdvice global handler
├── domain
│   └── models            Pure business models (no framework deps)
├── service               Business logic / use cases
├── infrastructure
│   └── persistence/{repository,entity,mapper}
├── config                Spring configuration
└── common
    ├── exception         Framework-free technical exceptions
    ├── util              Stateless helpers
    └── logging           Correlation id filter
```

See [`.claude/rules/architecture.md`](.claude/rules/architecture.md) for the full rules.

## Getting Started

### Prerequisites

- JDK 21
- No local Maven needed — use the committed wrapper (`mvnw` / `mvnw.cmd`).

### Run locally

The default profile uses an in-memory H2 database, so no external setup is required.

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

```bash
# macOS / Linux
./mvnw spring-boot:run
```

The service starts on **http://localhost:8080**.

### Build & test

```powershell
.\mvnw.cmd clean verify        # compile, run tests, package
.\mvnw.cmd test                # tests only
```

### Run with Docker

```powershell
.\mvnw.cmd clean package
docker build -t chubb-insurance .
docker run -p 8080:8080 chubb-insurance
```

## API

Base path: **`/api/v1/policies`**

| Method  | Path                       | Description                                   |
| ------- | -------------------------- | --------------------------------------------- |
| `GET`   | `/api/v1/policies`         | Paginated, filterable list of policies        |
| `GET`   | `/api/v1/policies/{id}`    | Single policy by id                           |
| `GET`   | `/api/v1/policies/summary` | Aggregate policy statistics                   |
| `PATCH` | `/api/v1/policies/flag`    | Flag a batch of policies for review           |

Pagination uses standard `?page=` and `?size=` query parameters (default size `10`,
max `100`). The response includes `totalElements` and `totalPages`.

### API documentation

With the app running, the interactive docs are available at:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI spec (source of truth):** [`src/main/resources/static/openapi/policies-api.yaml`](src/main/resources/static/openapi/policies-api.yaml)

The project is **contract-first**: the committed OpenAPI YAML is authoritative, and
Swagger UI is pointed at it.

## Configuration

Configuration lives in [`src/main/resources/application.yml`](src/main/resources/application.yml)
and is overridable via environment variables:

| Variable             | Default     | Description                          |
| -------------------- | ----------- | ------------------------------------ |
| `SERVER_PORT`        | `8080`      | HTTP port                            |
| `DB_USERNAME`        | `sa`        | Datasource username                  |
| `DB_PASSWORD`        | *(empty)*   | Datasource password                  |
| `JPA_DDL_AUTO`       | `update`    | Hibernate DDL mode                   |
| `H2_CONSOLE_ENABLED` | `true`      | Enable H2 console at `/h2-console`   |
| `PAGE_DEFAULT_SIZE`  | `10`        | Default page size                    |
| `PAGE_MAX_SIZE`      | `100`       | Maximum page size                    |

## Performance

There is a [k6](https://k6.io/) load script under [`k6/`](k6/) used to validate the
performance target: **p95 response time under 500ms at 50 concurrent users**.

## Project Conventions

Coding, logging, testing, and architecture rules are documented under
[`.claude/rules/`](.claude/rules/) and the project context under
[`.claude/context/`](.claude/context/). Key points:

- Files ≤ 200 lines, methods ≤ 50 lines.
- No magic numbers/strings; `Optional` over `null`.
- SLF4J only (no `System.out`); never log sensitive data (policy number, premium amount).
- Tests named `nameOfTest_whatYouAreTesting_outcome`; unit + integration coverage.
