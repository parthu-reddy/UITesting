# Deployment gate

## 2026-10-07T17:14+05:30 — Item5 A5b DONE: live arrival estimate verified on Dev

Deployed customer ea07602, delivery 48077cc, UI 7e7ebce (served == pin == HEAD, healthy). Order
f3b3d38b (Brand 1 Outlet 5) DELIVERED; the tracker showed "Live" at ASSIGNED (+1307 s), after pickup
(+1027 s) and +0 s 40 s after the rider's GPS reached the door. Invocations 1–2 errored on harness
issues (Java long through page.evaluate; my forced SSE map check, parked on the tunnel); 3 passed
1/1 by resuming the same order. Validator 10/10, 17/17, 8/8, 4/4.
[Evidence](evidence/a5b-phase4/lifecycle.json). **Next: item6** (backlog reconcile + final handoff).

## 2026-10-07T16:38+05:30 — Item5 A5b: phases 1–3 done locally; owner deploy pending

Live arrival estimate from the assigned rider's position. Plan and evidence:
[A5b_LiveRiderEta](../../../RandomDocuments/PendingWork_2026-10-07/A5b_LiveRiderEta/README.md).
- Delivery: the snapshot Lua now runs in a test (luaj); `/sync` stamps receipt time; `/batch` relays
  only the rider's assigned order (any rider could draw on any customer's map). Module 237 green.
- Customer: `LiveDeliveryEtaService` on `/active`, `/{id}`, `/batch`; `estimatedArrivalSource`
  ROUTE|LIVE + `estimatedArrivalTimeExpiresAt`. Maps route is fetched off-thread, ≤1 per order per 30 s.
  Module 529 green; spec +2 fields.
- UI: "Live" chip until expiry; 10 s polls while a rider is assigned. Vitest 1070, gates green.
- Validators 10/10, 17/17, 8/8; break-tests 9/9, 9/9, 3/3 RED-then-GREEN.
- E2E: `HappyDeliveryFlowTest` checks live at ASSIGNED and OUT_FOR_DELIVERY, then ≤2 min at the door.
  It compiles and passes the locator audit; it has not run.
**Waiting on the owner:** deploy delivery, customer, then UI
([A5b DEPLOY](../../../RandomDocuments/PendingWork_2026-10-07/A5b_LiveRiderEta/DEPLOY.md)). No wipe.
Then one fresh lifecycle, and `validate_a5b.py --phase 4`.

## 2026-10-07T16:00+05:30 — Item4 M1 DONE; first fully green lifecycle after all item3/M1 fixes

Deployed: customer 0fe11f0, delivery 911d678, maps 28afef7, UI 58542df, after the CommonLibrary
6dfdd0b publish; the tags equal the pins. `HappyDeliveryFlowTest` passes 1/1 on order 570bbdcb in
268s. Route responses carry integer totals through the typed RouteDto (rider 1129 s/4577 m). The
earnings increase, trip-details payout, 18 balanced ledger lines and the receipt all pass.
Validator: phase1 3/3, phase2 9/9, phase3 6/6.
**Next: item5 A5b.** Note for A5b: a missing total arrives as null, but the TS type omits null.
[M1](../../../RandomDocuments/PendingWork_2026-10-07/M1_NumericRoute/README.md).

## 2026-10-07T15:40+05:30 — Item4 M1: phases 1–2 done locally; owner deploy pending

The M1 plan lives in [M1_NumericRoute](../../../RandomDocuments/PendingWork_2026-10-07/M1_NumericRoute/README.md).

Phase1 observed through the real UI on order 046fa470 (delivered): the deployed route responses
carry integer totals (rider 939 s/4577 m), so the extraction is correct. That lifecycle errored
afterwards on a harness double-sum bug (₹63.48 vs 63.480000000000004), now fixed with whole paise.

Phase2, validator 9/9:
- the pre-M1 `travelSeconds()` stopgap is deleted;
- the delivery route is a typed RouteDto;
- MapsIntegration has extraction tests and a producer contract;
- the specs and UI types are regenerated;
- all touched builds are green.

**Waiting on the owner:** push CommonLibrary first and wait for its publish, then the rest, then
deploy ([M1 DEPLOY](../../../RandomDocuments/PendingWork_2026-10-07/M1_NumericRoute/DEPLOY.md)).
Phase3 is one lifecycle after that.

