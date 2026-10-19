# TODO

Work plan for the TPXImpact URL shortener exercise.

## 1. Decisions to record (`docs/decisions/`)

- [ ] Kotlin API framework: Ktor, Spring Boot, or http4k.
- [ ] Persistence: SQLite (a file on a volume, simplest) or PostgreSQL (closer
      to production, needs Compose). Schema migrations: Flyway or similar.
- [ ] Random alias strategy: length, alphabet (Base62 or URL-safe), and
      collision handling (retry, or a unique constraint plus retry).
- [ ] Frontend rendering: server-side templates in Express (for example,
      Nunjucks or EJS, optionally with the GOV.UK Frontend design system), or
      Express serving a small SPA.
- [ ] Build tooling: Gradle (Kotlin DSL) for the API, npm for the frontend.
      Decide on a monorepo layout, for example `api/` and `web/`.

## 2. Project scaffolding

- [ ] Monorepo layout: `api/`, `web/`, `docs/`, root `compose.yaml`.
- [x] Copy `openapi.yaml` into the repo as the source-of-truth contract, at
      `contracts/openapi.yaml`.
- [ ] Wire `run/*` scripts and `Makefile` targets (`install`, `build`, `test`,
      `lint`, `clean`) to the real toolchains.
- [ ] Update the devcontainer with the JDK, Gradle, and Node toolchains.
- [ ] Linting and formatting: ktlint or detekt (Kotlin), and ESLint plus
      Prettier (frontend).
- [ ] Fix template leftovers: the README links to a missing `LICENSE.txt`, and
      CONTRIBUTING links to a missing `SECURITY.md`.
- [ ] CI workflow (GitHub Actions): lint, test, and build the images on every
      push.

## 3. API (Kotlin)

### Endpoints (per `openapi.yaml`)

- [ ] `POST /shorten`
  - Request `{ fullUrl, customAlias? }` returns `201 { shortUrl }`.
  - Generate a random alias when `customAlias` is absent.
  - Return `400` for invalid input or an alias that's already taken.
- [ ] `GET /{alias}` returns `302` to the full URL, or `404` if not found.
- [ ] `DELETE /{alias}` returns `204`, or `404` if not found.
- [ ] `GET /urls` returns `200` with a list of `{ alias, fullUrl, shortUrl }`.

### Validation and error handling

- [ ] `fullUrl` is required, must be absolute, uses only `http` or `https`, and
      has a sensible maximum length.
- [ ] `customAlias` has an allowed character set (for example,
      `[A-Za-z0-9_-]`) and minimum and maximum lengths.
- [ ] Reserve aliases that clash with routes (`urls`, `shorten`, `health`, and
      so on).
- [ ] Decide whether aliases are case-sensitive.
- [ ] Use one consistent JSON error body (for example, RFC 9457
      `application/problem+json`). The spec gives no error schema, so record
      this as an assumption.
- [ ] Handle malformed JSON and the wrong content type with `400`, not `500`.
- [ ] Handle a concurrent create with the same alias through a DB unique
      constraint, then map the violation to `400`.

### Persistence and configuration

- [ ] Repository layer behind an interface (an in-memory fake for unit tests).
- [ ] Schema: `alias` (PK), `full_url`, `created_at`.
- [ ] URLs survive restarts. Prove it with a test or a documented manual check.
- [ ] Configurable public base URL for building `shortUrl` (the spec example is
      `http://localhost:8080`).
- [ ] CORS configured for the frontend origin, if the browser calls the API
      directly.
- [ ] `GET /health` (liveness and readiness) for container orchestration.
- [ ] Structured JSON logging, including request IDs.

## 4. Frontend (Express)

- [ ] Form: full URL plus an optional custom alias, which shows the resulting
      short URL with a copy button.
- [ ] List all shortened URLs (`GET /urls`), with a delete action per row.
- [ ] Show API errors clearly (alias taken, invalid URL, API unreachable).
- [ ] The API base URL comes from config, not hard-coded.
- [ ] Basic accessibility: labels, error summary, keyboard navigation.

## 5. Tests

- [ ] API unit tests: alias generation, validation, and service logic.
- [ ] API integration tests: HTTP endpoints against a real DB (Testcontainers,
      or a temporary SQLite file).
- [ ] Contract test: validate responses against `openapi.yaml` (for example,
      with an OpenAPI request/response validator).
- [ ] Persistence-across-restart test.
- [ ] Frontend tests: route handlers with `supertest`, plus API error
      rendering, with the API stubbed.
- [ ] Optional: one end-to-end smoke test (Playwright) against
      `docker compose up`.

## 6. Containerisation

- [ ] `api/Dockerfile`: a multi-stage Gradle build, a slim JRE runtime, and a
      non-root user.
- [ ] `web/Dockerfile`: a multi-stage Node build, a slim runtime, and a
      non-root user.
- [ ] `compose.yaml`: api, web, a DB (if Postgres) or a volume for SQLite, and
      health checks.
- [ ] One command to run everything: `docker compose up --build`.

## 7. Architecture write-up (hiring-team addition)

Put it in `docs/design/`, with a link from the README. Keep it high level.

- [ ] **Solution diagram:** users, the CDN or load balancer, the frontend
      (Express), the API (Kotlin), the database, and the monitoring and logging
      stack, with their interactions. Use Mermaid so it renders on GitHub, or
      export a PNG or SVG.
- [ ] **Deployment:** pick one cloud and justify it. For example, on AWS:
  - ECS Fargate or App Runner for both containers.
  - RDS PostgreSQL.
  - An ALB with redirect routing.
  - ECR for images.
  - Secrets Manager.
  - Terraform or CDK for IaC.
  - A CI/CD pipeline from GitHub Actions.
- [ ] **Scalability:**
  - The API and web tiers are stateless and scale horizontally.
  - The workload is read-heavy (redirects), so cache hot aliases (Redis or
    ElastiCache), and consider CDN caching of `302`s.
  - DB read replicas.
- [ ] **Resilience:**
  - Multi-AZ deployment.
  - Health checks and auto-restart.
  - Managed DB backups and point-in-time recovery.
  - Graceful degradation of the frontend when the API is down.
- [ ] **Observability:**
  - Structured logs.
  - Metrics: request rate, latency, 4xx and 5xx counts, redirect hit rate.
  - Tracing with OpenTelemetry, shipped to CloudWatch, X-Ray, or Grafana.
  - Alerting.
- [ ] **Security notes:**
  - Abuse and spam: rate limiting, a WAF, and URL blocklists.
  - Authentication for delete. The spec has none, so record this as a known
    gap.
  - HTTPS everywhere.
- [ ] **Trade-offs:** PaaS versus Kubernetes, SQLite versus Postgres, and
      server-rendered versus SPA, plus what to do differently at real scale.

## 8. README and submission

- [ ] Replace the template README with:
  - Prerequisites.
  - How to build and run locally, with Docker and without.
  - Example usage: frontend screenshots or steps, plus `curl` examples for
    each endpoint.
  - Notes and assumptions: the "Kotlin and Express" reading, the error format,
    alias rules, and reserved words.
  - Links to the architecture write-up and diagram.
  - Time spent.
- [ ] Check that the commit history is small and meaningful, and shows the
      reasoning behind the work.
- [ ] Do a final pass from a clean clone: `docker compose up --build`, run
      tests, and try every endpoint.
- [ ] Submit the repo link to the hiring team.
