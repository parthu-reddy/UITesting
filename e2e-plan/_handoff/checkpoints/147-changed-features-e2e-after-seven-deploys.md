# Checkpoint 147 — changed-features E2E after the seven-service deploy

2026-10-10 16:49–17:40 IST. Owner: "go ahead with all 316", then "run RiderAvailabilityUiTest too, the rider is offline".
Selection: `select_tests.py --files` over the 8 deployed commits (7 services + UITesting 56b29d4) = 316 methods / 86 classes
(16 slow left out by the selector). Preconditions checked read-only before each run: customer_db `orders` = 0 rows,
all 34 seeded riders OFFLINE (`delivery_executives.status`).

Evidence: `evidence/deploy-seven-2026-10-10/` (part 1, stopped by fail-fast at 27/85), `…-part2/` (47 ungated classes),
`…-rider-availability/`, `…-race-fast/`, `…-race-debug/`.

## Totals (parts 1+2; includes the 4 canaries and parameterized cases)

337 results: 290 pass, 23 failure, 9 error, 15 skipped. **No failure traced to the seven backend deploys.**

## Every non-pass, classified

| Class / method | Result | Cause | Evidence |
|---|---|---|---|
| AdminFleetSafetyRoutedUiTest (2) | error | **Race hidden by slow-mo** | fails fast 2/2 twice; passes `--debug` 2/2 |
| AdminSupportChatIsolationRoutedUiTest (3, then 4) | error | **Race hidden by slow-mo** | launcher click: "element was detached from the DOM"; passes `--debug` 4/4 |
| LoginThemeUiTest (2) | failure | **Race hidden by slow-mo** | dark-mode contrast 2.89 / 1.02 read right after the theme click; passes `--debug` 2/2 |
| AdminDispatchSafetyRoutedUiTest#manualInterventionGuards… | failure, then pass | Intermittent race (suspect: `waitForDriverCandidates` first-of-3 matches the transient "No online drivers" before `driverCandidatesOrderId` catches up) | passed on fast rerun |
| AdminLiveOpsFleetTest#openWalletDlqTab | failure, then pass | Intermittent race: tab switch renders heading with `loading=false`, `walletOutbox=null` before the effect; test uses instant `isVisible()` | passed on fast rerun; wallet-service served the request, no error |
| RiderAvailabilityUiTest | failure | Same instant-`isVisible()` shape at line 58 ("Completed Deliveries" heading). Duty cycle itself PASSED (online/reload/offline/reload/online); rider 7000000001 back OFFLINE 12:02:27Z | rider-availability evidence |
| RestaurantDiscoveryUiTest#searchNoResults… | error, then pass | 503 from the Cloudflare quick tunnel, not Dev: nginx (tunnel origin) logged 0×503 in 15 min, every `nearby` it saw = 200; gateway/customer-service 11/11 | passed on fast rerun |
| CustomerOrderHistoryUiTest (11 of 12) | failure | Data: "Requires a retained delivered/cancelled history fixture" — Dev has 0 orders. The 1 pass (empty history) exercises the new paged order read live | |
| AdminOrderMoneyOutcomesTest, ChatSupportWindowClosedTest | error | Data: ParameterizedTest with no arguments (`money.outcomes`, `support.closed.order.ids`) | |
| CustomerSettingsUiTest#deliveredOrderReview…, PartnerReadOnlyUiTest#riderCompletedTrip…, RetainedOrderStateUiTest, RiderReviewHistoryApiTest | failure/error | Data: need a delivered/retained order id | PartnerReadOnly's 401 = teardown (`REJECTED reason=session-revoked` 23 ms after logout) |
| AdminRefundRetryFlowTest, SupportRefundResolutionFlowTest, ChatHistoryPagingTest, OrderReviewsFlowTest, RestaurantEarningsLiveTest, SessionManagementTest (3), CrossRoleSessionIsolationTest (1), ProfileSettingsTest (1) | skipped | By design: assumption on a fixture property | |
| AdsManagerPortalUiTest, CampaignModerationActivationFlowTest, DeliveryApplicationApiTest, DeliveryOnboardingUiTest, EntitlementRevocationTest | failure | Gated: need their runner / `bp.*.preflight` (fail-fast stopped part 1 on this) | |

## Gated runs (17:41–17:54) — all PASS

- C2 O3 runner (`--admin-phone 1000000002`): RestaurantApplicationApiTest, DeliveryApplicationApiTest,
  AdminPartnerApprovalsUiTest#privateReviewAndDecisions, UnapprovedOutletHiddenTest — 4/4. Allocation
  `target/business-platform/o3/allocation-a6fa20bda2d95b6d.json`. Browser 401s = 16 gateway `REJECTED reason=entitlements-changed`
  (the designed refresh signal), none other.
