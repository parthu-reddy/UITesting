# 01 — Login and OTP — All Scenarios

## 2026-10-04T17:21:35.186761+05:30 — O4 one-login rewrite in progress

The deployed role-selector login remains on UI d79c33a. O4 auth/API/session/staff/computed entitlement changes are local and unpublished; auth33/33, gateway30/30 and staff11/11 focused checks pass. Real H2 D5/portal/consumer tests are in progress with failures retained. No O4 UI/harness implementation or deployed E2E pass yet. The previous role-based login scenario tables below describe historical deployment until replaced in this phase. New visible O4 journeys are defined in BusinessPlatform Phase4 validation.md; former direct API/DB/token assertions are explicitly superseded and deferred in [DEFERRED-O4-INTERNAL-ASSERTIONS.md](DEFERRED-O4-INTERNAL-ASSERTIONS.md). Finish coherent UI/harness/seeds, publish then clean Dev deploy, and execute only rendered controls. Keep existing wait/rate/SSE exclusions and all owned fixtures.

Successful login, signup, resend and rejection scenarios use real backend authentication and no mocked tokens. Deferred rate-limit response tests use browser routing and are explicitly UI-contract coverage. After the browser requests
an OTP through the normal login flow, the test clicks the same Dev Autofill Code button used
for manual developer login. On a Dev backend with `DEV_OTP_ENABLED=true`, this facility accepts valid 10-digit
customer/rider/restaurant numbers. Administrator autofill is allowlisted to the two provisioned
Dev administrators. It is absent in production. The runner-secret harness is parked.
The deployed login and registration results are recorded in PENDING.md.

## Batch 1 — Happy-path login per role

| ID | Role / phone | Action | Expected result |
|---|---|---|---|
| AUTH-01 | Customer 8000000001 | Select "Order Food" role → enter phone → tap "Send OTP" → tap "Autofill Code" → tap "Verify". | `CustomerDashboardPage.waitForDashboard()` resolves; profile role stored as `CUSTOMER`. |
| AUTH-02 | Restaurant 9000000001 | Select "Restaurant Partner" → same OTP flow. | `RestaurantDashboardPage.waitForDashboard()` resolves; role `RESTAURANT`. |
| AUTH-03 | Rider 7000000001 | Select "Delivery Executive" → same OTP flow. | `DeliveryDashboardPage.waitForDashboard()` resolves; role `DELIVERY`. |
| AUTH-04 | Admin 1000000001 | Same OTP flow → if profile-modal appears complete it. | `AdminPortalPage` visible; role `ADMIN`. |

## Batch 2 — Wrong OTP rejection

| ID | Role | Action | Expected result |
|---|---|---|---|
| AUTH-05 | Customer | Request OTP → change first digit → submit. | Auth rejection, visible error toast/inline, OTP form still visible, no token stored. |
| AUTH-06 | Restaurant | Same wrong OTP. | Same rejection. |
| AUTH-07 | Rider | Same wrong OTP. | Same rejection. |
| AUTH-08 | Admin | Same wrong OTP. | Same rejection. |

## Batch 3 — Phone input validation

| ID | Role | Action | Expected result |
|---|---|---|---|
| AUTH-09 | Customer | Submit empty phone form. | Native HTML `required` prevents form submission; field gets focus; no `/auth/initiate` call made. |
| AUTH-10 | Restaurant | Enter letters (e.g. "abc") in phone field. | Non-digit characters are stripped; no OTP request for invalid length number. |
| AUTH-11 | Rider | Enter 15 digits. | Only first 10 digits retained by `AuthForm` (client-side strip); no malformed 15-digit request sent. |
| AUTH-12 | Admin | Same 15-digit strip test. | Same result. |

## Batch 4 — OTP form validation

| ID | Role | Action | Expected result |
|---|---|---|---|
| AUTH-13 | Customer | Request OTP → submit empty OTP box. | Native `required` prevents verify call; OTP box gets focus. |
| AUTH-14 | Restaurant | Enter `abc123` in OTP field. | Letters stripped; only `123` remains (3 digits); verify not called until 6 digits present. |
| AUTH-15 | Rider | Enter 10 digits in OTP field. | Only first 6 retained; verify call uses only 6-digit value. |
| AUTH-16 | Admin | Click "Back" on OTP screen. | Returns to phone screen with original number pre-filled; user is unauthenticated. |

## Batch 5 — Resend OTP

| ID | Role | Action | Expected result |
|---|---|---|---|
| AUTH-17 | Customer | Request OTP → click "Resend OTP" → click "Autofill Code" to enter the new OTP → verify. | New OTP accepted; dashboard renders. |
| AUTH-18 | Restaurant | Same resend flow. | Same result. |
| AUTH-19 | Rider | Same resend flow. | Same result. |
| AUTH-20 | Admin | Same resend flow. | Same result. |

## Batch 6 — Role-selector UI navigation (UI-only, no OTP)

