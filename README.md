# TPXimpact take-home programming project

**A URL shortener MVP in Kotlin and Express.**

The system is an API service, written in Kotlin with Ktor, and a decoupled web
client, served by Express. Short URLs are stored in PostgreSQL.

## 🚀 Run the system

You need only **Docker**, with Docker Compose v2. From the repository root:

```sh
docker compose up --build
```

The first build takes a few minutes. Once all three containers (`db`, `api`,
and `web`) are up:

| What        | Where                   |
| ----------- | ----------------------- |
| Web client  | <http://localhost:3000> |
| API service | <http://localhost:8080> |

Open the web client to shorten, list, and delete URLs. Short URLs point at the
API service, which redirects them. The database isn't published on the host.

To stop the system, press Ctrl+C, or run `docker compose down`. The stored
short URLs survive. To delete them too, run `docker compose down --volumes`.

If port `3000` or `8080` is taken, set `WEB_PORT` or `API_PORT`, eg.
`API_PORT=8090 docker compose up --build`.

## 🧪 Test the system

The tests need the JDK and Node.js, which the
[devcontainer](./.devcontainer/) provides. Open the repository in it, or
install the toolchain on the host, as the
[onboarding guide](./docs/development/onboard.md) describes. Then:

```sh
make install   # Once, after cloning.
make check     # Linters, then all tests, stopping at the first failure.
```

`make check` runs these narrower targets in turn:

```sh
make lint         # ktlint, ESLint, and tsc.
make test         # Unit and integration tests for the API service and the web client.
make acceptance   # Playwright tests against the whole system, under Docker Compose.
```

`make acceptance` runs its own copy of the system, on ports `3001` and `8081`,
with its own database, so it doesn't disturb a system that's already running.

In the devcontainer, `make up`, `make down`, and `make reset` do the same as the
`docker compose` commands above. Run `make` to list every target, or see
[Tools](./docs/development/tools.md).

## 📡 Example requests

These run against the API service on <http://localhost:8080>. Responses are
trimmed. Every response also carries an `X-Request-Id` header.

**Shorten a URL with a random alias:**

```sh
curl -i http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"destinationUrl": "https://www.gov.uk/browse/driving"}'
```

```http
HTTP/1.1 201 Created
Content-Type: application/json

{"shortUrl":"http://localhost:8080/RqEQrBI"}
```

**Shorten a URL with a custom alias:**

```sh
curl -i http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"destinationUrl": "https://www.tpximpact.com/", "customAlias": "tpx-readme"}'
```

```http
HTTP/1.1 201 Created
Content-Type: application/json

{"shortUrl":"http://localhost:8080/tpx-readme"}
```

**Follow a short URL:**

```sh
curl -i http://localhost:8080/tpx-readme
```

```http
HTTP/1.1 302 Found
Cache-Control: no-store
Location: https://www.tpximpact.com/
```

**List all short URLs, newest first:**

```sh
curl -i http://localhost:8080/urls
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

[{"alias":"tpx-readme","destinationUrl":"https://www.tpximpact.com/","shortUrl":"http://localhost:8080/tpx-readme"},
 {"alias":"RqEQrBI","destinationUrl":"https://www.gov.uk/browse/driving","shortUrl":"http://localhost:8080/RqEQrBI"}]
```

**Delete a short URL:**

```sh
curl -i -X DELETE http://localhost:8080/tpx-readme
```

```http
HTTP/1.1 204 No Content
```

**Get the API contract:**

```sh
curl -i http://localhost:8080/openapi.yaml
```

```http
HTTP/1.1 200 OK
Content-Type: application/yaml

openapi: 3.0.3
info:
  title: URL Shortener API
…
```

