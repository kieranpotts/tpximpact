# Contract tests against the running API service

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-21
- Decision date: —
- PR: —

## Status

PROPOSED

**Not in-scope for MVP.**

## Context

The API service must conform to its OpenAPI contract. Today, conformance is
checked in-process. Every HTTP test in the API service's unit suite validates
its response against the contract.

That leaves gaps:

- Only the responses the tests happen to elicit are checked. Nothing shows
  that every documented operation and status code is reachable, or that
  inputs the tests didn't think of are handled as documented.

- The checks run against Ktor's in-memory test host, not the built container
  image, so wiring, configuration, and the bundled copy of the contract go
  unchecked.

- The acceptance tests exercise the running system, but assert on specific
  fields, not on the contract as a whole.

## Decision

To be decided. The options below are the candidates.

## Consequences

To be completed once a decision is made.

## Alternatives

Options to consider include the following.

- **A Postman collection, run with Newman.** Import the contract into a
  Postman collection, add test scripts that assert status codes and validate
  bodies against the contract's schemas, commit the exported collection, and
  run it with Newman against the system under Compose. The same collection
  serves developers and API consumers for manual exploration. But it's a second,
  hand-maintained copy of the contract, which drifts unless it's regenerated and
  its test scripts re-applied. Test scripts are JavaScript embedded in JSON,
  which is hard to review in diffs. Cases are still hand-picked, so coverage is
  no wider than the existing tests.

- **Contract validation in the acceptance tests.** Validate every API response
  the Playwright suite receives against the contract, as the API service's
  `apiTest` client already does in-process. Reuses an existing suite and
  toolchain, and checks the real container image. Coverage is limited to what
  the acceptance tests exercise.

- **Property-based testing with Schemathesis.** Generate requests from the
  contract, send them to the running API service, and check that every
  response conforms, and that no input produces a `5xx`. Finds inputs nobody
  wrote a test for, and needs no test cases to maintain. Adds a Python tool
  (run as a container), runs are non-deterministic, and stateful flows, such
  as alias conflicts, need extra configuration.

- **A validating proxy, such as Prism, in front of the API service.** Route
  acceptance-test traffic through a proxy that rejects any request or response
  that breaks the contract. Checks all traffic without changing the tests, but
  adds a component to the test topology, and coverage is again limited to the
  requests the tests send.

- **Dredd.** Generates one request per documented example and checks the
  response. No longer maintained.

- **Status quo.** Rely on the in-process validation. Cheapest, and catches
  most drift where it's introduced, but leaves the gaps above.

## Deferred decisions

- Whether contract tests run on every push, or only before a release, once a
  delivery pipeline exists (see [ADR 0022](../0022/)).

- Whether to check the contract for breaking changes against the previous
  release, with a diff tool such as oasdiff. That's a check of the contract
  itself, not of the API service's conformance to it.
