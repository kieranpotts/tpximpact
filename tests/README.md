# Tests

Automated tests. The structure mirrors [`src/`](../src/), with one extra
directory for end-to-end acceptance tests.

- [**`api-service/`**](./api-service/) \
  Tests for `src/api-service`, in Kotlin with JUnit 5. `unit/` holds fast
  in-memory tests, including HTTP tests validated against the OpenAPI contract.
  `integration/` holds tests against PostgreSQL in Testcontainers. Both are
  compiled and run by the API service's Gradle build.

- [**`web-client/`**](./web-client/) \
  Tests for `src/web-client`, in TypeScript with Vitest. It's an npm package
  of its own, which imports the application from `src/web-client`.

- [**`acceptance/`**](./acceptance/) \
  End-to-end tests of the whole system, running under Docker Compose. The tests
  are written in TypeScript with Playwright, and named after the requirement IDs
  in the [specification](../docs/specs/README.md).

`make test` runs `api-service/` and `web-client/`. `make acceptance` runs
`acceptance/`.
