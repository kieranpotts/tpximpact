# Workshop title, eg. "Payment flow threat model"

- **Participants:** Your Name [@your-github-handle], ...
- **Workshop date:** YYYY-MM-DD
- **Report PR:** #...
- **Scope:** The revision assessed, eg. `@<commit>`, and the components,
  services, or data flows examined
- **Frameworks:** STRIDE | LINDDUN | OWASP Top 10 | ...

## Summary

A short, single-paragraph statement of what was assessed and the headline
exposure found.

## System context

What was modeled: the components, trust boundaries, data stores, and data
flows. Embed or link the data-flow diagram. State what was deliberately
out-of-scope, so the coverage can be judged.

## Threats identified

One row per threat. Every threat MUST be classified using a named framework and
rated by likelihood and impact, using a consistent scoring scheme.

| ID  | Threat      | Type     | Probability | Impact | Severity | Register ref |
| --- | ----------- | -------- | ----------- | ------ | -------- | ------------ |
| T01 | Short title | SPOOFING | LIKELY      | SEVERE | HIGH     | TA1          |
| T02 | Short title | TAMPERING| POSSIBLE    | MARGINAL | LOW    | —            |

### T01 — Short title

- **Type:** SPOOFING | TAMPERING | REPUDIATION | DISCLOSURE | DENIAL OF SERVICE
  | ELEVATION OF PRIVILEGE | ...
- **Probability:** PROBABLE | LIKELY | POSSIBLE | UNLIKELY | RARE
- **Impact:** CATASTROPHIC | CRITICAL | SEVERE | MARGINAL | NEGLIGIBLE
- **Location:** The component, module, or data flow affected.

What the threat is, and how it would be realized. Be specific about where in
the system it occurs.

Proposed mitigation, or a reasoned recommendation to accept the risk.

### T02 — Short title

- **Type:** ...
- **Probability:** ...
- **Impact:** ...
- **Location:** ...

...

## Register updates

Which rows were added to or changed in the [risk register](../REGISTER.md) as a
result of this workshop. A threat not worth tracking needs a one-line note
saying why it was not promoted.

## Notes

Anything else that would help a future reader interpret this assessment:
assumptions made, areas the participants were unsure about, or follow-up work
that should be scheduled.
