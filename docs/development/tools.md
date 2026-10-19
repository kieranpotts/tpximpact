# Tools

The `Makefile` at the project root is the single entry point for the
day-to-day development lifecycle. Simply run `make` to see what tools
are available.

Each Make target is a thin wrapper around a same-named script in the
root-level `run/` directory. Logic lives in the run scripts, not the
`Makefile`. Scripts can be run directly — eg. `./run/build` — when that's
more convenient.