## 2026-10-07T14:45+05:30 — checkpoint128: ITEM3 CLOSED; banner fix verified live

UI 8c5b15e is served. A read-only on-duty probe saw only "Connecting to dispatch…" on three reloads,
never "Connection lost", and the rider was restored to Offline. All item3 dispositions are recorded.
**Next: item4 M1, then item5 A5b** (still paused; a fresh session is suggested). Owned fixtures
d3e0ebed and f01c1e92 are DELIVERED. [Checkpoint128](checkpoints/128-item3-closed.md).

## 2026-10-07T14:15+05:30 — checkpoint127: checkpoint-125 fixes verified live; banner fix pending deploy

UI cfb83e2 is served, and the Dev data was wiped by a fresh install (old fixtures are gone). After
deployment, the resumes of d3e0ebed and f01c1e92 pass 1/1 each. The retained money check on f01c1e92
passes 1/1: 18 lines balance ₹92.72 and the rider net is ₹21.16. All four checkpoint-125 fixes are
verified live.

The intermittent missing courier pin was proven to be Playwright's ageing static GPS fix, not a
product defect, and the harness now refreshes it.

New, local only: the dispatch banner said "Connection lost" on every page load before the first
connection. It now says "Connecting to dispatch…", proven red first; UI 179/1,065 green.
**Waiting on the owner: a FoodDeliveryAppUI deploy (5 files). Pull Deployment first.**
[Checkpoint127](checkpoints/127-postdeploy-active-rider-verified.md).

## 2026-10-07T13:50+05:30 — Redeploy confirmed served; Dev data wiped by fresh install

The owner redeployed at 08:04Z with the correct pins (UI cfb83e2 plus customer, delivery and maps).
The served `DeliveryDashboard-DzSDaAKB.js` contains all four new strings and none of the old fee
text. The served map chunk places known pins before the geolocation call.

The owner also did a **fresh installation that wiped all old data**. Fixtures 6fbe0289, ce254f3a and
ticket 47ffbccd are gone and are marked `wipedAt`. The retained read-only check on ce254f3a therefore
failed: History returned 200 with empty content. This is not a product defect. Their earlier
results remain dated history.

The trip-details "Your net payout" assertion is now a shared helper, run by the main lifecycle as
well. One fresh `HappyDeliveryFlowTest` lifecycle with the visual audit is running against the new
build. Evidence goes to `evidence/127-postdeploy-active-visual`, and any created identity must be
retained. [Evidence](evidence/127-postdeploy-served-and-wipe.json).

## 2026-10-07T13:30+05:30 — UI deploy did NOT take: stale Deployment pins

The owner reported "Everything is deployed". UI commit cfb83e2 contains exactly the 11
checkpoint125 files, and CI 37588967285 succeeded. However, the live site still serves 6eb4ef6:
`DeliveryDashboard-CubehF8y.js`, with 0 new strings and the old "credit"/"Total Earnings" text.

DEPLOY_LOG shows the 07:51:09Z deploy used food-delivery-app-ui **6eb4ef6**. That deploy ran from
a Deployment checkout 4 commits behind origin/main. customer-service, delivery-service and
maps-integration went out at their previous tags too. The fix is `git pull` in Deployment, then
redeploy. No E2E was run against the stale build. [Evidence](evidence/126-deploy-stale-pins.json).
The checkpoint125 next gates are unchanged.

## 2026-10-07T11:37:19+05:30 — Retained finance now green; active rider regression running

Same6fbe0289 money/quote continuation passes1/1 through real payout drawer/order-reference controls:
18balanced ledger lines, CARD/SUCCESS₹43.02, restaurant₹6.54 and rider₹21.16,0new order/refund.
First invocation's brand-decorated-name assertion failure is preserved separately. UI6eb4ef6 is live;
receipt and owned47ffbccd REJECTED audit already pass. One justified fresh active lifecycle is
running; current authoritative ONLINE/live-location preflight is verified, no final result yet.
Its new identity is retained immediately at creation. Item3 active, M1/A5b paused.
[Checkpoint124](checkpoints/124-deployed-receipt-owned-refund-and-active-rider-gate.md) supersedes earlier finance-running text below.

