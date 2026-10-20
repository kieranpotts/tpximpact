# RFC 9457 for API errors

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

The contract declares error status codes, but no error body. The web client
must show the user which form field was at fault, and a single `400` can mean
an invalid URL, an invalid alias, or an alias that's taken.

## Decision

Every error response from the API's public routes will be an RFC 9457 problem
details body (`application/problem+json`), with a fixed URN `type` for each
kind of error.

Anything unexpected becomes a generic `500` that exposes no internal detail.

## Consequences

- The web client can show each error on the right form field.

- The API's contract tests must allow bodies on error responses.

- The API contract is extended with the error vocabulary.

## Alternatives

- **A bespoke error body.** Simpler, but it's one more ad hoc format for clients
  to learn.
