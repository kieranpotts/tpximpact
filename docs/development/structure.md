# Repository filesystem structure

How the repository is organised on disk.

There are three top-level directories: `src/` for everything that ships, `tests/`
for everything that proves it works, and `docs/` for everything that explains it.

## Top level

```
.
├── docs/                     # Specs, design, decisions, plans, and development docs.
├── run/                      # Development lifecycle scripts, wrapped by the Makefile.
├── src/
│   ├── api-service/          # Kotlin API service (self-contained Gradle build).
│   │   └── contract/         # The API contract, owned and served by the API service.
│   │       └── openapi.yaml
│   └── web-client/           # Express frontend (self-contained npm package).
├── tests/
│   ├── api-service/          # API service tests, compiled and run by its Gradle build.
│   ├── web-client/           # Frontend tests (npm package).
│   └── acceptance/           # End-to-end acceptance tests of the whole system (npm package).
│
├── .devcontainer/
├── Makefile
└── README.md
```

Each application under `src/` is its own Docker build context, with its own
`Dockerfile` and `.dockerignore`. No image contains anything from `tests/`.
