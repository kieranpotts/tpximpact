# Repository filesystem structure

How the repository is organised on disk.

There are three top-level directories: `src/` for everything that ships, `tests/`
for everything that proves it works, and `docs/` for everything that explains it.

## Top level

```
.
├── docs/                     # Specs, designs, decisions, plans, and development docs.
├── run/                      # Development lifecycle scripts, wrapped by the Makefile.
├── src/
│   ├── api-service/          # Kotlin API service (self-contained Gradle build).
│   │   ├── contract/         # The API contract, owned and served by the API service.
│   │   │   └── openapi.yaml
│   │   └── src/main/         # Application code and resources.
│   └── web-client/           # Express web client (self-contained npm package).
│       └── src/              # Application code.
├── tests/
│   ├── api-service/          # API service tests, compiled and run by its Gradle build.
│   │   ├── unit/
│   │   └── integration/
│   ├── web-client/           # Web client tests (npm package).
│   └── acceptance/           # End-to-end acceptance tests of the whole system (npm package).
│
├── .devcontainer/
├── CHANGELOG.md
├── compose.yaml              # Runs the whole system locally.
├── Makefile
└── README.md
```

## Source

`src/` has one subdirectory per independently deployable application. Each is
self-contained, with its own build, `Dockerfile`, and `.dockerignore`.

- `api-service/` is the Kotlin API service, built with Gradle. It also owns the
  API contract, statically maintained at `contract/openapi.yaml`, which is the
  source of truth for the API service's behavior.

- `web-client/` is the Express web client, an npm package. It's a client of the
  API service.

## Tests

`tests/` mirrors `src/`, plus an `acceptance/` suite for the whole system. Test
code is kept out of `src/`, so it never ends up in a Docker image.

- `api-service/` has source directories only (`unit/` and `integration/`). The
  API service's Gradle build points its test suites, `test` and
  `integrationTest`, at them. Each holds a `kotlin/` directory, and a
  `resources/` directory when it needs one.

- `web-client/` is an npm package of its own, holding the test tooling. Run
  `run/install` to install dependencies in both it and `src/web-client/`. It
  mustn't install its own copy of any package that the application imports.

- `acceptance/` is an npm package holding the Playwright suite. It imports
  nothing from `src/` and tests the running system through its public URLs.

The OpenAPI contract has no tests of its own. The API service's HTTP tests
validate every response against it.
