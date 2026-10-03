# Current checkpoint

## Latest: checkpoint35 (2026-10-03T15:50:36+05:30)

**O1 local verified; not deployed/live verified.** Read [checkpoint35](checkpoints/35-business-platform-o1-local.md).
Complete BusinessPlatform folder read. Organisation/security/audit/event work and clean shared-consumer
suites pass; E2E compiles only. D15 unconfirmed. Owner checkpoint34 deployment remains first prerequisite,
then O1 publish/deploy/config rollout/live gate. No Dev writes or new fixtures. O2+ waits on deployed
O1 green; O5/W3/A4 own requested UI/UX consistency.

## Latest: checkpoint34 (2026-10-03T10:27:00+05:30)

**Local, not deployed.** Campaigns start-advertising step (user decision) and ten defects: registration always failed (ad wallet currency "AD_CREDIT" into `wallets.currency VARCHAR(3)`; now INR, backfill runner deleted) and `/advertisers/me` 400→404 (campaign-service); advertiser wallet gated on a role nobody has (wallet-service, now RESTAURANT/ADMIN); UI sent ad budgets/bid ×100 and showed top-ups ×100; card read a field the server never sends; balance never loaded; dead radius field; the always-empty restaurantId chain and dead RestaurantPortal removed; dead RESTAURANT_MANAGER role (governmentid-service). **Pending deploy:** food-delivery-app-ui (checkpoint33 earnings + campaigns), campaign-service, wallet-service; governmentid-service optional. Dev data unchanged: 0 advertiser profiles. Product gap for the user: no campaign can reach ACTIVE (creative moderation has no caller). See [checkpoint34](checkpoints/34-campaigns-onboarding-and-ad-money.md).

## Latest: checkpoint33 (2026-10-03T09:55:00+05:30)

Restaurant earnings never loaded (dashboard restaurantId is always ""); fixed locally, **UI deploy pending**. Campaigns cannot work end to end: no advertiser profile exists or can be created; **decision pending with the user**. See [checkpoint33](checkpoints/33-restaurant-earnings-and-campaigns.md).

## Latest: checkpoint32 (2026-10-03T09:47:00+05:30)

Reviews and support: proven live, no product defects, no deployment needed. bb43e2a4 now carries four immutable reviews (do not reuse it for a review flow). Reviews validator 85/85. See [checkpoint32](checkpoints/32-reviews-and-support.md).

## Latest: checkpoint31 (2026-10-03T09:32:00+05:30)

Deployed and verified: UI `6eb1743` (order-money panel), customer `07ea84f`, chat `089e5eb`, payment-gateway `5220632`. **No deployment pending. The priority list is complete** (table in [checkpoint31](checkpoints/31-priority-list-complete.md)). Owned orders: 0554f250, d3acfc93, bb43e2a4, cf608115, 19359711 (cancelled), bf109947 (rejected). Riders OFFLINE.

## Latest: checkpoint30 (2026-10-03T09:05:00+05:30)

Deployed unchanged since 29. **Pending:** food-delivery-app-ui (admin order-money panel shows payment, refunds and booked amounts). New owned orders: **19359711** (customer-cancelled, refunded 53.53) and **bf109947** (restaurant-rejected, refunded 53.53). MONEY-05 is red on the deployed UI as expected. See [checkpoint30](checkpoints/30-admin-order-money-outcomes.md).

## Latest: checkpoint29 (2026-10-03T08:50:00+05:30)

Deployed and verified: customer `07ea84f`, chat `089e5eb`, payment-gateway `5220632`, UI `766b214`. **No deployment pending.** Isolation, CHAT-22 and REFUND-RETRY-01 all pass live. Owned orders: 0554f250, d3acfc93 (support outcomes, isolation fixture), bb43e2a4 (60-message chat), **cf608115** (history paging + ₹1.13 refund declined once, retried, COMPLETED). Rider 7000000026 OFFLINE. See [checkpoint29](checkpoints/29-deployed-priority-runs.md).

## Latest: checkpoint28 (2026-10-03T08:10:00+05:30)

