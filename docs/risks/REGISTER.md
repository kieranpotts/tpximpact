# Risk register

The living register of security and privacy risks currently tracked for this
project. This is the single source-of-truth for where each risk stands
right now.

Rows are updated in place as mitigations are applied, risks are reassessed, and
scheduled reviews are performed. A row is merged to the main trunk alongside the
change it describes, so the register never overstates or understates the system's
actual security posture.

## Fields

- **Ref:** Unique reference number, eg. `TA1`. Prefix by source where useful,
  eg. `TA` = threat assessment, `AS` = AppSec.

- **Risk:** Short, descriptive, unique name. Must say what is impacted.

- **Type:** Threat classification, using a consistent framework (STRIDE,
  LINDDUN, OWASP, etc.).

- **Details:** Where and how the threat occurs. Be specific.

- **Probability:** PROBABLE | LIKELY | POSSIBLE | UNLIKELY | RARE

- **Impact:** CATASTROPHIC | CRITICAL | SEVERE | MARGINAL | NEGLIGIBLE

- **Severity:** CRITICAL | HIGH | MEDIUM | LOW — combined probability × impact.

- **Mitigation:** The mitigation steps, or an explicit decision to accept the
  risk. Link out to the issue or pull request tracking the work.

- **Status:** PENDING | IN PROGRESS | COMPLETED

- **Residual risk:** CRITICAL | HIGH | MEDIUM | LOW — what remains after
  mitigation.

- **Reviewed:** Date the risk was last reviewed (`YYYY-MM-DD`).

## Register

Sort by severity (critical first), then by residual risk.

| Ref | Risk       | Type | Details               | Probability | Impact | Severity | Mitigation           | Status  | Residual risk | Reviewed   |
| --- | ---------- | ---- | --------------------- | ----------- | ------ | -------- | -------------------- | ------- | ------------- | ---------- |
| TA1 | Short name | ...  | Where and how it occurs | LIKELY    | SEVERE | HIGH     | The steps, and #NN   | PENDING | LOW           | YYYY-MM-DD |

## Retired risks

Risks that no longer apply. The threat has been designed out, the component
removed, or the risk fully mitigated with negligible residual risk.

| Ref | Risk | Type | Retired    | Reason |
| --- | ---- | ---- | ---------- | ------ |
|     |      |      |            |        |
