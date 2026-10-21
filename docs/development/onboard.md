# Installation and usage

## ☑️ Requirements

- **Docker**, with Docker Compose v2.

Nothing else is needed to run the system. The JDK and Node.js run inside the
workspace devcontainer and the application/service containers.

## 📦 Installation

Clone the repository, then build and start the whole system from its root.

```sh
git clone git@github.com:kieranpotts/tpximpact.git
cd tpximpact
docker compose up --build
```

The first build takes a few minutes. Later builds reuse Docker's cache.

> [!NOTE] You can run `docker compose` from the host or from inside the
> devcontainer. The devcontainer talks to the host's Docker daemon, so Compose
> starts the containers on the host, either way. The devcontainer also shares
> the host's network, so the published ports are reachable on `localhost` from
> both.

Compose starts three containers: the database (`db`), the API service (`api`),
and the web client (`web`). The API service starts once the database is
healthy.

> [!TIP] See [Tools](./tools.md) for full documentation on all available
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

> [!NOTE] The devcontainer shares the host's network, so the ports that Docker
> Compose and Testcontainers publish are reachable on `localhost`.

To run the system in the background, and wait until every container is
healthy:

```sh
docker compose up --build --detach --wait
```

### Ports

The web client is published on port `3000`, and the API service on `8080`. If
either port is taken on your machine, set `WEB_PORT` or `API_PORT` to publish
on a different one.

```sh
WEB_PORT=3001 docker compose up --build
WEB_PORT=3001 make acceptance
```

To set them permanently, copy `.env.example` to `.env` and set them there.
Docker Compose reads `.env` automatically, and variables set in the shell take
precedence over it.

### Stopping

```sh
docker compose down
```

The stored data is on the `db-data` volume, and survives `docker compose down`.
To delete it too, run `docker compose down --volumes`.
