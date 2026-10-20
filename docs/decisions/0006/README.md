# Server-rendered web client with Express and Nunjucks

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

PROPOSED

## Context

The web client must be served by Express on Node.js — this is a technical
constraint imposed by the requirements specification.

The web client must be decoupled from the API, reaching stored URLs only
through the API's HTTP interface.

The user interface of the web client is very simple.

## Decision

The web client will be built as a server-rendered application, with Express 5
on Node.js 24, and Nunjucks templates.

It's a back-end for front-end (BFF). The browser talks only to the web client,
using ordinary links and form posts, and the web client calls the API
server-to-server.

Forms use Post/Redirect/Get, without server-side sessions. Everything works
without client-side JavaScript.

## Consequences

- The API service needs no CORS configuration, because browsers never call it
  cross-origin.

- The web client and the API service can share a private network.

- Pages load fast, and are accessible by default.

- Every interaction is a full page load. A richer UI would push toward
  client-side rendering.

- The web client is stateless, and scales horizontally.

## Alternatives

**A single-page application, such as React, served by Express.** It puts two
build pipelines in one component. Either the browser calls the API, which needs
CORS, or Express proxies every call, which makes it a back-end for front-end
anyway, with more moving parts.

**Next.js.** It would replace Express, not run on it.
