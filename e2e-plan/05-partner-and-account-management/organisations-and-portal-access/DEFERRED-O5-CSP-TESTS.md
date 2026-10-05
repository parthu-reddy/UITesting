# Deferred O5 CSP negative-origin proof

Updated 2026-10-04. DEFERRED, not executed, not counted as a passing test.

| ID | Deferred assertion | Why it is deferred | Permitted replacement or unblock |
|---|---|---|---|
| O5-CSP-001 | Remove a required CSP origin and prove the deployed CspSmokeUiTest reports the resulting violation. | Current E2E uses only actual deployed UI responses; response interception/injection is prohibited. Deliberately breaking the shared Oracle Dev policy is outside the tested release. | Keep source/header unit checks and the positive deployed five-portal origin/console artifact. Resume only in a separately authorised controlled UI environment or when the owner permits a local browser response fixture. |

Natural duration, deliberate rate-limit, internal-state and SSE deferrals remain in their existing documents. This deferral does not replace the required positive deployed CSP gate.
