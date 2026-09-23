# 01 — Sessions and Role Access — All Scenarios

All scenarios use real UI interactions only. No token injection, no direct storage edits.

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
| SESSION-09 | Restaurant | Login → log out → reload. | Active and failing: deployed Profile settings returns to Live Kitchen and no Log Out action renders. Reload persistence itself passes. |
| SESSION-10 | Rider | Login → log out → reload. | Implemented and live-passed; role selector remains after reload. |
| SESSION-11 | Admin | Login → log out → reload. | Same. |

## Batch 4 — Cross-role isolation (three simultaneous contexts)

| ID | Description | Action | Expected result |
|---|---|---|---|
| SESSION-12 | Customer + Restaurant | Open Context A (Customer) and Context B (Restaurant) simultaneously. | Each sees only its own dashboard; no role-swap. |
| SESSION-13 | Customer + Rider | Open Context A (Customer) and Context C (Rider) simultaneously. | Same isolation. |
| SESSION-14 | All three | Open Customer, Restaurant, Rider in three contexts simultaneously. | Each context renders its own dashboard independently; no JS errors. |

## Batch 5 — Role-gated page access (UI-only)

| ID | Role | Action | Expected result |
|---|---|---|---|
| SESSION-15 | Unauthenticated | Navigate directly to the app root while unauthenticated. | Role selector is shown; no dashboard leaks. |
| SESSION-16 | Customer | After login, customer-specific UI controls (e.g. "Deliver to", cart icon) are visible; restaurant-specific controls (e.g. "Incoming Orders") are absent. | Correct role-specific UI rendered. |
| SESSION-17 | Restaurant | Restaurant-specific tabs (Incoming, Preparation, Ready) visible; customer cart absent. | Correct UI. |
| SESSION-18 | Rider | Rider-specific UI ("Today's Earnings", online toggle) visible; restaurant/customer controls absent. | Correct UI. |
| SESSION-19 | Admin | Admin portal tabs (Users, Categories, Ledger, etc.) visible; customer/rider controls absent. | Correct UI. |

## Batch 6 — Active device visibility

| ID | Description | Action | Expected result |
|---|---|---|---|
| SESSION-20 | Current device appears | Customer opens Account Settings without removing any session. | Implemented and live-passed: Logged-in Devices renders at least one device with Remove and Last Active controls/text. |
