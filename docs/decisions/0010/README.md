# Random alias generation

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

PROPOSED

## Context

When a request to shorten a URL has no custom alias, the API must generate
one automatically.

Generated aliases must be short, safe in a URL path, unique among all stored
aliases (including custom ones), and never be a reserved word.

Generates aliases must be highly random. They must not be predictable from the
previous alias, so a user can't traverse the links created after theirs.

## Decision

Each random alias will be generated as 7 characters drawn uniformly from the
62-character Base62 alphabet, using a cryptographically secure random source.
That gives 62<sup>7</sup> =  3.5 trillion possible aliases.

Reserved words will be discarded before insertion.

A collision on insert should trigger a retry with a fresh alias. After five
failed attempts the request should fail with `500`.

## Consequences

- Every generated alias is also a valid custom alias, so both kinds share one
  namespace and one set of rules.

- Aliases reveal nothing about creation order or volume.

- The alphabet mixes cases, so aliases are case-sensitive.

- Collisions are rare even at a billion stored links, so the `500` path
  exists only for correctness.

- There's no filter for offensive character sequences. This is to be revisited
  in a future ADR.

## Alternatives

- **Encoding a database sequence.** Unique without retries, but aliases become
  sequential and enumerable, they leak creation volume, and custom aliases can
  still collide with them.

- **Hashing the full destination URL.** Aliases would be deterministic, so
  anyone could compute the alias for a known destination URL. This conflicts
  with the requirement that aliases be unpredictable. It would also force every
  request to shorten the same URL to share one alias, which would limit feature
  extensibility.

- **Six characters.** Workable, but collisions climb noticeably at hundreds of
  millions of links. One more character makes retries a non-issue.

## Deferred decisions

- **Offensive character sequences.** Whether aliases, both custom and generated,
  need a filter or offensive language.
