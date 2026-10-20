# The API service serves its contract at `/openapi.yaml`

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

API clients need the contract. Publishing it from the running service is a
common convention. It lets tools load it straight from the API service, and the
published document always matches the deployed version.

## Decision

The API service will serve the static contract file at `GET /openapi.yaml` on its
public port, as `application/yaml`. The file is bundled into the API service at
build time.

This path can't collide with an alias, because aliases can't contain a full
stop (see [ADR 0009](../0009/README.md)).

## Consequences

- Clients and tools can discover the API service from the API service itself.

- The contract is public information, so serving it exposes nothing sensitive.

- The hand-written contract stays the source of truth. It isn't generated from
  the code.

## Alternatives

- **Serve it from the web client.** But the contract describes the API service,
  and a document served by the API service always ships with the version it
  describes.

- **Serve Swagger UI or Redoc as well.** Friendlier, but it widens the API
  service's public surface, for a convenience that any OpenAPI-compatible tool
  already gives.

## Deferred decisions

- **Generating the contract from code.** Whether to derive the contract from
  the API service's routes and types, instead of maintaining it by hand, so that
  the two can't drift apart. Until drift becomes a problem, the hand-written
  contract stays the source of truth.

- **Version number paths.** Whether to serve the contract under a versioned
  path, such as `/v1/openapi.yaml`, so that clients can pin to a major version
  of the API service. Until the API service has more than one major version, a
  single unversioned route is enough. `v1`, `v2`, etc. have been added to the
  reserved words list, to protect against clashes with custom aliases.
