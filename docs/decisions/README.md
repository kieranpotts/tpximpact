# 📜 Architecture decision records

This is a permanent, append-only archive of every significant technical decision
made about this project, including ideas that were ultimately rejected.

An ADR covers a technical decision — _how_ the system is built. Product
decisions — _what_ the system should do — belong under [`specs/`](../specs/).

## Structure

One file per decision, numbered in the order the decisions were made. An ADR's
index number is unique and permanent.

```
decisions/
├── TEMPLATE.md         # The starting point for a new record.
└── <index>/            # One decision.
    ├── README.md       # The decision record itself.
    └── …               # Diagrams and other materials referenced from the README.
```

## Lifecycle

```
PROPOSED ──> ACCEPTED ──> SUPERSEDED
    │
    └──────> REJECTED
```

A record is written as `PROPOSED`, reviewed via a pull request, and merged once
it is `ACCEPTED` or `REJECTED`.

Once merged, the document is immutable. Only the value of a merged report's
`Status` field may later be changed to `SUPERSEDED`, with a pointer added to
the newer decision record that overrides it.

Thus, to revisit a past decision, write a new record. Do not edit an old one.

## Index

- [0001](./0001/) Monorepo — ACCEPTED
- [0002](./0002/) Ktor for the API — PROPOSED
- [0003](./0003/) PostgreSQL for persistence — PROPOSED
- [0004](./0004/) Flyway for schema migrations, run at API startup — PROPOSED
- [0005](./0005/) Plain JDBC with HikariCP for data access — PROPOSED
- [0006](./0006/) Server-rendered web client with Express and Nunjucks — PROPOSED
- [0007](./0007/) TypeScript for the web client — PROPOSED
- [0008](./0008/) GOV.UK Frontend for the user interface — PROPOSED
- [0009](./0009/) Custom alias validation rules — PROPOSED
- [0010](./0010/) Random alias generation — PROPOSED
- [0011](./0011/) URL validation rules — PROPOSED
- [0012](./0012/) No caching of redirects — PROPOSED
