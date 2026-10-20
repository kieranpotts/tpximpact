# Configuration through environment variables

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

Both applications need settings that differ by environment, and the same image
must run unchanged in every environment.

There are two API URLs, and they're easy to confuse. The public base URL is the
prefix of every shortened, aliased URL that users see. The internal base URL is
how the web client reaches the API.

## Decision

Each application will read its configuration from environment variables once,
at startup, into a typed, immutable object. It validates every value, and exits
with a clear message if any is missing or invalid. Nothing else reads the
environment.

The two API URLs are separate settings: `PUBLIC_BASE_URL` on the API, and
`API_BASE_URL` on the web client.

Compose sets working values for every variable. In production, the platform
injects secrets at runtime. They're never baked into images.

## Consequences

- Misconfiguration fails fast, at startup.

- Environment variables are visible to anything that can inspect the process.
  That's the usual, accepted trade-off.

## Alternatives

- **Ktor's HOCON configuration file.** Idiomatic for Ktor, but it's a second
  place to look for settings, and its substitution syntax is easy to get wrong.
