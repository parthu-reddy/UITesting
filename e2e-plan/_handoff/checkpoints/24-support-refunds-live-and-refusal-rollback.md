# Checkpoint24: support refunds live; a refusal crossing a transactional proxy lost its reply

Updated 2026-10-02T23:58:00+05:30. A new agent resumed from the checkpoint23 screenshot ("Deploy customer-service, the gateway config and the UI. Publish CommonLibrary. After that I'll rerun the support-refund test"). The user reaffirmed: never assume, always check the code; production-ready; **never commit or push** (permanent).

## Checkpoint23 gate: verified deployed (read-only)

- Running images, all healthy: customer-service `2b103b2`, api-gateway `3074b96`, food-delivery-app-ui `08d086d` (full redeploy at 2026-10-02T17:28:58Z per `Deployment/DEPLOY_LOG.md`).
- Gateway config: the file Config Server serves (`/config/api-gateway.yml`) is byte-identical to local `Deployment/api-gateway.yml` (md5 `12d39284…`). Live probe: `POST /api/v1/customer/orders/{id}/refund-request` → **404** (route deleted); `/api/v1/money/…` unauthenticated → 401.
- customer_db: flyway `20261002220000` success; `refund_items` columns `id, refund_id, order_item_id, quantity, created_at` (amount dropped).
- CommonLibrary: "Publish to GitHub Packages" run 37035377453 succeeded on `d43af60`.
- Note: the VM's own `Deployment` git checkout is behind (`2d12b72`, older env pins), yet the right images run. Nothing depends on it here; recorded only so nobody reads pins from it.

## The Dev data was reset with that deploy

Every database's `flyway_schema_history` rows were installed at 17:32Z; 554 users, 504 customers, 104 outlets, 34 riders (all OFFLINE), **0 orders**. Checkpoint23's fixtures (c463191b, 7f7af6a5, ticket 2f5d7acf) no longer exist, so its recorded rerun command could not run. Manifests moved to `fixtures/historical-pre-reset-2/`. Actors keep their UUIDs (deterministic seed).

## Runs

1. **Fresh `HappyDeliveryFlowTest#completeOrderLifecycle`** (no resume): **PASS** 1/1, 0 skip, 397.5s. Order **0554f250-eade-4677-8e51-18f4aacd22f5** (Brand 1 Outlet 3, CARD ₹53.53, one item). DB: HANDED_OVER/DELIVERED, 18 ledger entries balanced at 115.46, 0 rejections, rider OFFLINE. Console noise explained from source: the 503 on `POST /chat/sessions` is `OrderChatChecks`' deliberate first-POST retry fixture (nginx/gateway saw only the 200 retry); the 409 on `delivery-availability` is the correct "no free partner" once the only online rider was assigned. [Evidence](../evidence/24-fresh-happy-lifecycle.json).
2. **`SupportRefundResolutionFlowTest`, now on one order** (`-Dsupport.order.id=0554f250…`; see Decision): 3 tests, **2 PASS, 1 FAIL**, 91s. [Evidence](../evidence/24-support-refund-run.json).
   - SUPPORT-REFUND-02 denial: PASS. Ticket 054a652b REJECTED with notes and resolver; no refund; payment status and ledger line count unchanged.
   - SUPPORT-REFUND-01 reduced award: PASS. Ticket 25ac7a74 RESOLVED 12.00; refund fe60ce55 COMPLETED, ORIGINAL_METHOD, RESTAURANT_FAULT, completedAt set; intent PARTIALLY_REFUNDED; REFUND 12.00/12.00; restaurant CLAWBACK 4.28 = 19.11 × round4(12/53.53); 22 entries balance at 131.74. **This proves the checkpoint23 item-refund fix live.**
   - SUPPORT-REFUND-03 already-refunded item: **FAIL**. No chat reply within 20s.

## Defect: a business refusal dead-lettered instead of answering

