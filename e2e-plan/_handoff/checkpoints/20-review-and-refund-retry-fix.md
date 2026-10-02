# Checkpoint20: review of checkpoints 18/19, recovery endpoint removed, refund retry made to work

Updated 2026-10-02T19:10:00+05:30. The user asked for a review of the checkpoint18/19 changes against production standards before work resumes. All changes are local and uncommitted; the user commits, pushes and deploys.

## User decisions (this session)

- **Delete the payment-gateway capture-recovery endpoint** (checkpoint19). It is gone, with its 3 test classes, and the 2 test edits are reverted. Nothing referencing it remains in the workspace.
- **Fix the admin refund retry across customer service and payment gateway.**
- **Nothing is in production. The user will delete all data and reseed with dummy data** when told there is inconsistency. This supersedes the earlier "no reset, resume the exact retained orders" rule for the three fixtures below. Verify the fixes on fresh owned orders after the reset.

## Review findings (each confirmed in source)

1. The recovery endpoint was repair code for three Dev rows and read its source data from the outbox, which `OutboxProcessor.cleanupOutboxEvents` (daily, 00:00 UTC) deletes once PROCESSED rows are 7 days old. It wrote a second capture-reference scheme, and it could not complete a refund anyway (finding 2).
2. The checkpoint18 admin retry could not complete a card/UPI refund. The gateway's `refund_req:<refundId>` key, held after initiation, dropped the retry as a duplicate. And `retryStuck` failed the refund again on its first sweep, because `attempts` (already 5) is over 3.
3. The gateway outbox enqueue was copied three times. The sweeper's copy sent STORE_CREDIT refunds to the payment gateway.
4. `useOrderRefunds` hand-rolled polling instead of using `usePolling`, and polled every 15 s forever, even with no refunds.
5. The chat newest-first change is correct. Its only consumer (`useChatSession.ts`) re-sorts page 0.

## Changes

- **PaymentGatewayIntegration**: deleted `AdminCaptureRecoveryController`, `ConfirmedCaptureRecoveryService` and 3 tests. `WebhookProcessingService.refundInitiationKey()` is now the single source of the key name. `handleRefundFailure` deletes the key in the same transaction as the FAILED outcome. 3 test constructors updated, plus a round-trip test (initiate → sweeper ping dropped → definitive failure → admin retry initiates again).
- **CustomerApplication**: `enqueueGatewayRefund` helper used by `request`, `retryFailed` and `retryStuck`. The sweeper routes by destination (STORE_CREDIT → `WALLET_CREDIT_REQUESTED`, same refund id) and uses attempt-scoped keys `refund_sweep:<id>:<attempts>`. New `sweep_attempts` budget (`MAX_SWEEP_ATTEMPTS = 3`), reset by an admin retry; `attempts` stays as history. **New migration `V20261002190000__refund_sweep_attempts.sql`.**
- **FoodDeliveryAppUI**: `useOrderRefunds` is rebuilt on `usePolling` and polls only while a refund is REQUESTED or PROCESSING. In `usePolling`, each poll is now scheduled after the previous request settles, and a per-effect cancel flag fixes a duplicate polling chain on key change (proven: 6 requests vs 3).
- **CommunicationService**: test-only. `OpenApiGenerationTest` was missing its `ChatEventBroadcaster` mock and had been red since `ddb8264`.

## Verification

Exact counts and the guards seen red are in [evidence/20-local-results.json](../evidence/20-local-results.json): customer 176/34 classes, gateway 40/14, chat 24/8, UI 737/121 files plus typecheck and lint, all with 0 failures. Every new test was observed failing with its defect reintroduced. The money audit (0/23) and lifecycle gate (68/68) are clean; readiness failures 4.1 and 5.4 are pre-existing or stale. This is local proof only: nothing is deployed, and there is no live financial proof.

## Known gaps (not fixed)

- If the provider never sends an outcome (lost callback, or our callback processing crashed), the `refund_req` key stays held, so a retry is still dropped. That is the safe choice, because re-initiating an unknown outcome risks paying twice. It needs provider refund-status reconciliation, which doesn't exist.
- Razorpay's real webhook passes the `rfnd_` id where the customer service expects our refund UUID. Mocks are unaffected.
- Chat history `size` is unbounded.

## Next

1. The user commits and deploys payment-gateway, customer-service (including the migration), chat-service (test-only change) and the UI, and the checkpoint18 batch if it isn't already deployed.
2. The user resets and reseeds the Dev data. Confirm the seeded actors from TEST-DATA.md exist afterwards.
3. Run the existing cancellation/rejection/happy flows **without** the `resume`/`refund.resume` properties, so they create fresh owned orders. Then verify refund COMPLETED, payment REFUNDED, a balanced REFUND ledger and the customer UI's refund state.
