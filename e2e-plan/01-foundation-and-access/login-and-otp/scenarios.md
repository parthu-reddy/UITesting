# 01 — Login and OTP — All Scenarios

All scenarios use real backend authentication and no mocked tokens. After the browser requests
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
| AUTH-24 | Mobile | Toggle between customer and rider roles 3 times; verify no JS errors in console. | Console clean; no duplicate re-renders. |

## Batch 7 — Logout

| ID | Role | Action | Expected result |
|---|---|---|---|
| AUTH-25 | Customer | Login → open Settings → tap "Logout". | Implemented and live-passed; role selector remains after reload. |
| AUTH-26 | Restaurant | Same logout flow. | Active and failing: profile/settings navigation returns to Live Kitchen, so no Log Out control appears. |
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
