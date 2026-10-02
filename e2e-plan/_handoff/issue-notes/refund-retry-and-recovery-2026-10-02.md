# Refund retry that could never complete, and repair code that should not have been written

Recorded 2026-10-02 (e2e-plan checkpoint20). Each item below was confirmed in source, and each fix has a test that fails when its defect is put back.

## Mistakes

1. **A retry that reuses an identity a downstream guard has already consumed is a no-op.** The admin "retry refund" (checkpoint18) re-sent the same refund id. PaymentGatewayIntegration's `PaymentEventConsumer` stores `refund_req:<refundId>` after a successful initiation and keeps it forever, so the retry was logged as a duplicate and dropped. The UI still said "queued". Unit tests on the customer side passed because none of them followed the message to the consumer. **Fix:** `WebhookProcessingService.handleRefundFailure` releases the key in the same transaction that records the definitive FAILED outcome. An attempt still waiting for its outcome stays protected.
2. **A retry budget that never resets turns a retry into a delayed failure.** `retryStuck` fails a refund once `attempts > 3`. The admin retry deliberately kept `attempts` as history (4 → 5), so the sweeper failed it again on its first pass, 5 minutes later. **Fix:** keep `attempts` as the full history and add `sweep_attempts` (migration `V20261002190000`), which an admin retry resets to 0.
3. **Copy-pasting the outbox block hid a destination bug.** The gateway-refund outbox code existed three times. The sweeper's copy sent `PAYMENT_REFUND_REQUESTED` for STORE_CREDIT refunds, asking a payment gateway to return money it never took. **Fix:** one `enqueueGatewayRefund` helper, and the sweeper routes by destination (store credit goes to WalletService under the same refund id).
4. **Repair code for test data.** A permanent ADMIN endpoint was written to rebuild capture records for three Dev orders. Nothing is in production, so the right move is to reset the Dev data, not to ship repair code. The endpoint also relied on the outbox as an evidence store, but processed outbox rows are deleted after 7 days. **Rule:** before writing recovery code, ask whether the broken state can exist in production at all. If it can only exist in dev data, ask the owner to reset.
5. **A hand-rolled poller next to a shared one.** `useOrderRefunds` reimplemented the `setTimeout` loop that `src/hooks/usePolling.ts` exists to replace, and it polled every 15 s forever, even for orders with no refunds. **Fix:** use `usePolling` and poll only while a refund is REQUESTED or PROCESSING.
6. **The shared poller itself could run two chains.** If the request key changed while a timer-triggered request was in flight, the old chain saw the new effect's `isSubscribedRef === true` and scheduled again (6 requests where 3 were expected). The first request also overlapped the next poll. **Fix:** a per-effect `cancelled` flag, and each poll is scheduled only after the previous request settles.
7. **A test that does not fail on the defect proves nothing.** The first duplicate-chain test passed against the old hook, because it changed the key during the *initial* request, which the old code handled. It was rewritten to change the key during a timer-triggered request, and only then did it go red against the old code.

## Pre-existing gaps noted, not fixed here

- Real-provider refund callbacks: Razorpay's webhook passes the provider refund id (`rfnd_…`) as `refundId`, but CustomerApplication parses it as a UUID. Mock gateways pass our id, so Dev works.
- Chat `GET /sessions/{id}/messages` has no upper bound on `size`.
- Chat `OpenApiGenerationTest` had been red since `ddb8264` (missing `ChatEventBroadcaster` mock). Fixed in checkpoint20.
- Readiness gate 4.1 (single-method persistence tests) and 5.4 (greps a URL literal that the typed client now hides) are stale or pre-existing. They are not regressions.
