# 📋 Requirements specification

What the system does, in business terms — the major operations and business
rules, and the constraints within which the system must operate.

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

## Behaviors (functional requirements)

### B1 — Shorten a URL with a random alias

A `POST /shorten` request whose body holds a valid `destinationUrl` and no
`customAlias` returns `201 Created`, with a JSON body whose `shortUrl` is the
system's public base URL followed by a random alias.

### B2 — Shorten a URL with a custom alias

A `POST /shorten` request whose body holds a valid `destinationUrl` and a valid,
unused `customAlias` returns `201 Created`, with a JSON body whose `shortUrl` is
the system's public base URL followed by that alias.

### B3 — Reject invalid input

A `POST /shorten` request returns `400 Bad Request`, and mutates no state,
when any part of the request body is not valid JSON or does not conform to
the schema defined in the API contract. The response says which input fields
are invalid, and why.

### B4 — Reject an alias that's already taken

A `POST /shorten` request returns `400 Bad Request`, and leaves the existing
mapping unchanged, when its `customAlias` is already in use.

### B5 — Redirect a short URL

A `GET /{alias}` request for an existing alias returns `302 Found`, with a
`Location` header holding the alias's destination URL. For an alias that does
not exist, it returns `404 Not Found`.

### B6 — Delete a short URL

A `DELETE /{alias}` request for an existing alias returns `204 No Content` and
removes the alias, after which `GET /{alias}` returns `404 Not Found`. For an
alias that does not exist, it returns `404 Not Found`.

### B7 — List all short URLs

A `GET /urls` request returns `200 OK`, with a JSON array holding one
`{ alias, destinationUrl, shortUrl }` object for every alias currently stored.

### B8 — Persist across restarts

Every alias that exists when the API service stops still exists, with the same
destination URL, when the API service starts again.

### B9 — Shorten a URL from the web client

The web client presents a form with a required destination URL field and an
optional custom alias field. Submitting a valid form shows the resulting short
URL.

### B10 — List and delete short URLs from the web client

The web client lists every stored short URL, with its alias and destination
URL, and lets the link editor delete any of them.

### B11 — Show API errors in the web client

When the API service rejects a request, or cannot be reached, the web client shows the
link editor a message that says what went wrong, and keeps what they entered in the
form.

### B12 — Reject an unsupported content type

A `POST /shorten` request whose `Content-Type` is not `application/json`
returns `415 Unsupported Media Type`, and stores nothing.

This is an extension to the original API contract supplied by TPXimpact.

### B13 — Reject an oversized request

A `POST /shorten` request whose body is larger than 16 KiB returns
`413 Content Too Large`, and stores nothing.

This is an extension to the original API contract supplied by TPXimpact.

### B14 — Publish the API contract

A `GET /openapi.yaml` request to the API service returns `200 OK`, and returns the
static OpenAPI specification file.

This is an extension to the original API contract supplied by TPXimpact.

## Business rules

The following business rules are extensions to the requirements specified
by TPXimpact.

### R1 — Valid aliases

An alias is 3 to 64 characters long, and uses only the letters `A–Z` and
`a–z`, the digits `0–9`, the hyphen (`-`), and the underscore (`_`). The same
rule applies to custom and random aliases, which share one namespace.

### R2 — Reserved aliases

These words can't be used as aliases, whatever their case: `shorten`, `urls`,
`api`, `docs`, `health`, `metrics`, `static`, and `assets`. The first two are
API routes. The rest are held back for routes the API service may need later.

### R3 — Aliases are case-sensitive

`MyLink` and `mylink` are different aliases. A short URL only resolves when
its alias is typed with the same letter case it was created with.

### R4 — Random aliases

A random alias is 7 characters long, drawn from letters and digits
only. It's unpredictable. Knowing one alias reveals nothing about any other
alias, or about how many exist. It's never a reserved alias.

### R5 — Valid destination URLs

After surrounding whitespace is trimmed, a destination URL is valid when:

- it is at most 2,048 characters long;
- it is an absolute URL with the scheme `http` or `https`;
- it has a host name;
- it has no embedded user name or password (`https://user:pass@host/`);
- and its host is not the system's own public host, so a short URL can't
  redirect to itself or to another short URL.

Internationalised domain names must be given in their ASCII form.

The system does not check that the URL exists or is reachable.

### R6 — An empty custom alias is invalid

A `customAlias` that is present but empty is rejected as invalid, not treated
as a request for a random alias. Only a missing or `null` `customAlias` asks
for a random one.

### R7 — Destination URLs are stored as given

The trimmed destination URL is stored and redirected to exactly as submitted,
with no other normalisation.

### R8 — No deduplication

Shortening the same destination URL more than once creates a separate alias each
time. Each alias is independent, and can be deleted on its own.

### R9 — Short URLs can't be changed

An alias's destination URL never changes. To point an alias somewhere else,
delete it and create it again.

### R10 — Deleted aliases are free to reuse

Once an alias is deleted, anyone can create it again, straight away. Short URLs
never expire. An alias exists until it's deleted.

### R11 — List order

`GET /urls` lists short URLs newest first. Short URLs created at the same
moment are ordered by alias.

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

### Q5 — Tested

Automated tests cover the behaviors of both the API service and the web client. They run
with a single documented command, and they pass.

### Q6 — Technology stack

The API service is written in Kotlin. The web client is served by Express on Node.js.
These are fixed requirements for programming evaluation.
