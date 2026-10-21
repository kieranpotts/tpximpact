#
# Task runner for this project's development lifecycle. Each target wraps a
# same-named script in `run/`.
#
# See: docs > development > tools.
#

.PHONY: install build up down logs status psql reset check test acceptance lint format version clean help

help:
	@echo "Available targets:"
	@echo "  install     - Install project dependencies."
	@echo "  build       - Build production-grade artifacts."
	@echo "  up          - Build and start the whole system, and wait until it's healthy."
	@echo "  down        - Stop the system, keeping its stored data."
	@echo "  logs        - Follow the running system's logs. Ctrl+C to stop."
	@echo "  status      - Show the system's containers and their health."
	@echo "  psql        - Open a psql session on the system's database. Type "exit" to leave. "
	@echo "  reset       - Stop the system and delete its stored data."
	@echo "  check       - Run all static and dynamic checks sequentially, from linters to acceptance tests."
	@echo "  test        - Run the automated test suite."
	@echo "  acceptance  - Run the acceptance tests against the whole system."
	@echo "  lint        - Run the linters."
	@echo "  format      - Run the formatters."
	@echo "  version     - Tag a release point."
	@echo "  clean       - Remove build output and caches."
	@echo "  help        - Show this help message."

install:
	./run/install

build:
	./run/build

up:
	./run/up

down:
	./run/down

logs:
	./run/logs

status:
	./run/status

psql:
	./run/psql

reset:
	./run/reset

check:
	./run/check

test:
	./run/test

acceptance:
	./run/acceptance

lint:
	./run/lint

format:
	./run/format

version:
	./run/version

clean:
	./run/clean
