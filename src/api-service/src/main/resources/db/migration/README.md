# Database migrations

These files are applied by [Flyway](https://documentation.red-gate.com/flyway)
on startup.

## Naming

Flyway requires this filename format, and ignores or rejects files that do not
match it:

```
V<version>__<description>.sql
```

- `V` marks a versioned (run-once) migration.

- `<version>` (eg. `1`, `2`, `2.1`) determines the order in which migrations run,
  and is recorded in Flyway's schema history table so that each migration is
  applied exactly once.

- The separator is **two** underscores.

- `<description>` is free text, with underscores rendered as spaces in the
  history table.

## Rules

- Never edit a migration that has been applied anywhere. Flyway checksums each
  file and fails validation if one changes. Add a new, higher-versioned
  migration instead.

- Do not reuse or reorder version numbers.