Trace (chat_db, customer logs): the quote request was persisted and published; `ChatRefundProcessorService` logged `ITEM_ALREADY_REFUNDED`, then `UnexpectedRollbackException: Transaction silently rolled back because it has been marked as rollback-only` on every attempt, then `DLT_RECORD_WRITTEN … replay={"dltTopic":"chat-events.DLT","partition":0,"offset":0}`. Cause: `RefundService.quote` is a `@Transactional` proxy method; it joined the listener's `TransactionTemplate` transaction, and its exception marked that transaction rollback-only even though the listener caught it. The CHAT_REFUND_ERROR reply and the idempotency key rolled back with it. The existing unit test mocks both the TransactionTemplate and RefundService, so it could not see this.

**Whole-set check.** A scan of every service for the shape (transactional context, try calling another transactional bean, catch without rethrow), first proven on the HEAD file, plus reading each hit, found the same defect in **`OrderEventConsumer`** (through `requestRefundWithoutPoisoningTheEvent`, called inside the transaction lambda) and **`PaymentEventConsumer`**. A refund *routing refusal* (`REFUND_STATE_INVALID` for an INITIATED card intent, `REFUND_EXCEEDS_REMAINING`) was caught "so the state change is kept", but it would have rolled the restaurant cancellation, delivery failure or payment-state change back and dead-lettered the event. The other candidates were classified non-defects ([local results](../evidence/24-local-results.json)). The scan is kept at [tools/scan_rollback_only.py](../tools/scan_rollback_only.py) (5 remaining candidates, all classified).

## Fix (CustomerApplication, local, uncommitted)

- `ChatRefundProcessorService`: each handler validates and prices **outside** any transaction, then writes its reply (and, for a request, the ticket) plus the idempotency key in one transaction. The open-ticket check stays inside that transaction. The private `@Transactional` `publishErrorEvent` (ineffective on a private method, and it swallowed save failures) is replaced by `errorEvent`/`chatSessionEvent`; a reply that cannot serialize now throws.
- `RefundService.request` is split into a write-free `plan()` (validation, idempotent replay, item quote, remaining, routing) and `record()` (writes). New `requestUnlessRefused(cmd)` returns a pre-write refusal instead of throwing it; anything after the refund row is written still throws and rolls the caller back. `request()` behaves as before (same refusal messages).
- `OrderEventConsumer` and `PaymentEventConsumer` call `requestUnlessRefused` and log `REFUND_NOT_ROUTED`; their catch blocks are deleted.

## Verification (local)

- `ChatRefundRefusalPersistenceTest` (new, real proxies, H2): **red before the fix** with the production exception (2 of 3; the control passed); green after.
- `RefundRefusalInCallerTransactionTest` (new, 4): the trap as a control (`UnexpectedRollbackException`, state not kept), returned refusal keeps the caller's state, accepted refund written with it, post-write failure still rolls back. Mutations seen red: refusal thrown again (error), post-write failure converted (failure).
- `OrderEventConsumerRefundRefusalTest` (new, 2): seen red when the consumer is reverted to `request()`+catch ("Wanted but not invoked").
- `PaymentEventConsumerTest` updated to the new API (its "must not throw" case had passed only because RefundService was a mock).
- Customer **full `clean test`: 465 tests in 92 classes, 0 fail/error/skip** (456/89 + the 9 new tests in 3 classes).
- Money audit 0/23 (clean); readiness 59/61 without `--with-tests` (4.1/5.4 pre-existing); lifecycle 68/68; validate_core_services 50/56 with the same 6 pre-existing findings.

## Pending

1. **The user deploys customer-service** (no migration, no config, no UI). Verify the image and health.
2. Rerun `SupportRefundResolutionFlowTest` on a delivered order with no tickets or refunds. 0554f250 now carries the denial and the award, so 02 and 01 cannot rerun on it. Either run one fresh happy lifecycle and the full class on it, or run only SUPPORT-REFUND-03 on 0554f250: `-Dtest='SupportRefundResolutionFlowTest#refundedItemCannotBeRefundedAgain' -Dsupport.order.id=0554f250-eade-4677-8e51-18f4aacd22f5`. Its precondition (exactly one COMPLETED award, a RESOLVED ticket) holds today.
3. The chat-events.DLT p0 o0 record stays as the audit of the pre-fix failure; replaying it is not needed.
