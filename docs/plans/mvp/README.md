# URL shortener MVP - high level delivery plan

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-20
- Last updated: 2026-10-22
- Plan PR: —

## Status

IN PROGRESS

## References

- Implements: [requirements specification](../../specs/), B1 to B14, R1 to
  R11, and Q1 to Q7.

- Informed by: the accepted architectural decisions,
  [ADR 0001](../../decisions/0001/) to [ADR 0018](../../decisions/0018/).

- Targets design: [`docs/design/`](../../design/), and the
  [repository layout](../../development/structure.md).

## Out-of-scope

- Everything in the proposed decisions, [ADR 0019](../../decisions/0019/) to
  [ADR 0023](../../decisions/0023/).

- Pagination of `GET /urls`, authentication, and link ownership. Each needs a
  change to the specification first.

## Approach

**Walking skeleton first.** The riskiest part of the project is the toolchain
and wiring, not the features: two build systems, three test packages, test
suites that live outside the apps they test, Testcontainers inside a
devcontainer, and Playwright against Compose. The first increment proves all
of it end to end, around placeholder apps, so every later increment is
ordinary feature work.

**Then vertical slices, one behavior at a time.** Each later increment
delivers a small set of requirements whole: code, tests at every relevant
level, and acceptance tests named after the requirement IDs. The API service
leads, and each web client increment follows once the endpoints it calls
exist, so its acceptance tests run against the real system.

**Every increment is shippable.** Each lands as one pull request. At the end
of every increment, `docker compose up --build` runs the system, and
`make check` passes. No increment relies on
work scheduled after it.

**Every increment updates the changelog.** Before an increment is marked
done, its notable changes are added to the `[Unreleased]` section of the
[changelog](../../../CHANGELOG.md), in the same pull request.

**Details are settled in implementation.** The design docs and ADRs say what
to build. Where implementation shows them to be wrong or incomplete, the
increment that finds out corrects the docs in the same pull request.

## Task breakdown

This plan tracks the status of each increment. There is no separate issue
tracker.

| ID  | Increment                                      | Delivers                     | Status    | Depends on |
| --- | ---------------------------------------------- | ---------------------------- | --------- | ---------- |
| T01 | Walking skeleton                               | Q4, Q5, Q6                   | ✅ DONE   | —          |
| T02 | API service foundations, and the contract      | B14, Q1, Q2                  | ✅ DONE   | T01        |
| T03 | Shorten URLs through the API                   | B1 to B4, B12, B13, R1 to R8 | ✅ DONE   | T02        |
| T04 | Follow short URLs                              | B5, B8, Q7                   | ✅ DONE   | T03        |
| T05 | Shorten URLs from the web client               | B9, B11, Q3                  | ✅ DONE   | T03        |
| T06 | List and delete short URLs through the API     | B6, B7, R9 to R11            | ✅ DONE   | T04        |
| T07 | List and delete short URLs from the web client | B10                          | ✅ DONE   | T05, T06   |
| T08 | Accessibility and progressive enhancement      | —                            | ⭕ TODO   | T07        |
| T09 | Documentation and release                      | —                            | ⭕ TODO   | T08        |

### Increments

- **T01 — Walking skeleton.** The devcontainer, with the JDK, Node.js, and
  access to Docker. The API service's Gradle build, with its unit and
  integration suites sourced from `tests/api-service/`. The web client and its
  test package, and the acceptance test package. Linters and formatters for
  both languages. Both `Dockerfile`s, and `compose.yaml` with `db`, `api`, and
  `web`. The `run/` scripts and `Makefile` targets. Each app serves only a
  placeholder page. Done when every `make` target works, and one placeholder
  test passes at each test level, including a Testcontainers database test
  and a Playwright smoke test against Compose.

- **T02 — API service foundations, and the contract.** The
  [configuration](../../design/concepts/configuration.md),
  [error handling](../../design/concepts/error-handling.md) (problem details
  and `X-Request-Id`), graceful shutdown, the database connection pool, and
  migrations at startup. The HTTP test harness, which validates every
  response against the contract. `GET /openapi.yaml` is the first real
  behavior, and proves the harness, the image, and the acceptance suite.

- **T03 — Shorten URLs through the API.** The
  [domain model](../../design/concepts/domain-model.md), with the alias and
  destination URL rules. The schema and repository for short URLs.
  `POST /shorten`, with random and custom aliases, validation, alias
  conflicts, and the `415` and `413` checks. Hostile-input tests show that no
  input causes a `5xx`. Unit tests show that the random alias generator draws
  from a cryptographically secure source, as [ADR 0010](../../decisions/0010/)
  requires, so aliases can't be predicted from earlier ones. Tests with an
  injected generator that returns a duplicate first show that a collision is
  retried and succeeds, and that five consecutive collisions end in the
  documented failure, not a hang.

- **T04 — Follow short URLs.** `GET /{alias}` redirects, and `404`s for
  unknown and invalid aliases. Tests that restart the API service and its
  database, and show that every alias survives. Fill in the R7 acceptance
  test, `tests/acceptance/stored-as-given.spec.ts`, which T03 left as a
  `test.fixme` placeholder because nothing could observe a stored destination
  URL before redirects existed. A test that stops the database and shows that
  a redirect fails cleanly, with a problem details `503` and no stack trace or
  hang, and that redirects resume when the database returns.

- **T05 — Shorten URLs from the web client.** The web client's
  configuration, security headers, GOV.UK Frontend layout, and its client for
  the API service. The shorten form, the result shown after a successful
  submission, and error messages that keep the link editor's input, both when
  the API service rejects the request and when it can't be reached.

- **T06 — List and delete short URLs through the API.** `GET /urls`, newest
  first, and `DELETE /{alias}`. Tests show that a deleted alias stops
  redirecting, and can be created again straight away.

- **T07 — List and delete short URLs from the web client.** The table of
  short URLs on the home page, and a delete button on each row.

- **T08 — Accessibility and progressive enhancement.** The copy-link button,
  which the page works without. Automated accessibility scans of every page
  state in the acceptance suite. A check that every requirement ID has a test
  named after it.

- **T09 — Documentation and release.** Rewrite the README: prerequisites, how
  to run and test the system, example requests, notes and assumptions, links
  to the docs, and time spent. Fill in the developer docs that are still
  templates. Correct any design doc the implementation has drifted from.
  Review the whole codebase for `TODO` comments and either resolve each one
  or turn it into a documented follow-up. In the README's notes and
  assumptions, state the scale the MVP is designed for (a single PostgreSQL
  instance, which is ample below about 100 million links), and list what it
  deliberately omits: redirect caching, distributed ID generation, and a CDN.
  Each omission names the metric that would justify adding it, and links to
  [ADR 0021](../../decisions/0021/). Verify everything from a clean clone, tag
  a versioned release, and submit.

## Dependency graph

```mermaid
graph LR
  T01 --> T02 --> T03
  T03 --> T04 --> T06
  T03 --> T05
  T05 --> T07
  T06 --> T07
  T07 --> T08 --> T09
```

## Open questions

None.
