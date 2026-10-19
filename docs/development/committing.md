# Commit Messages

The commit log is a first-class artifact of this project. It outlives issues,
tickets, and decision logs, and it is the source-of-truth for understanding how
the codebase got to where it is. Commit deliberately, with the same care given
to the code itself.

## Revision types

Each commit MUST be scoped to exactly one of the following eleven revision
types, which is written as the prefix of the commit message header.

| Type          | Covers                                                                        |
| ------------- | ----------------------------------------------------------------------------- |
| `behavior`    | A new, changed, deprecated, or removed user-facing behavior.                  |
| `quality`     | A change to a dynamic quality attribute — latency, throughput, security, etc. |
| `fix`         | Resolution of a defect: bug, regression, vulnerability, incident.             |
| `step`        | An increment toward a larger change, not user-facing on its own.              |
| `refactor`    | An improvement to internal design or structure, behavior unchanged.           |
| `style`       | Presentation-only changes: whitespace, wrapping, formatter runs.              |
| `maintenance` | Upkeep: dependency updates, CI config, test and docs improvements.            |
| `chore`       | Small housekeeping, not worth tracking in the issue tracker.                  |
| `version`     | Preparation of a new numbered release.                                        |
| `merge`       | An explicit merge commit.                                                     |
| `revert`      | Reverting an earlier commit.                                                  |

Two distinctions are easy to get wrong:

- **`quality` vs. `refactor`.** Quality revisions improve external, dynamic
  qualities that users can observe or measure. Refactors improve internal,
  static qualities that only developers see.

- **`style` vs. `refactor`.** A style commit changes only how the code looks.
  A refactor changes how it is structured.

## Commit message format

A commit message consists of a header, an optional body, and optional footers.
Each block is separated by a single empty line.

```txt
<type>: <description>

[<body>]

[<footers>]
```

Write commit messages in American English, using US-ASCII characters only.

### Header

The header — the subject line — is the only REQUIRED part. It consists of the
commit type and a description, separated by a colon and exactly one space.

- Write the description in lowercase, with no terminating period.

- Use the imperative mood, present tense: "remove", not "removed" or "removes".
  The description should complete the sentence, "If applied, this commit will
  …".

- Start with a verb describing the action taken. Two exceptions: a `fix` need
  only name the problem being fixed, and a `version` need only give the
  version number.

- Keep the whole header under 50 characters where possible. It MUST NOT exceed
  72. Only automated `merge` and `revert` headers are exempt.

- Do not put issue numbers in the header. They belong in the footers.

The point of these rules is that `git log --oneline` reads as a coherent
high-level narrative of the project.

```txt
chore: initial commit, add readme
step: add openapi specification
fix: invalid yaml formatting
refactor: move openapi spec to resources directory
behavior: enable route to openapi spec
version: 0.0.0-beta
```

### Body

The body is OPTIONAL. While the header records _what_ changed, the body records
_why_: the motivation, the background context, the approaches that were
considered and rejected, and anything a future maintainer would want to know
that cannot be read off the diff. Recording what the change deliberately does
_not_ do is often as valuable as recording what it does.

Do not restate what the commit object already carries. There is no reason to
list the files that changed.

Write full sentences terminated by periods, in paragraphs separated by blank
lines. Markdown-style bullet lists are welcome, with hanging indents for
wrapped lines. Keep lines to 72 characters.

```txt
fix: prevent racing of requests

Introduce a request id and a reference to the latest request. Dismiss
incoming responses other than from latest request.

Remove timeouts which were previously used to mitigate the racing
issue but which are now obsolete.

Refs: #123
```

### Footers

Footers are a block of key-value pairs, separated from what precedes them by a
single blank line, one pair per line, in the form `<key>: <value>`. Keys are
case-insensitive; by convention only the first word is capitalized.

Cross-reference the issues and pull requests relevant to the change. Use
`Closes` to have the referenced issue closed automatically when the commit
lands on the default branch, or `Refs` to reference it without closing it.

```txt
Closes: #123
Refs: #456
```

## Validation

Commit validation is not enabled for this project.