- C2 O4/O5 runner: 16/17 first pass; DeliveryOnboardingUiTest hit ADMIN_STEP_UP_RATE_LIMITED on 1000000001 (my race reruns had
  spent that phone's budget; the dedicated runners don't read ADMIN-STEP-UPS.json) → `--only delivery --admin-phone 1000000002` PASS.
- ApprovedRestaurantBrandSearchUiTest (O3 manifest) PASS; RegistrationUiTest 3/3 PASS.
- C3: SponsoredListingRegressionTest pre-check PASS; W1–W3 + A1–A4 7/7 PASS (fresh 9999 phones 9999102841, 9999043872,
  9999844776, 9999713856, each checked absent from identity_db users + organisation_invitations first); post-check PASS;
  campaign_db = 2 DRAFT + 2 PAUSED, as the plan expects. Note: run_e2e_batch.py ran the admin classes (A3, A4) first —
  it does reorder; I first said it keeps selection order.
- RiderAvailabilityUiTest on 7000000001 (owner-approved): duty cycle PASS, line-58 instant isVisible FAIL; rider OFFLINE after.

## Not run yet

- Order lifecycle (C4: HappyDeliveryFlowTest etc.) is tagged slow, so the selector excluded it; every data failure above
  needs its D1/D2 orders. Slow tests need the owner's OK.

## Lessons

- The selector's `--git` sees only uncommitted changes; after the owner commits, use `--files` with the deployed commits.
- `select_tests.py` output includes classes that need a dedicated runner; `run_e2e_batch.py` runs them plainly and they
  fail in < 1 s. Partition gated classes out before a batch (I did it after the fail-fast).
- Fast defaults (2026-10-08 18:30) had never been applied to these routed admin classes: their last passes were 06:22–16:27
  that day. Three classes fail deterministically without slow-mo.

## Race fixes (owner: "fix the races, then run the order lifecycle"), 19:48–20:12 IST — local, uncommitted

Isolation first (one variable at a time): chat + theme fail headed-fast too (pure timing); fleet fails only headless+fast.

| Test | Root cause (from source / probe) | Fix | Proof |
|---|---|---|---|
| LoginThemeUiTest | `.app-background` transitions `background-color` over `--duration-slow` 380 ms; contrast read mid-fade | wait for `document.getAnimations()` CSSTransitions to finish, threshold 4.5 unchanged | 4/4 fast headless (was 0/2 every run) |
| AdminSupportChatIsolationRoutedUiTest | `OpenChatHelper` opens the widget via ref right after mount; the test clicked the 1–2-frame launcher → "detached", 60 s click wait | wait for the wrapper's own open (session) first; launcher only if the widget stayed closed | 8/8 fast (was 0–1/4) |
| AdminFleetSafetyRoutedUiTest | `/admin` redirects to `/admin/map` (AdminPortal.tsx:169), so the map read the REAL deployment during sign-in, before the routes; probe showed the page with Dev data (99/2/100) and fixture counters 0. `openFleetTab()` then clicked the already-open tab | park on Categories (wait for its mount read) before registering routes, so openFleetTab mounts the map fresh | 6/6 fast headless (was 1/3) |
| AdminDispatchSafety (candidates) | **UI defect**: `usePolling` starts each selection with isLoading=false + previous key, so "No online drivers available nearby." flashed before every candidate load | FoodDeliveryAppUI `AdminManualInterventions.tsx`: `driverCandidatesPending` shows loading until this order's candidates arrive; 2 vitest cases (new one red on old code) | typecheck, lint, 1096/1096 vitest. **Needs a UI deploy**; the E2E wait stays as the guard |
| AdminLiveOpsFleetTest operations tabs | heading renders a frame before the tab's fetch; instant `isVisible()` | `empty.or(firstEntry).waitFor(15 s)` | 4/4 tabs; guard proven: wrong empty text → error, reverted |
| RiderAvailabilityUiTest line 58 | instant `isVisible()` after "Trips Completed" click | waitFor heading, then Delivered-or-empty | 2/2 on 7000000001; OFFLINE before and after |

Files: UITesting LoginThemeUiTest, AdminSupportChatIsolationRoutedUiTest, AdminFleetSafetyRoutedUiTest, AdminLiveOpsFleetTest,
RiderAvailabilityUiTest; FoodDeliveryAppUI AdminManualInterventions.tsx + .test.tsx.

## C4 order lifecycle (owner: "then run the order lifecycle"), 20:13–20:34 IST

Before every order-placing run: read-only `orders` status counts + seeded rider duty (all 34 OFFLINE before and after each).

