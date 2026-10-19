# 📋 Requirements specification

What the system does, in business terms — the major operations and business
rules, and the constraints within which the system must operate.

## Context

The system turns long URLs into short ones. A user submits a full URL, and
optionally a custom alias of their choosing, and gets back a short URL that
redirects to the original.

The system has two components: a RESTful API, written in Kotlin, and a
decoupled web frontend, served by Express.

The system depends on a persistent data store, which holds the mapping from
each alias to its full URL. It has no other external dependencies.

The actors on this system are:

- **Link creators**, who shorten, list, and delete URLs through the web
  frontend.

- **API clients**, such as `curl` or other services, which call the API
  directly.

- **Link visitors**, who follow a short URL and are redirected to the full URL.

## Behaviors (functional requirements)

### B1 — Shorten a URL with a random alias

A `POST /shorten` request whose body holds a valid `fullUrl` and no
`customAlias` returns `201 Created`, with a JSON body whose `shortUrl` is the
system's public base URL followed by a randomly generated alias.

### B2 — Shorten a URL with a custom alias

A `POST /shorten` request whose body holds a valid `fullUrl` and a valid,
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
`Location` header holding the alias's full URL. For an alias that does not
exist, it returns `404 Not Found`.

### B6 — Delete a short URL

A `DELETE /{alias}` request for an existing alias returns `204 No Content` and
removes the alias, after which `GET /{alias}` returns `404 Not Found`. For an
alias that does not exist, it returns `404 Not Found`.

### B7 — List all short URLs

A `GET /urls` request returns `200 OK`, with a JSON array holding one
`{ alias, fullUrl, shortUrl }` object for every alias currently stored.

### B8 — Persist across restarts

Every alias that exists when the API stops still exists, with the same full URL,
when the API starts again.

### B9 — Shorten a URL from the frontend

The frontend presents a form with a required full URL field and an optional
custom alias field. Submitting a valid form shows the resulting short URL.

### B10 — List and delete short URLs from the frontend

The frontend lists every stored short URL, with its alias and full URL, and lets
the user delete any of them.

### B11 — Show API errors in the frontend

When the API rejects a request, or cannot be reached, the frontend shows the
user a message that says what went wrong, and keeps what they entered in the
form.

## Qualities (non-functional requirements)

### Q1 — Conforms to the API contract

Every API path, method, status code, and response body shape matches
[`contracts/openapi.yaml`](../../contracts/openapi.yaml).

### Q2 — Fails gracefully

No client input, however malformed, causes the API to return a `5xx` response.

### Q3 — Decoupled frontend

The frontend is a separately built and deployed component. It interacts with
stored URLs only through the API's public HTTP interface, and it reads the API's
base URL from configuration.

### Q4 — Containerised

The API and the frontend each build to a container image. The whole system
starts locally with the commands documented in the README.

### Q5 — Tested

Automated tests cover the behaviors of both the API and the frontend. They run
with a single documented command, and they pass.

### Q6 — Technology stack

The API is written in Kotlin. The frontend is served by Express on Node.js.
These are fixed requirements for programming evaluation.
