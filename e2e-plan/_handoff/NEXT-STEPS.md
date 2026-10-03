# Ordered continuation

## Current order (checkpoint35 — 2026-10-03T15:50:36+05:30)

1. Owner deploy/prove checkpoint34; confirm D15 before O1 deployment.
2. Publish CommonLibrary/Identity stubs; rebuild/deploy Identity; sync gateway/shared/five histogram configs. No O1 reset/seed.
3. Read-only verify versions/health/migrations; resolve current Dev URL from deployment-context.md.
4. UITesting: `python3 scripts/run_organisation_o1_e2e.py --app-url <current-Dev-URL>`; preserve exact manifests, including failures.
5. Exact-org SQL/outbox and real p95/breaker proof per [checkpoint35](checkpoints/35-business-platform-o1-local.md). Compilation is not live proof.
6. Once O1 live green, confirm D3 and start O2. D4/D6/D13 gate later phases; UI work O5/W3/A4.


## Current order (checkpoint34)

0. Owner's next big work item is planned in `RandomDocuments/BusinessPlatform_2026-10-03/` (organisations, one login + launcher, business wallet, Ads Manager). Deploy checkpoint 34 before its phase O1 starts; its A4 phase deletes the restaurant Campaigns tab and the campaign E2E tests below.

1. Wait for the user to deploy **food-delivery-app-ui**, **campaign-service**, **wallet-service** (governmentid-service optional). Verify images/health read-only.
2. Earnings: re-read the three figures read-only (76.44 / 8.96 / 67.48 at checkpoint33), then `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q -Dtest=RestaurantEarningsLiveTest -Dapp.url=<tunnel> -Drestaurant.phone=9000000001 '-Dearnings.outlet=Brand 1 Outlet 3' -Dearnings.expected.net=<db> -Dearnings.expected.clawbacks=<db> -Dearnings.expected.pending=<db> -DexcludedGroups=slow-auth,auth-rate-limit test`.
3. Campaigns, read-only first: `-Dtest=RestaurantCampaignsLiveTest '-Dcampaign.outlet=Brand 1 Outlet 3'` (same base flags). With 0 advertisers this verifies the start step and stops (assumption, not a pass for ONBOARD). Then `-Dcampaign.onboard=true` once (creates the owner's advertiser + ad wallet permanently; the user chose this flow), then `-Dcampaign.create=true` once (one DRAFT; never serves or spends). Check campaign_db/wallet_db read-only after.
4. Ask the user about the activation gap (ad group + creative + moderation + activate) before building any of it.
5. Then rider-wallet-earnings-and-history (locator audit already shows "Today’s Earnings" missing in PartnerReadOnlyUiTest:64,75), profiles-settings-and-kyc, restaurant-outlets-and-menu-management.

## Current order (checkpoint33)

1. User decision: how a restaurant becomes an advertiser (campaigns).
2. Deploy the UI (earnings wiring; plus the campaigns UI if decided in time), then run `RestaurantEarningsLiveTest` with `'-Dearnings.outlet=Brand 1 Outlet 3' -Dearnings.expected.net=<db> -Dearnings.expected.clawbacks=<db> -Dearnings.expected.pending=<db>` (re-read the three figures read-only first; 76.44 / 8.96 / 67.48 at checkpoint33).
3. Then rider-wallet-earnings-and-history, profiles-settings-and-kyc, restaurant-outlets-and-menu-management.

## Current order (checkpoint32)

1. Next feature: phase 05 — campaigns-and-promotions, profiles-settings-and-kyc, restaurant-earnings, restaurant-outlets-and-menu-management, rider-wallet-earnings-and-history (one at a time, source first).
2. A review flow needs a delivered order with no reviews (14-day window); bb43e2a4 is used.
3. Carry-overs: SupportRefundResolutionFlowTest live re-run; page-size/validation task; validate_core_services 6 pre-existing; readiness 4.1/5.4.

## Current order (checkpoint31)

1. Priority list complete. Next features from FEATURE-STATUS, one at a time: 04 reviews-and-support, then phase 05 (partner and account management), 06 live operations and users/moderation, 07 resilience.
2. Re-prove SupportRefundResolutionFlowTest at its next natural run (needs a fresh in-window delivered order).
3. Separately: the page-size/validation task, validate_core_services' 6 pre-existing findings, readiness 4.1/5.4.

## Current order (checkpoint30)

1. Wait for the user to deploy **food-delivery-app-ui**; verify image/health.
2. Rerun: `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=AdminOrderMoneyOutcomesTest' -Dapp.url=https://gulf-strike-dark-extras.trycloudflare.com/ -Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 '-Dmoney.outcomes=bb43e2a4-5e05-4a7c-9e0d-7f3d5e7c9fb4:DELIVERED,d3acfc93-7aa2-4aae-aad0-7a9711c31b8d:PARTIAL,cf608115-ee1e-468c-8a7f-1212e1cef902:PARTIAL,19359711-45d4-4d44-b17b-cbf9405419de:CANCELLED,bf109947-3f5c-4033-885a-0b3b0e309159:REJECTED' -DexcludedGroups=slow-auth,auth-rate-limit test` (expect 5/5; read-only, no time window).
3. The priority list is then complete. Next: re-prove SupportRefundResolutionFlowTest at its next natural run, then the remaining features in FEATURE-STATUS.

## Current order (checkpoint29)

1. Priority list next: admin order-money views per outcome (delivered, cancelled, rejected, refunded, failed).
2. Re-prove SupportRefundResolutionFlowTest live at its next natural run (it now uses `SupportRefundSteps`).
3. Separately: the page-size/validation-handler task, validate_core_services' 6 pre-existing findings, readiness 4.1/5.4. Then the remaining features in FEATURE-STATUS.

## Current order (checkpoint28, supersedes 27's run list)

1. Wait for the user to deploy **customer-service, chat-service, payment-gateway, food-delivery-app-ui**. Verify images/health read-only.
2. Isolation (any time): the ChatAndRefundIsolationTest command under checkpoint27 below (expect 1/1).
3. One fresh `HappyDeliveryFlowTest#completeOrderLifecycle` (no resume) → order X. Within two hours of its delivery, run in this order, each with `-Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 -DexcludedGroups=slow-auth,auth-rate-limit`:
   - `-Dtest=ChatHistoryPagingTest -Dchat.history.order.id=X` (expect 1/1);
   - `-Dtest=AdminRefundRetryFlowTest -Dretry.order.id=X` (expect 1/1; needs X to have no tickets or refunds).
   If the lifecycle errors after a successful server action, resume X on its own id.
4. SupportRefundResolutionFlowTest now uses `SupportRefundSteps` (moved helpers); re-prove it live at its next natural run on a fresh order rather than spending a lifecycle on it alone.
5. Then: admin order-money views per outcome.

## Current order (checkpoint27)

1. Wait for the user to deploy **customer-service, chat-service, food-delivery-app-ui**. Verify images/health read-only.
2. Rerun: `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=ChatAndRefundIsolationTest' -Dapp.url=https://gulf-strike-dark-extras.trycloudflare.com/ -Dcustomer.phone=8000000484 -Disolation.customer.phone=8000000485 -Drestaurant.phone=9000000002 -Drider.phone=7000000027 -Dadmin.phone=1000000001 -Disolation.order.id=d3acfc93-7aa2-4aae-aad0-7a9711c31b8d -DexcludedGroups=slow-auth,auth-rate-limit test` (expect 1/1).
3. Rerun: `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=ChatHistoryPagingTest' -Dapp.url=https://gulf-strike-dark-extras.trycloudflare.com/ -Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 -Dchat.history.order.id=<order inside its 2h window> -DexcludedGroups=slow-auth,auth-rate-limit test`. bb43e2a4 qualifies until about 09:29 IST; after that a fresh delivered order (lifecycle) is needed.
4. Admin refund retry live: waits on the user's decision (Dev mocks cannot fail a refund).
5. Then the priority list continues with admin order-money views per outcome.

## Current order (checkpoint26)

1. ~~Server-side support window~~: decided 2026-10-03, stays UI-only (USER-INSTRUCTIONS).
2. Watch HappyDeliveryFlowTest reliability: two consecutive fresh runs each hit a different harness timing miss after successful server actions (accept-navigation wait at :563 via DispatchPingPage:64, delivered-summary 5s at :732). On a third, inspect the waits before creating more orders; resume the exact order each time.
3. Continue the priority list: admin refund retry live on a definitive provider FAILED, chat isolation and history beyond 50 messages, admin order-money views per outcome, then the remaining features. Separately: validate_core_services' 6 pre-existing findings and readiness 4.1/5.4.

## Current order (checkpoint25)

1. Wait for the user to deploy **food-delivery-app-ui** (dead-button fix). Then check in the browser, on 0554f250 (outside the window), that "Something wrong with this order?" is gone, and on an order within two hours of its last update that it still opens the chat.
2. Ask/record the user's decision on enforcing the two-hour support window server-side.
3. Support-refund reruns need an order **last updated less than two hours ago** (the test now asserts it). d3acfc93's window closes about 2026-10-03T06:29+05:30. After that, the full class needs a fresh delivered order (fresh lifecycle, then `-Dsupport.order.id`).
4. Continue the priority list: admin refund retry live on a definitive provider FAILED, chat isolation and history beyond 50 messages, admin order-money views per outcome, then the remaining features in FEATURE-STATUS. Separately: the pre-existing validate_core_services findings and readiness 4.1/5.4.

## Current order (checkpoint24)

1. Wait for the user to deploy **customer-service** ([DEPLOYMENT-GATE](DEPLOYMENT-GATE.md)). Verify the image and health (read-only).
2. Rerun SUPPORT-REFUND-03 on the owned order (its precondition holds: one COMPLETED award, one RESOLVED ticket):
   `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=SupportRefundResolutionFlowTest#refundedItemCannotBeRefundedAgain' -Dapp.url=https://gulf-strike-dark-extras.trycloudflare.com/ -Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 -Dsupport.order.id=0554f250-eade-4677-8e51-18f4aacd22f5 -DexcludedGroups=slow-auth,auth-rate-limit test`
   Needs `target/lifecycle/0554f250-eade-4677-8e51-18f4aacd22f5.json` (copy from fixtures/ if absent). Check the customer outbox gets a CHAT_REFUND_ERROR and nothing new lands in chat-events.DLT.
3. The full class (02 → 01 → 03) needs a delivered order with no tickets or refunds: one fresh happy lifecycle, then `-Dsupport.order.id=<new id>`. Run it only when the full chain must be re-proven, not as a routine rerun.
4. Then the priority list below (admin retry live, chat isolation/history >50, admin order-money views per outcome), then the remaining features. Separately: the 6 pre-existing validate_core_services findings and readiness 4.1/5.4.

## Current order (checkpoint23)

1. Deploy customer-service, the gateway config and the UI; publish CommonLibrary. Verify images, the flyway row, and that the column is dropped.
2. Rerun SupportRefundResolutionFlowTest on c463191b (partial) and 7f7af6a5 (deny); command in checkpoint23. Ticket 2f5d7acf is OPEN and gets approved by scenario 01.
3. Separately: refresh the stale validate_core_services findings (6, all pre-existing).

## Current order (after checkpoint22)

The core money flows are green on fresh data. Continue the priority list:
1. Real support-ticket refund outcomes on a delivered owned order (approve / partial / deny, the remaining cap, idempotency); c463191b or 7f7af6a5 can be used, respecting the quantities already consumed.
2. Admin retry, live: only for an owned refund with a definitive provider FAILED (now possible end to end after checkpoint20).
3. Chat isolation and history beyond 50 messages; admin order-money views for each outcome.
4. Optional admin decision: resolve the ledger rejection a7862d1a with a note (its movement is now posted).
5. The remaining features in FEATURE-STATUS.md. Excluded: SSE (quick tunnel), slow waits, rate limits.


## Current order (checkpoint22)

1. The user deploys ledger-service (migration V20261002210000). Verify the image, health and the flyway row.
2. With **user approval**, replay DLT `{"dltTopic":"ledger-events-dlt","partition":0,"offset":0}` through the ledger admin endpoint. Then run `HappyDeliveryFlowTest` with `-Dresume.delivered.order.id=7f7af6a5-1d86-4f29-b4cc-36eefe69a74b` (strict money plus quote).
3. One fresh full happy lifecycle (no resume properties).


## Current order (checkpoint21)

1. Wait for the user to deploy customer-service and delivery-service (the UI only carries regenerated types). Check images and health.
2. Resume HappyDeliveryFlowTest on 7f7af6a5 with `-Dresume.order.id` and `-Dresume.outlet` (the exact command is in checkpoint21). Do not create a replacement.
3. Then run one fresh happy lifecycle to prove the accept→active gap closes live. Then do the quote and support-refund outcomes on a delivered order, and the remaining features.


## Current order (checkpoint20, 2026-10-02T19:10:00+05:30). Supersedes the retained-recovery steps below.

1. Wait for the user to deploy the checkpoint20 changes ([DEPLOYMENT-GATE](DEPLOYMENT-GATE.md)) and to reset and reseed the Dev data. Then check image versions and health, and that the seeded actors in TEST-DATA.md exist.
2. Run `OrderCancellationFlowTest#customerCancelsBeforeAcceptance` and `RestaurantRejectFlowTest#restaurantCancelsOrder` **without** `-Drefund.resume.order.id`, so they create fresh owned orders. Assert refund COMPLETED with completedAt, payment REFUNDED, a balanced REFUND ledger, and the customer UI refund state settling with polling stopped. Save the new manifests to fixtures/.
3. Run `HappyDeliveryFlowTest#completeOrderLifecycle` once on the fresh seed (no resume properties). Then do the real quote and support-refund outcomes on that delivered order.
4. Admin retry, live: only for an owned refund that reached a *definitive* FAILED from the provider. A lost-callback refund stays held by design (see checkpoint20's known gaps).
5. Then continue the remaining features in FEATURE-STATUS.md.

Steps below that mention the three retained orders, stored-confirmation recovery or "resume exact IDs" are historical.


## Pending deployment gates

[Deploy customer-service, chat-service and UI](DEPLOYMENT-GATE.md) for exact selected retry, newest history and visible refund read/retry/refresh. Local checks passed; these changes are not yet deployed. No reset/seed/config/migration. Wait for user confirmation, verify versions/health, then validate dependent browser behavior. Keep independent source/recovery work separate while waiting. Additional checkpoint19 payment-gateway capture recovery is ready locally (58selected checks); it uses verified stored confirmation evidence and existing admin routing. Deploy it separately, then verify exact original-ID local capture restoration before proceeding with downstream refund recovery.

## Current next job: retained money recovery

1. Re-inspect deployed health/image versions, source working trees and exact owned order/refund/payment/transaction/ledger state. Check rider OFFLINE. Compare with CURRENT-STATE; stop a fixture-based mutation if ownership/environment was reset or outcome changed. Do not repeatedly run tests against a known false prerequisite.
2. Review existing recovery contracts before acting: CustomerApplication selected retry is now fixed locally and awaits deployment; verify its exact-identity transactional queueing before use. The separate automatic RefundService.retryStuck still needs destination/race/serialization review. PaymentGatewayIntegration PaymentEventConsumer holds refund_req:<refundId> after initiation even when async callback failed. Fix/validate exact-identity recovery where needed, not a new refund ID or generic global sweep. The normalized mock callback now requires a real persisted confirmed capture. Old SUCCESS intents have no automatic backfill.
3. Prepare a concrete owned recovery with original identities and audit trail. Checkpoint19 supplies an ADMIN-only stored-confirmation recovery path, pending deployment/runtime proof. Only actual confirmed capture evidence can backfill its missing transaction; no fake amount, receipt or artificial balance. Ensure failure/cancellation refunds cannot exceed captured remaining amount; avoid duplicate external initiation. Accidental refund on the delivered order requires separate decision/audit, not ordinary retry. Do not reset/delete state or make an erroneous refund succeed simply to get a green test.
4. Rebuild the retained delivery distribution from authoritative stored order economics with the original deterministic transaction identity. Blind DLQ replay uses wrong wallet funding/leg ordering; a queue resolve note is not posting. There is no verified automatic admin replay/book path for rebuilding the command yet. Validate backend recovery, then the exact posted restaurant/rider net amounts and balanced order-scoped lines.
5. Restore/validate the retained delivered business outcome only through reviewed audited correction using actual delivery-service completion proof; preserve accidental-refund history. A raw SQL status edit without audit and financial reconciliation is not a completed product recovery. Do not invent an endpoint or claim such recovery is already implemented.
6. Once eligibility is valid, use existing HappyDeliveryFlowTest retained branch to verify real quote response (order/item/quantity/reason, exact tax-inclusive amount and action). Quote-only mode is not money/full lifecycle coverage. Submit/resolve a support request only as the intentional owned scenario, then check separate ticket/refund/payment/ledger states.
7. Resume existing cancellation/rejection tests via -Drefund.resume.order.id, with their manifests. Do not create another cancellation or rejection. Check exact original refundID, amount/destination, completedAt, paymentREFUNDED, balanced REFUND ledger, customer rendering and admin exact-order money.

## Next meaningful priority outcomes

- Real support ticket approval, reduced partial award, denial, whitespace/amount/remaining-cap validation, immutable completion, retry/idempotency and duplicate result protection. Reuse the owned delivered order when state permits; separate tickets only for incompatible decisions and respect consumed item quantities/refundable remainder.
- Unrelated actor/order isolation for chat/refunds/admin, latest history beyond50messages and concurrent session creation. Earlier successful same-order message receipt does not prove these.
- Validate checkpoint18 refund UI read-error/retry/refresh after deployment confirmation. Local silent-empty behavior is fixed; actual deployed read and completed-state evidence remain open.
- Admin order-money views for delivered/cancelled/rejected/refunded/failed outcomes; committed payout maker/checker only with isolated owned payee and second approved admin. Browser-routed fixtures prove UI requests, not settlement.

## Then finish the remaining audit

Complete current restaurant acceptance/preparation source mapping using canonical acceptance, then rider availability/dispatch exceptions, reviews/support and remaining phase05–07 features. Feature matrix lists all28folders. Inspect one folder at a time and reuse scenario/test inventory instead of reimplementing earlier passed cases. Existing historical103failures and 40 backend documents still require source-grounded closure individually; full report closure is not claimed.

## When a source fix needs deployment

Run all affected existing tests/contract/startup/persistence checks; write precise changes, test counts and runtime limits in a handoff. User commits/pushes/deploys. Wait for completion confirmation, then resume exact owned fixtures. Do independent read-only/routed/local work during the wait; do not churn failing live tests during deployment. Update the portable handoff before stopping.

## Excluded now

Do not resume SSE, natural expiry/long timeout/grace/reset waits or relaxed Dev rate-limit exhaustion. Do not restore the withdrawn rider item checklist. Do not rerun successful happy acceptance as a duplicate test. Do not claim the entire suite works until meaningful executions prove it.
