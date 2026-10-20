# Persistence

Short URLs are stored in PostgreSQL. Nothing else is persisted: the web client
and the API service's processes are stateless.

```sql
CREATE TABLE short_url (
    alias       VARCHAR(64)   NOT NULL,
    full_url    VARCHAR(2048) NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT short_url_pkey PRIMARY KEY (alias)
);

CREATE INDEX short_url_created_at_idx ON short_url (created_at DESC, alias);
```

- `alias` is the primary key. Its unique index serves every redirect and
  delete, and enforces uniqueness. Comparison is case-sensitive.

- `created_at` is set by the database. Its index serves the newest-first
  listing.

All SQL lives in `JdbcShortUrlRepository`, as prepared statements, each in
auto-commit mode.

## Guarantees

- **Uniqueness:** inserts use `ON CONFLICT (alias) DO NOTHING`, so concurrent
  inserts of the same alias are serialised by the primary-key index. The
  application takes no locks.

- **Read-after-delete:** nothing is cached, so a redirect after a successful
  delete always returns `404`.

- **Durability:** a `201` is returned only after the insert commits.

## Migrations

Schema changes are Flyway migrations (`V1__…sql`, `V2__…sql`, and so on),
never edited once merged.
