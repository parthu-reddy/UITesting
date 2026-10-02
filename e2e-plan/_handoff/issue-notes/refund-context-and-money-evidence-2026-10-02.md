# Refund context and monetary evidence — 2026-10-02

## Partial quote lost its scope

Source review while consolidating the existing happy delivery and refund tests found that `ChatRefundQuoteResponseEvent` exposed the amount/type but omitted the selected items and reason. `ChatMessageList` then submitted a PARTIAL request without items. `RefundService` interprets an empty item selection as the whole order. This was a source-confirmed expansion path, not a claim that a real refund was paid.

The quote event now carries the validated order ID, items and reason. CustomerApplication rejects PARTIAL requests/quotes with missing or empty items, unknown refund types, and ambiguous FULL requests with selected items. The UI retains the quote context, blocks incomplete/mismatched partial quotes, and labels the action Submit Refund Request. OPEN means a support ticket is under review; it does not mean a refund is approved or paid. The OPEN decision now includes its requested amount.

## Admin money fields misrepresented evidence

The admin order-money page used `line.id`, although the actual ledger DTO supplies `transactionId` and `referenceId`. It also converted missing amounts to zero and labeled delivery SGST/CGST as TDS. The UI now displays the actual transaction identifier, shows Unavailable for absent amounts, uses the correct tax label, and includes the platform bonus deduction in restaurant payout details. A negative restaurant payout remains signed.

## Verification boundary

48 focused CustomerApplication backend checks and 17 focused UI checks passed, alongside common-core installation, UI typecheck/lint and E2E compilation. 25 existing chat authorization backend checks also passed. Deployment and the combined real delivery/chat/money/refund-quote E2E remain pending. CommunicationService forwards refund payloads without re-binding this DTO, so no CommunicationService code change is needed for these additive fields.

## Fixture recovery

After the user's disk cleanup, Oracle was healthy with 162 GB free, but the fresh databases initially had no identity users/orders. The user completed seeding; the four selected role accounts were verified active and 104 outlets were present. Historical order manifests are no longer fixtures for this baseline. No records were deleted by this audit. The earlier observation of large Docker container storage did not establish which files caused it.

## Reduced itemized support approval rejected

A local regression reproduced that AdminRefundController accepts an override below the verified quote but RefundService then required exact equality whenever items were present. A valid lower award therefore failed. The service now allows a reduction only for ADMIN + CUSTOMER_TICKET + a non-null ticket ID; all amounts above the selected-item quote remain blocked, and the original remaining-payment guard stays in force. Six regression cases cover reduced/exact/above amounts and unauthorized/non-ticket sources; the 54-check focused refund suite passed. No deployed financial decision has been claimed.

## Chat initialization cancelled its own history load

Existing browser-routed chat tests remained in Loading chat. Source confirmed that setSessionId changes an initialization-effect dependency, cancelling the same effect before history returns and before it clears loading. History now has a separate cancellable effect; session initialization clears loading promptly and returned history is merged by message ID with messages received during the fetch. 16 focused chat/readiness UI checks, typecheck and lint passed. The three routed chat browser cases remain pending deployment; the corrected admin refund-policy browser cases passed.

## Existing fixture repairs

Removed the stale assertion that session creation sends a browser userId; membership is server-derived. Scoped admin refund/support toast assertions to the polite live region instead of also matching the spinner. STOMP fixture command parsing accepts LF and CRLF. The ledger pagination fixture omitted the API success/data envelope, yielding an empty table; its envelope is corrected and a focused rerun is pending. Routed financial writes are intercepted and do not establish real refund settlement.

Final pre-deployment browser checkpoint: all ten payout fixtures passed, four of seven money recovery fixtures passed, and one of two ledger/order-money fixtures passed. Three money recovery failures were exact event-ID text locators against a producer/event combined row. Ledger pagination lacked the API envelope (now corrected, not rerun). Three corrected refund-policy cases and two read-only queue checks passed. User requested a pause until deployment confirmation; no new lifecycle order was created.

## Fresh-seed integration findings

