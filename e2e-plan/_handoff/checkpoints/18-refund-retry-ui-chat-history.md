# Checkpoint 18 — selected refund retry, refund UI recovery and newest chat history

Updated 2026-10-02T18:13:07+05:30. User said limits reset and authorized continuing priority work. Latest source work is local and awaits user deployment; no automatic publish/deploy authorization was renewed. No protected workflow edits, new live orders/refunds/messages, cleanup/reset or retained-record repair occurred.

## Implemented source fixes

### CustomerApplication: selected failed refund retry

AdminDlqService.retryRefund now delegates to transactional RefundService.retryFailed(refundId), rather than modifying a row and dispatching the global age-filtered sweep. The service locks the exact FAILED refund and its matching order payment intent, validates an exact positive amount, confirmed available payment state and remaining balance including REQUESTED/PROCESSING/COMPLETED commitments, then queues only the original refund identity. ORIGINAL_METHOD uses payment outbox; STORE_CREDIT uses wallet outbox, preserving the stable refundId used by WalletService's credit idempotency. Unique transport attempt key is refund_retry:<originalId>:<cumulativeAttempt>. Attempt history is incremented, not reset. The response says queued/completion pending. Enqueue and status changes are atomic; duplicate PROCESSING/terminal retries reject; NONE/invalid gateway/mismatched intent are rejected.

Real Spring/JPA persistence check commits local fixtures, forces failure after a real outbox insert, proves rollback of both outbox and refund, then successful exactly-one enqueue, unchanged unrelated failed refund and rejected duplicate retry. Mock cases cover destination/identity/cap/invalid state. This is queueing proof, not provider completion.

The original automatic retryStuck sweeper still has destination/race/serialization concerns; it was not rewritten in this batch. PaymentGatewayIntegration still holds initiation keys after async failures. A selected retry will not automatically recover a historically held gateway request. Old missing confirmed capture transactions and delivered status/ledger correction remain open. Do not invoke the new retry against the retained historical fixtures until downstream safe recovery is prepared; never retry the accidental delivered refund to make a test green.

### FoodDeliveryAppUI: refund read failure/retry and open-view refresh

useOrderRefunds returns order-scoped refunds/error/loading/retry. Failed reads show visible error/retry rather than becoming silent empty data. The last status for the same order remains visibly labeled if a refresh fails. The terminal/delivered view refreshes every 15 seconds after request completion while mounted; no overlapping scheduled fetches, cross-order response leakage or timers after leaving/disabling the view. Both delivered and cancelled/failed views use the result. A legitimate successful empty list remains empty. Fake-clock tests cover pending→completed, failed refresh retention, retry, late prior-order responses and disabled/unmounted scheduling. No intentional live waiting was introduced.

### CommunicationService: newest history window

ChatMessageRepository uses createdAt DESC, id DESC. getMessageHistory page0 now contains the newest window; subsequent pages contain older messages. Existing UI merges/sorts history chronologically. One real JPA local fixture covers 61-message newest/older page boundaries, no overlap, another session excluded, empty beyond-end page, stable same-timestamp ID ordering. Controller/session authorization checks and startup were run. No migration/config required; older-message UI pagination is still not implemented and is not claimed by this fix.

## Verification

120 selected customer backend invocations +22 selected chat backend invocations +30 local UI invocations passed, zero failures/errors/skips in final reports. UI typecheck and lint passed; diff checks passed. Counts are unique selected invocations; reruns counted once. Initial test/import/config/lint failures are preserved in evidence/18-local-results.json, not erased by final success. H2 PostgreSQL-mode checks are local proof, not Oracle/provider or deployed E2E completion. Full build_verify/full suite were not run. No new E2E class added; original HappyDelivery/cancellation/rejection resume branches remain the runtime reuse plan.

## Deployment gate and exact continuation

Deploy customer-service, chat-service and FoodDeliveryAppUI after user commit/push. No Config Server change, migration, database reset or reseeding. Payment amount guard is already deployed as 0a0a227. See ../DEPLOYMENT-GATE.md. After confirmation inspect versions/health, validate refund read-error/retry in deployed browser-local routing and retained order read state, then continue downstream exact-ID capture/refund recovery. Do not rerun known-false delivered quote/money prerequisites or duplicate successful happy acceptance. Preserve original manifests/IDs. Next steps are ../NEXT-STEPS.md; constraints are ../USER-INSTRUCTIONS.md.
