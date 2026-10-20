# Custom alias validation rules

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

The specification requires the API service to reject an invalid custom alias, but
neither it nor the contract says what makes an alias invalid. The contract types
an alias as a plain string, which accepts any value.

Aliases share the API service's root path with fixed routes, so an alias has the
potential to clash with a fixed route. For example, if the API service serves
`GET /health` and a link creator creates the custom alias `health`, without
validation the alias would be stored but requests to `/health` would reach the
fixed route instead — the alias is never resolved. Worse, existing aliases may
become unreachable if they clash with fixed routes that are added in the future.

Aliases also appear in URLs that people read, copy, and type.

For these reasons, aliases must be validated against a set of rules.

## Decision

The following validation rules will be implemented.

An alias, whether custom or generated, is valid when it is 3 to 64 characters
from `A–Z`, `a–z`, `0–9`, `-`, and `_`, and isn't a reserved alias. Reserved
words are today's routes, plus a few held back for routes the API service may
need in the future. These include the version path segments `v1`, `v2`, and so
on, which are held back so the API service can serve versioned paths, such as
`/v1/openapi.yaml`, without clashing with a custom alias.

Excluding `.` keeps file-like paths, such as `/openapi.yaml`, free for the API
service to use for future fixed routes.

Aliases are case-sensitive. Reserved aliases are compared case-insensitively.

These rules will be enforced in one place, in the API service's domain model.
The web client doesn't duplicate them, but renders the API service's validation
messages.

## Consequences

- No alias can shadow a fixed route, so every stored alias resolves.

- New top-level routes must use reserved aliases. Reserving a new word later
  needs a check that no stored alias already uses it.

- Aliases that differ only in case can belong to different people. A link
  creator who mistypes the case of a link gets a `404`.

- An empty form field shouldn't silently produce a random alias, so the web
  client must omit a blank alias.

## Alternatives

- **Case-insensitive aliases.** Friendlier to type, but they shrink the random
  keyspace (assuming a consistent maximum length) by a high factor.

- **Any non-empty string, percent-encoded.** Maximally permissive, but it
  invites slashes, spaces, and look-alike Unicode characters, which make short
  URLs hard to share.
