# Pending refund and payment recovery work


## 2026-10-05T11:57:12+05:30 — O4/O5 scoped regression acceptance

The required current methods for this feature pass on the retained Dev fixtures. Canonical60631296 is delivered with exact receipt/posted earnings/18balanced ledger lines, selected-item quote without refund submission and four immutable reviews; actual participant chat round trips are retained from the same lifecycle. Settings15/15 distinct methods,partner10/10,restaurant earnings1/1,navigation2/2,admin-money1/1 and exact beneficiary queue1/1 pass. Original failed invocations remain separately recorded; no duplicate lifecycle or server cleanup. See the checkpoint113 release evidence and feature-specific artifacts. Unselected/outcome/provider/internal/routed/duration/rate/SSE cases remain outside this acceptance.
## 2026-10-05T07:02:09+05:30 — checkpoint107: exact money, selected-item quote and no-rating guard pass

Existing retained-delivered Happy branch invocation1 passes1/1 (23.304s),0failures/errors/skips. Rendered receipt and rider trip agree with actual admin money: CARD/SUCCESS,total₹72.81,food₹46.67,GST₹2.34,delivery₹18.80,platform₹5.00,restaurant net₹34.67,rider net₹21.16. All18 ledger lines balance; exact restaurant/rider posted payouts match. No refund exists and no refund request was submitted. Selected-item partial quote matches visible Items+GST, retains its context and enables the final request button without clicking it. Evidence56 preserves counts/numbers and the no-new-order result.

Existing delivered read-only batch invocation1 passes2/2: CustomerSettings unrated dialog requires at least three target groups, its unrated submit stays disabled and no review POST occurs (11.608s); PartnerReadOnly exact rider trip shows the owned outlet, Delivered/date and positive payout (6.045s). Evidence57 retains both independent counts. They reuse60631296; no new checkout, review or refund write.

Existing OrderReviewsFlowTest#participantsReviewEachOther invocation1 is running on that same owned delivered order after the no-rating guard. It intentionally writes four dummy participant reviews through real UI and then verifies submitted targets read-only; old direct aggregate/cache methods remain disabled/deferred. Remaining: read-only settings/partner screens, restaurant earnings/statement/navigation/admin-money, final O4/O5 gates on UI509d084, metrics and final docs/checklists. Stop after O4/O5.

Read ../../_handoff/CURRENT-STATE.md and NEXT-STEPS.md first. Historical scenario/checkpoint passes do not close these gaps.

- New callback exact-amount guard: 32 selected local payment checks passed; payment-gateway deployment confirmed healthy at checkpoint 17 (../../_handoff/CURRENT-STATE.md). No reset/migration/config needed.
- The three retained order/refund IDs and manifests are in the handoff. Cancellation and rejection UI outcomes passed, but each original-method refund is FAILED with no completion. Do not create replacements or duplicate the canonical happy delivery.
- Exact-ID atomic admin retry is implemented locally and awaits deployment; verify it before use. The separate automatic sweep still has destination/race/enqueue-error concerns. Respect destination and remaining cap. Resolve initiation-vs-callback state safely; held gateway idempotency keys cannot simply be deleted.
- Checkpoint19 implements exact ADMIN-only capture restoration from verified stored PROCESSED confirmation evidence;58local checks passed. Payment-gateway deployment/live original-ID restoration is pending. This does not automatically recover held refund requests or repair delivered ledger/status.
- Delivered fixture needs audited delivery/ledger correction and separate accidental-refund disposition. Do not retry that accidental refund or use raw status edits as product recovery. Original deterministic ledger transaction identity/economics must be preserved.
- Once eligibility is valid, prove real quote roundtrip, intentional owned support approval/partial/denial, and separate customer/refund/payment/ledger completion. Current reduced-amount override is an API capability; admin UI has no editable amount control.
- Customer refund read-error/retry/open-view refresh is fixed locally with fake-clock tests and awaits UI deployment; deployed UI proof remains open.
- New callback guard rejects inconsistencies; it does not prove a real provider did not move money. Production reconciliation/idempotency requires source/provider evidence, separate from Dev mock proof.

Natural retry/expiry/grace/reset waits and rate-limit exhaustion are excluded. See this folder's DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md. Skipped cases are not passes.
