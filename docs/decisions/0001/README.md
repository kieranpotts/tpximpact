# Monorepo

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

ACCEPTED

## Context

The system has two independently deployable applications, a Kotlin API service
and an Express web client. The requirements specification calls for the API
service and web client to be highly decoupled, with separate technology
stacks, build steps, and deployment pipelines. At the same time, a requirement
of development and testing is to be able to build and run the whole end-to-end
system.

## Decision

Everything will be kept in one repository, with two main top-level directories:
`src/` and `tests/`.

```
.
├── src/
│   ├── api-service/    # The API service, a self-contained Gradle build.
│   │   └── contract/   # The OpenAPI contract, owned and served by the API service.
│   └── web-client/     # The web client, a self-contained npm package.
│
└── tests/
    ├── api-service/    # Unit and integration tests for src/api-service.
    ├── web-client/     # Tests for src/web-client.
    └── acceptance/     # End-to-end tests of the whole system.
```

The repository root holds only artifacts that span the whole project, such as
the project `README.md`, and IDE and Git config files.

Other top-level directories include `docs/`, which will capture developer
documentation references from the project readme, and `run/`, which will store
shell scripts for automation of development and operations processes, many of
which will be targets of the root-level `Makefile`.

## Consequences

- A reviewer clones one repository.

- A change that spans the API service and the web client, for example a new
  field, is one atomic commit across both. The commit history reads as vertical
  (full-stack) stories.

- Each application's image is built from its own directory alone.

- Tests are packaged differently in each toolchain.

## Alternatives

- **One independently deployable application per code repository.** Overkill for
  such a simple system, in which server-side and client-side changes can be
  comfortably made at the same time. Each application could easily be lifted
  into its own repository later, by moving `src/x` and `tests/x` together, plus
  other minor repointing.