**A rejected request.** Errors are
[RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details, which name
each invalid field:

```sh
curl -i http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"destinationUrl": "ftp://example.com", "customAlias": "no"}'
```

```http
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{
  "type": "urn:problem:url-shortener:validation-failed",
  "title": "The request is not valid",
  "status": 400,
  "detail": "The request has 2 invalid fields.",
  "errors": [
    {"field": "destinationUrl", "code": "url-unsupported-scheme", "message": "Enter a URL starting with http:// or https://"},
    {"field": "customAlias", "code": "alias-invalid-length", "message": "Alias must be between 3 and 64 characters"}
  ]
}
```

To explore the API interactively, see
[Testing the API with Postman](./docs/development/postman.md).

## 📝 Notes, assumptions, and limits

### Assumptions

Where the brief was silent, I made the choices below. Each is specified in the
[requirements specification](./docs/specs/), and justified in an ADR.

- **Aliases** are 3 to 64 letters, digits, hyphens, and underscores. They're
  case-sensitive. A few words are reserved for routes
  ([R1 to R3](./docs/specs/README.md#r1--valid-aliases),
  [ADR 0009](./docs/decisions/0009/)).
- **Random aliases** are 7 letters and digits, drawn from a cryptographically
  secure source, so they can't be guessed or enumerated
  ([R4](./docs/specs/README.md#r4--random-aliases),
  [ADR 0010](./docs/decisions/0010/)).
- **Destination URLs** must be absolute `http` or `https` URLs, and are stored
  exactly as given. A short URL can't point at another short URL
  ([R5](./docs/specs/README.md#r5--valid-destination-urls),
  [R7](./docs/specs/README.md#r7--destination-urls-are-stored-as-given),
  [ADR 0011](./docs/decisions/0011/)).
- **Short URLs** are never deduplicated, never changed, and never expire. A
  deleted alias is free to reuse at once
  ([R8 to R10](./docs/specs/README.md#r8--no-deduplication)).
- **Deleting** a valid alias that doesn't exist returns `204`, so a repeated
  delete is harmless ([B6](./docs/specs/README.md#b6--delete-a-short-url)).
- **Redirects** are `302`, and never cached, so a delete takes effect at once
  ([ADR 0012](./docs/decisions/0012/)).
- **The API service** also rejects non-JSON bodies with `415` and bodies over
  16 KiB with `413` ([ADR 0015](./docs/decisions/0015/)), serves its contract at
  `GET /openapi.yaml`, and serves a welcome page at `GET /`.
- **Errors** are problem details, with no internal detail. While the database
  is down, requests that need it get a `503`, and the service recovers on its
  own ([Q7](./docs/specs/README.md#q7--survives-a-database-outage)).

### Scale

The MVP is designed for a single PostgreSQL instance, which is ample below
about 100 million links. A redirect is one primary-key lookup, and the API
service is stateless, so it scales out. The proposed production setup is in
[ADR 0021](./docs/decisions/0021/).

It deliberately omits the following. Each is worth adding only once a metric
shows the need ([ADR 0012](./docs/decisions/0012/),
[ADR 0021](./docs/decisions/0021/)).

| Omitted                     | Add it when                                                                                                                                                    |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Redirect caching            | Redirect latency (p99), or database CPU and read load, breaches its target. Needs a cache that's invalidated on delete.                                        |
| Distributed ID generation   | Alias collisions on insert, and the retries they cause, become measurable. With 100 million links, about 3 in 100,000 random aliases collide, and are retried. |
| A CDN in front of redirects | Origin request volume, or latency for distant visitors, outgrows the region. It also needs the spec to allow deletes to take effect late.                      |

### Out of scope

- Pagination of `GET /urls`, authentication, and link ownership. Each needs a
  change to the specification first.
- The proposed ADRs, [0019](./docs/decisions/0019/) to
  [0024](./docs/decisions/0024/), and [0026](./docs/decisions/0026/): health
  and metrics endpoints, structured logs, production hosting, CI/CD,
  idempotent link creation, contract tests against the running service, and
  automated accessibility tests.

## 🗒️ Debriefing notes

My approach to this project involved the following stages.

1. **Define the specifications.** Write the requirements specification and
   business rules, and refine the client-server API contract.

2. **Plan the design.** Use [architecture decision records](./docs/decisions/)
   to drive design choices. Establish the project's ubiquitous language, the
   domain model, and the [high-level design](./docs/design/), described
   through multiple architectural viewpoints.

3. **Plan the implementation.** [Plan the MVP delivery](./docs/plans/mvp/), broken
   down into eight independently shippable increments, T01 to T08.

4. **Prepare the development environment.** Configure a reproducible
   devcontainer, plus editor and developer tooling configuration. Use `make` as
   a wrapper for every dev workflow command except Git. Prepare frameworks for
   unit, integration, and acceptance tests.

5. **Build a walking skeleton.** A thin end-to-end slice through the API
   service and the web client, with no behavior yet, to prove the toolchain.

6. **Build the features.** Add behavior to the walking skeleton one increment
   at a time, until the system meets the brief.

All artifacts — specifications, design docs, delivery plans, source code, and
tests — are maintained together in this repository.

Development is AI-assisted. I coordinated coding agents to produce first drafts
of most of the code and other artifacts. I review, correct, and refine the
output before committing, though for this task, due to time constraints, I have
been less rigorous in manual quality control than I would normally be. I match
the model to the task: Opus for design and deep reasoning, Sonnet for general
implementation, and Kimi or GLM for documentation drafts and other ad hoc work.

Testing follows a loose TDD approach. The [requirements specification](./docs/specs/)
determines the acceptance tests, and each behavior is committed together with
the tests that verify it, though the tests are not necessarily written first.

Commits are small and atomic, each prefixed with a type, as described in
[Committing](./docs/development/committing.md). They go straight onto a
development trunk, with no feature branches. The [changelog](./CHANGELOG.md) is
maintained by hand, and `make version` tags each release point.

This workflow is typical of how I run my personal projects: specification and
design up front, driven by recorded decisions; agent-drafted first versions;
and small, tested increments that I review before committing.

**Development time:** ~2.5 working days so far. I would expect production-grade
work to take at least twice as long. <!-- TODO: Update final delivery time. -->

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
