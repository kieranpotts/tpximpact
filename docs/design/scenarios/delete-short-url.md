# S5: Delete a short URL, in the browser

```mermaid
sequenceDiagram
  autonumber
  actor C as Link editor (browser)
  participant W as Web client
  participant A as API service
  participant D as Database

  C->>W: POST /links/my-alias/delete
  W->>A: DELETE /my-alias
  A->>D: DELETE WHERE alias = ?
  D-->>A: 1 row deleted
  A-->>W: 204 No Content
  W-->>C: 303 See Other, Location: /?deleted=my-alias
  C->>W: GET /?deleted=my-alias
  W->>A: GET /urls
  A-->>W: 200 [ … without my-alias … ]
  W-->>C: 200 Page: "my-alias was deleted" banner, and the updated table
```

If the API service returns `404` (someone else deleted the link first), the web client
treats the outcome as the one the link editor wanted, and shows the same banner.
