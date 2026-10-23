# 📝 Debriefing notes

My approach to this project involved the following stages.

1. **Define the specifications.** Write the requirements specification and
   business rules, and refine the client-server API contract.

2. **Plan the design.** Use [architecture decision records](./docs/decisions/)
   to drive design choices. Establish the project's ubiquitous language, the
   domain model, and the [high-level design](./docs/design/), described
   through multiple architectural viewpoints.

3. **Plan the implementation.** [Plan the MVP delivery](./docs/plans/mvp/),
   broken down into independently shippable increments.

4. **Prepare the development environment.** Configure a reproducible
   devcontainer, plus editor and developer tooling configuration. Use `make` as
   a wrapper for every dev workflow command except Git. Prepare frameworks for
   unit, integration, and acceptance tests.

5. **Build a walking skeleton.** A thin end-to-end slice through the API
   service and the web client, with no behavior yet.

6. **Build the features.** Add behavior to the walking skeleton one increment
   at a time, following the MVP delivery plan, until the system meets the brief.

All artefacts — specifications, design docs, delivery plans, source code, and
tests — are maintained and evolved together in this repository.

The MVP is an API service, written in Kotlin with Ktor, and a decoupled
web client served by Express. There is no single-page application framework such
as React or Vue — the front-end is server-side rendered. Short URLs are
persisted in PostgreSQL.

Development was highly AI-assisted. I coordinated coding agents to produce first
drafts of most of the code and all other artefacts. I then review, correct, and
refine the output before committing — but, for this task, due to time
constraints, I have been less rigorous in manual quality control than I would
normally be.

I match the LLM to the task: Opus for design and deep reasoning, Sonnet for
generalimplementation, and Kimi or GLM for documentation drafts and other ad
hoc work.

Testing follows a loose TDD approach. The [requirements specification](./docs/specs/)
determines the acceptance tests, and each behavior is committed together with
the tests that verify it — though the tests are not necessarily written first.
The specifications and acceptance tests cross-reference each other through
unique identifiers (eg. B1, B2... for behaviors/features, Q1, Q2... for
non-functional quality attributes, and R1, R2... for business rules).

Commits are small and atomic, each prefixed with a type, as described in
[Committing](./docs/development/committing.md). They go straight onto a
development trunk, with no feature branches. The commit history is therefore
highly granular. The [changelog](./CHANGELOG.md), which is maintained by hand,
is more coarse-grained, with minor versions capturing discrete increments or
themes such as refactoring work. `make version` automates the tagging and
documenation of release points.

This workflow is typical of how I run my personal projects: specification and
design up front, driven by recorded decisions; agent-drafted first versions;
and small, tested increments that I manually review — and rewrite if necessary
— before committing.

**Development time:** ~3 working days. I would expect production-grade work to
take at least twice as long. I cut some corners here, due to time constraints
this week. I've focused on high-level design and delivery planning, with less
attention on low-level code standards than I would have liked.
