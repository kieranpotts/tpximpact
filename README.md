# TPXimpact take-home programming project

**A URL shortener MVP in Kotlin and Express.**

## Debriefing notes

My approach to this project involved the following stages.

1. **Define the specifications.** Write-up requirements specifications and
   business rules, and refine the client-server API contract.

2. **Plan the design.** Use [architecture decision records](./docs/decisions/)
   to drive design choices. Establish the project's ubiquitous language, the
   domain model, and the high-level design through multiple
   [architectural views](./docs/design/).

3. **Plan the implementation.** [Plan MVP delivery](./docs/plans/), broken down
   into independently shippable increments.

4. **Prepare the development environment.** Configure a reproducible devcontainer,
   plus code editor and developer tooling configuration. Use `make` as a wrapper
   for all dev workflow commands (except Git). Prepare frameworks for unit tests
   on the client and server side, and end-to-end acceptance tests.

5. **Build a walking skeleton.** Develop a thin end-to-end slice through the API
   service and the web client. No behavioral requirements implemented yet.

Features are then built incrementally against the delivery plan. <!-- TODO: Total steps? -->

<!-- TODO: AI-assisted development workflow. Loose TDD approach to construction phase. -->
<!-- TODO: Answer other questions from brief. -->

Commits are small and atomic. Each one is prefixed by a type, which is loosely
based on Conventional Commits. This important commit types are `behavior` for
changes to runtime behavior, `quality` for cross-cutting concerns and
non-functional requirements, `step` for an incremental step toward either of
those outcomes, `fix` for bugs in the production system, and `maintenance`
for docs and tooling. The commit types `chore` and `style` capture tidy-ups
and routine housekeeping.

**Development time:** ~2 working days so far.

## 📓 Developer documentation

> [!IMPORTANT]
> The capitalized words REQUIRED, MUST, MUST NOT, RECOMMENDED, SHOULD,
> SHOULD NOT, OPTIONAL, and MAY throughout all docs, code, tests, and all
> other artifacts in this repository are to be interpreted as described in
> [IETF RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

- [**🛠️ Development tools and methods**](./docs/development/) \
  Local development requirements and the day-to-day workflow.
  👉 [Start here.](./docs/development/onboard.md)

- [**📋 Requirements specification**](./docs/specs/) \
  Defines the behaviors and qualities of the system.

- [**📐 Design docs**](./docs/design/) \
  Architectural views of the system.

- [**📜 Architecture decision records**](./docs/decisions/) \
  An historical archive of key design and technical decisions.

- [**🗺️ Delivery plans**](./docs/plans/) \
  Decomposes delivery into independently shippable increments.

---

Copyright © 2026 Kieran Potts, all rights reserved.
