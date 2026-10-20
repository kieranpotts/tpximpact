# S2: Submit an alias that's already taken

```mermaid
sequenceDiagram
  autonumber
  actor C as Link editor (browser)
  participant W as Web client
  participant A as API service
  participant D as Database

  C->>W: POST /links (destinationUrl, customAlias=my-alias)
  W->>A: POST /shorten {destinationUrl, customAlias}
  A->>D: INSERT … ON CONFLICT DO NOTHING
  D-->>A: 0 rows inserted
  A-->>W: 400 application/problem+json, type …:alias-taken
  W->>A: GET /urls
  A-->>W: 200 [ … ]
  W-->>C: 400 Page: error summary, inline error on the alias field, inputs kept
```

The same flow, without the database step, applies to any validation failure
(B3). The API service returns `validation-failed` with one entry per bad field, and the
web client shows each one against its field.
