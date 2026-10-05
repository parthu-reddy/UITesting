# Durable audit status: refunds-and-payment-recovery


## 2026-10-05T11:57:12+05:30 — O4/O5 scoped regression acceptance

The required current methods for this feature pass on the retained Dev fixtures. Canonical60631296 is delivered with exact receipt/posted earnings/18balanced ledger lines, selected-item quote without refund submission and four immutable reviews; actual participant chat round trips are retained from the same lifecycle. Settings15/15 distinct methods,partner10/10,restaurant earnings1/1,navigation2/2,admin-money1/1 and exact beneficiary queue1/1 pass. Original failed invocations remain separately recorded; no duplicate lifecycle or server cleanup. See the checkpoint113 release evidence and feature-specific artifacts. Unselected/outcome/provider/internal/routed/duration/rate/SSE cases remain outside this acceptance.
## 2026-10-05T07:02:09+05:30 — checkpoint107: exact money, selected-item quote and no-rating guard pass

Existing retained-delivered Happy branch invocation1 passes1/1 (23.304s),0failures/errors/skips. Rendered receipt and rider trip agree with actual admin money: CARD/SUCCESS,total₹72.81,food₹46.67,GST₹2.34,delivery₹18.80,platform₹5.00,restaurant net₹34.67,rider net₹21.16. All18 ledger lines balance; exact restaurant/rider posted payouts match. No refund exists and no refund request was submitted. Selected-item partial quote matches visible Items+GST, retains its context and enables the final request button without clicking it. Evidence56 preserves counts/numbers and the no-new-order result.

Existing delivered read-only batch invocation1 passes2/2: CustomerSettings unrated dialog requires at least three target groups, its unrated submit stays disabled and no review POST occurs (11.608s); PartnerReadOnly exact rider trip shows the owned outlet, Delivered/date and positive payout (6.045s). Evidence57 retains both independent counts. They reuse60631296; no new checkout, review or refund write.

Existing OrderReviewsFlowTest#participantsReviewEachOther invocation1 is running on that same owned delivered order after the no-rating guard. It intentionally writes four dummy participant reviews through real UI and then verifies submitted targets read-only; old direct aggregate/cache methods remain disabled/deferred. Remaining: read-only settings/partner screens, restaurant earnings/statement/navigation/admin-money, final O4/O5 gates on UI509d084, metrics and final docs/checklists. Stop after O4/O5.

Updated 2026-10-02T17:13:01+05:30. Review: Current active priority; exact owned recovery review. Implementation/next scope: Latest completion/capture/source fixes deployed. Evidence: 81 affected local passes; all 3 owned refunds FAILED; no live completion.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Checkpoint 16 (2026-10-02T17:26:45+05:30): callback amount guard passed 32 affected local payment checks; deployment pending. Existing local fixture extended, no new E2E class/order. [Pending details](PENDING.md). Current retained financial recovery remains open; no reset/reseed required.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint 17 (2026-10-02T17:44:47+05:30): payment amount guard deployed/healthy at 0a0a227fa97e455d90aba2d7f6e9daa1bd558729. Earlier gate is closed. Three retained refunds remain FAILED; delivered status/ledger correction and exact-ID retry are open. Rider OFFLINE; no reset/cleanup/new lifecycle or financial mutation. Current continuation: [latest checkpoint](../../_handoff/checkpoints/17-deployment-confirmation-and-budget-handoff.md).

Checkpoint18 (2026-10-02T18:13:07+05:30): local exact-refund queueing, UI refund read/retry/refresh and newest chat window fixes complete; 120customer/22chat/30UI final checks plus typecheck/lint passed. Customer/chat/UI deployment pending. No new live record/action/E2E lifecycle; historical refund/capture/ledger recovery is still open. See [checkpoint18](../../_handoff/checkpoints/18-refund-retry-ui-chat-history.md) and [deployment gate](../../_handoff/DEPLOYMENT-GATE.md).

Checkpoint19 (2026-10-02T18:27:17+05:30): exact stored-confirmation capture recovery implemented locally in payment-gateway;58selected checks including committed concurrency and Spring method security passed. Deployment/live capture restoration pending; all3owned refunds stillFAILED. Source proof has3matching PROCESSED confirmations; no financial action performed. See [checkpoint19](../../_handoff/checkpoints/19-stored-confirmation-capture-recovery.md) and [deployment gates](../../_handoff/DEPLOYMENT-GATE.md).

## Checkpoint20 (2026-10-02T19:10:00+05:30)

Review result: the checkpoint19 recovery endpoint is deleted, and the checkpoint18 retry is fixed so it can actually complete (the gateway releases its held key on definitive failure; customer service has a separate `sweep_attempts` budget and routes by destination). Refund UI polling stops once refunds settle. Local proof only, with every guard seen red; see [checkpoint20](../../_handoff/checkpoints/20-review-and-refund-retry-fix.md). The three retained fixtures will be erased by the user's data reset. Next: after deployment and reset, run cancellation and rejection without resume properties and assert actual completion (refund COMPLETED, payment REFUNDED, balanced REFUND ledger, UI state).


## Checkpoint24 (2026-10-02T23:58:00+05:30)

- Item-level refund completion is now proven live (support award fe60ce55 COMPLETED with CLAWBACK posted), closing the checkpoint23 `refund_items.amount` defect on deployed code.
- New defect class found and fixed locally: an automatic refund **routing refusal** (`REFUND_STATE_INVALID`, `REFUND_EXCEEDS_REMAINING`) inside `OrderEventConsumer`/`PaymentEventConsumer` was caught "so the state change is kept", but crossing RefundService's transactional proxy had already marked the transaction rollback-only: the cancellation, delivery-failure or payment-state change would have rolled back and the event gone to the DLT. Now `RefundService.requestUnlessRefused` returns pre-write refusals; `RefundRefusalInCallerTransactionTest` proves it with real transactions (control, keep, accept, post-write rollback), with mutations seen red. Local proof only; not exercised live (needs an INITIATED-intent cancellation, not part of this run). Deployment pending with customer-service.

Checkpoint27 (2026-10-03T08:00:00+05:30): refund reads are isolated live (another customer 403). Admin retry on a definitive provider FAILED has no live path in Dev: every payment mock always succeeds a refund. Its proof remains checkpoint20's local real-transaction tests; a Dev-mock failure seam is a pending user decision.

Checkpoint29 (2026-10-03T08:50:00+05:30): passed live on deployed code — see [checkpoint29](../../_handoff/checkpoints/29-deployed-priority-runs.md) (isolation on d3acfc93; CHAT-22 and REFUND-RETRY-01 on cf608115).
