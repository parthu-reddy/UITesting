# Shared lifecycle coverage and financial context

Use one owned successful delivery lifecycle for related acceptance, chat, receipt, earnings, admin-money and refund-quote assertions. Call helpers at the appropriate order state from the existing test; do not make independent tests depend on execution order or global mutable IDs. Keep isolated browser contexts for each actor. Separate orders only when terminal outcomes conflict, such as delivery versus rejection or cancellation.

Read-only follow-ups may load an explicit retained manifest, then verify the order ID, participants, environment and current state. A reset database invalidates historical manifests. Retain test records; an idle rider may be made OFFLINE with authoritative confirmation, while an active delivery must be preserved.

Fail closed on financial scope: PARTIAL requires nonempty validated item IDs/quantities and a reason. A quote amount alone cannot reconstruct its selection. Bind the quote/request to the exact order and actor; do not silently reinterpret an empty partial selection as FULL. The server recalculates authoritative amounts and applies refund caps. A support request, approval, processing and actual payment completion are different states and need separate evidence.

Preserve missing monetary values as unknown, signed values as signed, and transaction/reference IDs from the actual API contract. Compare exact decimal arithmetic and balanced reference-scoped ledger legs. Do not substitute zero for missing evidence. Distinguish local service tests, browser-routed UI fixtures and deployed cross-role integration in reports. Excluded expiry, rate-limit and parked SSE scenarios are deferred, not passed.

Separate asynchronous lifetimes when an effect changes its own dependencies. Creating a session must not cancel its history response or strand its loading state. Cancel stale history on session changes, merge by message ID with already received live messages, and report history failure without invalidating a usable session. Scope status locators to the intended live region; a loading spinner is also a status element. Fixture envelopes, STOMP frames and actor fields must match the current server/client contract.

An administrator's reduced support award must remain bounded by the validated item quote and remaining paid amount; retain item accounting and the audited ticket identity. Do not remove selected items to bypass an equality check.

## Real persistence and startup boundaries

When mapping lazy associations with open-in-view disabled, load exactly the associations the response needs in a bounded read-only repository query. Verify mapping after the persistence context is cleared. Remote ledger/provider reads should follow the database read, without extending its transaction.

A replacement Spring Boot component scan must preserve TypeExcludeFilter and AutoConfigurationExcludeFilter as well as service-specific exclusions. Conditional auto-configurations belong to Boot's ordered import phase. Real context tests must assert required capabilities such as the outbox publisher, not merely a nonempty bean count; manually constructed publishers cannot prove production wiring.

Refund approval, order cancellation, refund completion and balance credit are distinct facts. Render REQUESTED/PROCESSING as pending, FAILED/CANCELLED as not returned, and COMPLETED as returned to its recorded destination. Use completedAt, never updatedAt, as completion evidence. Delivered orders may still have refunds; surface those records independently of lifecycle status.

## Financial messaging and posting evidence

Exercise the real Kafka adapter with the producer's String payload, metadata headers and partition key. Calling a listener with a manually built envelope does not verify its wire contract. Preserve stable event identity and let infrastructure failures reach Kafka recovery. Mock providers must emit the same normalized per-operation amount/unit and explicit result fields as production adapters; successful partial refunds still need a supported completion event.

Capture and distribution are separate accounting steps. Once capture debits a customer wallet or credits gateway receivables into clearing, delivery distributes clearing funds without a second customer debit. Fund payable accounts before contributions/taxes; unordered collections cannot define execution order. Validate posted payee net amounts against the exact order and payout history, in addition to debit/credit balancing. Capture-only evidence cannot establish delivered settlement.

A PostgreSQL JSON column needs actual ORM JSON binding, not merely columnDefinition=jsonb. Dead-letter audit paths must also retain malformed/empty input and claim stable terminal keys transactionally. Keep logs bounded and preserve the original payload in durable audit storage. An operator resolution note is not a replay, refund completion or booked ledger entry.

## Outcome dimensions, confirmed captures and affected-test discovery

Order status, delivery outcome and completion timestamp are separate authoritative facts. A handover status can persist after delivery; scheduled age queries must exclude ended outcomes/completion timestamps and recheck current facts under a row lock. Late failure/cancellation and duplicate completion must not refund or settle a completed delivery again. Exercise stale-candidate races and the database query with fixed/old timestamps, without wall-clock expiry waits.

Consumers on a shared topic must bind the actual supported event type. Never turn the generic else branch into capture success or infer financial outcomes from similar JSON fields. Use the real binder/adapter in producer-stub contracts and assert that refund commands and unrelated aggregates cannot touch capture state.

Persist the confirmed capture transaction atomically with intent success and its outbox result. Retain exact amounts, stable audit references, capture time and origin; a normalized internal reference is not a provider receipt ID. Refunds require existing capture evidence rather than manufacturing it during refund. Duplicate success preserves partial/full refund state and creates neither another capture nor another completion event. One real repository scenario can cover capture, partial/full refund and duplicate assertions.

When changing a dependency or listener boundary, search all constructors, handler references, @Import contexts and producer/consumer contracts. Run affected existing tests alongside new focused checks, including repository and startup wiring. Keep an explicit class/count result manifest; report missing/skipped/blocked scope and never describe selected checks as the entire suite.


## Exact amounts and selected recovery identity

A provider/refund completion describes an exact movement. Never silently clamp it to a remaining balance or publish a zero/artificial completion. Reject inconsistent amounts without changing persisted balances/outbox, and preserve evidence for reconciliation. Exercise negative boundaries inside the existing committed financial fixture as well as valid partial/full/duplicate results; local JPA proof remains distinct from provider/deployed proof.

An admin retry for one refund must not dispatch a global age-filtered sweep. Keep original refund/payment/order identities, lock and atomically enqueue the selected operation, preserve destination and check remaining amounts. Distinguish initiation acceptance from callback completion; do not delete idempotency history or blindly call a provider again to bypass a held key.


## Checkpoint18: transport attempts, read failure and persisted proof

Keep one business refundId across transport retries; a retry outbox key may distinguish cumulative attempts without changing the wallet/provider business identity. Queueing and status updates must be in one transaction, with real persistence rollback proof after an outbox insert/final write failure. Revalidate remaining money after a failed refund stopped reserving it. Report initiation and completion separately; do not erase held idempotency history to force provider action.

For an open customer money view, distinguish empty successful reads from failure, expose retry, keep last same-order values labeled stale on failed refresh, and prevent late prior-order responses/timer leaks/overlapping scheduled requests. Fake-clock tests can prove refresh behavior without intentional waits.

For newest chat windows, use descending persisted timestamp with a deterministic tie-break, then chronological rendering. Verify beyond page-size and same-time boundaries with real repository data; Java UUID.compareTo is signed and differs from database lexical ordering. Configure test datasource replacement/driver/dialect explicitly before describing an H2 PostgreSQL-mode test as such. Local proof remains separate from deployed provider/cross-role behavior.
