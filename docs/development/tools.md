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
| `make up`         | Builds and starts the whole system in the background, and waits until every container is healthy.                |
| `make down`       | Stops the system and removes its containers. The stored data is kept.                                            |
| `make logs`       | Follows the running system's logs. Press Ctrl+C to stop.                                                         |
| `make status`     | Shows the system's containers, their health, and their published ports.                                          |
| `make psql`       | Opens a `psql` session on the running system's database.                                                         |
| `make reset`      | Stops the system and deletes its stored data, after asking for confirmation.                                     |
| `make check`      | Runs `make lint`, then `make test`. Run it before pushing.                                                       |
| `make test`       | Runs the API service's unit and integration tests, and the web client's tests.                                   |
| `make acceptance` | Starts the whole system under Docker Compose, runs the acceptance tests against it, then removes it.             |
| `make lint`       | Checks code style (ktlint, ESLint with StandardJS rules) and types (`tsc`), over source and test code.                   |
| `make format`     | Fixes what `make lint` can fix.                                                                                  |
| `make version`    | Tags a release point, from the `[Unreleased]` entries in `CHANGELOG.md`.                                         |
| `make clean`      | Removes build output and caches. Installed dependencies are kept.                                                |

`make acceptance` runs the system as a separate Compose project, with its own
database volume, so it doesn't touch the data of a system started with
`make up`. It publishes the same ports, though, so stop any other running copy
of the system first, with `make down`.

`make reset` also removes anything a killed `make acceptance` run left behind.

> [!TIP]
> Arguments can be passed by running a script directly. `run/acceptance`
> passes them to Playwright, `run/psql` to `psql`, and `run/up`, `run/down`,
> and `run/logs` to the matching `docker compose` command. For example:
>
> ```sh
> ./run/acceptance --headed
> ./run/logs api
> ./run/psql -c '\dt'
> ./run/reset --yes
> ```