| Step | Result | Orders |
|---|---|---|
| HappyDeliveryFlowTest (2) | PASS, 7 min | D1 299c2b52 (Brand 1 O5), D2 1759ef25 (Brand 1 O5), D3 ab5fa0d5 (Brand 2 O5), all HANDED_OVER (= delivered; IOrderRepository:116) |
| CustomerSettingsUiTest#deliveredOrderReview…, OrderReviewsFlowTest#participantsReviewEachOther, RiderReviewHistoryApiTest, SupportRefundResolutionFlowTest (3), ChatHistoryPagingTest (D1); AdminRefundRetryFlowTest (D2) | all PASS | refunds: D1 Rs12.00, D2 Rs1.13, both RESTAURANT_FAULT COMPLETED |
| PickupDeliveryOtpTest (1 enabled method) | PASS | +1 delivered |
| OrderCancellationFlowTest | FAIL then PASS | **Timing, fixed**: restaurant read model got CANCELLED 1.7 s after the cancel (14:55:02.41 → 14:55:04.15Z) and the dashboard polls every 5 s; the test allowed 5 s → 15 s. First run's order d627bf1c refunded Rs43.02 COMPLETED (checked) |
| RestaurantRejectFlowTest | PASS | R1 0eac723e CANCELLED_BY_RESTAURANT, refund COMPLETED |
| CustomerOrderHistoryUiTest#cancelledHistoryRow… (R1) | PASS | |
| CustomerOrderPlacementTest (2) | PASS | 2 PENDING_ACCEPTANCE → restaurant-timeout sweeper |
| DelayApprovalFlowTest (2) | PASS | 1 ACCEPTED (60-min sweeper), 1 CANCELLED_BY_RESTAURANT (delay rejected, refund COMPLETED) |
| AdminOrderMoneyOutcomesTest | **5/5 PASS** | D3 DELIVERED, D1+D2 PARTIAL, d627bf1c CANCELLED, R1 REJECTED |
| RetainedOrderStateUiTest (D3), PartnerReadOnlyUiTest#riderCompletedTrip… (D3) | PASS | |

Mistake: my first money-outcomes run passed `$D1:PARTIAL` unbraced in zsh, which applies `:P` as a modifier and mangled
the value (ArrayIndexOutOfBounds in the test's parser). Brace variables before `:` in zsh.

Still to run: full CustomerOrderHistoryUiTest as `-Dcustomer.phone=8000000001` (its default is 8000000484) after the
placement orders auto-cancel; ChatSupportWindowClosedTest needs orders delivered > 2 h ago (D1 qualifies from 16:45Z = 22:15 IST).

## Order history, full class as 8000000001 (20:43)

12/13 PASS (was 1/12 before the lifecycle). historyPaginationPreservesRowsWithoutDuplicates: no "Load More History" — data,
not a regression: history = status terminal OR delivery terminal (IOrderRepository findHistoryOrderIdsForCustomer) = 2 CANCELLED
+ 4 CANCELLED_BY_RESTAURANT + 4 DELIVERED = 10; page size 10 (OrderController default) → `last=true`. The ACCEPTED delay order
becomes the 11th when the 60-min sweeper cancels it (~21:30 IST); a background wait then reruns the method.
20:46: the ACCEPTED delay order ended CANCELLED_BY_PLATFORM at 20:45:52 (rider offline at dispatch, not the 60-min sweep),
making 11 history rows; historyPaginationPreservesRowsWithoutDuplicates PASS — page 2 of the rewritten paged order read, live.
Order history class now 13/13 across runs. Dev: 4 DELIVERED, 2 CANCELLED, 4 CANCELLED_BY_RESTAURANT, 1 CANCELLED_BY_PLATFORM; none active.

## UI 3e151c7 deployed (owner: "deployed the UI, go ahead with the checks"), 21:50 IST

- Verified: FoodDeliveryAppUI HEAD 3e151c7 == origin/main (clean; commit contains AdminManualInterventions.tsx + .test.tsx)
  == pin == DEPLOY_LOG 2026-10-10T16:18:47Z == running image (healthy). UITesting still uncommitted (51 files; runner uses the tree).
- AdminDispatchSafetyRoutedUiTest fast ×3: 18/18. Supporting only — the old failure was intermittent; the unit test is the proof.
- Sanity on the new UI: LoginTheme 2/2, SupportChatIsolation 4/4, FleetSafety 2/2, ops tabs 4/4.
- ChatSupportWindowClosedTest waits for 16:55Z (latest delivered order f240c12e closes 16:54:18Z; D1 16:45:56Z;
  rule `CHAT_AFTER_FINISH_MS` = 2 h from updatedAt).
