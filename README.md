# TPXimpact take-home programming project

**A URL shortener MVP in Kotlin and Express.**

## Debriefing notes

My approach to this project involved the following stages.

1. **Define the specifications.** Write-up requirements specifications and
   business rules, and refine the client-server API contract.

2. **Plan the design.** Use [architecture decision records](./docs/decisions/)
   to drive design choices. Establish the project's ubiquitous language, the
   domain model, and the [high-level design](./docs/design/) — described
   through multiple architectural viewpoints.

3. **Plan the implementation.** [Plan the MVP delivery](./docs/plans/), broken
   down into independently shippable, stable increments.

4. **Prepare the development environment.** Configure a reproducible devcontainer,
   plus code editor and developer tooling configuration. Use `make` as a wrapper
   for all dev workflow commands (except Git). Prepare frameworks for unit tests
   on the client and server sides, plus for end-to-end acceptance tests.

5. **The walking skeleton.** Develop a thin end-to-end slice through the API
   service and the web client. No behavioral requirements implemented yet.

Features are then built incrementally into the walking skeleton against the
[MVP delivery plan](./docs/plans/mvp/README.md). A total of nine tasks (T01-T09)
take the system to the requirements specified in the project brief.

All artifacts — requirements specifications, design docs, delivery plans, as
well as source code and tests — are all under source control, maintained together
in this repository.

Development is AI-assisted. I coordinated coding agents to produce first drafts
of most of the code and other artifacts. I always review, correct, and refine
the output manually before committing — though for this task, due to time
constraints, I have been less rigorous in manual quality controls than I would
normally be. I match the LLM to the task: Opus for design and deep reasoning,
Sonnet for general implementation, and alternating between Kimi and GLM for
documentation drafts and other ad hoc work.

Testing follows a loose TDD approach. The [requirements specification](./docs/specs/)
determines the acceptance tests, and each behavior is committed together with
the tests that verify it — though the tests are not necessarily written first.

Commits are small and atomic. Each one is prefixed by a type, which is loosely
based on Conventional Commits. This important commit types are `behavior` for
changes to runtime functional, `quality` for cross-cutting concerns and
non-functional requirements, `step` for an incremental step toward either of
those outcomes, `fix` for bugs in the production system, `refactor` for
non-trivial structural changes to any production assets, and `maintenance`
for major documentation updates and tooling reconfiguration. The commit types
`chore` and `style` capture tidy-ups and routine housekeeping.

The changelog is maintained manually, though an interactive script
(`make version`) is used to tag stable release points and capture the bundled
changes in the changelog.

This workflow is indicative of my current working methodology for my personal
coding projects: specification and design up front, driven by recorded design
decisions; agent-drafted first versions; and small, tested, atomic increments
that I review before committing directly to a development (non-production ready)
trunk — no feature branches.

The proposed production setup is described in
[ADR 0021: Production hosting](./docs/decisions/0021/).
Observability is partly covered by
[ADR 0020: Structured JSON logs with request-ID propagation](./docs/decisions/0020/).

**Development time:** ~2.5 working days so far. I would expect delivery time to
be at least 2x this for production-grade work. <!-- TODO: Update final delivery time. -->

## 📓 Developer documentation

> [!IMPORTANT]
> The capitalized words REQUIRED, MUST, MUST NOT, RECOMMENDED, SHOULD,
> SHOULD NOT, OPTIONAL, and MAY throughout all docs, code, tests, and all
> other artifacts in this repository are to be interpreted as described in
> [IETF RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

- [**🛠️ Development tools and methods**](./docs/development/) \
  Local development requirements and the day-to-day workflow.
  👉 [Start here](./docs/development/onboard.md) to build and run the system
  locally.

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