| ID | Viewport | Action | Expected result |
|---|---|---|---|
| AUTH-21 | Desktop 1280px | Cycle through all 4 role tabs via click; verify each shows the correct phone form label. | Labels match role names without OTP being triggered. |
| AUTH-22 | Mobile 390px | Swipe/tap carousel to each role; verify each phone form appears. | Carousel navigation works; no scroll breakage. |
| AUTH-23 | Desktop | Select Customer → type partial phone → click Back → select Restaurant → verify phone field is empty. | Back navigation resets form state. |
| AUTH-24 | Mobile | Toggle between customer and rider roles 3 times; verify no JS errors in console. | No page or console errors and no OTP initiation during three customer/rider switch cycles; rendering performance is not asserted. |

## Batch 7 — Logout

| ID | Role | Action | Expected result |
|---|---|---|---|
| AUTH-25 | Customer | Login → open Settings → tap "Logout". | Implemented and live-passed; role selector remains after reload. |
| AUTH-26 | Restaurant | Same logout flow. | Historical navigation blocker; fresh logout verification belongs to sessions-and-role-access. |
| AUTH-27 | Rider | Same logout flow. | Implemented and live-passed; role selector remains after reload. |
| AUTH-28 | Admin | Same logout flow. | Role selector visible. |

## Seeded customer states — 2026-10-01

These checks use existing coverage and leave fixture profiles, addresses and active flags unchanged.
Each isolated test session is logged out afterward. Snapshot the fixture state before and after.

| ID | Fixture | Action | Required result | Existing coverage |
|---|---|---|---|---|
| CUSTOMER-STATE-01 | `8000000501`, incomplete profile | Login with a valid Dev OTP | Verify and profile reads return 200; mandatory profile form appears with empty name/email; no profile save | `ProfileSettingsTest#completeProfileModalPrompt`, opt in with `scenario.customer.enabled=true` |
| CUSTOMER-STATE-02 | `8000000502`, no addresses | Login and inspect automatic location selector | Address read returns 200 with an empty list; selector visible | `CustomerHomeAddressTest#addressModalOpens` |
| CUSTOMER-STATE-03 | `8000000502`, no addresses | Inspect saved address list | Exact empty-state message and Add New Address action visible; zero saved addresses and no selected persisted address ID | `CustomerHomeAddressTest#addressCountAccurate` |
| CUSTOMER-STATE-04 | `8000000503`, suspended identity | Submit its valid OTP | Verify returns 403; visible inactive-account error; OTP form remains; no token/profile stored | `LoginSmokeTest#failedLogin`, `login.roles=CUSTOMER`, `login.rejection=inactive` |

All four seeded customer cases passed live on 2026-10-01, without failures, errors or skips.
Profiles, active flags and saved addresses were unchanged afterward; active session lists were empty.

## Current audit additions — 2026-10-01

| ID | Role / action | Required result | Existing class mapping |
|---|---|---|---|
| AUTH-29 | All four roles submit phone lengths 1, 7, 8 and 9 | Visible client validation; no OTP initiation; form remains | LoginValidationTest#phoneValidation |
| AUTH-30 | All four roles submit OTP lengths 1, 3 and 5 | Visible client validation; no verification or token | LoginValidationTest#otpValidationAndBack |
| AUTH-31 | All four roles resend then submit the previous code and current code | Distinct old code rejected, no token; current code accepted under the selected identity | LoginValidationTest#resendRejectsThePreviousCode |
| AUTH-32 | Unallowlisted admin 1000000099 | No Create account or Dev autofill; lookup denied 403; no token/profile | LoginValidationTest#unallowlistedAdministratorCannotRetrieveDevOtpOrRegister |
| AUTH-33 | Seeded incomplete customer 8000000501 submits empty/whitespace/101-character names and missing/malformed email | No profile PUT; fixture stays incomplete; own session revoked | ProfileSettingsTest#completeProfileModalPrompt, scenario.customer.enabled=true |
| AUTH-34 | Fresh customer, rider and restaurant signup through explicit Create account | Selected role only; profile/onboarding reaches dashboard; Dev government mock; rider biometric owned by that account; created identities/data/sessions are retained; idle rider duty ends OFFLINE | RegistrationUiTest REG-01/02/03 via run_registration_e2e.py |
| AUTH-35 | Correct role login returns identity token | JWT phone/role/subject matches the selected account and stored profile; successful logout immediately revokes it | LoginSmokeTest#successfulLogin |

All opt-in fixture cases must be run with their designated data. A disabled fixture case is not passing coverage. AUTH-25–28 logout, persistence and cross-role access continue in the sessions feature; this folder must link its fresh evidence instead of retaining stale deployment failures.

Intentional duration and rate-limit scenarios are deferred at the user's request. See DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md for implementation, tags, opt-in flags and final-run requirements. They are excluded from the current fast validation totals.
