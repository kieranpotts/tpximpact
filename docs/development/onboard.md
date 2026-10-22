# Installation and usage

## ☑️ Requirements

- **Docker**, with Docker Compose v2.

Nothing else is needed to run the host system. The JDK and Node.js run inside the
workspace devcontainer and the application/service containers.

If you want to run the [dev tools](./tools.md) directly on the host, you will
need **GNU Make** and **Bash** (4.4 or later), plus:

- **A JDK**, version 17 or later, to run Gradle. The API service builds with
  JDK 25. Gradle downloads it if it's not already installed.

- **Node.js 24** or later, with npm.

- **Git**, for `make version`.

- **Chromium's system libraries**, for `make acceptance`. Install them with
  `npx playwright install-deps chromium`, from `tests/acceptance/`.

- **[pre-commit](https://pre-commit.com)**, to commit from the host. The Git hook
  runs `make lint` and `make test` before every commit. See
  [Pre-commit hook](./pre-commit.md).

## 📦 Installation

Clone the repository, then build and start the whole system from its root.

```sh
git clone git@github.com:kieranpotts/tpximpact.git
cd tpximpact
make up
```

If you have only Docker, run Compose directly instead. It runs in the
foreground, and the first build takes a few minutes.

```sh
docker compose up --build
```

`make up` starts the system in the background, and returns once every container
is healthy. The first build takes a few minutes. Later builds reuse Docker's
cache. Run `make up` again after changing the code, to rebuild and restart what
changed.

> [!NOTE]
> You can run `make up`, and the other targets that drive Docker Compose, from
> the host or from inside the devcontainer. The devcontainer talks to the
> host's Docker daemon, so Compose starts the containers on the host, either
> way. The devcontainer also shares the host's network, so the published ports
> are reachable on `localhost` from both.

Compose starts three containers: the database (`db`), the API service (`api`),
and the web client (`web`). The API service starts once the database is
healthy.

> [!TIP]
> See [Tools](./tools.md) for full documentation on all available
> dev lifecycle commands, or simply run `make` in the project root directory.

## 🧭 Usage

| What        | Where                   |
| ----------- | ----------------------- |
| Web client  | <http://localhost:3000> |
| API service | <http://localhost:8080> |

The database isn't published on the host.

Open the web client to shorten, list, and delete URLs. Short URLs point at the
API service, which redirects them. To call the API service directly, see
[Calling the API](./api.md), or the API contract it serves:

```sh
curl http://localhost:8080/openapi.yaml
```

While the system is running:

```sh
make status   # Show the containers, their health, and their ports.
make logs     # Follow the logs. Press Ctrl+C to stop.
make psql     # Open a psql session on the database.
```

### Ports

By default, the web client is published on port `3000`, and the API service on
`8080`. If either port is taken on your machine, set `WEB_PORT` or `API_PORT`
to publish the services on different ports.

```sh
WEB_PORT=3002 make up
API_PORT=8090 docker compose up --build
```

The acceptance tests (executed via `make acceptance`) run a separate instance
of both services on ports `3001` and `8081`. This allows acceptance tests to be
run alongside development instances of the system (run via `make up`). Set
`ACCEPTANCE_WEB_PORT` or `ACCEPTANCE_API_PORT` to change the ports for the
system-under-test in acceptance tests.

```sh
ACCEPTANCE_WEB_PORT=3003 make acceptance
```

To set any of them permanently, copy `.env.example` to `.env` and set them there.
Docker Compose reads `.env` automatically, and variables set in the shell take
precedence over it.

See [Troubleshooting](./troubleshooting.md#-ports) if you hit a port conflict.

### Stopping

```sh
make down
```

The stored data is on the `db-data` volume, and survives `make down`. To delete
it too, run `make reset`.

Without Make, press Ctrl+C, or run `docker compose down`. To delete the stored
data too, run `docker compose down --volumes`.

## 🧪 Testing

The tests need the JDK and Node.js, which the devcontainer provides. Open the
repository in it, or install the toolchain on the host, as the requirements
above describe. Then:

```sh
make install   # Once, after cloning.
make check     # Linters, then all tests, stopping at the first failure.
```

`make check` runs these narrower targets in turn:

```sh
make lint         # ktlint, ESLint, and tsc.
make test         # Unit and integration tests for the API service and the web client.
make acceptance   # Playwright tests against the whole system, under Docker Compose.
```

`make acceptance` runs its own copy of the system, on ports `3001` and `8081`,
with its own database, so it doesn't disturb a system that's already running.
See [Ports](#ports).
