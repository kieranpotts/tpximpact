# 📐 Design docs

This section documents the current architecture of the production system.

These artifacts are descriptive. They state what the architecture _is_, in the
present tense. They do not record the requirements the system satisfies — those
live in the [requirements specification](../specs/) — nor the rationale for the
choices behind the design, which is captured in the [ADRs](../decisions/).

This is living documentation. A change to the architecture is merged to `main`
at the same time as the code and configuration, so the docs never drift from
reality.

## Architectural views

An architecture has several audiences asking different questions, so it is
documented as a set of views rather than one document. Each reader goes straight
to the view that answers their question, without wading through detail
irrelevant to it.

The views below are a reduced set, drawn from the 4+1 architectural view
model and using [C4](https://c4model.com/) for diagram notation. The first four
form an abstraction ladder, with the most abstract first, and a reader can stop
at whatever altitude answers their question. The remaining two cut across the
ladder rather than sitting on a rung.

- **`conceptual/`** \
  The whole system at-a-glance: major parts, system landscape, the shape of
  the whole.

- **`logical/`** \
  The composition of the system: functional components, responsibilities,
  relationships.

- **`process/`** \
  How the system runs: processes, services, concurrency, communication.

- **`physical/`** \
  Where the system lives: hosts, networks, environments, deployment topology.

- **`scenarios/`** \
  Key end-to-end flows, cutting through the other views.

- **`concepts/`** \
  Crosscutting concerns — domain model, security, persistence, error handling,
  observability — applied system-wide.

A [`glossary.md`](./glossary.md) alongside them defines the architecture- and
domain-specific terms used across the views.

Create each view as a directory with its own `README.md`, using
[`TEMPLATE.md`](./TEMPLATE.md) as the starting point. Supporting artifacts
references from the `README` can be placed in the same directory.
