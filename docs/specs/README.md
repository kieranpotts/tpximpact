# 📋 Requirements specification

What the system does, in business terms — the major operations and business
rules, and the constraints within which the system must operate.

> [!IMPORTANT]
> The capitalized words REQUIRED, MUST, MUST NOT, RECOMMENDED, SHOULD,
> SHOULD NOT, OPTIONAL, and MAY are to be interpreted as described in
> [IETF RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

## Context

The system turns long URLs into short ones. A link editor submits a destination URL,
and optionally a custom alias of their choosing, and gets back a short URL that
redirects to the original.

The system has two components: an API service, written in Kotlin, and a
decoupled web client, served by Express.

The system depends on a persistent database, which holds the mapping from
each alias to its destination URL. It has no other external dependencies.

The actors on this system are:

- **Link editors**, who shorten, list, and delete URLs through the web
  client.

- **API clients**, such as `curl` or other services, which call the
  API service directly.

- **Short URL visitors**, who follow a short URL and are redirected to the
  destination URL.

## Traceability

Requirements and acceptance tests reference each other. Each requirement that an
[acceptance test](../../tests/acceptance/) verifies ends with a "Verified by"
link to that test. In turn, each acceptance test carries a
`// Requirement: <ID>` comment naming the requirement it verifies. When adding
or renaming either, update both sides.

## Behaviors (functional requirements)

### B1 — Shorten a URL with a random alias

A `POST /shorten` request whose body holds a valid `destinationUrl` and no
`customAlias` returns `201 Created`, with a JSON body whose `shortUrl` is the
system's public base URL followed by a random alias.

Verified by: [`shorten-random-alias.spec.ts`](../../tests/acceptance/shorten-random-alias.spec.ts).

### B2 — Shorten a URL with a custom alias

A `POST /shorten` request whose body holds a valid `destinationUrl` and a valid,
unused `customAlias` returns `201 Created`, with a JSON body whose `shortUrl` is
the system's public base URL followed by that alias.

Verified by: [`shorten-custom-alias.spec.ts`](../../tests/acceptance/shorten-custom-alias.spec.ts).

### B3 — Reject invalid input

A `POST /shorten` request returns `400 Bad Request`, and mutates no state,
when any part of the request body is not valid JSON or does not conform to
the schema defined in the API contract. The response says which input fields
are invalid, and why.

Verified by: [`reject-invalid-input.spec.ts`](../../tests/acceptance/reject-invalid-input.spec.ts).

### B4 — Reject an alias that's already taken

A `POST /shorten` request returns `400 Bad Request`, and leaves the existing
mapping unchanged, when its `customAlias` is already in use.

Verified by: [`reject-taken-alias.spec.ts`](../../tests/acceptance/reject-taken-alias.spec.ts).

### B5 — Redirect a short URL

A `GET /{alias}` request for an existing alias returns `302 Found`, with a
`Location` header holding the alias's destination URL. For an alias that does
not exist, it returns `404 Not Found`.

Verified by: [`redirect-short-url.spec.ts`](../../tests/acceptance/redirect-short-url.spec.ts).

### B6 — Delete a short URL

A `DELETE /{alias}` request for an existing alias returns `204 No Content` and
removes the alias, after which `GET /{alias}` returns `404 Not Found`. For a
valid alias that does not exist, including one already deleted, it also returns
`204 No Content`, so repeating a delete is harmless. For a path that cannot be
an alias, it returns `404 Not Found`.

Verified by: [`delete-short-url.spec.ts`](../../tests/acceptance/delete-short-url.spec.ts).

### B7 — List all short URLs

A `GET /urls` request returns `200 OK`, with a JSON array holding one
`{ alias, destinationUrl, shortUrl }` object for every alias currently stored.

Verified by: [`list-short-urls.spec.ts`](../../tests/acceptance/list-short-urls.spec.ts).

### B8 — Persist across restarts

Every alias that exists when the API service stops still exists, with the same
destination URL, when the API service starts again.

Verified by: [`persist-across-restarts.spec.ts`](../../tests/acceptance/persist-across-restarts.spec.ts).

### B9 — Shorten a URL from the web client

The web client presents a form with a required destination URL field and an
optional custom alias field. Submitting a valid form shows the resulting short
URL.

Verified by: [`shorten-from-web-client.spec.ts`](../../tests/acceptance/shorten-from-web-client.spec.ts).

### B10 — List and delete short URLs from the web client

The web client lists every stored short URL, with its alias and destination
URL, and lets the link editor delete any of them.

Verified by: [`list-and-delete-from-web-client.spec.ts`](../../tests/acceptance/list-and-delete-from-web-client.spec.ts).

### B11 — Show API errors in the web client

When the API service rejects a request, or cannot be reached, the web client shows the
link editor a message that says what went wrong, and keeps what they entered in the
form.

Verified by: [`show-api-errors.spec.ts`](../../tests/acceptance/show-api-errors.spec.ts).

### B12 — Reject an unsupported content type

A `POST /shorten` request whose `Content-Type` is not `application/json`
returns `415 Unsupported Media Type`, and stores nothing.

This is an extension to the original API contract supplied by TPXimpact.

Verified by: [`reject-unsupported-content-type.spec.ts`](../../tests/acceptance/reject-unsupported-content-type.spec.ts).

### B13 — Reject an oversized request

A `POST /shorten` request whose body is larger than 16 KiB returns
`413 Content Too Large`, and stores nothing.

This is an extension to the original API contract supplied by TPXimpact.

Verified by: [`reject-oversized-request.spec.ts`](../../tests/acceptance/reject-oversized-request.spec.ts).

### B14 — Publish the API contract

A `GET /openapi.yaml` request to the API service returns `200 OK`, and returns the
static OpenAPI specification file.

This is an extension to the original API contract supplied by TPXimpact.

Verified by: [`publish-contract.spec.ts`](../../tests/acceptance/publish-contract.spec.ts).

### B15 — Welcome visitors to the root path

A `GET /` request to the API service returns `200 OK`, with an HTML page that
says "Welcome to the URL shortener".

This is an extension to the original API contract supplied by TPXimpact.

Verified by: [`welcome-page.spec.ts`](../../tests/acceptance/welcome-page.spec.ts).

### B16 — Open short URLs in a new tab

Every short URL that the web client shows as a link opens in a new browser
tab, so the link editor stays on the web client. Screen readers announce
that each link opens in a new tab.

Verified by: [`open-short-urls-in-new-tab.spec.ts`](../../tests/acceptance/open-short-urls-in-new-tab.spec.ts).

## Business rules

The following business rules are extensions to the requirements specified
by TPXimpact.

### R1 — Valid aliases

An alias is 3 to 64 characters long, and uses only the letters `A–Z` and
`a–z`, the digits `0–9`, the hyphen (`-`), and the underscore (`_`). The same
rule applies to custom and random aliases, which share one namespace.

Verified by: [`valid-aliases.spec.ts`](../../tests/acceptance/valid-aliases.spec.ts).

### R2 — Reserved aliases

These words can't be used as aliases, whatever their case: `shorten`, `urls`,
`api`, `docs`, `health`, `metrics`, `static`, and `assets`. The first two are
API routes. The rest are held back for routes the API service may need later.

Nor can `v` be followed by one or more digits, such as `v1` or `v10`, whatever
its case. These are held back for versioned API paths, such as
`/v1/openapi.yaml`.

Verified by: [`reserved-aliases.spec.ts`](../../tests/acceptance/reserved-aliases.spec.ts).

### R3 — Aliases are case-sensitive

`MyLink` and `mylink` are different aliases. A short URL only resolves when
its alias is typed with the same letter case it was created with.

Verified by: [`case-sensitive-aliases.spec.ts`](../../tests/acceptance/case-sensitive-aliases.spec.ts).

### R4 — Random aliases

A random alias is 7 characters long, drawn from letters and digits
only. It's unpredictable. Knowing one alias reveals nothing about any other
alias, or about how many exist. It's never a reserved alias.

Verified by: [`random-aliases.spec.ts`](../../tests/acceptance/random-aliases.spec.ts).

### R5 — Valid destination URLs

After surrounding whitespace is trimmed, a destination URL is valid when:

- it is at most 2,048 characters long;
- it uses only printable ASCII characters, with no spaces;
- it is an absolute URL with the scheme `http` or `https`;
- it has a host name;
- it has no embedded user name or password (`https://user:pass@host/`);
- and its host is not the system's own public host, so a short URL can't
  redirect to itself or to another short URL.

Anything else must be percent-encoded, and internationalised domain names
must be given in their ASCII form. A browser's address bar copies URLs in
this form. The rule keeps every destination URL safe to send, unchanged, in
the `Location` header of a redirect.

The system does not check that the URL exists or is reachable.

Verified by: [`valid-destination-urls.spec.ts`](../../tests/acceptance/valid-destination-urls.spec.ts).

### R6 — An empty custom alias is invalid

A `customAlias` that is present but empty is rejected as invalid, not treated
as a request for a random alias. Only a missing or `null` `customAlias` asks
for a random one.

Verified by: [`empty-custom-alias.spec.ts`](../../tests/acceptance/empty-custom-alias.spec.ts).

### R7 — Destination URLs are stored as given

The trimmed destination URL is stored and redirected to exactly as submitted,
with no other normalisation.

Verified by: [`stored-as-given.spec.ts`](../../tests/acceptance/stored-as-given.spec.ts).

### R8 — No deduplication

Shortening the same destination URL more than once creates a separate alias each
time. Each alias is independent, and can be deleted on its own.

Verified by: [`no-deduplication.spec.ts`](../../tests/acceptance/no-deduplication.spec.ts).

### R9 — Short URLs can't be changed

An alias's destination URL never changes. To point an alias somewhere else,
delete it and create it again.

Verified by: [`unchangeable-short-urls.spec.ts`](../../tests/acceptance/unchangeable-short-urls.spec.ts).

### R10 — Deleted aliases are free to reuse

Once an alias is deleted, anyone can create it again, straight away. Short URLs
never expire. An alias exists until it's deleted.

Verified by: [`reuse-deleted-aliases.spec.ts`](../../tests/acceptance/reuse-deleted-aliases.spec.ts).

### R11 — List order

`GET /urls` lists short URLs newest first. Short URLs created at the same
moment are ordered by alias.

Verified by: [`list-order.spec.ts`](../../tests/acceptance/list-order.spec.ts).

## Qualities (non-functional requirements)

### Q1 — Conforms to the API contract

Every API service path, method, status code, and response body shape matches the
OpenAPI contract.

### Q2 — Fails gracefully

No client input, however malformed, causes the API service to return a `5xx` response.

### Q3 — Decoupled web client

The web client is a separately built and deployed component. It interacts with
stored URLs only through the API service's public HTTP interface, and it reads the API service's
public base URL from configuration.

### Q4 — Containerised

The API service and the web client each build to a container image. The whole system
starts locally with the commands documented in the README.

Verified by: [`containerised.spec.ts`](../../tests/acceptance/containerised.spec.ts).

### Q5 — Tested

Automated tests cover the behaviors of both the API service and the web client. They run
with a single documented command, and they pass.

### Q6 — Technology stack

The API service is written in Kotlin. The web client is served by Express on Node.js.
These are fixed requirements for programming evaluation.

### Q7 — Survives a database outage

While the database is unavailable, every API service request that needs it
returns `503 Service Unavailable` within 5 seconds, with a problem details
body that reveals no internal detail. Requests that don't need the database
are unaffected. Once the database is available again, the API service serves
requests as normal, without being restarted.

Verified by: [`database-outage.spec.ts`](../../tests/acceptance/database-outage.spec.ts).
