# 01 — Environment and Test-Data — All Scenarios

Prerequisites for every scenario in this folder: the configured deployment is reachable (`TestConfig.APP_URL`), OTP autofill is available, and the four seeded accounts have complete profiles.

| ID | Role / context | Action | Expected result | Notes |
|---|---|---|---|---|
| ENV-01 | — | Verify `TestConfig.APP_URL` is set via `-Dapp.url` JVM property; fallback to `E2E_APP_URL` env var; fallback to hardcoded default. Open the URL and confirm the login role-selector renders. | Role-selector screen visible within 15 s. | URL precedence test only; no account interaction. |
| ENV-02 | — | Verify that default phones are 8000000001 (customer), 9000000001 (restaurant), 7000000001 (rider), 1000000001 (admin). Override each via JVM property (`customer.phone`, etc.) and confirm `TestConfig` reads the override. | Correct number returned for each getter. | Pure configuration assertion; no browser. |
| ENV-03 | Customer 8000000001 | Open the app with the seeded customer and confirm the Home address is present in the address selector without creating a new address. | 'Home' entry visible in `SavedDeliveryAddressPage`. | Prerequisite for all order-flow tests. |
| ENV-04 | Admin 1000000001 | Log in. If the profile-completion modal appears, fill name + email using defaults (`E2E Admin`, `e2e-admin-1000000001@example.com`) and submit. Verify admin dashboard renders. | Admin dashboard (`AdminPortalPage`) visible. If profile was already complete, modal must NOT appear again. | Idempotent — safe to run multiple times. |
| ENV-05 | Customer + Restaurant + Rider | Open three isolated browser contexts. Log each in simultaneously. Confirm customer's selected outlet is < 5 km from rider's default location before any order test runs. | All three dashboards visible; outlet distance verified via UI badge or distance label. | Prerequisite gate for Phase 03 tests. |
| ENV-06 | — | Verify that test contexts close cleanly after each test. Open a new context, navigate to the app, then close the context. Confirm no lingering sessions prevent the next test from opening a clean context. | No `ContextAlreadyDisposedError`; subsequent context opens without auth state. | TestBase teardown validation. |
| ENV-07 | — | Verify headless mode works: run `LoginSmokeTest#successfulLogin` with `-Dheadless=true`. Confirm test passes without UI window. | Same pass result as headed mode. | CI gate check. |
| ENV-08 | — | Verify slow-motion flag works: run with `-Dslow.mo=200` and confirm the test doesn't time out at 200 ms delay. | Test completes within the configured `DEFAULT_TIMEOUT`. | Environment config sanity. |
