# Host ports for the acceptance tests

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-22
- Decision date: —
- PR: —

## Status

PROPOSED

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

To be decided. The options below are the candidates.

## Consequences

To be completed once a decision is made.

## Alternatives

Options to consider include the following.

- **Separate default ports for acceptance runs.** `run/acceptance` defaults
  `WEB_PORT` and `API_PORT` to other fixed values, eg. `3001` and `8081`, and
  exports them, so the disruptive tests' Compose commands see the same
  configuration. A two-line change, and the ports are predictable, which helps
  when debugging with `--headed`. Overrides still work, though values in
  `.env` would no longer apply to acceptance runs. Two acceptance runs at once
  would still collide.

- **Free ports chosen by the script.** `run/acceptance` asks the operating
  system for two free ports, and exports them before `docker compose up`.
  Avoids every collision, including between parallel runs, and keeps
  `PUBLIC_BASE_URL` correct. More script, ports that change on every run, and a
  small race between choosing a port and binding it.

- **Fail fast with a clear message.** Before starting, `run/acceptance` checks
  whether the system is already running, or the ports are taken, and exits
  with instructions to run `make down` or set the ports. Doesn't remove the
  collision, but replaces Compose's bind error with a more user-friendly error,
  with clear steps to take to resolve the issue. Could be combined with either
  option above.

- **Playwright inside the Compose network.** The acceptance run publishes no
  host ports. Playwright runs in a container on the project's network, and
  reaches the system at `http://web:3000` and `http://api:8080`. Isolates the
  run completely, but adds a service or profile, an image to keep in step with
  `PLAYWRIGHT_VERSION`, and makes `--headed` runs impractical.

- **Status quo.** Stop the running system first, or set the ports by hand, as
  the troubleshooting guide describes. Costs nothing, but the failure recurs,
  and the workaround is easy to forget.