One shared CARD order delivered, including real cross-role chat; admin money then failed with HTTP 500. Oracle logs identify `LazyInitializationException: Order.orderItems ... no Session` in AdminOrderMoneyService. The service used findById while open-in-view is disabled. Use a bounded read-only item entity graph; do not keep a DB transaction open over the ledger HTTP call. The repository regression clears the persistence context and passes the detached aggregate through the real money mapper.

A real retained quote stayed UNPROCESSED with zero retries in the chat outbox. ChatServiceApplication replaces Boot's component scan but omitted its TypeExcludeFilter and AutoConfigurationExcludeFilter. The auto-configuration was discovered too early for its datasource condition; no OutboxProcessor bean existed. The old context test only counted beans and passed despite this. Assert the required publisher exists in the real context, preserve Boot's exclusions, and retain existing messages/outbox rows so normal scheduling can drain them after deployment.

Cancelled order status was treated as proof of a refund: Total Refunded/Returned text appeared before completion. Delivered summary omitted refund records entirely. CustomerMoneyController used updatedAt as completedAt. Use the independent refund status and actual completedAt; only COMPLETED describes returned money, missing values say unavailable, and order lifecycle status stays unchanged.

The first retained quote rerun inherited a deliberate first-session 503 from OrderChatChecks. Construct injected-failure helpers only in the branch that actually tests recovery. Do not misclassify an injected fixture failure as a deployed backend defect.

Additional strict startup diagnostic: disabling bean overrides fails on duplicate `wallet-service.FeignClientSpecification`. Both Deployment/application.yml and platform-defaults.yml intentionally currently enable overrides; the normal startup/context suite passes with that configured setting. The stricter setting is not a deployment requirement and was not silently counted as a pass. Unique Feign context IDs remain a separate source-backed bootstrap follow-up.

`ExceptionsSupportUiTest` asserted that Reject/Cancel/Rate buttons were absent when no order existed, so its successful invocations proved none of the named business operations. Remove vacuous methods and map to canonical real flows. Preserve distinct outcomes but consolidate overlapping paid fixtures, with a manifest-driven continuation after a terminal outcome so financial failure never creates another duplicate lifecycle.

## Raw Kafka contract and Dev mock refund completion

The postdeployment quote reached chat-events.DLT because the producer emits JSON payload and metadata headers while the listeners accepted OutboxEvent envelopes. Direct-method unit tests had hidden this incompatibility. Both listener entry points now accept String plus real headers/key; existing business guards remain. Real Spring Kafka adapter tests verify quote/ticket input and response storage; response failures propagate to retry/DLT.

Customer cancellation and restaurant rejection correctly reached terminal states, but Dev Razorpay's scheduled callback sent nested paise to a normalized rupee handler. Normalize the mock boundary rather than loosening financial validation. Successful/failed result producers must set isSuccess explicitly; partial refunds use the supported PAYMENT_REFUNDED completion contract while intent state retains PARTIALLY_REFUNDED. Refund completion updates the locked local payment intent from actual cumulative completed amounts.

## Balanced capture is not delivered settlement

Admin money returned 200 and balanced capture rows, but no restaurant/rider earnings had posted. The source DELIVERED event debited CUSTOMER_CREDIT after capture and used unordered Set legs, allowing tax/contribution debits before funding payables. Distribute captured funds from clearing; the customer fixed platform fee already stays in clearing; fund payables before deductions. Strict shared E2E now checks posted net payables, not merely any balanced rows.

LedgerRejection.payload declared jsonb but lacked Hibernate JSON binding. Oracle rejected varchar inserts, including the DLT handler, producing repeated retries and oversized exception headers. Add JSON binding, preserve invalid/empty raw input in a JSON envelope, reuse the transactional terminal-key recorder and bound diagnostic logs. Repository regression runs on H2 are local evidence; PostgreSQL after deployment remains required. Existing DLQ records need audited recovery; resolving a queue row does not book money.

Known follow-up: failed refund retry resets status then invokes a global age-filtered sweep; initiated-command idempotency can suppress callbacks after earlier failures. Do not assume redeployment repairs retained records or silently issue a new refund ID. Current owned IDs and complete evidence are in RandomDocuments/E2ECoverageAudit_2026-10-01/14-postdeployment-financial-contracts.md.