Still deployed: customer `cc04ed7`, UI `41578ee`; chat `7cffcad` and payment-gateway `eea4bfe` unchanged. **Pending deployment (checkpoints 27 + 28):** customer-service, chat-service, payment-gateway, food-delivery-app-ui. New since 27: the Dev refund failure seam (₹1.13 declined once) and the Money Operations **Failed Refunds** tab (the admin UI had no retry screen). See [checkpoint28](checkpoints/28-admin-retry-seam-and-ui.md).

## Latest: checkpoint27 (2026-10-03T08:00:00+05:30)

Deployed: customer `cc04ed7`, UI `41578ee` (unchanged). **Pending deployment:** customer-service (order read 404), chat-service (history page bound) and UI (load earlier messages). Live isolation holds for chat and refunds; the order read answered 500 to an outsider (fixed locally). Long chat history was unreachable past 50 (fixed locally; CHAT-22 red on the deployed UI as expected). bb43e2a4's chat now holds 60 messages (40 seeded "E2E history …", retained). Admin retry live is not reachable in Dev (decision asked). See [checkpoint27](checkpoints/27-priority-chat-isolation-history.md).

## Latest: checkpoint26 (2026-10-03T07:40:00+05:30)

**Deployed and verified:** UI `41578ee` (support-button fix) and customer-service `cc04ed7`. **No deployment pending.** The button is hidden after the two-hour chat window (CHAT-REFUND-05 PASS on 0554f250 and d3acfc93) and still opens the quote form inside it (delivered follow-up PASS on new order **bb43e2a4-5e05-4a7c-9e0d-7f3d5e7c9fb4**: DELIVERED, quoted, no tickets or refunds, ledger 115.46 balanced). Rider 7000000026 OFFLINE. The fresh lifecycle itself failed at :732 on an unexplained 5s timing miss (see [checkpoint26](checkpoints/26-ui-support-button-verified.md)).

## Latest: checkpoint25 (2026-10-03T04:40:00+05:30)

**Deployed and verified:** customer-service `cc04ed7` (the checkpoint24 fix). No data reset. **SupportRefundResolutionFlowTest PASS 3/3** on fresh order **d3acfc93-7aa2-4aae-aad0-7a9711c31b8d** (delivered 2026-10-02T22:59:03Z by a resumed lifecycle). The refusal is now answered and committed; 0 dead letters and 0 rollback-only errors since the deploy.

Owned orders: d3acfc93 (denial 005c08a4, award a301b711 → refund d3e4b73c COMPLETED ₹12.00, CLAWBACK 4.28, item consumed, two refusals answered) and 0554f250 (the same outcomes from checkpoint24; outside the chat window). Rider 7000000026 OFFLINE.

**Local, pending the user:** FoodDeliveryAppUI dead-button fix ("Something wrong with this order?" shown only while the order chat is offered). **Decision pending:** the two-hour support window is UI-only; the backend accepts refund requests at any age. See [checkpoint25](checkpoints/25-support-refunds-pass-deployed.md).

## Latest: checkpoint24 (2026-10-02T23:58:00+05:30)

**Deployed and verified:** the checkpoint23 gate (customer `2b103b2`, gateway `3074b96` + its served config, UI `08d086d`, CommonLibrary published). **The Dev data was reset with that deploy:** checkpoint23's orders and ticket are gone; actors keep their UUIDs.

**Owned order:** `0554f250-eade-4677-8e51-18f4aacd22f5`. Customer 8000000484, restaurant 9000000001, rider 7000000026 (OFFLINE), Brand 1 Outlet 3, CARD ₹53.53, one item `aa2d832f` ×1. DELIVERED by a fresh full lifecycle (PASS). Ticket 054a652b REJECTED (denial), ticket 25ac7a74 RESOLVED with refund fe60ce55 COMPLETED ₹12.00 (restaurant fault, CLAWBACK 4.28), intent PARTIALLY_REFUNDED, ledger balanced at 131.74, 0 rejections. The item is consumed: any new quote for it must answer ITEM_ALREADY_REFUNDED.

