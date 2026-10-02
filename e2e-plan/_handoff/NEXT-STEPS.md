# Ordered continuation

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
