# Structured JSON logs with request-ID propagation

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

PROPOSED

**Not in-scope for MVP.**

## Context

One link editor action passes through the web client, the API service, and the
database. When something fails, an operator needs every log line for that action,
across both applications. The platform collects logs from stdout, and they
should be machine-queryable.

## Decision

Both applications will write one JSON object per line to stdout, with Logback
in the API service and pino in the web client, using shared field names.

Every request carries a request ID, in the `X-Request-Id` header. The web
client accepts or generates it, and forwards it to the API service. Both attach
it to every log line.

Request bodies and credentials are never logged, and destination URLs only at
`DEBUG` level.

## Consequences

- One request ID ties a link editor action to every log line it caused, in both
  applications.

- Logs can be queried without parsing rules.

- Raw JSON is hard to read in a terminal, so local development can use a
  human-readable format instead.

- A request ID is not a trace. Timing across services needs distributed
  tracing, which only the production deployment adds.

## Alternatives

- **OpenTelemetry tracing from day one.** More powerful, but it needs a
  collector, which adds a container locally. Request IDs give most of the
  debugging value for none of that cost.
