# Error handling

Errors are produced in one place per component, in one format, and never leak
internal detail.

## API service

Route handlers contain no `try`/`catch`. Domain and service code throws typed
exceptions, and the `StatusPages` plugin maps every exception, including
framework ones for malformed requests, to an
[RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `application/problem+json`
response.

| Problem type             | Status | When                                                       |
| ------------------------ | ------ | ---------------------------------------------------------- |
| `malformed-request`      | 400    | Not valid UTF-8 JSON, nested too deeply, or not an object. |
| `validation-failed`      | 400    | Field rules broken. `errors[]` lists every one.            |
| `alias-taken`            | 400    | `customAlias` already exists.                              |
| `not-found`              | 404    | The alias doesn't exist, or can't exist.                   |
| `payload-too-large`      | 413    | Body over 16 KiB.                                          |
| `unsupported-media-type` | 415    | `Content-Type` isn't `application/json`.                   |
| `internal-error`         | 500    | Unexpected fault. `detail` gives only the request ID.      |

Rules:

- No client input produces a `5xx`.
- `500` responses never include exception messages or stack traces. These are
  logged at `ERROR` with the request ID.
- Every response carries an `X-Request-Id` header, accepted from the caller or
  generated.
- A browser following a dead short URL (`Accept: text/html`) gets a small HTML
  "link not found" page instead of JSON.

The `message` texts follow GOV.UK error message style, because the web client
shows them verbatim.

## Web client

The API client never throws for an expected outcome. It returns one of:

| Result        | Produced by                                             | Web client response                                        |
| ------------- | ------------------------------------------------------- | ---------------------------------------------------------- |
| `ok`          | `2xx`                                                   | Redirect (`303`) or render the page.                       |
| `rejected`    | `4xx`, with the problem body parsed if valid            | Re-render the form with `400`, inputs kept, errors shown.  |
| `unavailable` | Connection failure, timeout, `5xx`, or unparseable body | Re-render with `503`, inputs kept, "service unavailable".  |

A `404` from a delete counts as success. An Express error handler catches
anything else, logs it, and renders a generic `500` page with no stack trace.
