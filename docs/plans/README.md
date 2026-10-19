# 🗺️ Delivery plans

This section is used for planning when, and in what order, future work will
get done. A plan answers "how do we get from the current system to the
intended one?"

A plan is a self-contained body of work with a goal, a scope, and a
decomposition into tasks. Each task links out to its concrete issue or pull
request — that tracker, not the plan, owns the task's live status. The plan's
value is the decomposition and the sequencing.

## Structure

One directory per plan:

```
plans/
├── TEMPLATE.md       # The starting point for a new plan.
└── <slug>/           # One plan.
    ├── README.md     # The plan itself.
    └── …             # Sequence diagrams, data, mock-ups, or other artifacts.
```

The slug is the plan's permanent identity.

## Lifecycle

```
DRAFT ──> PLANNED ──> IN PROGRESS ──> DONE
            │             │
            └─────────────┴────────> ABANDONED
```

A plan is a mutable working document while it is open, and becomes immutable
once it is done or abandoned.

Keep the task breakdown and dependency graph current — adding, dropping, and
re-sequencing tasks — as reality unfolds.
