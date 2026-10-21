#
# Task runner for this project's development lifecycle. Each target wraps a
# same-named script in `run/`.
#
# See: docs > development > tools.
#

.PHONY: install build test acceptance lint format version clean help

help:
	@echo "Available targets:"
	@echo "  install     - Install project dependencies"
	@echo "  build       - Build production-grade artifacts"
	@echo "  test        - Run the automated test suite"
	@echo "  acceptance  - Run the acceptance tests against the whole system"
	@echo "  lint        - Run the linters"
	@echo "  format      - Run the formatters"
	@echo "  version     - Tag a release point"
	@echo "  clean       - Remove build output and caches"
	@echo "  help        - Show this help message"

install:
	./run/install

build:
	./run/build

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
