# GOV.UK Front-end for the user interface

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-19
- Decision date: —
- PR: —

## Status

PROPOSED

## Context

The web client MVP is simple. It will need only an HTML form with validation
messages, and a table view and some buttons. It must show API errors clearly,
and should be accessible.

The project brief suggests the GOV.UK design system, among others.

## Decision

The UI will be built with GOV.UK Frontend 5, from npm, using its Nunjucks
macros and its precompiled assets, with no Sass build step. Errors follow the
design system's validation pattern.

## Consequences

- Every element the UI needs is an accessible, well-tested component, with
  little custom CSS.

- The error-presentation pattern is already solved.

- The look is recognisably GOV.UK. That suits public-sector work, but would
  need replacing or customising for a branded product.

- The precompiled CSS means no theming.

- The licence restricts the crown logo and the font to government services, so
  they must stay out of the source and build.

## Alternatives

**Bootstrap or Tailwind CSS.** Neither provides an accessible, prescribed
pattern for validation errors, so a bespoke one would have to be designed.
Tailwind also needs a CSS build step.