**Support refunds live:** denial PASS, reduced award PASS (proves the checkpoint23 item-refund fix). SUPPORT-REFUND-03 FAIL: the refusal dead-lettered (`chat-events.DLT` p0 o0) instead of answering. Root cause is a refusal thrown through a `@Transactional` proxy inside the listener's transaction (rollback-only). The same defect is in OrderEventConsumer and PaymentEventConsumer refund routing refusals. **Fixed locally in CustomerApplication; 465/92 clean test green; pending deployment of customer-service.** See [checkpoint24](checkpoints/24-support-refunds-live-and-refusal-rollback.md).

## Latest: checkpoint23 (2026-10-02T22:15:00+05:30)

The ledger rejection a7862d1a is resolved. Support ticket 2f5d7acf (c463191b, item quote 17.91) is **OPEN**: its approval failed on the refund_items defect and rolled back. No support refunds exist yet. **Pending: deploy customer-service** (migration V20261002220000), then rerun SupportRefundResolutionFlowTest (the command is in checkpoint23).


## Latest: checkpoint22 complete (2026-10-02T21:35:00+05:30)

Deployed and verified: customer 8b10f5a, delivery 8429333, ledger 1e256ef (Flyway 20261002210000), UI a7b204f, all healthy. Owned orders after the reset: b7d01530 (cancelled, refund COMPLETED), 881ba9a0 (rejected, refund COMPLETED), 7f7af6a5 (DELIVERED; earnings posted by the approved DLT replay; strict money and quote PASS), c463191b (DELIVERED by the fresh full lifecycle PASS; posted directly). One ledger rejection row (a7862d1a) remains unresolved as the audit record of the pre-fix failure. Rider 7000000026 OFFLINE. **No deployment pending.**


## Latest: checkpoint22 (2026-10-02T21:00:00+05:30)

- 7f7af6a5 is **DELIVERED** (resume passed). Its delivered ledger distribution (tx d1d5d411, event a7862d1a) is **rejected**, at ledger-events-dlt p0 o0, so the restaurant and rider payables are not posted. Root cause: the ledger unique constraint per (tx, account, direction). The fix with migration V20261002210000 is local; **deploy ledger-service** pending.
- Rider 7000000026 should be idle after delivery; recheck it is OFFLINE.


## Latest: checkpoint21 (2026-10-02T20:05:00+05:30)

- The data was reset after the checkpoint20 deploy (verified). Fresh owned orders: b7d01530 (CANCELLED, refund COMPLETED) and 881ba9a0 (CANCELLED_BY_RESTAURANT, refund COMPLETED), both PASS. 7f7af6a5 is **ACCEPTED/ASSIGNED, rider 7000000026 ON_DELIVERY**; the happy flow failed at the rider's active trip. Manifests are in fixtures/.
- Root cause: the rider `/orders/active` only read the customer service's copy, which trails DRIVER_ASSIGNED by about 2s. The fix is local (customer + delivery + UI types). **Pending deployment: customer-service, delivery-service**, and the UI optionally. No migration. See [checkpoint21](checkpoints/21-fresh-flows-and-rider-active-gap.md).


## Latest: checkpoint20 (2026-10-02T19:10:00+05:30). Supersedes the fixture sections below.

- Review found the checkpoint19 recovery endpoint unfit for production and the checkpoint18 admin retry unable to complete. User decisions: delete the endpoint, and fix the retry in both services. Done locally, uncommitted. See [checkpoint20](checkpoints/20-review-and-refund-retry-fix.md) and [evidence](evidence/20-local-results.json).
- **The user will delete all Dev data and reseed.** The owned orders, refunds and chat session listed below are known-inconsistent and will be erased: 3 FAILED refunds with no capture transaction; order b83c71bd is DELIVERY_FAILED in customer_db but DELIVERED in delivery_db, with an accidental refund; its delivery ledger distribution is missing; chat quote requests sit in chat-events.DLT; `refund_req` keys are held. Do not resume them. Recheck seeded actors after the reset.
- Pending deployment: payment-gateway, customer-service (new migration `V20261002190000`), chat-service (test-only) and UI. Checkpoint18 deployment completion is still unconfirmed.


