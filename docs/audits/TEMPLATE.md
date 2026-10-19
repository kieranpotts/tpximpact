# Audit title, eg. "Payment module architecture review"

- **Auditors:** Your Name [@your-github-handle], ...
- **Audit date:** YYYY-MM-DD
- **Audit PR:** #...
- **Scope:** The revision examined, eg. `@<commit>`, and the parts of the
  codebase covered

## Summary

A short, single-paragraph verdict on the overall health of the system
as-built, and the headline concerns. What is the shape of the problem?

## Scope and method

What was examined (directories, modules, packages) and how (eg. static
reading, dependency graphing, the deletion test, a module-depth walk).

State what was deliberately out-of-scope, so the audit's coverage can be
judged.

## Findings

Each finding names a structural problem in the as-is system, its location, and
why it matters. Order by priority.

| ID  | Finding     | Type                  | Priority | Location         |
| --- | ----------- | --------------------- | -------- | ---------------- |
| F01 | Short title | Shallow abstraction   | HIGH     | module / path    |
| F02 | Short title | Tangled dependency    | MEDIUM   | module / path    |
| F03 | Short title | Single-caller wrapper | LOW      | module / path    |

### F01 — Short title

- **Type:** Shallow abstraction | Tangled dependency | Single-caller wrapper |
  Duplication | Leaky boundary | Inverted dependency | Misnamed abstraction |
  ...
- **Priority:** HIGH | MEDIUM | LOW
- **Location:** The module or path where it lives.

What the problem is — describe the structure that exists. Why it matters — the
cost it imposes on changeability, coupling, or comprehension.

### F02 — Short title

- **Type:** ...
- **Priority:** ...
- **Location:** ...

...

## Themes

Recurring patterns across the findings, which may hint at systemic issues that
the individual findings are merely symptoms of.

## Priorities

A prioritized shortlist of the findings that would bring the most value in
being resolved.
