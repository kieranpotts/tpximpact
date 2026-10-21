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

## 📦 Installation

Clone the repository, then build and start the whole system from its root.

```sh
git clone git@github.com:kieranpotts/tpximpact.git
cd tpximpact
make up
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

Currently, each application serves only a placeholder page.

```sh
curl http://localhost:8080/
# URL shortener API service
```

While the system is running:

```sh
make status   # Show the containers, their health, and their ports.
make logs     # Follow the logs. Press Ctrl+C to stop.
make psql     # Open a psql session on the database.
```

### Ports

The web client is published on port `3000`, and the API service on `8080`. If
either port is taken on your machine, set `WEB_PORT` or `API_PORT` to publish
on a different one.

```sh
WEB_PORT=3001 make up
WEB_PORT=3001 make acceptance
```

To set them permanently, copy `.env.example` to `.env` and set them there.
Docker Compose reads `.env` automatically, and variables set in the shell take
precedence over it.

See [Troubleshooting](./troubleshooting.md#-ports) if you hit a port conflict.

### Stopping

```sh
make down
```

The stored data is on the `db-data` volume, and survives `make down`. To delete
it too, run `make reset`.
