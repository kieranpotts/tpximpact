# PostgreSQL for persistence

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

PROPOSED

## Context

The API must persist each alias-to-destination URL mapping across restarts.

Production may need to scale horizontally, running several API replicas. So
the stored must be designed to be shareable between processes. For example,
two concurrent requests for the same custom alias must not both succeed, so
the store must enforce uniqueness atomically.

## Decision

PostgreSQL 18 will be used as the only data store, in a container locally and
in tests, and potentially as a managed multi-AZ service in production.

The data is one narrow table, keyed by alias, and the workload is mostly
primary-key lookups.

Alias uniqueness is enforced by the table's primary key, not by application
code.

## Consequences

- The same engine can run in development, CI, and production, so there are no
  dialect surprises.

- Alias races are settled by the database in one statement, with no explicit
  locking.

- The API is stateless, so it scales horizontally against a shared database.

- Local runs need a third container, and the API must wait for it to be
  healthy.

- Integration tests need a Docker daemon.

## Alternatives

**SQLite on a volume.** The simplest way to persist data, with no extra
service. But it can't be shared by several API replicas, so production would
need a different engine, and local runs would stop being representative.

**A key-value store, such as Redis or DynamoDB.** A natural fit for lookups by
alias, but listing every URL needs a full scan, and neither runs locally as
simply as PostgreSQL.
