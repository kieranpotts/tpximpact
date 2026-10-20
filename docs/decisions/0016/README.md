# Containers

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

The API service and the web client must each build to a container image, and the
whole system must start locally with documented commands. Reviewers will run it
with only Docker installed.

## Decision

Each application will have a multi-stage `Dockerfile` in its own directory,
built from that directory alone. Runtime images will be Debian-based, contain no
build tools or test code, and run as a non-root user.

`compose.yaml`, at the repository root, runs the database, the API, and the web
client. A database health check orders startup, and a named volume persists the
data.

## Consequences

- One command, `docker compose up --build`, runs the whole system from a clean
  clone.

- Images are small, don't run as root, and are the same ones that would be
  deployed to production.

- The first build is slow. Caches make later builds fast.

- Data survives `docker compose down`, but not `down -v`.

## Alternatives

- **Jib, to build the API image without a `Dockerfile`.** Neat, but it makes the
  image build depend on Gradle on the host, and it's less transparent than a
  `Dockerfile`.

## Deferred decisions

- **Distroless runtime images.** A smaller attack surface, but with no shell,
  health checks need a custom probe binary. Adopt as a later hardening step.
