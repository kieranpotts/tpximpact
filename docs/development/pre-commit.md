# Pre-commit hook

A Git `pre-commit` hook runs `make lint`, then `make test`, before every commit,
and aborts the commit if either fails.

The acceptance tests are too slow to run on every commit, so the hook leaves
them out. It is RECOMMENDED to run `make check`, which runs all three, before
pushing fresh commits to the shared trunk.

The hook is managed by the [pre-commit](https://pre-commit.com) framework, and
declared in `.pre-commit-config.yaml` at the project root.

## Installation

### In the devcontainer

Nothing to do. The devcontainer has pre-commit installed, and runs
`pre-commit install` every time it starts.

### On the host

It is RECOMMENDED to install the pre-commit framework system-wide, with
[pipx](https://pipx.pypa.io/). You only need to do this once.

```sh
pipx install pre-commit
```

Then install the hook, from the project root:

```sh
pre-commit install
```

To remove the hook:

```sh
pre-commit uninstall
```

## Usage

The hook runs on every `git commit`. Before running the checks, pre-commit
stashes any changes you haven't staged, so the checks run against what you're
about to commit, not against your working tree. It puts the changes back
afterwards, whether the checks pass or fail.

To check your work-in-progress, you can run the checks without committing:

```sh
pre-commit run
```

To skip the hook for a single commit, eg. a work-in-progress commit that you'll
amend before pushing, or a commit that changes only documentation:

```sh
git commit --no-verify
```

Skip the pre-commit checks sparingly. Every commit pushed to the shared trunk
SHOULD pass at least `make lint && make test`, and preferably `make acceptance`
too.
