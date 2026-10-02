## Current execution

21 required fast invocations passed under retention policy; reports selected in 03-retained-final-results.json. All collision/device cases use fresh retained customers. Slow/rate execution excluded. Later prose labelled deployment-pending describes historical checkpoints, superseded by this proof.

# 01 — Sessions and Role Access — All Scenarios

Current review 2026-10-01: login/reload/logout actions use the real UI. Read-only JWT/session API assertions prove current identity and immediate revocation; no token injection or direct storage edits. Device removal, when implemented, must target only a session created by the same test. Device mutation/collision cases explicitly allocate fresh retained accounts; idle-rider teardown follows the current duty policy. Intentional expiry/idle duration and rate-limit cases are deferred.

## Batch 1 — Session persistence per role

| ID | Role | Action | Expected result |
|---|---|---|---|
| SESSION-01 | Customer | Login via OTP → hard-reload page (`F5`/`page.reload()`). | Customer dashboard still visible without re-login prompt. |
| SESSION-02 | Restaurant | Login → reload. | Restaurant dashboard persists. |
| SESSION-03 | Rider | Login → reload. | Rider dashboard persists. |
| SESSION-04 | Admin | Login → reload. | Admin portal persists. |

## Batch 2 — Unauthenticated context isolation

| ID | Description | Action | Expected result |
|---|---|---|---|
| SESSION-05 | Customer vs. fresh context | While customer is logged in on context A, open fresh context B for the same URL. | Context B shows role selector, not the customer dashboard. Auth cookie/token from A does not bleed into B. |
| SESSION-06 | Restaurant vs. fresh context | Same check for restaurant. | Same isolation. |
| SESSION-07 | Rider vs. fresh context | Same check for rider. | Same isolation. |

## Batch 3 — Logout and session termination

| ID | Role | Action | Expected result |
|---|---|---|---|
| SESSION-08 | Customer | Login → log out via Settings → reload. | Implemented and live-passed; role selector remains after reload. |
| SESSION-09 | Restaurant | Login → log out → reload. | Restaurant logout passed in the current retained-data rerun; earlier navigation failure is historical. |
| SESSION-10 | Rider | Login → log out → reload. | Implemented and live-passed; role selector remains after reload. |
| SESSION-11 | Admin | Login → log out → reload. | Same. |

## Batch 4 — Cross-role isolation (three simultaneous contexts)

| ID | Description | Action | Expected result |
|---|---|---|---|
| SESSION-12 | Customer + Restaurant | Login all three roles and reload; then explicitly log out the customer. | Identity/UI remain isolated; restaurant stays authenticated. |
| SESSION-13 | Customer + Rider | Open Context A (Customer) and Context C (Rider) simultaneously. | Same isolation. |
| SESSION-14 | All three | Open Customer, Restaurant, Rider in three contexts simultaneously. | Each context renders its own dashboard independently; no JS errors. |

## Batch 5 — Role-gated page access (UI-only)

| ID | Role | Action | Expected result |
|---|---|---|---|
| SESSION-15 | Unauthenticated | Navigate directly to the app root while unauthenticated. | Role selector is shown; no dashboard leaks. |
| SESSION-16 | Customer | Open customer dashboard. | Home Deliver-to renders; Kitchen Kanban and Accept Order are absent. |
| SESSION-17 | Restaurant | Open restaurant dashboard. | Kitchen Kanban renders; customer Deliver-to and View Cart are absent. |
| SESSION-18 | Rider | Rider-specific UI ("Today's Earnings", online toggle) visible; restaurant/customer controls absent. | Correct UI. |
| SESSION-19 | Admin | Admin portal tabs (Users, Categories, Ledger, etc.) visible; customer/rider controls absent. | Correct UI. |

## Batch 6 — Active device visibility

| ID | Description | Action | Expected result |
|---|---|---|---|
| SESSION-20 | Current device appears | Customer opens Account Settings without removing any session. | Implemented and live-passed: Logged-in Devices renders at least one device with Remove and Last Active controls/text. |

The legacy SESSION-MGMT plan now follows the current inline UI: SESSION-MGMT-01 and SESSION-MGMT-02 strictly verify the `Logged-in Devices` section and current browser session. SESSION-MGMT-05 verifies that only per-device `Remove` actions exist. The settings section has no remove-all control. The separate login collision modal appears on the third distinct device and is covered below.

## Current source-to-scenario mapping