Latest update 2026-10-02T18:27:17+05:30: checkpoint19 stored-confirmation capture-recovery source is ready locally in PaymentGatewayIntegration,58selected checks passed. Payment-gateway working tree now contains this undeployed batch; previous clean-HEAD wording below is historical checkpoint17 only. Checkpoint18 customer/chat/UI deployment was acknowledged but completion is pending. Current read-only owned snapshot is evidence/18-retained-recovery-read.json: three FAILED refunds, no capture Transactions, three stored payment confirmations, riderOFFLINE. No live recovery occurred. See checkpoints/19-stored-confirmation-capture-recovery.md and DEPLOYMENT-GATE.md.

## Last confirmed deployed versions (checkpoint17)

At checkpoint17, payment-gateway HEAD/image was0a0a227fa97e455d90aba2d7f6e9daa1bd558729 and customer-service image was4efcf76d9477083dd736c55bcc86b395512f3f1e; customer/payment/chat/ledger were healthy. These are the last confirmed versions, not a checkpoint18/19 confirmation. PaymentGatewayIntegration now has the local capture-recovery batch. Customer/chat/UI deployment completion remains unconfirmed. Reinspect build/image health after the user confirms each gate.

## Owned actors

| Role | Phone | UUID |
|---|---|---|
| Customer | 8000000484 | 99d619e3-1470-46e8-9e9b-faeced724757 |
| Restaurant | 9000000001 | 426f016b-c98e-43c1-b464-94e1a587f6d3 |
| Rider | 7000000026 | 5a41832f-5596-4bb1-a9bf-6ce416b38b95 |
| Admin | 1000000001 | 4e1f984b-ff9f-5af8-82ff-a5923096f1f1 |

Outlet: Brand 1 Outlet 3; existing Home address. Rider is authoritatively OFFLINE. Manifests are in fixtures/ and must be checked against current ownership/state; these are retained, not disposable cleanup targets.

## Owned orders and current blockers

All three CARD orders total ₹43.35. Latest read-only refund/rider/capture/confirmation evidence is [checkpoint18 recovery read](evidence/18-retained-recovery-read.json); broader historical deployment/payment details remain in evidence/16-deployed-state.json and evidence/16-owned-payment-state.json. Both customer and gateway intents are SUCCESS, but the gateway has no capture transaction or gateway refund rows for these three historical orders; each amountRefunded is zero. A database reset is unnecessary and would erase these retained validation fixtures. The latest user asked whether to reset; the answer was no. No recovery mutation has been executed.

| Purpose | Order ID | Current state | Refund ID/state |
|---|---|---|---|
| Canonical delivered chat/money/quote | b83c71bd-022c-439d-8586-aa4fe5b1c0d1 | Customer DELIVERY_FAILED / FAILED despite deliveredAt; delivery service RELEASED / DELIVERED | Accidental automatic cae2548a-5c41-47a0-93d0-c3c2732cd93a, FAILED, attempts4, no completion |
| Customer cancellation | 0b22b53a-c718-4bec-b854-b736e24bee29 | CANCELLED | 61893082-a1fa-47ac-91a2-fde3669c26e9, FAILED, attempts4, no completion |
| Restaurant rejection | 176fb42c-46e9-49b2-b52f-43c571603199 | CANCELLED_BY_RESTAURANT | 1b62e2a8-4aa3-4e1a-bd1c-a8f2c394d8cd, FAILED, attempts4, no completion |

Delivery completed at 2026-10-02T08:41:08.141848Z. Customer AbandonedDeliverySweeper selected the old HANDED_OVER order at 10:45:29Z, ignoring deliveredAt/DELIVERED, failed it and triggered the accidental refund. Source protections are now deployed, but do not undo existing corruption. Preserve the audit. The latest quote invocation failed its DELIVERED prerequisite before requesting a quote; do not rerun it unchanged while that prerequisite is false.

