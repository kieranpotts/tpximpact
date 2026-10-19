# 📋 Requirements specification

What the system does, in business terms — the major operations and business
rules, and the constraints within which the system must operate.

There are two parts:

- **[The specification](./SPECIFICATION.md)** is living documentation describing
  what the production system does at the current checked out revision. It is
  updated in place, and merged to the main trunk at the same time as the code it
  specifies, so it never drifts from reality.

- **The proposals** are an immutable, append-only log of product decisions,
  including those that were rejected. Together they justify why the
  specification says what it says.

The specification covers _what_ the system does. _How_ it is built is described
in the [design docs](../design/) with the rationale recorded in the
[ADRs](../decisions/).

## Structure

One directory per proposal:

```
specs/
├── SPECIFICATION.md  # The living specification. Updated in place.
├── TEMPLATE.md       # The starting point for a new proposal.
└── <slug>/           # One proposal.
    ├── README.md     # The proposal itself.
    └── …             # Wireframes, mock-ups, or other supporting artifacts.
```

The slug is the proposal's permanent identity.

## Lifecycle

```
DRAFT ──> PROPOSED ──> ACCEPTED ──> RELEASED ──> SUPERSEDED
              │
              └──────> REJECTED
```

A proposal is opened as a pull request. As well as the proposal document, the PR
includes the proposed edits to the specification. Proposals and specification
edits are merged at the same time as the implementation.

Rejected proposals are merged to the main trunk too, but with the specification
edits reverted so the main trunk only ever represents the as-is system.
