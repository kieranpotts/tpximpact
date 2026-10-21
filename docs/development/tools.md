# Tools

The `Makefile` at the project root is the single entry point for the
day-to-day development lifecycle. Simply run `make` to see what tools
are available.

Each Make target is a thin wrapper around a same-named script in the
root-level `run/` directory. Logic lives in the run scripts, not the
`Makefile`. The scripts may be run directly — eg. `./run/build` — when that's
more convenient.

> [!IMPORTANT]
> The `run/` scripts MUST be executable. Git records them as
> executable, but a checkout can lose that, eg. on a filesystem without Unix
> permissions, or with `core.fileMode` set to `false`. If `make` reports
> `Permission denied`, run `chmod +x run/*` from the project root.

## Targets

The `make` targets work the same inside the devcontainer or on the host. The
devcontainer provides the toolchain (JDK, Node, Docker, and Chromium's system
libraries). On the host, you must install them yourself.

| Target            | What it does                                                                                                     |
| ----------------- | ---------------------------------------------------------------------------------------------------------------- |
| `make install`    | Installs Gradle, the npm packages in `src/web-client`, `tests/web-client`, and `tests/acceptance`, and Chromium. |
| `make build`      | Builds the API service's and the web client's container images.                                                  |
| `make test`       | Runs the API service's unit and integration tests, and the web client's tests.                                   |
| `make acceptance` | Starts the whole system under Docker Compose, runs the acceptance tests against it, then removes it.             |
| `make lint`       | Checks code style (ktlint, ESLint, and Prettier) and types (`tsc`), over source and test code.                   |
| `make format`     | Fixes what `make lint` can fix.                                                                                  |
| `make version`    | Tags a release point, from the `[Unreleased]` entries in `CHANGELOG.md`.                                         |
| `make clean`      | Removes build output and caches. Installed dependencies are kept.                                                |

`make acceptance` runs the system as a separate Compose project, with its own
database volume, so it doesn't touch the data of a system started with
`docker compose up`. It publishes the same ports, though, so stop any other
running copy of the system first.

> [!TIP]
> Arguments for Playwright can be passed by running the script directly,
> eg. `./run/acceptance --headed`.
