# Skills

The following skills are available to support the maintenance of this repository.

Every skill MUST be listed here, with a link to its `SKILL.md` and a summary of
the situation in which an agent should reach for it. A skill that lives outside
this directory — elsewhere in the repository, or in another repository
altogether — SHOULD still be listed, so that this file is a complete index.

The entries below are illustrative. Replace them with the skills this project
actually provides.

- **[release/SKILL.md](./release/SKILL.md)** \
  Checklist for cutting a release.

- **[migration/SKILL.md](./migration/SKILL.md)** \
  Guidance for writing database migrations.

- **[../../tools/skills/code-review/SKILL.md](../../tools/skills/code-review/SKILL.md)** \
  Generic code review checklist, shared by every package in this mono-repo.

- **<https://example.com/standards/api-design/blob/main/SKILL.md>** \
  API design conventions, maintained outside this repository.

## Compatibility

These skills are compatible with the [Agent Skills](https://agentskills.io/)
convention. Most agent harnesses support this convention natively, but
workarounds may be required for harnesses that do not.

### Claude Code

Claude Code does not scan `.agents/`. It discovers skills only in:

- `~/.claude/skills/<name>/SKILL.md` (personal, all projects).
- `.claude/skills/<name>/SKILL.md` (project — the working directory and every
  parent directory up to the repository root).
- `<plugin>/skills/<name>/SKILL.md` (plugin).
- `.claude/skills/` inside any directory passed via `--add-dir` or `/add-dir`.

Note that the `permissions.additionalDirectories` setting in `settings.json`
grants file access only — unlike `--add-dir`, it does not load skills.

Referencing this directory from `AGENTS.md` is not sufficient. That injects
prose into the context window, telling the agent the directory exists, but it
does not register the skills: they cannot be invoked as `/<name>`, they do not
appear in autocomplete, they are not loaded automatically when relevant, and
their bodies are not loaded on demand.

The workaround is to commit a symlink from the location Claude Code does read
to the canonical directory. Claude Code follows symlinks when discovering
skills, and loads a skill only once if the same target is reachable from more
than one location. From the repository root:

```sh
ln -s ../.agents/skills .claude/skills
```

Verify the link resolves, and that Claude Code can see the skills:

```sh
ls .claude/skills/
```

Restart Claude Code afterwards if a session is already open. Claude Code
watches skill directories for changes and picks up edits to a `SKILL.md`
without a restart, but it can only watch a top-level skills directory that
existed when the session started.

Two caveats:

1. Only the `<skill-name>` entries within a skills directory are documented as
    supporting symlinks. Linking the whole `.claude/skills` directory, as above,
    is undocumented. If a future release stops following it, link each skill
    individually instead — this is equivalent, but requires a new link whenever
    a skill is added:

    ```sh
    mkdir -p .claude/skills
    for skill in .agents/skills/*/; do
      ln -sfn "../../$skill" ".claude/skills/$(basename "$skill")"
    done
    ```

2. Git records symlinks as mode `120000` blobs, but a Windows client checks
    them out as plain text files containing the link target unless
    `core.symlinks` is enabled *and* the user has Developer Mode enabled or is
    running an elevated shell. Contributors working on Windows without those
    prerequisites MUST either satisfy them or replace `.claude/skills` with a
    copy of `.agents/skills`, kept in sync manually.
