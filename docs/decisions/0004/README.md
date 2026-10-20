# Flyway for schema migrations

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

The database schema must exist before the API serves traffic. It must be
created the same way on a developer's machine, in tests, and in production,
and it must be able to evolve without manual SQL.

## Decision

The schema will be managed with Flyway, using plain versioned SQL migrations
that ship inside the API.

The API runs the migrations at startup, before it accepts requests. If they
fail, it exits with a non-zero status.

## Consequences

- One code path creates the schema in every environment, including tests.

- When several replicas start at once, Flyway's lock lets one migrate while
  the others wait.

- During a rolling deployment, old and new versions share one schema, so
  migrations must be backward compatible. Destructive changes take two
  releases.

- The API's database user needs rights to change the schema. Running
  migrations as a separate task, with a separate privileged user, requires a
  hardening step that is outside the scope of this ADR.

## Alternatives

- **A separate migration task.** The cleaner production pattern, with a
  least-privilege API user, and migrations decoupled from rollout. But it adds a
  Compose service and a deployment step, which a one-table schema doesn't yet
  justify. To be revisited in a future technical decision.

## Deferred decisions

- **Separate migration task.** Run migrations as a separate task, with a
  separate privileged user, once the schema justifies the extra Compose service
  and deployment step. That would let the API run with a least-privilege
  database user.
