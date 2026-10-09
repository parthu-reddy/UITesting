# Checkpoint 134: lane 3 applied; sweep stopped early at owner's request (2026-10-08 14:15 IST)

## Decisions (owner, direct, 12:33): apply lane 3's 7 diffs; C1-C4 approved; start now. See DECISIONS.md top.
Owner later (14:05): only ~25 min left → stop the long sweep, run only what is needed.

## Done
- Lane 3 diffs applied (9 tracked clean files deleted; restorable from git HEAD). Classifier fix: `classify_impact.py`
  now recognises `FixtureShell::isFixturedApi` (triage had refused 2 classes). Triage C 26/40, A 53/232, F 4/4; Phase 4
  gate 10/10 (88); validate_p02 6/6; `mvn clean test-compile` rc 0.
- TestBase logs `[E2E WRITE] <method> <status> dev|fixture <url>` for every API write; verified both ways live.
- Sweep `evidence/sweep-77f29d7` (13:11-14:08): 38/60 classes + 1 orphan admin method (report copied by hand). Stopped
  by me between tests at 14:08 (runner killed; its in-flight `AdminLiveOpsFleetTest#openRejectionsTab` finished and passed).
- `evidence/needed-now-77f29d7` (14:09-14:13): AdminSupportTicketResolutionRoutedUiTest 3/3 (the only remaining class
  that exercises chat, the one area 77f29d7 changed), SettingsTest 2/2, UnapprovedOutletHidden 1/1, SessionUiTest 14/15.
- Totals this session: 202 pass, 9 failures, 6 errors, 1 skip. Inventory at cutoff 00:46:06Z: PASS_CURRENT 187,
  PASS_OLDER 97, LAST_FAILED 25, NEVER_RUN 28.

## Dev state changed
- MenuCartUiTest set menu item 60c6e41e-f617-584e-9329-ccfa0747b537 at outlet 36342aa6-05fe-5cb3-a755-21b207c601bd OUT
  OF STOCK (by design, retained, never restored). Only non-auth, non-quote Dev write in the whole run.
- `POST /orders/quote` rows (transient; ExpiredQuoteSweeper). No orders, tickets, refunds, wallet rows.

## Non-passes, each diagnosed (none is a product regression)
| Test | Cause | Fix |
|---|---|---|
| ApprovedRestaurantBrandSearchUiTest, ChatSupportWindowClosedTest, RetainedOrderStateUiTest | impact F, no -D fixture (my selection error) | run with props / after C2, C4 |
| CustomerOrderHistory pagination + cancelledHistory | need ≥11 orders / a restaurant-cancelled order | rerun after C4 |
| CustomerSettings deliveredOrderReview…, PartnerReadOnly riderCompletedTrip… | need an owned delivered order id prop | rerun with C4's D1 |
| MenuCartUiTest#outOfStockItemsCannotBeAdded | stale: waits for POST /menu-overrides/, UI sends PUT …/stock | update predicate; triage B misses the write |
| ProfileSettingsTest#sharedSettingsHistoryTab | fixed 500 ms wait + non-waiting isVisible() | auto-waiting assertion, rerun |
| RiderOnboardingFullTest#approvedRider… (never run before) | Playwright isDisabled ignores `<fieldset disabled>` | assert attribute / inner control |
| SessionUiTest#reloadAndLogout[4] | 429: runner `uses_admin` misses the parameterized admin case | teach uses_admin; rerun |

## Not run (resume list): `evidence/sweep-77f29d7-remaining.txt`, minus the 4 needed-now classes: 19 admin classes,
~80 methods, ~100 min of pacing. Re-checks of admin pages UI 77f29d7 did not touch.
