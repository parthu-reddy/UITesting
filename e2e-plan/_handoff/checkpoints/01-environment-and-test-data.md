# 01 — Environment and test data

## Current retained-data policy — 2026-10-01

The user explicitly requested no automatic test cleanup, with rider duty as the exception. Preserve created users, profiles, addresses, brands, outlets, orders and server sessions. Do not deactivate, delete, revoke or restore them as teardown. Retain run manifests to identify test-created data. Explicit logout/device removal/cancellation actions stay when they are the behavior being tested; they are not automatic teardown. Browser/tab/resource disposal remains normal. An authenticated idle rider is made OFFLINE at teardown, with authoritative server confirmation; an active delivery is preserved until completion. Slow and rate-limit scenarios remain opt-in and excluded.

Earlier cleanup/retirement evidence below describes past runs before this instruction. It does not govern future execution. Retained state must be checked as each feature is reviewed; do not rely on an automatically empty session list or restored stock.


Status: setup checks validated; dependent session/order requirements remain assigned to their owning features. Next: login-and-otp.

Reviewed: current TestConfig, TestBase, LoginPage, LoginSmokeTest, TestConfigTest, SavedAddressOutletUiTest, RoleCard, Identity AuthController/AuthService and Gateway revocation. Historical admin readiness notes are stale after provisioning/deployment.

| Scenario | Mapping and fresh evidence |
|---|---|
| ENV-01 | Three TestConfigTest URL-precedence checks; clean-context LoginSmokeTest check |
| ENV-02 | Two TestConfigTest cases for overrides and random baseline pools |
| ENV-03 | SavedAddressOutletUiTest#existingHomeAndNearbyBrand1Outlet passed at slow.mo=200; existing Home and Brand1 Outlet 6 at 0.1 km; no order created |
| ENV-04/07 | LoginSmokeTest: four role successes and four wrong-OTP rejections passed, headless |
| ENV-05 | Dependency: three-role simultaneous authenticated isolation and per-order authoritative duty/location/WebSocket/proximity; still requires owning-feature proof |
| ENV-06 | All four fresh contexts were unauthenticated; four-role login cleanup got logout 200 then protected session read 401 using the revoked token |
| ENV-08 | Customer positive login passed with slow.mo=200 |

Final focused evidence: 5 local configuration checks + 9 live LoginSmoke invocations + 1 slow customer login + 1 Home/outlet check = 16 passing invocations. No final failures/errors/skips. Durable reports live in evidence/01-*.xml and 01-environment-results.json.

Initial readiness run: the new clean-context selector incorrectly expected the role card's accessible name to equal only its title. Source shows a title plus description. That case failed; a source-grounded prefix locator corrected it. The focused recheck and subsequent nine-case login run passed. This error remains in 01-environment.log.

Implemented: centralized test phone providers used by TestBase; configuration assertions; clean four-context check; own-session cleanup for login/readiness; gateway revocation assertion in cleanup; redact OTP/token URL values from browser diagnostics. No application/backend changes or deployment needed.

Plan corrections: replace fixed defaults with actual random seeded pools; remove arbitrary-admin assumption; distinguish browser context closure from server revocation; distinguish nearby customer outlet from dispatch readiness; correct eight-vs-six setup scenario count. Plan details are in the owning README/scenarios.

Background 409 delivery availability and occasional quote 400 were observed during the read-only menu view. This batch does not prove checkout or rider availability. No duty, dispatch, order or financial mutation occurred. Other test classes still need their own cleanup audit; this does not assert the whole harness is isolated.