## 2026-10-07T11:33:21+05:30 — checkpoint124: UI live; retained receipt and owned rejected audit pass

Owner confirmed UI-only deployment. UI6eb4ef6's actual CI asset is observed by normal rider login,
1/1green. Retained6fbe0289 receipt/quote is fixed live,1/1green,0new order/refund submission.
Owned ticket47ffbccd was created/rejected once; original writer errored after rejection200 while
reading an unavailable request body. Same-ticket read-only resume passes1/1, verifies REJECTED,
exact resolution note/actor/timestamp and0customer refunds, with0new order/resolve writes.
Preserve both invocations; no duplicate ticket or refund approval. All three green methods have
0failures/errors/skips/network/browser errors within their stated scope.

Item3 stays active for strengthened active rider checks. The money helper now follows actual
Pending Payouts/drawer/order-reference controls; retained money continuation is running, not passed.
Then one fresh active lifecycle is justified for changed mobile UX, with current preflight/retention.
No UI deployment is currently pending for these fixes. M1/A5b remains paused.
[Current checkpoint124](checkpoints/124-deployed-receipt-owned-refund-and-active-rider-gate.md) supersedes older deployment-waiting statements below.

## 2026-10-07T11:17:55+05:30 — checkpoint123: active receipt/mobile fixes; owner deploying UI

Items1/2 are DONE. Item3 alone remains active. UI5a1e97c's canvas/sidebar fixes and F12 pass live3/3.
The new owned lifecycle reached DELIVERED, then failed the history-to-receipt assertion:1failure,
0errors/skips. Keep **6fbe0289-645e-4f1c-ae0d-caab21689070** at Brand1Outlet5,
customer8000000001/owner9000000001/rider7000000001. Its401 was after sign-out in teardown.

Additional UI fixes keep terminal history over stale active cache, fit the pickup hint and place48px
rider chat in the header away from swipes. Local UI1049/174files,focused34,typecheck,lint,build,
Phase4source12/12 pass. Owner committed6eb4ef6 and GitHub37577846909 is publishing; actual live
deployment is not yet confirmed. [Exact UI handoff](../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ACTIVE-DEPLOYMENT.md).

Read-only All Tickets E2E passes1/1,GET200,0rows/0resolution writes; populated states remain unverified.
Owner **approved** one dummy request/rejection on6fbe0289, with approval confirmation cancelled and no
refund approval/payout. OwnedRefundVisualUiTest is prepared/compiled, not yet executed. First verify
the retained receipt after deployment, then this writer. Strengthened active mobile checks need one
justified new lifecycle after deployment; preserve any new identity on failure. M1/A5b stays paused.

[Checkpoint123](checkpoints/123-active-visual-receipt-and-mobile-fixes.md), [local gates](evidence/123-active-ui-local-gates.json),
[All Tickets proof](evidence/123-admin-all-refunds-invocation1.json),
[approved exact fixture plan](../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ADMIN-FIXTURE.md).
Older running/deployment-waiting statements below are dated history superseded by this entry.

## 2026-10-07T10:49+05:30 — UI visual defects fixed live; active-state audit continues

Owner published UI `5a1e97c`; GitHub build37574491813 succeeded and matches all three tested files.
RoleVisualAuditUiTest invocation5 passes **3/3, 0 failures/errors/skips/network/browser errors**.
Actual captures show opaque dark rider canvas, full wrapped sidebar labels and settled Support
Tickets. The two UI defects and F12 are fixed; no current UI deployment gate remains.

Item3 stays active for D1/E2 fixture states. Existing HappyDeliveryFlowTest now has opt-in assigned/
out-for-delivery 390×844 visual assertions; compile and focused locator checks pass. Its one-method
UI-only run has started with customer8000000001/owner9000000001/rider7000000001; it created owned
order6fbe0289-645e-4f1c-ae0d-caab21689070. No result is claimed while it runs. Retain/resume that
exact fixture on failure. [Active-state plan](../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ACTIVE-STATE-PLAN.md).

[Green evidence](evidence/122-role-visual-invocation5-green.json), [owner publication](evidence/122-owner-ui-publication.json).
M1/A5b remain paused; no new backend deployment or state shortcut. Older deployment-waiting entries
below describe their dated state.


## 2026-10-07T10:35+05:30 — checkpoint122: owner is deploying UI

