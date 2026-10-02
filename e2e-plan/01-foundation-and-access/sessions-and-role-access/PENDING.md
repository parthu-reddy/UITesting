## Current proof

All 21 fast core invocations now passed under retained-data policy (03-retained-final-results.json). Deployment-interrupted attempts are separate. The earlier deployment-pending collision statements below are historical; both cancel/replacement passed after the UI fix was deployed. Signed-tuple replay, direct-port proof, isolated suspension/role removal and last-admin PostgreSQL concurrency remain unverified/unfixed as applicable.

# Remaining checks and historical evidence

## Current retained-data policy — 2026-10-01

The user explicitly requested no automatic test cleanup, with rider duty as the exception. Preserve created users, profiles, addresses, brands, outlets, orders and server sessions. Do not deactivate, delete, revoke or restore them as teardown. Retain run manifests to identify test-created data. Explicit logout/device removal/cancellation actions stay when they are the behavior being tested; they are not automatic teardown. Browser/tab/resource disposal remains normal. An authenticated idle rider is made OFFLINE at teardown, with authoritative server confirmation; an active delivery is preserved until completion. Slow and rate-limit scenarios remain opt-in and excluded.

Earlier cleanup/retirement evidence below describes past runs before this instruction. It does not govern future execution. Retained state must be checked as each feature is reviewed; do not rely on an automatically empty session list or restored stock.


## Current status — 2026-10-01

The 19 listed fast session checks passed. Restaurant logout now passes; the earlier navigation failure is historical. The device-list harness now waits for the successful API response and rendered rows rather than counting before the React effect completes. No assertion was weakened to accept a missing current browser.

## Security-report work still open

- BackendIssues_2026-09-29/BACKEND-ISSUES.md suspension/role removal: local access-revocation regression passed; current live UI logout/device removal proves immediate JWT blacklist enforcement. This is not deployed suspension/role-removal proof. Those actions require a disposable account owned by the admin-operation test and an independent before/after check; do not suspend or remove roles from seeded users. Carry to phase 06 users-categories-and-moderation.
- Last-admin PostgreSQL competing-transaction protection: the existing mocked unit test passes, but concurrency against real PostgreSQL is still unimplemented/unverified. Never race removal against the shared Dev administrators. Needs an isolated database fixture.
- P1-IDENTITY-HEADER-REPLAY-AND-REVOCATION-2026-09-30.md remains a current source gap: IdentityTokenService.verify checks HMAC equality without tuple age; Gateway accepts a correctly signed internal tuple before JWT blacklist enforcement; the shared filter does not enforce freshness/revocation. The passing unsigned-header and ordinary bearer-revocation checks do not cover replay of a genuinely signed tuple. Remediation/consumer rollout and dedicated regression proof are still required; do not label the complete security boundary validated.
- Direct IdentityService interface: controller authorization reflection and Gateway unsigned-header denial passed, but fresh direct-service forged-header negative checks remain distinct deployment proof. Gateway denial alone does not prove protection on published port 8086.
- Collision cancel/replacement are now implemented with isolated fresh customers, but their live validation requires deployment of the verified UI error-parser fix. See the current checkpoint below.

## Deferred execution

Natural JWT/session expiry and rate-limit enforcement are excluded per user request. See DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md. Readiness waits for DOM/API responses are ordinary fast test synchronization.

## Historical evidence

The earlier device-count failure and restaurant settings-route failure are retained in E2EFullSuite_2026-09-27 and previous plan history. Current durable evidence is in RandomDocuments/E2ECoverageAudit_2026-10-01/evidence. Original reports have not been rewritten as passes.

## Retained-data session fixtures and collision defect — current checkpoint

`SessionManagementTest#removeOwnSecondDeviceRevokesOnlyThatSession` now registers a fresh customer in its first browser, saves Home and creates the second device itself. The Dev-only allocation runner requires `--session-case device-removal`, supplies the unused phone/preflight properties and excludes slow/rate groups. Explicit conditions prevent running mutation cases against a shared seeded account. The exact one/two-session ownership assertions remain. One live invocation passed with no skip; read-only PostgreSQL/Redis proof confirms the customer is still active and the first MacOS/Chrome session remains. Browser disposal does not revoke it.

The missing collision cases are implemented behind `--session-case limit-cancel` and `limit-replacement`: three distinct browser fingerprints, exact server session IDs, cancellation retaining both existing devices, and selected-device replacement with immediate revoked JWT 401 while the untouched and replacement devices remain valid. They do not wait for expiry or exercise rate limits. Each invocation allocates a fresh retained customer.

Live cancel uncovered a product failure after correcting the new test's response nesting: HTTP 409 returned `data.activeSessions`, but useOtpLogin read the wrong Axios error fields, so the modal did not open. FoodDeliveryAppUI now reads `error.response.status` and `error.response.data`, accepting only a nonempty device list. Local validation: 21 hook checks, typecheck and changed-file ESLint passed. User is deploying; live cancel/replacement remain deployment-pending. Do not count them as passes. The first failed attempt (test response shape) and second (product modal failure) remain in run logs/manifests; their accounts/sessions were retained too.

The old 19-case proof predates the no-cleanup policy. The updated device case has fresh proof; the other historical cases have not all been rerun under the new policy. Signed-tuple replay, direct-port proof and isolated admin-operation fixtures remain open in PENDING.md.

## Deployed session collision proof

The user deployed the collision fix. Cancel passed (03-cancel-deployed.xml): third browser remains logged out, exact first/second IDs remain valid. Replacement passed (03-replacement-deployed.xml): the selected Windows/Firefox token receives 401; first MacOS/Chrome and replacement Linux/Safari sessions both remain valid/listed. No skips/failures/errors in either final one-case run. Read-only PostgreSQL/Redis checks confirm both customers remain active with two sessions each. Device removal had already passed with its first session retained. The scoped three-case mutation/collision set is now verified under the no-cleanup policy. Earlier 18 core session invocations retain historical proof; they were not all repeated after that policy change.
