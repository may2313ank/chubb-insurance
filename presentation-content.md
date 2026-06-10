# Policy Overview Dashboard — BFF API (APAC-2847)
Presentation content, generated from the actual application.

---

## Slide 1 — Architecture Design & Decisions

**Style: Layered architecture with a domain core (dependencies point inward)**
- `api → service → domain` and `infrastructure → domain`
- `domain` has zero framework/persistence dependencies — pure business models
- `api` never touches `infrastructure` directly; always via `service`
- DTOs never leave `api`; JPA entities never leave `infrastructure` — only domain models cross layers
- An explicit **mapper** at every boundary (`RequestDtoToDomain`, `DomainToResponseDto`, `PolicyMapper`)

**Package layout** (`com.chubb.assessment`)
- `api` → controller, dto/{request,response}, mapper, exception
- `domain/models` → Policy, PolicyFilter, PolicyStatistics, FlagResult, PolicyExpiry
- `service` → PolicyService (business logic / orchestration)
- `infrastructure/persistence` → entity, repository, mapper; plus `cache`
- `common` → exception, util, logging (cross-cutting)
- `config` → Spring configuration

**Key design decisions**
- **Contract-first**: static OpenAPI YAML is the source of truth, not generated from code
- **Specification pattern** (`PolicySpecificationFactory` + `JpaSpecificationExecutor`) for composable, injection-safe filtering
- **Domain helper `PolicyExpiry`** centralizes the 30-day "expiring soon" rule (DRY, single source)
- **Centralized error handling** via `@RestControllerAdvice` — maps domain/technical exceptions to HTTP, never leaks stack traces (AC6)
- **Cross-cutting tracing** via `CorrelationIdFilter` + SLF4J MDC in `common/logging`
- **Enforced code rules**: file ≤ 200 lines, method ≤ 50 lines, no magic values, cyclomatic complexity < 4 (Strategy over branching), `Optional` over `null`

---

## Slide 2 — GIT details & working platform

**Repository**
- Remote: `https://github.com/may2313ank/chubb-insurance.git`
- Default branch: `main`
- Conventional commits; `target/` and Maven wrapper internals git-ignored

**Working platform**
- OS: Windows 11
- Language/runtime: **Java 21 (LTS)**
- Build: **Maven 3.9.x** via committed wrapper (`./mvnw`)
- IDE: IntelliJ IDEA; AI pair-assistant: Claude Code
- DB (dev/test): **H2 in-memory**; target datastore: PostgreSQL 16 (Spring Data JPA)

**Tech stack (pinned)**
- Spring Boot **3.4.1** (latest − 1, for stability)
- springdoc-openapi (Swagger UI) 2.7.0
- JUnit 5 (Jupiter) via `spring-boot-starter-test`
- Validation, Web, Data JPA starters

---

## Slide 3 — API Specification

**Contract-first OpenAPI 3.0.3** — `src/main/resources/static/openapi/policies-api.yaml`, served at `/swagger-ui.html` (Swagger UI points at the static spec, not code-generated docs)

**Base path: `/api/v1/policies`**

| Method & Path | Purpose |
|---|---|
| `GET /api/v1/policies` | Paginated list; filter (status, lineOfBusiness, region, effective-date range) + free-text `q` — all **query params**; `page`/`size`/`sort` for paging |
| `GET /api/v1/policies/{id}` | Full detail of one policy by UUID |
| `PATCH /api/v1/policies/flag` | Bulk-flag policies for review by id list; returns requested/flagged/missingPolicyIds |
| `GET /api/v1/policies/summary` | Aggregated stats: counts by status, total premium by line of business, expiring-soon count |

**Response shaping (frontend-friendly, per ACs)**
- Display values: `ACTIVE → "Active"`, `SG → "Singapore"` (AC3/AC4)
- `isExpiringSoon` true when expiry within 30 days (AC5)
- Paged response includes `totalElements` / `totalPages` (AC2)
- Money returned as `{ amount, currency }`

**Error contract** — standard `ErrorResponse` (timestamp, status, error, message, path); `400` invalid input, `404` not found, `503` datastore unavailable. No stack traces exposed.

---

## Slide 4 — AI Working Journal

**File:** `.claude/context/ai-journal.md` — a complete prompt log of the AI-assisted build

**What each entry records**
- **When** it was prompted (date)
- **What** was asked (the prompt)
- **What** the AI did (the response)
- **Outcome**: Accepted / Rejected / Challenged / Pending

**Why it matters**
- Full traceability and auditability of every AI-generated change
- Captures **challenges and course-corrections**, not just successes (e.g. removed an over-engineered facade interface; GET-with-body reverted to query params)
- Records *why* decisions were made and which assumptions were flagged to the user

**Scale**
- 27 logged interactions spanning project bootstrap → rules → contract → vertical slices → security review → tracing
- Governing rules live alongside it in `.claude/rules/` (java-code-style, logging, testing, architecture) and `CLAUDE.md`

---

## Slide 5 — Testing Strategy

**Layered tests mirroring the architecture — 26 tests, all green**

| Test | Type | Scope |
|---|---|---|
| `PolicyServiceTest` (5) | Unit (Mockito) | Business logic: list, get-by-id, flag, statistics, 404 path |
| `PolicyControllerTest` (8) | Web slice (`@WebMvcTest` + MockMvc) | Endpoints, validation → 400, not-found → 404, JSON shape |
| `CorrelationIdFilterTest` (3) | Unit | ID generation, inbound-header reuse, MDC cleanup |
| `PolicySpecificationFactoryTest` (4) | Unit | Filter predicate construction |
| `PolicyRepositoryIntegrationTest` (6) | Integration (`@DataJpaTest` + H2) | Real persistence: spec filtering, search, aggregation, expiry window |

**Principles**
- Unit tests for **service & controller** layers; **integration tests including DB** (per testing rules)
- Naming: `methodUnderTest_scenario_expectedOutcome`
- JUnit 5 assertions; mocks at boundaries, real DB for repository behavior
- Exception/error paths explicitly covered (400 / 404 / 503)
