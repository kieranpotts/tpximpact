# Changelog

<!-- The changelog SHOULD be hand-maintained. Keep the [Unreleased] section
up-to-date with new revisions introduced to the development trunk. The format
is based on <https://keepachangelog.com/en/1.1.0/>. -->

## [Unreleased]

## [0.0.0] - YYYY-MM-DD

**Added**
- Add CSV export to the transactions table.
- Add `--dry-run` flag to the `migrate` CLI command.

**Changed**
- Cut p95 latency on the search endpoint from 800ms to 150ms by adding a
  database index on `orders.customer_id`.
- Replace the bundled date picker with the browser's native `<input type="date">`.

**Deprecated**
- Deprecate the `/v1/users` endpoint in favor of `/v2/users`. `/v1/users`
  will be removed in the next major release.

**Removed**
- Removed support for Node.js 16, which reached end-of-life.

**Fixed**
- Fix a race condition that could double-charge a customer when the retry
  handler fired before the original payment had settled.

**Security**
- Bump `lodash` from 4.17.15 to 4.17.21 to patch a prototype pollution
  vulnerability ([CVE-2020-8203](https://nvd.nist.gov/vuln/detail/CVE-2020-8203)).
