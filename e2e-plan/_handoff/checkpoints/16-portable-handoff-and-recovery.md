# Checkpoint 16: portable handoff and retained recovery prerequisites

The latest customer-service/payment-gateway fixes were deployed and both services are healthy. No database reset/reseed is needed. No live financial recovery or new order was executed during this checkpoint; rider is OFFLINE.

The portable handoff contains all 28 feature status entries, the source/test inventory (325 historical methods, 71 classes), 103 historical failures, 40 backend report indexes, 14 preserved audit checkpoints, the three exact owned manifests, constraints and deferred work. All 67 Markdown files in the handoff/feature entries had valid linked file targets before this checkpoint note was added. Links to large original logs and actual source intentionally remain in the assembled workspace; the handoff is context, not a duplicate repository. Inventory counts should be rescanned after source edits by other agents.

## Source-confirmed recovery blockers

- CustomerApplication `com.fooddelivery.order.service.AdminDlqService.retryRefund` selects a FAILED refund, marks PROCESSING and calls global `RefundService.retryStuck`. The selected row is freshly updated, so the five-minute filter may omit it; unrelated old refunds can be touched. It has no enclosing atomic target transaction.
- `RefundService.retryStuck` re-enqueues only gateway requests, even though STORE_CREDIT has a separate wallet path. Serialization/save exceptions are swallowed. Existing unit cases prove stable refund identity and attempt limit, but not target isolation/atomic rollback/destination preservation. This is open work, not repaired.
- PaymentGatewayIntegration `PaymentEventConsumer.consumeOrderEvents` retains `refund_req:<refundId>` after initiation returns true. Dev mock callback failure can happen later; retries of the same refund are then ignored. Do not erase idempotency keys or create another refund identity. Production `RazorpayStrategy` sends `receipt=refundId`, but source alone does not prove provider idempotency/reconciliation; do not assume an accepted/in-flight refund is safe to initiate again.
- Historical gateway SUCCESS intents have no capture Transaction. Latest confirmed-capture fix does not backfill automatically. Normal refund completion still requires capture proof. The accidental refund on the actually delivered order needs separate audited correction; do not retry it as if it were a legitimate cancellation.
- Original delivered ledger command was rejected because of funding/leg order. Latest producer fix does not rebuild/rebook it. A DLQ resolve note is not a posting. Recovery must retain original deterministic identities and actual economics.

## Newly found money callback boundary

`WebhookProcessingService.handleRefundSuccess` used to clamp an excessive callback amount to remaining intent balance, then publish COMPLETED. After full repayment it could record/publish a zero refund under a fresh refund ID. This fabricates the callback amount and can disagree with the customer refund row. The working change rejects excess and non-cent-exact/invalid amounts before mutation. The existing committed local H2 capture/partial/full/duplicate test now also covers invalid/zero/negative/over-remaining results and unchanged balances/refund rows/outbox. Verification/deployment status is in CURRENT-STATE; this note alone is not a pass or deployment claim.
