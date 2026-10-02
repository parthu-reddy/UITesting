# Environment and test-data scenarios

Reviewed against current TestConfig, TestBase, LoginPage, RoleCard, AuthService and TEST-DATA.md on 2026-10-01. Normal automatic login clicks the same Dev Autofill Code control as manual login. The runner-secret OTP harness remains disabled. Dev/test government approval does not contact real providers. Ordinary authentication uses existing baseline accounts; explicit signup uses the disposable runner.

| ID | Action and exact coverage | Required result and cleanup |
|---|---|---|
| ENV-01 | TestConfigTest property/environment/default URL cases; LoginSmokeTest clean-context readiness | URL precedence preserved; deployed role selector visible; no auth token/profile before login. |
| ENV-02 | TestConfigTest phone override and baseline pool checks | All four overrides honored; baseline selection stays in 500/10/30 seeded pools; default admin 1000000001; no negative/disposable fixtures selected. |
| ENV-03 | SavedAddressOutletUiTest#existingHomeAndNearbyBrand1Outlet | Reuse saved Home, choose displayed Brand1 outlet below 5 km, assert selected outlet; create no address/order; revoke current test session. |
| ENV-04 | LoginSmokeTest#successfulLogin ADMIN invocation | Existing provisioned administrator reaches the correct dashboard and stores ADMIN role. Profile completion only with explicitly configured identity; no implicit admin registration. Revoke current session. |
| ENV-05 | CrossRoleSessionIsolationTest in sessions-and-role-access; three-role extension required there | Customer, restaurant and rider sessions coexist in independent contexts. Before ordering, separate preflight must prove rider authoritative ONLINE, live location/WebSocket and outlet proximity; dashboard presence is insufficient. |
| ENV-06 | LoginSmokeTest#isolatedContextsStartWithoutAuthentication plus successful-login teardown | Four fresh browser contexts have no auth token/profile. Logged-in readiness tests explicitly revoke only their own server session before context closure. Browser closure alone does not prove revocation. |
| ENV-07 | LoginSmokeTest headless execution, four role invocation reports | Real Dev login works headlessly, wrong codes are rejected, none of the required invocations skipped. |
| ENV-08 | LoginSmokeTest#successfulLogin CUSTOMER invocation with slow.mo=200 | Real login completes within configured waits despite deliberate action delay; revoke current session. No full suite timing claim. |

ENV-05 spans authenticated session isolation and per-order readiness. Record their own fresh proof in the owning feature folders; this setup audit must not claim those business scenarios complete from independent login cases.