**FoodDeliveryAppUI** only: forced-dark rider canvas and fully readable wrapped sidebar labels.
Local UI1045/1045 and toolchain green; final strengthened RoleVisualAuditUiTest is pending deployed
proof. Owner answered “I'll deploy the UI now”; wait for confirmation before the live rerun.
[Exact scope and command](../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-DEPLOYMENT.md).
Paused maps/ETA code is incomplete and outside this UI gate. Historical gates below are not current.

## 2026-10-05T11:57:12+05:30 — checkpoint113: O4/O5 complete; stop boundary reached

Phase4 and Phase5 are published, deployed and complete within the documented Dev/UI-only scope. Final O4 gate8/8 and O5 gate8/8 have0failures/errors/skips. The required existing settings/partner/navigation/earnings/admin-money/delivery/chat/refund-quote/immutable-review regressions pass using the same retained delivered order. No new checkout repeated successful work. Restaurant5408c6e is now published/deployed; existing pending-queue check1/1 proves the exact outlet VERIFIED at₹34.67 and0payout writes.

Latest sources: UIf73ffe9 (GitHub37269604733,941/941), Restaurant5408c6e (GitHub37271732121,146/146 CI;163/163 local clean), Customer0849ee6,Identity5210c6e,Gateway0b729f0,Reviewsd589d2f; UITesting1f3dae0 compile33; Deployment8a31eb2. Final operator state at2026-10-05T06:23:59.320213+00:00:29running,26healthy checks/three without checks,0drift/restarts/recent error lines. Hardening15/15 and reconciliation29/0drift pass. Current public Oracle UI remains https://gulf-strike-dark-extras.trycloudflare.com.

Final protected ordinary-UI histograms: portals n268,p95estimate11.632ms/upperbucket12.583ms <=150ms; refresh n93,p95estimate30.479ms/upperbucket33.554ms; Gateway n4775,p95estimate102.061ms/upperbucket111.848ms. Both Measurements sections and final checklist boxes are recorded. [Release acceptance](../../../RandomDocuments/BusinessPlatform_2026-10-03/01_Organisations/RELEASE-2026-10-05.md) and evidence74–76 preserve limits/counts.

Canonical60631296-e064-4a59-a2ef-090549edcf01 remains DELIVERED/CARD/SUCCESS,total₹72.81,restaurant₹34.67,rider₹21.16,with four immutable participant reviews. Two earlier owned cancelled orders and all original failure/allocation manifests remain retained. Final phase allocations4de9964404871016 and7b20efe3e581ba19 remain. No further wipe/seed, refund/payout submission, live session cleanup or direct-state E2E occurred.

No required O4/O5 action remains after final documentation validation/commit. Preserve explicit O4-PERF-001 (missing before Gateway histogram),O5-CSP-001 (deliberate broken CSP),O4 internal replacements, duration/rate/SSE/provider/load/parked ONDC boundaries as unverified. W1–W3/A1–A4 remain unstarted. Stop now; a later user instruction is required to begin those phases. The broader E2E audit outside this scoped regression set is not declared complete. Earlier dated entries are history.

## Checkpoint35 gate (current; checkpoint34 first)

Confirm D15. After checkpoint34 deployment/proof, owner publishes CommonLibrary/Identity stubs,
rebuilds/deploys Identity and syncs Deployment gateway/shared/five histogram configurations. No O1
reset or seed. Read [checkpoint35](checkpoints/35-business-platform-o1-local.md) for exact rollout and
live runner/SQL/outbox/latency requirements. Local O1 and consumer suites passed; live gate remains open.


## Checkpoint34 gate (current, includes 33)

Deploy **food-delivery-app-ui** (earnings follow the outlet; Campaigns start step; ad budgets/bid/top-up in rupees; lifetimeBudget; balance on open; restaurantId chain and RestaurantPortal removed), **campaign-service** (`/advertisers/me` 404 when absent; ad wallet minted in INR, which registration needs to succeed at all; startup backfill runner removed) and **wallet-service** (`/api/v1/money/advertiser/**` gated RESTAURANT/ADMIN). **governmentid-service** optional (dead RESTAURANT_MANAGER role removed; no behaviour change). No migration, config or gateway change. UI vitest 771/123, typecheck, lint; clean tests campaign 32, wallet 56, governmentid 33.

