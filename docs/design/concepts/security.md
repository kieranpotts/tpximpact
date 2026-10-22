# Security

The system has no users, accounts, or permissions. Anyone who can reach it can
create, list, follow, and delete any short URL. Authentication and ownership
are out of scope for the MVP. The controls below address the threats that
remain.

| Threat                                               | Controls                                                                                                                     |
| ---------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| **Stored XSS** via a `javascript:…` destination.     | Only `http` and `https` URLs are accepted. Template autoescaping is on. The CSP allows only `'self'` scripts plus one nonce. |
| **Phishing disguise** via credentials in the URL.    | URLs with user-info are rejected.                                                                                            |
| **Redirect loops** through the shortener's own host. | URLs on the public base URL's host are rejected.                                                                             |
| **SQL injection.**                                   | Every query is a prepared statement. There is no dynamic SQL.                                                                |
| **Malformed or oversized input.**                    | Bodies are capped at 16 KiB, non-JSON and deep nesting are refused before parsing, and field lengths are enforced.           |
| **Header splitting** via `Location`.                 | Destination URLs must be printable ASCII, with no spaces or control characters, and must pass `java.net.URI` parsing.        |
| **Information leakage.**                             | `500` bodies carry only a request ID.                                                                                        |
| **Clickjacking** and other browser attacks.          | `helmet` sets `frame-ancestors 'none'`, `X-Content-Type-Options`, `Referrer-Policy: same-origin`, and HSTS.                  |
| **CSRF** on the web client's forms.                  | Form posts whose `Origin` doesn't match the web client's host are rejected.                                                  |
| **Container compromise.**                            | Non-root users, read-only root filesystems, and image scanning (Trivy in CI).                                                |
| **Secrets exposure.**                                | No secrets in images or Git. Secrets are supplied through environment variables.                                             |
| **Transport snooping.**                              | Out of scope for the MVP, which serves plain HTTP. Transport security is a deployment concern.                               |
