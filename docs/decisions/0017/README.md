# Testing strategy

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

Automated tests must cover both applications, and run with one documented
command. The API must conform to its contract, and never return a `5xx` for bad
input. Persistence across restarts can only be proved against a real database.

## Decision

Testing will happen at three levels.

1. **API tests.** Fast, in-process unit, service, and HTTP tests, using an
   in-memory repository, plus integration tests against real PostgreSQL in
   Testcontainers. Every HTTP test validates its response against the OpenAPI
   contract.

2. **Web client tests.** Route tests against a fake API, and API client tests
   against a local stub server, rather than mocked HTTP.

3. **Acceptance tests.** Black-box Playwright tests of the whole system under
   Compose, with accessibility scans.

`make test` runs the first two levels. `make acceptance` runs the third.

## Consequences

- Most behavior is proved in-process, in seconds, and contract drift fails the
  build where it's introduced.

- Persistence and alias races are proved against the real database engine, so
  `make test` needs a Docker daemon.

- The acceptance tests catch faults no component test can, such as wiring
  mistakes and image contents. They're slower, so they're a separate command.

- Only the web client tests cover the API being unreachable.

## Alternatives

- **Consumer-driven contract tests, with Pact.** Powerful when different teams
  own the consumer and the provider. Here, one OpenAPI document is already the
  contract, so validating against it directly is simpler.

- **An in-memory database for integration tests.** Faster, but it wouldn't
  exercise `ON CONFLICT`, or PostgreSQL's real concurrency behavior, which are
  what most needs testing.
