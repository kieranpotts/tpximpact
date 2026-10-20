# Idempotent link creation

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-20
- Decision date: —
- PR: —

## Status

PROPOSED

**Not in-scope for MVP.**

## Context

`POST /shorten` isn't idempotent. When the caller omits an alias, the API
service generates a random one (see ADR [0010](../0010/)), so repeating a request
creates a second link.

## Decision

To be decided. The options below are the candidates.

## Consequences

To be completed once a decision is made.

## Alternatives

- **`Idempotency-Key` request header.** The caller supplies a unique key, and
  the API service stores it with the created link, replaying the original
  response for a repeated key. Works for random and custom aliases, but needs
  key storage, expiry, and a rule for a reused key with a different body.

- **Deduplicating on the target URL.** Return the existing link when the same
  URL is submitted again. Needs no new header, but changes the meaning of
  creating a link: two people can no longer hold separate links to one URL.

- **Caller-chosen aliases only.** A repeated request with the same alias and
  target is already safe to treat as success. This removes random aliases, so
  it reverses [0010](../0010/).

## Deferred decisions

- Whether a retry policy in the web client follows, and which calls it covers.
