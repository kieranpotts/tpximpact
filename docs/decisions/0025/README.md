# Host ports for the acceptance tests

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-22
- Decision date: —
- PR: —

## Status

ACCEPTED

## Context

`make acceptance` runs the system as its own Compose project, with its own
database volume, so it doesn't touch the data of a system started with
`make up`.

But both publish on the same host ports, `3000` and `8080`. So, if a
copy of the system is already running, eg. for local development purposes,
then the acceptance run fails to start.

The current workarounds are to stop the running instance, or to change
`WEB_PORT` and `API_PORT`.

The host can't assign a port at random — the port must be configured before
`docker compose up`. That's because the API service must know its public port
before it starts, because `PUBLIC_BASE_URL`, which prefixes every short URL it
returns, is derived from `API_PORT`. This constrains our options.

## Decision

Acceptance runs will publish on their own host ports, `3001` for the web client
and `8081` for the API service, so they can run alongside a system started with
`make up`, which runs on `3000` and `8080` by default.

The environment variables `ACCEPTANCE_WEB_PORT` and `ACCEPTANCE_API_PORT` can
be used to override the default ports for the acceptance test system ports.
These may be configured in the shell or a local root-level `.env` file.

Before it starts the system, `run/acceptance` will check that both ports are
free. If either is taken, it will exit with a message naming the culprit, where
it can, and the steps to resolve it.

## Consequences

- The acceptance tests run (via `make acceptance`) even when another instance of
  the system has been started (via `make up`).

- The ports are predictable, which helps when debugging with `--headed`.

- `WEB_PORT`, `API_PORT`, and `PUBLIC_BASE_URL` no longer apply to acceptance
  runs.

- `run/acceptance` must read `.env` itself, for the two new variables, since
  Compose only reads it for the variables that `compose.yaml` interpolates.

- A port conflict fails in seconds, with an actionable message, rather than
  after the images are built.

- Two acceptance runs happening in parallel will still interfere with each other.
  They will share the same Compose project.

## Alternatives

- **Free ports chosen by the script.** `run/acceptance` asks the operating
  system for two free ports, and exports them before `docker compose up`.
  Avoids every collision, including between parallel runs, and keeps
  `PUBLIC_BASE_URL` correct. Not chosen because the ports change on every run,
  which makes debugging harder, and parallel acceptance runs aren't a need.

- **Playwright inside the Compose network.** The acceptance run publishes no
  host ports. Playwright runs in a container on the project's network, and
  reaches the system at `http://web:3000` and `http://api:8080`. Isolates the
  run completely, but adds a service or profile, an image to keep in step with
  `PLAYWRIGHT_VERSION`, and makes `--headed` runs impractical. Too much
  machinery for the problem.

- **Status quo.** Stop the running system first, or set the ports by hand, as
  the troubleshooting guide describes. Costs nothing, but the failure recurs,
  and the workaround is easy to forget.
