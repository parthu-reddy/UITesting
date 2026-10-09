# Checkpoint 130: F13, the E2E locator audit is green (2026-10-07T18:45+05:30)

Plan, ledger, gates and evidence: `RandomDocuments/PendingWork_2026-10-07/F13_LocatorAudit/README.md`.

## Result
- `RandomDocuments/UIRedesign_2026-09-18/tools/e2e_locator_audit.py --ui-ref HEAD` → **PASS**: 2645 sites,
  PASS 2116, FAIL 0, EXCEPTED 6, DYNAMIC 496, DEAD 27. Baseline was FAIL 74 in 28 files.
- `validate_f13.py --all` 20/20; `--phase 4 --evidence evidence/live` 8/8; prover 57/57.
- Every standing gate (item6 `validate_item6.py`) 9/9; all 11 gates exit 0.

## What the 74 rows were (ledger `tools/f13_ledger.json`, each citation machine-checked)
- 58 AUDIT_GAP: JSX text with `{holes}`, `confirm({ title })` dialog names, `setAttribute('aria-label')`,
  `<aside>`, regex locators vs templates, CSS-string escapes, Dev seed data (`E2E pending-brand`…),
  text a routed test serves itself, rupee amounts, option labels from `.ts` modules.
- 6 EXCEPTION: negative pins (RoleNavigation "Order Food"; dispatch "Partial Refund"/"Post-Delivery"),
  UA-composed `{os} · {browser}`, server OTP messages (live-passed at checkpoint 10).
- 10 STALE_TEST, rewritten (below). 0 UI defects.

## Test changes (UITesting, uncommitted)
| File | Change |
|---|---|
| tests/features/resilience/ResilienceRegressionUiTest | ISOLATION-05 `freshContextStartsAtLogin`: PHONE NUMBER + Send One-Time OTP |
| tests/features/resilience/ResponsiveAccessibilityTest | RESP-01 `desktopLoginFitsViewport`; RESP-03/04 no sign-in tab click; ACCESS-07/10 `visibleLoginButtonsHaveAccessibleNames` |
| tests/resilience/PageReloadRecoveryTest | RECOVERY-01/16: "Send One-Time OTP" hasCount(0) (the old "Order Food" check was vacuous) |
| tests/features/auth/SettingsTest | logout asserts the phone login (web-first; no 2 s sleep, no `content()` scrape) |
| util/StateSetupHelper | `getDeliveryOtp` detects the login screen by its OTP button |
| tests/features/fulfillment/RiderAvailabilityUiTest | `^(Today’s earnings|Paid today)$` |
| pages/delivery/RiderSettingsPage, tests/features/rider/RiderOnboardingTest | dead `isOnboardingWizardVisible` fallback deleted |
| tests/features/restaurant/RestaurantUiTest | REST-01 scenario handling only for 9000000014 (11–13 have no dashboard) |
| tests/features/organisations/PortalLauncherUiTest | NEW `restaurantWithoutBrandIsInvitedToStart` (9000000011: "Not started" + "Get started with a restaurant") |

## Live proof (Dev tunnel gulf-strike-dark-extras, UI 7e7ebce; phones 8000000001/9000000001/7000000001/1000000001)
One invocation, 7 classes, **11/11 executions, 0 failures/errors/skips**:
PageReloadRecoveryTest 2, ResilienceRegressionUiTest 1, ResponsiveAccessibilityTest 4, RestaurantUiTest 1,
RiderOnboardingTest 1, PortalLauncherUiTest 1, SettingsTest 1. XML: `F13_LocatorAudit/evidence/live/`.
Read-only: no order, ticket or fixture created; teardown signed sessions out; no rider duty change logged.

## Not done / for the owner
- `RiderAvailabilityUiTest` not run live: it cycles rider duty Online→Offline→Online, which this folder's
  USER-INSTRUCTIONS forbid for an already ONLINE rider. Static proof only (`RiderStatsBar.tsx:58`).
- REST-01 with `-Drestaurant.phone=9000000014` (the kept scenario branch) not run this session.
- DEAD 27 (failing locators in page-object methods no test reaches) is unchanged and was not F13 scope.
- Nothing deployed or needed: the changes are test code and the audit tool only. Commit UITesting and RandomDocuments when convenient.
