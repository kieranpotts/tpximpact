# No caching of redirects

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

Redirects dominate the workload, so caching them is the obvious optimisation.

But the specification requires a deleted alias to return `404` straight away,
and any cache could keep serving a deleted link until it expires. For the same
reason, the contract specifies `302` (temporary redirect), not `301` (permanent
redirect, which can be cached).

A redirect is a single primary-key lookup. A modest PostgreSQL instance serves
thousands of those a second, and the API service scales horizontally.

## Decision

Redirects will not be cached anywhere, for now. Redirect responses carry
`Cache-Control: no-store`, and the API service has no application-level cache.

When load justifies it, the scaling path is to scale out the API service and
scale up the database, then add a shared cache that's invalidated on delete.
Only after that, and only if the specification is relaxed, would a CDN be
deployed to cache redirects.

## Consequences

- Deletion takes effect at once.

- There's no cache to invalidate, warm, or monitor.

- Every redirect costs a database round trip. At very high volume, that's is
  expected to become an early bottleneck.

## Alternatives

- **An in-process cache in each API service replica.** The cheapest to add, but a delete
  on one replica can't invalidate the others' caches without a pub/sub channel.

- **CDN caching from day one.** The biggest performance win, but it breaks the
  requirement that deletes take effect at once.

- **`301` redirects.** Cheaper for repeat visitors, but they make deletion
  permanently ineffective for anyone who previously followed the alias link.

## Deferred decisions

- **Redirect caching.** Add a shared cache, invalidated on delete, when load
  justifies it. Requires a relaxation of the requirements to allow for
  eventual consistency of deleted aliases.
