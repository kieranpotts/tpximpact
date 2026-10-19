# ⚠️ Risk register

This section captures the security and privacy risks this project carries. It
stores reports from the threat modeling workshops that identified those risks
in the first place.

There are two parts, answering different questions:

- **[The risk register.](./REGISTER.md)** This is the single source-of-truth
  for where each tracked risk stands _right now_: its rating, its mitigation,
  and the status of that mitigation. This is living documentation, which means
  entries in the register are edited in place.

- **Threat modeling workshop reports.** This is an immutable, append-only
  archive. Each report captures what was found in one workshop: the system
  context assessed, the frameworks applied (eg. STRIDE), and the threats
  identified.

These complement the [architecture audits](../audits/). Both assess the
as-built system, but place emphasis on different qualities. Audits look at
structural design, while threat modeling workshops look at security and privacy
exposure.

## Structure

```
risks/
├── REGISTER.md         # The living register of tracked risks.
├── TEMPLATE.md         # The starting point for a new workshop report.
└── YYYY-MM-DD-<slug>/  # One workshop report, dated by when it was held.
    ├── README.md       # The report itself.
    └── …               # Diagrams, data-flow models, or other evidence.
```

## Workflow

1.  Scope the workshop. Which parts of the system, and which frameworks apply?
    Use STRIDE as a minimum. Add LINDDUN for privacy-sensitive systems.

2.  Gather the inputs: architecture diagrams from the [design docs](../design/),
    and the previous report if this is a re-assessment.

3.  Hold the workshop.

4.  Write up the report from [`TEMPLATE.md`](./TEMPLATE.md), into
    `YYYY-MM-DD-<slug>/README.md`.

5.  Promote each threat worth tracking into a new entry in
    [`REGISTER.md`](./REGISTER.md), with a fresh reference number.

A report is immutable once merged. To re-assess the system, hold a new workshop
— do not edit a merged report.
