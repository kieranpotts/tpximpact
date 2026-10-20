# URL validation rules

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: 2026-10-20
- PR: —

## Status

ACCEPTED

## Context

The API must reject a destination URL that is missing or isn't a valid absolute
URL.

Validation is also a security concern. The destination URL is sent back as the
`Location` header on every redirect, and it is rendered as a link in the web
client. A `javascript:` or `data:` URL would be a cross-site scripting vector.
A URL with embedded credentials is a classic phishing disguise. A URL pointing
back at the shortener can create a redirect loop.

## Decision

A destination URL will be accepted only if, after trimming, it is at most 2,048
characters, parses as an absolute URI with an `http` or `https` scheme and a
host, has no user-info, and doesn't point at the shortener's own host.

URLs are parsed with `java.net.URI`, not a regular expression. Otherwise, the
URL is stored as given.

The API never fetches it, and so never validates that the destination exists.

## Consequences

- A stored link can never run scripts when clicked.

- The web client escapes everything it renders.

- The API makes no request to the URL, so there's no server-side request
  forgery risk, and creating a link costs no network time.

- Links to hosts that don't exist are accepted.

- Internationalised domain names must be submitted in their ASCII (Punycode)
  form.

- There's not mechanism to stop chains of redirects through other shortener
  services.

## Deferred decisions

- **Destination validation.** Whether to check that a destination exists, or
  has a good reputation, before accepting it. If added, it should run
  asynchronously, so link creation isn't slowed down, and be guarded against
  server-side request forgery.
