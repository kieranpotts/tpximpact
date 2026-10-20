# Delivery pipeline (CI/CD)

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

PROPOSED

**Not in-scope for MVP.**

## Context

The repository is hosted on GitHub. Every push should be linted, tested, and
built, so that a broken change is caught before review. Production needs a
pipeline from a tested commit to running containers.

## Decision

GitHub Actions will be used, with one workflow on every push and pull request.
It checks the API service, checks the web client, runs the acceptance tests against
the built images, and scans those images for vulnerabilities.

Jobs call the same `run/` scripts that developers use locally. Third-party
actions are pinned to commit SHAs, and Dependabot keeps dependencies current.

The delivery pipeline, which isn't implemented here, extends this workflow on
the default branch. It pushes the images, authenticating with OIDC, deploys to
staging, and after a manual approval promotes the same images to production.

## Consequences

- Every pull request shows lint, test, acceptance, and scan results before
  review.

- GitHub's hosted runners include Docker, so Testcontainers needs no setup.

- The acceptance tests add a few minutes to each run.
