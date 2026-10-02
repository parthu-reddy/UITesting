# Checkpoint19 — restore a missing capture from stored confirmation evidence

> **SUPERSEDED by [checkpoint20](20-review-and-refund-retry-fix.md): the endpoint below was deleted by user decision and must not be deployed.**

Updated 2026-10-02T18:27:17+05:30. Checkpoint18 customer-service/chat-service/UI deployment was acknowledged by the user (“I’ll deploy and confirm”); completion has not been confirmed. This independent payment-gateway batch is local and needs a separate deployment. No workflow edit, publish/push/deploy, live financial action, new lifecycle, cleanup/reset/reseed or retained status correction occurred.

## Actual retained evidence

Read-only Oracle inspection at2026-10-02T12:48:04UTC confirms all3owned gateway intents SUCCESS, amount43.35, amountRefunded0; no successful capture Transactions and no matching stored webhook receipts. All3customer refunds remain FAILED/attempts4/completedAtnull; rider OFFLINE. Each gateway does have one PROCESSED PAYMENT_COMPLETED outbox confirmation with matching business order/gatewayOrder/amount. WebhookProcessingService stores this aggregate_id as the business orderID, not intentUUID; the initial intent-key count was corrected after reading the producer. Narrow identities/amounts/counts only are preserved in [evidence](../evidence/18-retained-recovery-read.json).

Stored source eventIDs:
- delivered b83c71bd-022c-439d-8586-aa4fe5b1c0d1 →8798e58f-bb1f-4f21-9425-c4796b32abc7;
- cancellation 0b22b53a-c718-4bec-b854-b736e24bee29 →27596bbd-1a0a-403f-8f04-5135d65c1543;
- rejection 176fb42c-46e9-49b2-b52f-43c571603199 →dd82821c-74cd-4f03-9a06-06f2f25f8d58.

These are persisted normalized Dev payment confirmations, not provider receipts. No payment or refund completion is invented.

## Implemented PaymentGatewayIntegration recovery

ConfirmedCaptureRecoveryService and AdminCaptureRecoveryController add POST `/api/v1/internal/admin/payments/dlq/captures/confirmations/{eventId}/recover`. It fits the existing protected admin-payment gateway route; no APIGateway or configuration change. ADMIN method security is enforced; actor comes from Authentication, not caller JSON. There is no caller-supplied amount, order, gateway, receipt or status.

The service requires an existing PROCESSED PAYMENT_COMPLETED/PAYMENT outbox row, valid typed payload, matching aggregate/order/gateway identity and exact positive cent amount, original nonfuture paidAt, and consistent current SUCCESS/PARTIALLY_REFUNDED/REFUNDED balance/state. It locks the matching intent, verifies any existing capture without rewriting it, or inserts exactly one SUCCESS local transaction. Concurrent requests serialize on that lock. The new reference `confirmed-event:<sourceUUID>` explicitly names local confirmation evidence; paymentNetwork is STORED_CAPTURE_CONFIRMATION and capturedAt is the original paidAt. This is not a fabricated provider payment ID. Intent/refund balances and lifecycle states are preserved. No new payment event, provider call, refund initiation, ledger posting or idempotency-key deletion occurs. The transaction reference links permanently to the stored source evidence; a structured operator/source/order/intent/transaction/result log is emitted after transaction completion.

Existing startup/HTTP webhook tests had Redis mocks whose opsForValue returned null when scheduled jobs ran before BeforeEach. Test-only deep-return Redis mocks prevent that setup error; production scheduling/configuration is unchanged. Startup now also asserts recovery service/controller beans exist.

## Verification

58 selected local invocations across15classes passed, failures/errors/skips0. Includes15new service invocations,1committed H2 PostgreSQL-mode concurrent recovery fixture,6actual Spring method-security invocations (4nonadmin roles, anonymous denial, ADMIN original-ID/actor), existing capture/partial/full refund persistence, HTTP webhook integration, startup/invariant tests and producer-stub consumer contract. Final log has0unexpected scheduled-task errors. Initial broad run had57test passes but4background Redis mock errors; the setup was repaired and final rerun counted once. No full build_verify/full suite or deployed recovery proof. See [local evidence](../evidence/19-local-results.json).

## Deployment and continuation

Deploy payment-gateway after user commit/push when ready; no migration/reset/reseed/config/other-service deployment required for this additional batch. Checkpoint18 customer/chat/UI confirmation remains separately pending. See [deployment gates](../DEPLOYMENT-GATE.md).

After confirmation, verify version/health and original owned evidence again. Use normal browser Dev Autofill admin authentication and the exact stored eventID for a reviewed owned capture recovery. Confirm one local transaction with exact original amount/time/source identity, unchanged intent/refunds/other orders and a duplicate no-op. This is a capture-record recovery only; refunds remain a separate completion path.

Held `refund_req:<refundId>` initiation keys still suppress historical refunds. Do not delete keys, manufacture a provider result, change refundID or call the generic refund initiation endpoint blindly. Safe exact-original-ID downstream recovery and the automatic customer sweeper remain open. The delivered order also still needs audited delivery status/ledger correction and separate accidental-refund disposition; do not retry that accidental refund. Keep known-false money/quote tests blocked until prerequisites are repaired. Preserve owned manifests and update this plan after every meaningful result.
