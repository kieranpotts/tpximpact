# `415` and `413` for malformed requests

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

`POST /shorten` accepts only `application/json`, and a small message body.

The supplied API contract declares only `201` and `400` for this endpoint, and
doesn't say what happens when a client sends another content type or an
oversized body. An unbounded body is a security concern; it is an easy way to
exhaust a server.

HTTP defines `415` and `413` for exactly these use cases. They let any client,
proxy, or monitoring tool tell these mistakes apart from invalid data.

## Decision

`POST /shorten` will return:

- `415 Unsupported Media Type` when the content type is missing or isn't
  `application/json`. Parameters, such as `charset`, are allowed. The response
  carries an `Accept-Post` header.

- `413 Content Too Large` when the body exceeds 16 KiB, about five times the
  largest valid body. An oversized `Content-Length` is rejected without reading
  the body. Otherwise, reading stops at the limit.

Both responses carry a problem details body (see
[ADR 0014](../0014/README.md)), and both checks happen before the body is
parsed.

## Consequences

- Clients get the standard, specific status code for each mistake.

- Memory per request is bounded, whatever a client sends.

## Alternatives

- **Return `400` for both.** Conforms to the oroginal supplied API contract with
  no amendment, but hides specific, well-defined client errors behind a generic
  code.

- **Parse any body as JSON, whatever its content type.** Lenient, but a
  form-encoded body would fail with a misleading "malformed JSON" error.
