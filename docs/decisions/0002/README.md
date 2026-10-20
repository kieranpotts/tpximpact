# Ktor for the API service

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

The API service must be written in Kotlin. It's a small service, with a handful of
routes, backed by a single relational database. In a codebase this small, the
behavior can be written out explicitly in the application code, instead of being
left to framework conventions, annotations, and auto-configuration.

## Decision

The API service will be built with Ktor 3, on the Netty engine, running on
Java 25 (the current LTS release).

Objects are wired by hand in one composition root, with no dependency
injection container. The build is Gradle, with the Kotlin DSL and a version
catalog.

## Consequences

- The whole request pipeline is visible in a few files, with no classpath
  scanning or annotation processing to explain.

- Startup is fast and the image is small, suiting horizontal scaling.

- Things a larger framework gives for free, such as health endpoints, config
  binding, and problem details, are hand-written. But each is small.

- Ktor is less familiar than Spring to many JVM developers.

- Blocking database calls must be kept off Ktor's event loop.

## Alternatives

- **Spring Boot.** The most widely recognised JVM stack, with problem details,
  health checks, and metrics built-in. For a service this size, though, most of
  the code a reviewer would read is configuration and annotations, and the
  behavior lives inside the framework. It's the right choice where an
  organisation already standardises on Spring.

- **http4k.** Very easy to test in memory, but less widely known, and its idioms
  need more explanation, for little gain over Ktor.
