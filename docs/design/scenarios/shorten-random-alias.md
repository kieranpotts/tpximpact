# S3: Shorten a URL with a random alias, via the API service

```mermaid
sequenceDiagram
  autonumber
  participant K as API client
  participant A as API service
  participant D as Database

  K->>A: POST /shorten {destinationUrl}
  A->>A: Validate DestinationUrl
  loop Up to 5 attempts
    A->>A: Generate 7-char Base62 alias, skip if reserved
    A->>D: INSERT … ON CONFLICT DO NOTHING
    D-->>A: 1 row (done) or 0 rows (collision, retry)
  end
  A-->>K: 201 {shortUrl: "http://localhost:8080/aZ3k9Qx"}
```

A collision is vanishingly rare ([ADR 0010](../../decisions/0010/)).
If all five attempts collide, the API service returns `500 internal-error`, and logs it.