## Checkpoint33 gate (current)

Deploy **food-delivery-app-ui**: the Earnings tab follows the selected outlet (`RestaurantTabPanels`, `RestaurantEarningsTab`). vitest 759/121, typecheck, lint.

## No gate pending (checkpoint31)

UI `6eb1743` deployed and verified (MONEY-05 5/5).

## Checkpoint30 gate (current)

Deploy **food-delivery-app-ui** only: `AdminOrderMoney.tsx` gains Payment and Refunds plus Posted to ledger. No backend change. vitest 758/121, typecheck, lint.

## No gate pending (checkpoint29)

Checkpoints 27/28 deployed (customer `07ea84f`, chat `089e5eb`, payment-gateway `5220632`, UI `766b214`) and verified live.

## Checkpoint28 gate (current, includes 27)

Deploy **customer-service** (order read 404), **chat-service** (history page bound), **payment-gateway** (Dev-only `MockRefundFailureSeam`; prod profile unaffected) and **food-delivery-app-ui** (load earlier messages; Failed Refunds tab). No migration, config or gateway change. Clean tests: customer 467/93, chat 63/20, payment 93/28; UI vitest 754/121, typecheck, lint.

## Checkpoint27 gate (current)

Deploy **customer-service**, **chat-service** and **food-delivery-app-ui**. No migration, config or gateway change; chat openapi.json unchanged.
- customer-service: another customer's (or a missing) order reads as 404 instead of 500; dead `getOrderById` removed. Clean test 467/93.
- chat-service: history `page`/`size` bounded (400 outside 0.. / 1..100). Clean test 63/20.
- UI: "Load earlier messages" in chat. typecheck, lint, vitest 750/121.

## No gate pending (checkpoint26)

Checkpoint25's UI gate is deployed (`41578ee`) and verified live, both sides. Checkpoint24's customer-service gate is deployed (`cc04ed7`).

## Checkpoint25 gate (current)

Deploy **food-delivery-app-ui** only. Local, uncommitted in FoodDeliveryAppUI: `isOrderChatOffered` in `features/customer-orders/model/orderStatus.ts`, used by `CustomerOrderChat` and `OrderDeliveredSummary` so "Something wrong with this order?" is hidden once the order chat is no longer offered (it was a silent no-op). typecheck, `npm run lint` and vitest (743/121) pass. No backend, config or migration change.

Checkpoint24 gate (customer-service `cc04ed7`): deployed and verified at checkpoint25.

## Checkpoint24 gate (current)

Deploy **customer-service** only. No migration, no Config Server or gateway change, no UI change, no reset needed. Local, uncommitted in CustomerApplication:

- `ChatRefundProcessorService`: refusals are decided before the write transaction, so ITEM_ALREADY_REFUNDED (and every other quote/request refusal) reaches the customer as CHAT_REFUND_ERROR instead of a dead letter.
- `RefundService`: `request()` split into a write-free `plan()` and `record()`; new `requestUnlessRefused()` returns pre-write refusals.
- `OrderEventConsumer`, `PaymentEventConsumer`: use `requestUnlessRefused`, so a refund routing refusal keeps the order/payment state change.
- Tests: 3 new classes (9 tests), PaymentEventConsumerTest updated. Full clean test 465/92 green.

Checkpoint23 gate: deployed and verified (checkpoint24).

## Checkpoint23 gate (current)

- **customer-service:** migration `V20261002220000__drop_unused_refund_item_amount.sql` plus removal of `CustomerOrderController`. Full suite 456/456.
- **api-gateway:** `Deployment/api-gateway.yml` drops the `/api/v1/customer/**` predicate and the `/api/v1/customer` RBAC prefix (only the deleted controller used them). ApiGateway tests 20/20.
- **food-delivery-app-ui:** dead components deleted, generated types regenerated. Typecheck, lint and 737 tests pass.
- **CommonLibrary (publish):** `common-test` reverse schema guard; test-scope only, no runtime effect.

## Checkpoint22 gate (current)

Deploy **ledger-service**. It includes Flyway `V20261002210000__ledger_entries_unique_per_leg.sql` (adds leg_index, backfills, swaps the unique constraint), tested on real PostgreSQL. The UI only regenerated ledger types (optional). Checkpoint21 (customer + delivery) is deployed and verified.