Expected original delivered economics: restaurant ₹9.49, rider ₹18.86. Missing delivery transaction bf4406d0-9ce1-5811-a655-214f75a36503; source event 1173299a-c864-4712-8717-fb664a03e118. Captured ledger transaction 98dc631a-b9b7-5d33-abc2-010e14212e0d alone is insufficient. PostgreSQL now stores the rejection; resolving it is not booking its movement. Original command used wrong CUSTOMER_CREDIT funding and unordered legs; blind replay remains invalid.

Chat session b256d51f-0153-4f65-9e8c-571ae9819886. Original request outbox 7fa7095e-b04c-4b3e-97d3-c46992d53a9d is PROCESSED after publisher repair. Raw quote requests were in chat-events.DLT (observed partition 0 offsets 3/4); no quote response/ticket/refund was submitted by quote checks. Both raw-payload listener fixes are deployed; real quote roundtrip remains unverified because the owned delivered fixture is now corrupt.

## Latest verification

- 81 unique selected backend checks passed: ledger 16/customer 47/payment 18, zero failures/errors/skips, including previously missed LedgerDltRejectionTest, startup, actual producer-stub consumers, real EventBinder, HTTP webhook integration and committed local H2 capture/partial/full/duplicate-refund checks. These are affected checks, not a full build_verify/full-suite claim.
- Earlier 129 selected local financial/wire checks passed but omitted the existing LedgerDltRejectionTest. User caught the omission; 16 direct ledger dependencies then passed. Do not present 129 as complete dependency coverage.
- Admin money API/UI now200 and exact quoted arithmetic matches payout history, but posted delivered payables are absent. Strict retained money invocation failed missing RESTAURANT_PAYABLE in 16.33s before a quote.
- Distinct cancellation/rejection terminal UI outcomes passed, then whole invocations failed refund completion. They are not passed E2E flows.
- No new lifecycle, financial recovery mutation, server cleanup, commit/push/deploy was performed by the last continuation. New deployment confirmed here; next task is source-grounded controlled recovery, not another order.


## Latest deployment and bounded handoff

Verified 2026-10-02T17:44:47+05:30: amount guard deployed as 0a0a227fa97e455d90aba2d7f6e9daa1bd558729, running/healthy; repository clean. Earlier 32 affected local checks passed; those results remain local proof, not live financial recovery. [Latest read-only snapshot](evidence/17-deployed-confirmation.json). Checkpoint18 customer/chat/UI deployment is pending.

Three retained refunds still FAILED (attempts4/no completedAt); delivered-order status mismatch persists; rider OFFLINE. No reset/reseed, cleanup, new lifecycle or financial mutation performed. Targeted recovery and delivered ledger/status correction are still open. User requested a bounded continuation with roughly 6% usage remaining and may switch agents; this checkpoint preserves the exact resumption state. No new code or repeated known-invalid E2E was attempted in this limited pass.

Next: [ordered recovery](NEXT-STEPS.md), [checkpoint17](checkpoints/17-deployment-confirmation-and-budget-handoff.md). User deployment authorization/workflow restrictions and deferred scopes remain unchanged.


## Active checkpoint18 — limits reset; source batch ready

User resumed the work after limits reset. [Checkpoint18](checkpoints/18-refund-retry-ui-chat-history.md) records transactional selected-refund queueing, refund UI read/retry/refresh, newest stable chat history, and 120customer/22chat/30UI local passes plus typecheck/lint. These changes are **not deployed yet**. [Deployment handoff](DEPLOYMENT-GATE.md): customer-service,chat-service,UI only; no reset/seed/config/migration. No live financial mutation, new lifecycle, retained-record repair, cleanup or auto publish/deploy occurred. Last authoritative owned state remains checkpoint17; do not report it as a fresh snapshot from this local-only batch.

Open: safe gateway initiation reconciliation/confirmed historical capture, automatic sweeper destination/race path, delivered ledger/status correction, real quote/support financial outcomes and native chat aggregate/isolation/media proof. New retry queues the selected refund but does not bypass held gateway keys or prove returned money. Resume NEXT-STEPS and record future results here under AGENTS.md.
