# Environment and test data

## Current retained-data policy — 2026-10-01

The user explicitly requested no automatic test cleanup, with rider duty as the exception. Preserve created users, profiles, addresses, brands, outlets, orders and server sessions. Do not deactivate, delete, revoke or restore them as teardown. Retain run manifests to identify test-created data. Explicit logout/device removal/cancellation actions stay when they are the behavior being tested; they are not automatic teardown. Browser/tab/resource disposal remains normal. An authenticated idle rider is made OFFLINE at teardown, with authoritative server confirmation; an active delivery is preserved until completion. Slow and rate-limit scenarios remain opt-in and excluded.

Earlier cleanup/retirement evidence below describes past runs before this instruction. It does not govern future execution. Retained state must be checked as each feature is reviewed; do not rely on an automatically empty session list or restored stock.


Status: under fresh audit on 2026-10-01. Earlier login evidence is historical; see the current feature record in RandomDocuments/E2ECoverageAudit_2026-10-01. Order readiness is rechecked at each order run.

Default account selection is random within baseline seeded customer 8000000001–8000000500, restaurant 9000000001–9000000010 and rider 7000000001–7000000030 pools. Admin defaults to the provisioned 1000000001 account. TestConfig provides these selections; TestBase uses them to construct four isolated contexts. JVM phone overrides select dedicated scenario fixtures or reproduce failures. Negative-state fixtures and unused disposable registration pools are excluded from ordinary random selection.

URL precedence is -Dapp.url, then E2E_APP_URL, then the central TestConfig fallback. TestConfigTest exercises precedence, all four phone overrides and baseline range exclusion without opening a browser. LoginSmokeTest reuses real Dev UI autofill for all four roles and checks clean isolated contexts. SavedAddressOutletUiTest verifies the saved Home address and a displayed Brand1 outlet below 5 km without ordering.

Administrator entitlement requires a persisted active ADMIN assignment; arbitrary numbers cannot become administrators. Dev OTP lookup for non-admin roles accepts valid ten-digit numbers when enabled, while admin lookup is allowlisted to the provisioned administrators. Explicit registration remains separate from normal login. Government verification uses Dev mock approval.

Browser context closure clears browser state but does not revoke a server session. Automatic session revocation has been removed per the current retained-data policy. Closing browser contexts releases local resources and leaves server sessions retained. Shared teardown checks idle rider duty and makes it OFFLINE. Explicit logout cases still exercise logout.

See scenarios.md for ENV-01–08 and their exact executable mapping. Concurrent three-role isolation is owned by sessions-and-role-access. Authoritative rider duty/location/WebSocket and outlet proximity checks are owned by the order preflight; authentication or a nearby customer outlet alone does not prove dispatch readiness.

Historical evidence: the earlier three-role pass/admin-profile failure and subsequent eight-role login pass are documented in the login-and-otp folder. Those observations are not current completeness claims.