- SessionUiTest#reloadAndLogout: SESSION-01–11 and 15, all four roles, explicit UI logout under test; UI logout response 200 plus read-only revoked JWT probe 401 and absent token/profile after reload.
- CrossRoleSessionIsolationTest#simultaneousRolesRemainIsolatedAcrossReload: SESSION-12–18 and ENV-05 login-context portion, all three roles concurrently, own phone/profile/JWT, role-specific controls and no page exceptions. This does not prove order readiness/dispatch.
- CrossRoleSessionIsolationTest#customerLogoutDoesNotAffectRestaurantSession: SESSION-12–14, both partner contexts still authenticated after customer UI logout/reload.
- SessionManagementTest#currentSessionIsVisible: SESSION-20 / SESSION-MGMT-01/02; actual session response includes this browser's JWT sessionId and rendered row count matches server list. Wait for successful response and rendered state, rather than counting during loading.
- SessionManagementTest#sessionActionsMatchCurrentUiContract: SESSION-MGMT-05, per-device Remove only; no remove-all UI contract.

SESSION-19 admin-specific gate coverage and isolated device eviction are reviewed next. Fresh execution results belong in README/PENDING and the central checklist; implementation is not passing evidence.

## Additional boundary coverage

- SessionUiTest#unauthenticatedDeepLinksExposeOnlyLogin: SESSION-21, all four protected role paths redirect to login without any role dashboard or stored identity.
- SessionUiTest#authenticatedRoleCannotOpenAnotherDashboard: SESSION-22 / SESSION-19, each of four roles attempts the other three root routes; redirects to its own role, retains correct stored identity and exposes no other dashboard.
- SessionManagementTest#removeOwnSecondDeviceRevokesOnlyThatSession: SESSION-MGMT-03/04. Require a first-session list containing only this browser, create a second isolated browser with distinct device headers produced by its user agent, prove server IDs exactly match these two test sessions, remove only the matching second-device row through UI, assert second token 401 and first remains valid/listed across reload. The second session is removed as the tested action; the first session remains after execution. No seeded profile/account is deactivated.

- SessionUiTest#unsignedAdministratorHeadersCannotReadSessions: SESSION-24, anonymous request with invented ADMIN/user/session headers receives 401 at the gateway; no user data is read.
- CrossRoleSessionIsolationTest#forgedHeadersCannotChangeSessionOwnership: SESSION-23, customer JWT plus invented peer ADMIN/user/session headers still lists only this customer's session. DELETE of the restaurant test session is an idempotent no-op for the customer, and the restaurant's same token/session remains valid. Both identities/sessions belong to this test; never target an unrelated session.


## Retained-data session fixtures and collision defect — current checkpoint

`SessionManagementTest#removeOwnSecondDeviceRevokesOnlyThatSession` now registers a fresh customer in its first browser, saves Home and creates the second device itself. The Dev-only allocation runner requires `--session-case device-removal`, supplies the unused phone/preflight properties and excludes slow/rate groups. Explicit conditions prevent running mutation cases against a shared seeded account. The exact one/two-session ownership assertions remain. One live invocation passed with no skip; read-only PostgreSQL/Redis proof confirms the customer is still active and the first MacOS/Chrome session remains. Browser disposal does not revoke it.

The missing collision cases are implemented behind `--session-case limit-cancel` and `limit-replacement`: three distinct browser fingerprints, exact server session IDs, cancellation retaining both existing devices, and selected-device replacement with immediate revoked JWT 401 while the untouched and replacement devices remain valid. They do not wait for expiry or exercise rate limits. Each invocation allocates a fresh retained customer.

Live cancel uncovered a product failure after correcting the new test's response nesting: HTTP 409 returned `data.activeSessions`, but useOtpLogin read the wrong Axios error fields, so the modal did not open. FoodDeliveryAppUI now reads `error.response.status` and `error.response.data`, accepting only a nonempty device list. Local validation: 21 hook checks, typecheck and changed-file ESLint passed. User is deploying; live cancel/replacement remain deployment-pending. Do not count them as passes. The first failed attempt (test response shape) and second (product modal failure) remain in run logs/manifests; their accounts/sessions were retained too.

The old 19-case proof predates the no-cleanup policy. The updated device case has fresh proof; the other historical cases have not all been rerun under the new policy. Signed-tuple replay, direct-port proof and isolated admin-operation fixtures remain open in PENDING.md.

- SESSION-MODAL-01 maps to sessionLimitCancelPreservesExistingDevices. SESSION-MODAL-02 maps to sessionLimitReplacementRevokesSelectedDeviceOnly. Both use the real collision response and rendered modal; live proof is pending the UI deployment.

## Deployed session collision proof

The user deployed the collision fix. Cancel passed (03-cancel-deployed.xml): third browser remains logged out, exact first/second IDs remain valid. Replacement passed (03-replacement-deployed.xml): the selected Windows/Firefox token receives 401; first MacOS/Chrome and replacement Linux/Safari sessions both remain valid/listed. No skips/failures/errors in either final one-case run. Read-only PostgreSQL/Redis checks confirm both customers remain active with two sessions each. Device removal had already passed with its first session retained. The scoped three-case mutation/collision set is now verified under the no-cleanup policy. Earlier 18 core session invocations retain historical proof; they were not all repeated after that policy change.
