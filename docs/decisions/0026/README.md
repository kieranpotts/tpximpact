# Automated accessibility tests

- Authors: Kieran Potts [@kieranpotts]
- Created: 2026-10-22
- Decision date: —
- PR: —

## Status

PROPOSED

**Not in-scope for MVP.**

## Context

The web client is built with GOV.UK Frontend ([ADR 0008](../0008/)), whose
components are accessible and well tested. But accessibility depends on how
the components are put together: headings, labels, error summaries, link
text, and the visually hidden text on each short URL link. Nothing checks
those automatically. A change to a view could break them, and no test would
fail.

The [testing strategy](../0017/) has three levels. None of them includes
accessibility checks.

## Decision

The acceptance tests will scan every page state the web client renders with
axe, through `@axe-core/playwright`, against the WCAG 2.2 AA rules. Any
violation fails the test.

The states to scan include the empty home page, the home page with a list of
short URLs, the success banners, the form with validation errors, the API
service unavailable, and the error pages.

## Consequences

- Regressions that axe can detect, such as missing labels, broken heading
  order, and poor colour contrast, fail `make acceptance`.

- The scans run in a real browser, against the built image, so they check
  the CSS and markup that link editors actually get.

- Each scan adds a little time to the acceptance run.

- Automated scans find only some accessibility faults. Keyboard use, focus
  order, and screen reader output still need manual checks.

- A violation in GOV.UK Frontend itself would fail the tests until it's fixed
  upstream, or the rule is disabled with a recorded reason.

## Alternatives

- **axe in the web client tests.** Run `axe-core` over the HTML that the route
  tests render. Faster, and closer to the code, but without a browser or CSS,
  so it can't check colour contrast or anything that depends on layout.

- **Pa11y, or Lighthouse.** Both can run against the system under Compose, but
  each adds a second browser-driving tool beside Playwright, for checks that
  mostly come from axe anyway.

- **Manual audits only.** Catch more than any tool, but only when someone
  remembers to run them, so regressions slip through between audits.
