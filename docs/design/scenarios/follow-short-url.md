# S4: Follow a short URL

```mermaid
sequenceDiagram
  autonumber
  actor V as Short URL visitor (browser)
  participant A as API service
  participant D as Database
  participant T as Destination website

  V->>A: GET /aZ3k9Qx
  A->>A: Is "aZ3k9Qx" a valid alias?
  A->>D: SELECT full_url WHERE alias = ?
  D-->>A: https://example.com/very/long/url
  A-->>V: 302 Found, Location: https://example.com/…, Cache-Control: no-store
  V->>T: GET https://example.com/very/long/url
```

If the path segment isn't a valid alias, the API service returns `404` without
querying the database. If no row matches, it returns `404`, as an HTML page
for browsers and problem details for everything else
([ADR 0014](../../decisions/0014/)).