## Completed delivery swept as abandoned; capture transaction never inserted

Runtime confirmation is in RandomDocuments/E2ECoverageAudit_2026-10-01/15-delivered-terminal-and-capture.md. A delivered order retains HANDED_OVER as its order status, so status-plus-age alone is not abandonment. The job failed an already delivered order after two hours and issued an erroneous automatic refund. Exclude deliveredAt and ended delivery statuses in the real query, then lock and recheck both freshness and outcomes before writing. Late lifecycle failure/cancellation and duplicate delivery handlers must also preserve completion. Use old timestamps in local tests; do not wait two hours or duplicate a happy lifecycle.

PaymentEventConsumer bound every non-failure message as PAYMENT_COMPLETED, including its own PAYMENT_REFUND_REQUESTED commands. Explicitly allow producer event types and bind the actual type; unrelated topic messages must produce no payment/order/refund effect. A binder mock returning a fabricated successful event hides this defect; replace it with the real binder in consumer contracts.

Successful gateway confirmation only updated PaymentIntent and emitted completion; it never created the successful Transaction required for refund foreign-key/accounting. Persist confirmed capture, transaction and outbox under the locked intent. A normalized confirmation's stable internal audit reference is not a provider payment receipt; label its origin, never invent provider identity, and never synthesize capture inside refund processing. Verify committed capture → partial/full refund and duplicate messages against real repositories.

Verification omission: LedgerDltRejectionTest was missed when the listener switched from direct repository writes to LedgerRejectionRecorder. The selected 129 tests did not include it. After the user's correction, all direct ledger dependencies (unit, persistence, startup and consumer contract) were discovered and 16 checks passed. A focused count alone is not evidence that all dependent tests were updated.


## Checkpoint 16: exact retry isolation and truthful callback amount

Source review found that AdminDlqService.retryRefund changes one failed refund to PROCESSING and calls the global five-minute RefundService.retryStuck sweep. Fresh selected state can be omitted while unrelated refunds are enqueued. A safe repair needs an atomic exact-ID operation, destination-specific enqueue, remaining-amount validation, stable identities and a downstream reconciliation/idempotency design. Do not remove initiation keys or use another refund ID to force a retry. This recovery repair remains open.

WebhookProcessingService.handleRefundSuccess silently clamped an excessive amount and could emit zero as completed after full repayment. A working fix rejects mismatches and non-cent-exact/invalid values before mutation. The existing committed capture/refund test is extended using its same fixture; see UITesting/e2e-plan/_handoff/CURRENT-STATE.md for current local and deployment proof. Rejection is not proof a real provider did not transfer money; its reported mismatch needs reconciliation rather than an invented smaller movement. No live repair, cleanup or reset was performed.


## Checkpoint18: selected retry and UI recovery repaired locally

AdminDlqService now delegates to transactional RefundService.retryFailed: exact row/payment locking, stable refund identity/destination, cumulative attempt count, remaining cap and atomic outbox/status writes. This avoids the global age-filtered sweep omitting the selected row or touching others. A committed H2 PostgreSQL-mode check proves rollback after outbox insert/final refund-save failure, exactly-one successful enqueue, unchanged unrelated row and duplicate PROCESSING rejection. The original automatic sweeper and gateway accepted-initiation key remain separate concerns; deployment/reconciliation is not yet completed. Do not remove gateway keys or retry the accidental delivered refund.

Customer refund reads previously returned [] on error and fetched only once per open order. The UI now shows a retry/error, retains visibly labeled same-order last status on failed refresh and uses sequential15second refresh while mounted. Fake-clock checks cover pending→completed, late prior-order isolation, no overlap and stopping after disable/unmount. Local results are120customer/22chat/30UI with zero final failures/errors/skips plus typecheck/lint; deployment for customer/chat/UI is pending. Current proof is UITesting/e2e-plan/_handoff/checkpoints/18-refund-retry-ui-chat-history.md; no live record repair or reset was performed.
