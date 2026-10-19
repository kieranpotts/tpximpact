# 🔍 Architecture audits

These are point-in-time evaluations of the structural integrity of the codebase
as it exists in the main trunk.

An audit evaluates the code on its own terms, without cross-referencing the
intended architecture in the [design docs](../design/). Indeed, it can be
preferable for the auditor to have no prior knowledge of the trade-offs already
considered. The objective is to surface genuinely fresh insight.

An audit is evaluation only. It records what is there and why it costs
something. It does not propose fixes, alternative designs, or changes to the
code.

Security and privacy findings are out of scope. These are discovered in
threat modeling workshops and their mitigations tracked via the
[risk register](../risks/).

## Structure

One directory per audit, dated by when the system was examined.

```
audits/
├── TEMPLATE.md         # The starting point for a new audit report.
└── YYYY-MM-DD-<slug>/  # One report.
    ├── README.md       # The report itself.
    └── …               # Supporting artifacts referenced from the README.
```

## Immutability

A report is a snapshot in time, and is immutable once merged. Do not edit a
merged report — run a new audit instead.
