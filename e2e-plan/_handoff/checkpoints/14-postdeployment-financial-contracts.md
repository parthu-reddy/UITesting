# Postdeployment chat/refund/settlement contracts — 2026-10-02

This checkpoint uses the same seeded actors: customer 8000000484, restaurant 9000000001, rider 7000000026, admin 1000000001. No cleanup, publishing or deployment was performed. The final authoritative rider state is OFFLINE.

## Deployed evidence

- The original chat outbox event 7fa7095e-b04c-4b3e-97d3-c46992d53a9d is now PROCESSED, retries 0. The startup publisher fix is verified operationally.
- Admin money GET/UI for retained delivered order b83c71bd-022c-439d-8586-aa4fe5b1c0d1 returns HTTP 200. Actual ₹43.35 total, ₹9.49 restaurant payout and ₹18.86 rider payout agree with stored order/rider history. This does not prove posted earnings.
- Quote rerun first exposed an ambiguous transient “Requesting quote…” assertion. Removed it while retaining new-response/type/amount/action checks. The corrected rerun failed because no quote response arrived. Chat requests are published, but customer-service rejects the raw Kafka record before the old OutboxEvent listener can be called; matching quote events reached chat-events.DLT. No support ticket or refund was submitted by these quote checks.
- Customer cancellation created exactly one owned order 0b22b53a-c718-4bec-b854-b736e24bee29. Cancellation POST, exact tracker/headline, hidden cancellation action and removal from restaurant queue passed. Its ₹43.35 ORIGINAL_METHOD refund 61893082-a1fa-47ac-91a2-fde3669c26e9 remains PROCESSING, completedAt null (attempts 3 at final snapshot). Whole invocation failed at completion, not passed.
- Restaurant rejection created exactly one distinct order 176fb42c-46e9-49b2-b52f-43c571603199. Required reason/Back, rejection POST, retained customer reason/history and hidden cancel action passed. Its ₹43.35 ORIGINAL_METHOD refund 1b62e2a8-4aa3-4e1a-bd1c-a8f2c394d8cd remains PROCESSING, completedAt null (attempts 1 at final snapshot). Whole invocation failed at completion, not passed.
- Payment logs confirm the Dev Razorpay callback fails because it passes a nested paise payload to a normalized top-level rupee handler.
- The delivered money response contains only the two payment-capture ledger lines. DELIVERED command bf4406d0-9ce1-5811-a655-214f75a36503 was published but is in ledger-events-dlt. Rejection persistence fails with PostgreSQL jsonb/varchar binding errors; retries accumulate exception headers until RecordTooLargeException. No selected customer/restaurant/rider ledger accounts exist in the final read-only balance snapshot. Source also exposes customer-wallet debits after capture and unordered payable deductions. These are separate from the repaired admin HTTP 500.

## Source fixes ready for deployment

CustomerApplication: consume raw Kafka payload plus eventId/eventType/session key for chat refunds; serialize refund completion/failure using locked refund and payment rows; update cumulative payment status on actual completion; distribute already captured funds from clearing and fund restaurant/rider payables before deductions.

CommunicationService: matching raw-payload listener for quote/decision/error responses; preserve real event ID and session key; propagate storage/broadcast failure to Kafka recovery instead of acknowledging a lost response.

PaymentGatewayIntegration: mock Razorpay emits normalized per-refund INR amount; all successful refund results include isSuccess=true and PAYMENT_REFUNDED (also for partial amounts); failures include isSuccess=false. The cumulative intent still distinguishes PARTIALLY_REFUNDED/REFUNDED.

LedgerService: bind rejection payload as JSON, retain malformed/empty input in a JSON audit envelope, use the existing transactional terminal-key recorder for duplicate protection, and bound DLT logs/reasons instead of printing entire payload/header stacks.

UITesting: shared delivered money checks now require real restaurant/rider ledger entries and compare posted net amounts with exact-order payouts. Capture-only balanced rows cannot pass. No duplicate lifecycle added. The strict read-only retained rerun failed in 16.33 seconds at missing RESTAURANT_PAYABLE lines, confirming that this gap is detected before any additional quote request.

## Local verification and limits

129 unique focused backend invocations passed with zero failures/errors/skips across the four repositories. Two existing customer quote/ticket tests and three response cases use Spring Kafka's real record/message adapter, not just a handcrafted OutboxEvent. The mock callback is executed immediately with an injected scheduler: no intentional timer wait. Existing repository tests cover rejection JSON round trip, rollback separation and duplicate key behavior on H2 in PostgreSQL mode; the Oracle PostgreSQL runtime rerun is still required. E2E source compilation and repository diff checks passed. Local tests are not deployed financial settlement proof.

## Next checkpoint

Deploy/restart ledger-service, customer-service, chat-service and payment-gateway using unchanged user workflows. No CommonLibrary, UI, Config Server or migration is required for this batch. Prefer ledger-service first to stop the observed DLT persistence retry loop. Preserve the database and retained records.

After deployment, inspect the retained refunds before acting: automatic attempts may move them to FAILED while awaiting deployment. Resume the exact owned order IDs with -Drefund.resume.order.id; do not create replacement cancellation/rejection lifecycles. Existing failed events and malformed delivered ledger payloads are not repaired by restart: they need exact, audited recovery/rebuilt producer commands with the same deterministic identities. A blind replay of the old DELIVERED payload retains its incorrect wallet funding/ordering. Do not mark a rejection resolved until posting is proven.

Then complete the retained quote, controlled support decisions (approve/partial/deny and actual money states), and ownership/isolation checks. SSE, expiry/rate waits remain deferred. Support decision integration is still unverified. Existing AdminDlqService.retryRefund resets a failed refund then calls the global age-filtered sweep; payment refund_req idempotency may also suppress a previously initiated-but-failed mock callback. This recovery design is a recorded follow-up, not a completed recovery claim.
