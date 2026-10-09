# Login and OTP

## Current retained-data policy — 2026-10-01

The user explicitly requested no automatic test cleanup, with rider duty as the exception. Preserve created users, profiles, addresses, brands, outlets, orders and server sessions. Do not deactivate, delete, revoke or restore them as teardown. Retain run manifests to identify test-created data. Explicit logout/device removal/cancellation actions stay when they are the behavior being tested; they are not automatic teardown. Browser/tab/resource disposal remains normal. An authenticated idle rider is made OFFLINE at teardown, with authoritative server confirmation; an active delivery is preserved until completion. Slow and rate-limit scenarios remain opt-in and excluded.

Earlier cleanup/retirement evidence below describes past runs before this instruction. It does not govern future execution. Retained state must be checked as each feature is reviewed; do not rely on an automatically empty session list or restored stock.


Current audit, 2026-10-01: 39 required fast invocations passed, zero failures/errors/skips. Slow and rate-limit cases are excluded at the user's request. This is the login feature result, not a complete suite or session-management claim.

| Test selection | Fresh invocations |
|---|---:|
| LoginSmokeTest: four-role successful/wrong OTP plus fresh contexts; JWT identity matches selected account | 9 |
| RoleNavigationUiTest: desktop/mobile reset plus repeated mobile role switching | 9 |
| LoginValidationTest: short OTP/Back, resend, old-code rejection, unknown admin denial | 13 |
| LoginValidationTest#phoneValidation after user deployment | 4 |
| ProfileSettingsTest#completeProfileModalPrompt: invalid name/email without saving fixture | 1 |
| RegistrationUiTest: explicit fresh customer/rider/restaurant Dev signup | 3 |

The +91 phone form previously accepted short input. The fix requires ten national digits before network submission; the wider API schema and non-admin Dev OTP policy remain unchanged. Eighteen hook checks, typecheck and lint passed locally, followed by four live boundary checks after the user deployed.

Durable proof is in RandomDocuments/E2ECoverageAudit_2026-10-01/evidence; 02-fast-results.json lists all 39 selected cases. Reports in target/surefire-reports are overwritten by subsequent runs. Earlier failed rate probes used unsuitable production thresholds on relaxed Dev configuration and are retained separately; they do not affect current fast totals.

Signup used the existing Dev-only disposable-account runner. Its manifest 9248d2b153ff2d41 records unused phones and created UUIDs; cleanup deactivated only those users/rider/outlets and revoked sessions, retaining onboarding/audit records. Independent read-only verification confirmed cleanup, correct rider biometric ownership and unchanged seeded fixtures. No real government provider, order, duty or financial action was triggered.

The session feature owns four-role reload/logout, simultaneous customer/restaurant/rider sessions and device management. Other profile/settings methods belong to phase 05. Historical evidence remains distinguishable from this current run.

## Deferred cases

See [DEFERRED-WAIT-TESTS.md](DEFERRED-WAIT-TESTS.md) and [DEFERRED-RATE-LIMIT-TESTS.md](DEFERRED-RATE-LIMIT-TESTS.md). The rate-limit tests are opt-in; leave auth.limits.enabled unset and use -DexcludedGroups=auth-rate-limit for current runs. Natural OTP expiry moved to IdentityService unit tests (2026-10-08). Do not count deferred cases as passes.

See [scenarios.md](scenarios.md) and [PENDING.md](PENDING.md) for the mapping and outstanding dependent verification.
