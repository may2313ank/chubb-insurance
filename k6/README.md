# k6 Load Testing

Load tests for the Policies API, written with [k6](https://k6.io/).

## Prerequisites

- A running instance of the application (default `http://localhost:8080`):
  ```bash
  ./mvnw spring-boot:run
  ```
- k6 installed locally:
  - Windows: `winget install k6 --source winget` (or `choco install k6`)
  - macOS: `brew install k6`
  - Linux / CI: see https://k6.io/docs/get-started/installation/

## Running

From the repository root:

```bash
# Default: ramping read load (0 -> 50 VUs over ~2 minutes)
k6 run k6/policies-load-test.js

# Point at a different environment
k6 run -e BASE_URL=http://localhost:8080 k6/policies-load-test.js

# Quick smoke check (1 VU, short duration) — overrides the staged profile
k6 run --vus 1 --duration 10s k6/policies-load-test.js
```

## What it exercises

| Endpoint | Coverage |
| --- | --- |
| `GET /api/v1/policies` | Paginated listing with randomized `status`, `lineOfBusiness`, `region`, `page`, and `size` filters. |
| `GET /api/v1/policies/summary` | Aggregate statistics. |

## Thresholds (pass/fail gates)

The run fails (non-zero exit) if any of these are breached:

- `http_req_failed` rate `< 1%`
- `http_req_duration` `p(95) < 500ms`, `p(99) < 1000ms`
- `list_duration` `p(95) < 500ms`
- `summary_duration` `p(95) < 300ms`

Tune these in `options.thresholds` to match your environment's SLOs.

## Not covered

`GET /api/v1/policies/{id}` and `PATCH /api/v1/policies/flag` require policy
UUIDs, which the response DTOs intentionally do not expose, and the seed data
uses `RANDOM_UUID()` so IDs are not deterministic. To load test these endpoints,
seed deterministic IDs and pass them into a dedicated scenario.
