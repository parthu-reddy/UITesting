# Delivered-order protection and confirmed capture — 2026-10-02

## Deployment and retained fixture proof

User confirmed deployment of the four-service batch. All four services are healthy with new image tags. The ledger rejection for event 1173299a-c864-4712-8717-fb664a03e118 is now present in Oracle PostgreSQL at 10:39:47Z; JSON audit persistence is verified there. This is not proof that the rejected delivered transaction has posted.

The previously missed LedgerDltRejectionTest and listener/persistence dependencies passed: 12 invocations. Ledger startup and the actual producer-stub consumer contract added four passing invocations. Customer and payment dependent contracts/integration were then executed, rather than only the newly changed tests. The payment consumer contract now uses its real EventBinder, replacing a mock that always fabricated a PaymentSucceededEvent.

No new delivery/cancellation/rejection order, support ticket, refund command, replay or cleanup was issued by this continuation. Browser login sessions were retained. Rider remains authoritatively OFFLINE. All database investigation was read-only.

## New deployed failures

Retained delivered quote rerun failed on its mandatory history prerequisite: deliveryStatus was FAILED, not DELIVERED. It stopped before creating a quote. No quote success, chat-wire success or money completion is claimed.

Order b83c71bd-022c-439d-8586-aa4fe5b1c0d1 delivered at 08:41:08.141848Z. Delivery-service still records RELEASED / DELIVERED with releasedAt 08:41:08.048835Z. At 10:45:29Z customer-service's AbandonedDeliverySweeper selected the order because its order status remains HANDED_OVER and updatedAt was over two hours old. The query/recheck ignored deliveredAt and deliveryStatus. It changed deliveryStatus to FAILED, then emitted event 22367ec1-65b6-42b7-af0e-7000ad802fe8. The consumer changed status to DELIVERY_FAILED and created automatic ORIGINAL_METHOD refund cae2548a-5c41-47a0-93d0-c3c2732cd93a for ₹43.35. This happened autonomously; it was not an E2E refund submission.

Customer PaymentEventConsumer then interpreted PAYMENT_REFUND_REQUESTED messages as PaymentSucceededEvent because every non-failure type fell into its capture branch. It attempted repeated late-payment refunds, refused by REFUND_EXCEEDS_REMAINING. The selected contract/test fixtures had used a fabricated PAYMENT_SUCCESS type, hiding this boundary error.

The newly deployed Dev Razorpay callback accepted its normalized refund amount, then failed at 10:45:35Z: no successful Transaction exists for gateway PaymentIntent 99700392-414b-40cf-af30-b5c1bfb220a5. Source search confirms no successful capture transaction is ever inserted by handleSuccessfulPayment; it only marks intent SUCCESS and emits completion. Real gateway adapters, Dev callback and reconciliation share this handler. Refund processing correctly requires the capture record and must not bypass that requirement.

The two retained cancellation/rejection refunds are FAILED with four attempts. Existing retry/sweeper and initiation idempotency do not repair them merely through restart. Those known recovery gaps remain separate.

## Source fixes and local proof

CustomerApplication: abandonment query excludes all ended delivery statuses and any deliveredAt; a locked recheck includes current cutoff and outcome. HandedOverState ignores late failure/admin cancellation/duplicate delivery once delivery has completed, so there is no refund or duplicate booking. PaymentEventConsumer accepts only explicit PAYMENT_COMPLETED/PAYMENT_FAILED for payment-state changes; refund result handling remains separate. Existing invalid PAYMENT_SUCCESS fixtures now use the real producer type.

PaymentGatewayIntegration: a confirmed payment writes its successful transaction inside the same intent-locked transaction as success/outbox. A stable namespaced confirmed-intent audit reference identifies the normalized confirmation; it is explicitly not a provider payment receipt ID, and paymentNetwork records NORMALIZED_CAPTURE_CONFIRMATION. No capture is fabricated during refund. Duplicate success does not add another transaction/outbox or reset PARTIALLY_REFUNDED/REFUNDED. A later confirmed callback may fill a missing historical capture; this is not automatically invoked by deployment.

81 unique selected backend invocations passed, zero failures/errors/skips: ledger16, customer47, payment18. This includes existing ledger/payment consumer contracts, real customer repository scope, existing HTTP webhook integration, and a committed local JPA capture → partial refund → full refund path with duplicate confirmation/results. Tests use old timestamps or immediate callbacks; no intentional timer waits were added. JPA runs on isolated H2 in PostgreSQL mode, not Oracle. Existing HTTP integration retains its records and no longer calls deleteAll. Both changed repositories pass diff checks. This is an affected-test verification batch, not a full build_verify/full-suite claim.

## Next deployment gate and remaining work

Deploy/restart customer-service first to prevent further false abandonment, then payment-gateway. No UI, CommonLibrary, configuration, migration or workflow changes are required.

Deployment protects future records; it does not undo the corrupted retained order, remove/cancel its accidental refund, backfill old captures, or rebuild rejected delivery distributions. Preserve all records. Before owned recovery, inspect actual gateway/customer refund states and capture/ledger balances; a resolution note is not posting, and blindly replaying the original DELIVERED payload retains its invalid funding/order. Recovery must use stable original identities and an audit trail. Quote/chat/support decision integration and retained financial recovery remain blocked by these facts, rather than silently substituting a new lifecycle.

SSE, expiry and rate-limit waits remain parked. Support approve/partial/deny and ownership scenarios remain the priority once prerequisites and recovery are sound.