## Checkpoint21 gate (2026-10-02T20:05:00+05:30), current

Deploy **customer-service** and **delivery-service** together: the delivery service sends `confirmedOrderIds`, and the customer service includes those orders in the rider's active list. Deploy the customer service first or at the same time; an old customer service ignores the parameter, which only means the race stays. The UI changes only regenerated API types. No migration, config or reset. The checkpoint20 gate below is deployed and verified.

Please deploy **customer-service, chat-service and FoodDeliveryAppUI** for the selected retry, refund UI recovery/refresh, and newest chat history fixes. No config, migration, reset or seed needed. The agent has not committed/pushed/deployed or changed protected workflows. Payment-gateway amount guard from checkpoint17 is already deployed.

- CustomerApplication: retry only the selected FAILED refund atomically; preserve identity/destination, validate remaining amount, reject duplicate/invalid states; queued is not returned money.
- CommunicationService: history page zero returns newest50 with stable timestamp/ID ordering; existing UI renders chronology.
- FoodDeliveryAppUI: visible refund read error/retry, same-order retained status and periodic refresh while view open; disabled/left views stop refresh.

Validation: 120 selected customer checks,22 selected chat checks,30 local UI checks passed, zero final failures/errors/skips. Typecheck/lint/diff checks passed. Real local transaction rollback and history pagination checks included; no live money/record repair or new E2E lifecycle performed. Initial test/config mistakes and final counts are recorded in evidence/18-local-results.json. No full-suite/build_verify claim.

After deployment, verify images/health then validate deployed UI recovery/history behavior with existing owned contexts and routed error fixtures. Retained refunds remain FAILED in the last authoritative snapshot; the new selected retry alone does not resolve held gateway initiation keys, old missing capture transactions, original delivered ledger rejection, or delivered status mismatch. Do not invoke historical financial retry until safe downstream reconciliation is implemented/validated. Continue NEXT-STEPS.md without cleanup, replacement orders, SSE/slow/rate exhaustion or automatic workflow edits/deployment.

User acknowledged “I’ll deploy and confirm” on2026-10-02; await explicit completion before dependent live reruns. Independent payment-gateway stored-confirmation recovery review is underway, separate from this deployment batch.


## Checkpoint19 payment-gateway gate: WITHDRAWN

The capture-recovery endpoint was deleted before it was committed. Do not deploy it.

## Checkpoint20 gate (2026-10-02T19:10:00+05:30)

Deploy after the user commits and pushes. No Config Server change.

- **payment-gateway**: releases the `refund_req:<refundId>` key when a refund definitively fails, so an admin retry can initiate it again.
- **customer-service**: one enqueue helper; the sweeper routes STORE_CREDIT refunds to the wallet; a separate `sweep_attempts` budget that an admin retry resets. **Includes Flyway migration `V20261002190000__refund_sweep_attempts.sql`.**
- **chat-service**: test-only change (OpenAPI test mock). Nothing to redeploy unless the checkpoint18 chat change isn't deployed yet.
- **FoodDeliveryAppUI**: refund polling through `usePolling`, which stops once refunds settle; the shared `usePolling` no longer overlaps requests or duplicates its chain.

After deployment, the user resets and reseeds the Dev data. Then follow NEXT-STEPS.

<details><summary>Historical checkpoint19 text</summary>

## Additional independent gate — checkpoint19 payment-gateway

Deploy payment-gateway for the stored-confirmation capture-recovery endpoint under the existing admin DLQ route.58selected local checks passed, including committed concurrent recovery, method-security and existing producer contract/HTTP/startup checks; final failures/errors/skips and background-job errors0. No migration, reset/reseed, Config Server or gateway/UI changes. This is additional to checkpoint18; it does not replace its customer/chat/UI confirmation.

The endpoint restores only a local missing capture from a stored PROCESSED confirmation with exact identity/amount/time and consistent balances. It does not initiate payment/refund or post ledger movements. New source files and test-only Redis setup changes are in PaymentGatewayIntegration. Details/evidence/continuation: [checkpoint19](checkpoints/19-stored-confirmation-capture-recovery.md). User commits/pushes/deploys; agent has no current publish/deploy authorization. No retained financial mutation has run.

</details>
