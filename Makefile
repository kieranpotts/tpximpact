#
# Task runner for this project's development lifecycle. Each target wraps a
# same-named script in `run/`.
#
# See: docs > development > tools.
#

.PHONY: install build test lint version clean help

help:
	@echo "Available targets:"
	@echo "  install  - Install project dependencies"
	@echo "  build    - Build production-grade artifacts"
	@echo "  test     - Run the automated test suite"
	@echo "  lint     - Run the linter"
	@echo "  version  - Tag a release point"
	@echo "  clean    - Remove build output and caches"
	@echo "  help     - Show this help message"

install:
	./run/install

build:
	./run/build

test:
	./run/test

lint:
	./run/lint

version:
	./run/version

clean:
	./run/clean
