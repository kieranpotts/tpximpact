# Troubleshooting

Problems that commonly trip up developers, and how to get past them. Each
entry starts with the symptom.

## 🔌 Ports

### "port is already allocated", or "address already in use"

The web client is published on host port `3000`, and the API service on `8080`.
If something else on your machine is listening on either, Compose fails to
start that container.

Find what's using the port:

```sh
ss -ltnp | grep -E ':(3000|8080)\b'
```

If it's another copy of this system, stop it with `docker compose down`.
Otherwise publish on different ports, with `WEB_PORT` and `API_PORT`:

```sh
WEB_PORT=3001 API_PORT=8081 docker compose up --build
```

To set them permanently, copy `.env.example` to `.env` and set them there. A
variable set in the shell takes precedence over `.env`.

Only the host side of the mapping changes. The applications always listen on
`3000` and `8080` inside their containers, so containers reach each other at
the same addresses, regardless.

### `make acceptance` fails to start the system

`make acceptance` publishes the same ports as `docker compose up`. If a copy
of the system is already running, the acceptance run collides with it. Stop the
other copy first, or set `WEB_PORT` and `API_PORT` for the acceptance run. The
script reads the ports back from Compose, so the tests follow wherever they
were published.

```sh
WEB_PORT=3001 API_PORT=8081 make acceptance
```

### The tests pass against the wrong system

The acceptance tests reach the system only through `WEB_CLIENT_URL` and
`PUBLIC_BASE_URL`. If you run Playwright yourself, rather than through
`./run/acceptance`, they default to `http://localhost:3000` and
`http://localhost:8080`. Export both variables when you've published on
other ports.

## 🐳 Docker

### The integration tests can't find Docker

`make test` includes the API service's integration tests, which start
PostgreSQL in Testcontainers. They need a running Docker daemon.

In the devcontainer, Docker is the _host's_ daemon, reached through the
`docker-outside-of-docker` feature. Make sure Docker is running on the host.

Because the daemon is the host's, the PostgreSQL containers that Testcontainers
starts run on the host too, not inside the devcontainer. The tests connect to
them on `localhost`, which works because the devcontainer shares the host's
network (`--network=host`, with `TESTCONTAINERS_HOST_OVERRIDE=localhost`). If
you edit `.devcontainer/devcontainer.json`, keep those two settings.

### `docker compose up --wait` fails, or a container is "unhealthy"

Look at the logs of the container that Compose names:

```sh
docker compose logs api
```

The API service has no health endpoint, at this time. It counts as healthy once
its port accepts connections, so "unhealthy" means the process didn't start or
didn't bind port `8080`.

The `db` container must be healthy before `api` starts.

### Old data is still there, or the database won't start after a change

The database stores its data on the `db-data` volume, which survives
`docker compose down`. To start from an empty database, delete the volume too:

```sh
docker compose down --volumes
```

The volume is mounted at `/var/lib/postgresql`, not `/var/lib/postgresql/data`,
because PostgreSQL 18 images keep data in a versioned subdirectory. If you
change the major version of the `postgres` image, or the mount path, delete the
volume or PostgreSQL will refuse to start on the old data.

### A containerised application can't write to its filesystem

The `api` and `web` containers run with a read-only root filesystem, as a
non-root user. Only `/tmp` is writable. Anything else that tries to write will
fail, which is deliberate.

### A previous acceptance run left containers behind

`make acceptance` removes its containers and volume when it exits, pass or fail.
If the run was killed hard, eg. with `kill -9`, clean up by hand. It uses its
own Compose project:

```sh
COMPOSE_PROJECT_NAME=url-shortener-acceptance docker compose down --volumes --remove-orphans
```

## 🧰 Tooling

### `make` reports "Permission denied"

The `run/` scripts must be executable. A checkout can lose that, eg. on a
filesystem without Unix permissions. Run `chmod +x run/*` from the project
root.

### Playwright can't launch Chromium

`make install` downloads the browser, but Chromium also needs system libraries.
The devcontainer has them. Elsewhere, install them from `tests/acceptance/`:

```sh
npx playwright install-deps chromium
```

If you upgrade Playwright in `tests/acceptance/package.json`, update
`PLAYWRIGHT_VERSION` in `.devcontainer/Dockerfile` to match.

### The web client's tests can't resolve `express`

The web client's tests import the application from `src/web-client`, so they
use its installed packages. Run `make install`, which installs both
`src/web-client` and `tests/web-client`.

### `make lint` fails on code that compiles

`make lint` covers test code as well as source, and runs type checks (`tsc`) on
top of ESLint and Prettier. Run `make format` first, which fixes most style
problems.

### Tests or lint fail with the wrong Java or Node version

The project needs JDK 25 and Node.js 24 or later, which the devcontainer
provides. On the host, Gradle's toolchain must find a JDK 25, and
`engines` in each `package.json` requires Node 24+. The application containers
use the same versions.

### `make version` refuses to run

`make version` needs a clean working tree, and at least one entry under
`## [Unreleased]` in `CHANGELOG.md`. It commits and tags locally but doesn't
push. Push with `git push --follow-tags` once you're happy.

## 📦 Devcontainer

### The devcontainer won't start, with a bind mount error

`.devcontainer/devcontainer.json` bind-mounts these from your home directory,
and Docker fails to start the container if any is missing: `.gitconfig`,
`local.gitconfig`, `local.gitmessage`, `local.gitignore`, and `.ssh`. Its
`initializeCommand` creates empty ones on the host before the container starts,
so you'll see them in your home directory even if you don't use them. If it
can't, for example on a Windows host outside WSL, you'll need to create them
yourself.

### Files in the project are owned by the wrong user

The container runs as the non-root `vscode` user, UID 1000, so that
bind-mounted files keep your ownership on the host. If your host UID isn't 1000,
the build takes the UID and GID from the `UID` and `GID` environment variables.
Make sure they are exported in the shell that launches your editor.

Never work as `root` in the container, else build output and `node_modules` will
be root-owned on the host.

### Signing commits fails with "Inappropriate ioctl for device"

The devcontainer forwards your host's `gpg-agent` into the container, so
commits are signed by the host agent. Don't mount `~/.gnupg` into the container.
That makes `gpg` start its own agent, which has no terminal to prompt on.
