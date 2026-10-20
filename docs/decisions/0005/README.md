# Plain JDBC with HikariCP for data access

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

The API service will issues a small number of SQL statements, against one simple
lookup table. The insert will rely on PostgreSQL's `ON CONFLICT` clause.

## Decision

The database will be accessed through plain JDBC, with the PostgreSQL driver and
a HikariCP connection pool.

All SQL lives in one repository class, behind a port defined at the
application layer. Blocking calls run on an I/O dispatcher, and short timeouts
bound how long any request can wait on the database.

## Consequences

- The SQL a reviewer reads is the SQL that runs.

- The insert's update count answers the application's question: "was the alias
  free?"

- The port keeps SQL out of the domain layer, and lets tests use an in-memory
  repository.

- Row mapping is hand-written, which is a few lines for one table.

- If the schema grows to many tables with joins, this should be revisited.

## Alternatives

- **Exposed.** JetBrains' Kotlin SQL library, and a common pairing with Ktor. It
  adds a DSL, table definitions, and its own transaction model.

- **jOOQ.** Type-safe, but its code generation needs a database at build time.

## Deferred decisions

- **Data access library.** Replace plain JDBC if the schema grows to many
  tables with joins.
