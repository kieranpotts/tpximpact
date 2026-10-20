# Health and metrics on a separate management port

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

PROPOSED

**Not in-scope for MVP.**

## Context

Container orchestrators need liveness and readiness endpoints, and the
monitoring stack needs a metrics endpoint.

Aliases own almost all of the API service's public path namespace. Operational
endpoints also leak internal detail, which shouldn't be internet-facing.

## Decision

The API service will listen on two ports. The public port serves the contract's
routes. A separate management port serves liveness, readiness, and metrics, and
is never exposed publicly.

Liveness has no dependencies. Readiness checks that the database is reachable.

The web client has a single health endpoint, which doesn't depend on the API
service.

## Consequences

- The public attack surface is small.

- A database outage takes API service instances out of rotation, without the
  orchestrator killing them.

- Every environment must point its probes at the management port.

- There are two ports to explain.

## Alternatives

- **Operational routes on the public port, behind reserved aliases.** Simpler to
  wire, but metrics become internet-visible unless the load balancer filters
  them — and that's an easy rule to get wrong.
