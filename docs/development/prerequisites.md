# Prerequisites

The tooling needed to develop the project, over and above the runtime
requirements listed in the the project's root-level `README`.

# Pre-commit hooks

It is RECOMMENDED to install the [pre-commit](https://pre-commit.com) framework
to enable local validation hooks before committing. You need only to run the
following command once to install pre-commit system-wide:

```bash
pipx install pre-commit
```

Then install the pre-commit hooks in every local repository where you want
pre-commit checks to be run:

```bash
pre-commit install
```

This installs all hook types declared in `.pre-commit-config.yaml`
(`pre-commit`, `commit-msg`). Edit that file to configure the pre-commit
validation checks you want for your project.

The `validate-commit-message` hook checks the format of every commit message
against the conventions documented in [committing](./committing.md). The same
checks run in CI, via the `validate-commit-messages` workflow, so the hook is a
fast local preview of a gate that is enforced on push.

See the [pre-commit documentation](https://pre-commit.com) for more details.
