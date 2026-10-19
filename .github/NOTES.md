# GitHub configuration

<!-- Don't name this file README.md. GitHub will render it in place of the
root-level README from the project landing page. -->

PR and issue templates are automatically inherited from `kieranpotts/.github`.

The following are configured per-repository:

- **[CODEOWNERS](./CODEOWNERS)** \
  Reviewers automatically requested for pull requests, by file path.

- **[dependabot.yml](./dependabot.yml)** \
  Automated dependency updates. Must be named `dependabot.yml` — GitHub does
  not recognize the `.yaml` spelling used by the workflow files.

- **[DISCUSSION_TEMPLATE/](./DISCUSSION_TEMPLATE/)** \
  Forms for new discussion threads.

- **[settings.yml](./settings.yml)** \
  Repository configuration, applied by the `apply-settings` workflow on
  push to the default branch.

- **[workflows/](./workflows/)** \
  GitHub Actions workflows.

Third-party actions are pinned to a commit SHA, with the corresponding version
in a trailing comment. Actions published from `kieranpotts/actions` are tracked
by branch instead, so that consuming repositories pick up changes immediately.

The security policy lives in [SECURITY.md](../SECURITY.md), at the root of the
repository.
