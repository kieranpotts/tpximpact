# TypeScript for the web client

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

PROPOSED

## Context

The web client's most error-prone code is its API client, which builds request
bodies and interprets the outcomes of server operations. A type checker catches
two likely mistakes there: a request body that doesn't match the contract, and
outcome handling that misses a case.

## Decision

The web client's server code will be written in TypeScript, in `strict` mode,
compiled with `tsc` for production.

The API's request and response types will be written by hand, mirroring the
contract, not generated from it.

## Consequences

- There's a build step, and the image needs a build stage.

- Hand-written types can drift from the contract. The acceptance tests will
  be required to catch that.

## Alternatives

- **Plain JavaScript, with JSDoc types.** JSDoc with `tsc --noEmit` would give
  the same type checking without a build step. But JSDoc syntax is verbose for
  discriminated unions. It is also less familiar to developers.

- **Node.js native type stripping.** It would remove the build step, but it
  doesn't support every TypeScript feature, and type checking still needs `tsc`.
  It may be worth revisiting later, once it's more common.

- **Types generated from the contract.** No drift, but added complexity of
  generating the API contract from code. To be revisited in a later ADR.

## Deferred decisions

- **Node.js native type stripping.** Drop the build step once the feature is
  more common.

- **Types generated from the contract.** Generate the API's types from a
  contract that is itself generated from code, to remove drift.
