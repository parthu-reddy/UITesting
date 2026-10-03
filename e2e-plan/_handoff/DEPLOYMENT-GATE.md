# Deployment gate — checkpoint 18

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
