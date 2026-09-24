# Food Delivery UI end-to-end tests

## Run only login tests

Requires Java 17+, Maven and the Playwright Chromium browser. The development deployment must expose OTP autofill and the configured non-admin accounts must have complete profiles. The admin positive case can complete its required profile through the UI. Tests use real UI interactions and backend authentication, without mocked responses or bypass tokens.

```sh
cd '/Users/parthureddy/Documents/Food Delivery.nosync/UITesting'
mvn test -Dtest=LoginSmokeTest -Dheadless=true -Dslow.mo=0
```

Eight cases cover successful login and wrong-OTP rejection for customer, restaurant, delivery executive and admin. Wrong OTPs are derived from the current valid code to guarantee a mismatch. Success checks the role-specific dashboard and session; failure checks an authentication rejection, visible error and absent session.

Set a changed deployment URL in one place: `TestConfig.APP_URL`, or override without editing code:

```sh
E2E_APP_URL=https://your-new-host.example mvn test -Dtest=LoginSmokeTest
# JVM property takes precedence over the environment variable:
mvn test -Dtest=LoginSmokeTest -Dapp.url=https://your-new-host.example
```

Accounts can be overridden with `-Dcustomer.phone=...`, `-Drestaurant.phone=...`, `-Drider.phone=...`, `-Dadmin.phone=...`. See `TestConfig.java` for defaults. `-Dheadless=false` shows the browser; `-Drecord.video=false` disables recordings. Reports are in `target/surefire-reports`, videos in `target/videos`. Use dedicated test accounts if active-session limits prevent additional logins; the suite does not evict existing sessions. The admin positive case may complete its profile using configurable `admin.profile.name` and `admin.profile.email`; other profiles are unchanged.

## Folder map

- `FoodDeliveryAppUI`: React 19 / TypeScript / Vite, Express entrypoint `server.ts`. `src/pages` contains customer, restaurant, delivery and admin shells; `src/features` contains identity, orders, delivery tasks, catalog, payments, communication and other domain UI. `src/shared` holds reusable UI/hooks; `src/lib` and `src/api` contain auth and API clients, with generated API schemas. Vitest tests live alongside components and under `src/tests`.
- `UITesting`: Java 17 / Playwright / JUnit 5 Maven project. `base` provides configuration, isolated role contexts and failure capture. `pages/common/LoginPage` drives role selection, phone and OTP entry. Role-specific page objects cover dashboards, menus, carts, tracking and admin operations. `util` contains OTP, swipe and wait helpers. Existing suites are grouped into smoke, flows, features and resilience; this task validates login only, not those broader suites.

## Required ordering setup for future tests

These are user-provided project prerequisites:

1. A rider must be logged in, available and near the chosen outlet before an order can be placed. Recheck this live; an existing Chrome login is not a permanent guarantee.
2. Keep customer, restaurant and rider sessions open together to monitor order transitions.
3. The rider sees an order to accept only after the restaurant accepts it and preparation is within 15 minutes or is completed.
4. Reuse the existing **Home** address. For **Brand1**, always open the outlet dropdown and choose a nearby outlet under 4–5 km. The backend blocks distances above 5 km. Do not rely on the initially selected outlet: the supplied screenshot selected Outlet 5 at 6.7 km, while Outlet 10 was 1.2 km away. Verify current distances rather than hardcoding them.

Login tests do not place orders, create addresses, or toggle rider availability.

## Live validation — 2026-09-19

Ran the eight login cases against the configured Cloudflare tunnel: **6 passed, 2 failed**. Customer and restaurant successful login passed; wrong-OTP rejection passed for all four roles. The configured rider `5000000001` and admin `1000000001` displayed the required profile-completion form after valid OTP verification. Supply existing accounts with complete, readable profiles to finish these two positive cases. The form can also appear if the profile API fails, so its presence alone does not prove profile fields are missing. Failure screenshots and HTML are captured before contexts close.

## Phased coverage roadmap

See [e2e-plan](e2e-plan/README.md) for functionality folders and the one-folder-at-a-time workflow. [Seeded account data](e2e-plan/TEST-DATA.md) records the user-confirmed customer, restaurant and rider numbers for future tests.

## Latest login validation

After handling the authorized admin profile form, all eight LoginSmokeTest cases passed against the configured deployment (0 failures/errors/skips). Earlier results above are historical. See [login phase](e2e-plan/01-foundation-and-access/login-and-otp/README.md).

## Additional login validation

Run `mvn test -Dtest=LoginValidationTest -Dheadless=true -Dslow.mo=0` from UITesting for 12 additional cases covering required inputs, normalization, Back navigation and OTP resend across all roles. All 12 passed in the latest focused run. Avoid repeatedly running shared-account OTP suites within the backend rate-limit windows; see the login phase scenarios for limits and outstanding scope.

## UI-only session, role navigation and existing outlet selection

```sh
mvn test -Dtest=RoleNavigationUiTest,SessionUiTest,SavedAddressOutletUiTest -Dheadless=true -Dslow.mo=0
```

13 new cases passed in focused live runs. These suites interact with rendered UI only and do not create addresses or orders. See the respective functionality folders’ `PENDING.md` files for initial failures, their resolution, background resource errors and deferred questions. Shared OTP account limits still apply.

## Menu/cart UI batch

`mvn test -Dtest=MenuCartUiTest -Dheadless=true -Dslow.mo=0`: latest run had two passes and one cart-increment failure. See [cart failures](e2e-plan/02-customer-order-entry/cart-and-pricing/PENDING.md) for reproduction and artifacts. The failure is retained; the suite is not reported as green.

## Further UI-only batches

Added AdminReadOnlyUiTest, PartnerReadOnlyUiTest, CustomerSettingsUiTest and LoginThemeUiTest, plus three independent MenuCartUiTest cases. Focused runs validated 20 of 21 new cases; admin phone search remains failing. The earlier cart-drawer increment failure also remains open. See each functionality folder's PENDING.md for outcomes and scope limits. Use focused `-Dtest=ClassName` or `-Dtest=ClassName#methodName` runs; combining all login-heavy suites may exhaust shared OTP limits.

<!-- dummy data -->
