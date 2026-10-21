# Scalability and resilience

Scaling beyond MVP load is out of scope. This document records the expected
traffic pattern and the scalability and resilience measures already built into
the MVP design.

## Traffic pattern

The system is read-heavy. Redirects are expected to outnumber creates by a
hundred to one or more, and each redirect is one primary-key lookup. Rows are
small, so storage and throughput are not a concern at MVP volumes.

## Built into the MVP

- **Stateless components.** The web client and API service hold no state, so
  any number of instances can run side by side, and any instance can be
  replaced.

- **Graceful degradation.** If the database is down, the API service returns
  a `503` problem within about 2 seconds, and recovers by itself once the
  database is back, with no restart. The web client shows "service
  unavailable" with input kept. If the API service is down, the web client
  stays up and explains the problem.

- **Timeouts and no retries on non-idempotent calls.** Web client to API 3s,
  API to database 2s, so slow dependencies fail fast.

- **Health checks.** Liveness checks don't depend on the database, so a
  database outage doesn't cause instances to be restarted needlessly.

- **Graceful shutdown** in both components, so deployments don't drop in-flight
  requests.
