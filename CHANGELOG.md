# Changelog

All notable changes to this project are documented in this file. The format is
loosely based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and
the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The `[Unreleased]` section of the changelog is maintained by hand. Use
`make version` to prepare a new release tag.

## [Unreleased]

- `GET /` (with no path) on the API service now returns a "Welcome to the URL shortener" HTML page.
- Tighten descriptions of endpoint response types in OpenAPI spec - prettier in Postman.
- `GET /{alias}` redirects a short URL to its destination URL, with a `302` that's never cached.
- An unknown or invalid alias gets a `404`. Browsers see "link not found" HTML page.
- While the database is unavailable, requests that need it get a `503`.
- Acceptance tests show that short URLs survive restarts of the API service and its database.
- Refactor UI layer of API service to better separate routes from other components.
- Devtools fix: install application dependencies (`make install`) into devcontainer automatically.

## [0.3.0] - 2026-10-21

**Code refactoring and workflow refinement.**

- Refacture API service application code into traditional DDD layers.
- Requirements and their acceptance tests cross-reference each other.
- `run check` now runs all static and dynamic checks sequentially.
- A pre-commit hook runs `make lint` and `make test` before every commit.
- The devcontainer installs the pre-commit hook at bootup.

## [0.2.0] - 2026-10-21

**Shorten URLs.**

- `POST /shorten` creates a short URL, with a custom alias or a random 7-character one.
- Short URLs are stored in PostgreSQL. The schema is created by the first Flyway migration.
- Validation and constraints, including reserved words, on aliases.
- Constraints on allowed destination URLs.
- Invalid input gets a `400` response.
- A taken alias gets a `400` response.
- Non-JSON bodies get a `415`, and bodies over 16 KiB get a `413`.
- Hostile-input tests show that no request body causes a `5xx`.

## [0.1.0] - 2026-10-21

**API contract.**

- The API service serves its OpenAPI contract at `GET /openapi.yaml`.
- API errors are RFC 9457 problem details.
- Every API response carries an `X-Request-Id` header, which may be supplied or generated.
- Every log line carries the request ID.
- The API service reads its configuration from environment variables, exits if any missing or invalid.
- The API service runs Flyway migrations at startup. Pools database connections. 2-second timeouts.
- The API service shuts down gracefully on `SIGTERM`.
- HTTP tests validate every response against the contract.

## [0.0.0] - 2026-10-21

**Walking skeleton.**

- Placeholder API service and web client.
- Each application has its own container image.
- Both applications plus the database run via Docker Compose.
- Harnesses for unit, integration, and acceptance test suites.
- Linters and formatters for Kotlin and TypeScript.
- Devcontainer with the JDK, Node.js, and access to Docker.
- `make` targets for all dev and ops commands.
- Shared settings for VS Code, Zed Editor, and IntelliJ IDEA.
