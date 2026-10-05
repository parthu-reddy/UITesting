# Durable audit status: reviews-and-support


## 2026-10-05T11:57:12+05:30 — O4/O5 scoped regression acceptance

The required current methods for this feature pass on the retained Dev fixtures. Canonical60631296 is delivered with exact receipt/posted earnings/18balanced ledger lines, selected-item quote without refund submission and four immutable reviews; actual participant chat round trips are retained from the same lifecycle. Settings15/15 distinct methods,partner10/10,restaurant earnings1/1,navigation2/2,admin-money1/1 and exact beneficiary queue1/1 pass. Original failed invocations remain separately recorded; no duplicate lifecycle or server cleanup. See the checkpoint113 release evidence and feature-specific artifacts. Unselected/outcome/provider/internal/routed/duration/rate/SSE cases remain outside this acceptance.
## 2026-10-05T07:03:25+05:30 — checkpoint108: all four participant reviews pass and remain immutable

Existing OrderReviewsFlowTest#participantsReviewEachOther invocation1 passes1/1,0failures/errors/skips in23.732s on UI509d084/harnessafe12f1. Same60631296: customer rates restaurant4 with the recorded comment and rider5; restaurant rates customer5; Offline rider rates restaurant4. Reopened actual dialogs show saved stars/comment/Already reviewed and no submitted target radio group; unrated targets remain offered. Four intended dummy reviews are retained, no new order. Evidence58 and the owned review artifact preserve exact scope. The three direct aggregate/cache methods stay disabled/deferred, not selected or counted as skips/passes.

Existing settings/partner read-only invocation1 is running:14 customer settings/accessibility/theme and9 partner profile/history/earnings/stock-rendering methods. Source verification confirms TestBase calls random seededPhone per method when no property pins an actor; this batch omits the canonical phone overrides and creates no checkout/financial mutation. Exact canonical unrated dialog and completed rider trip already passed separately in evidence57, so they are not duplicated in this batch.

Remaining: successful23 read-only methods, nonzero restaurant earnings/statement, admin delivered-money outcome, restaurant navigation/retained review display, final O4/O5 image gates and measured histograms/checklists. Current delivered/money/quote proof stays in55–57. Stop after O4/O5.

## 2026-10-05T07:02:09+05:30 — checkpoint107: exact money, selected-item quote and no-rating guard pass

Existing retained-delivered Happy branch invocation1 passes1/1 (23.304s),0failures/errors/skips. Rendered receipt and rider trip agree with actual admin money: CARD/SUCCESS,total₹72.81,food₹46.67,GST₹2.34,delivery₹18.80,platform₹5.00,restaurant net₹34.67,rider net₹21.16. All18 ledger lines balance; exact restaurant/rider posted payouts match. No refund exists and no refund request was submitted. Selected-item partial quote matches visible Items+GST, retains its context and enables the final request button without clicking it. Evidence56 preserves counts/numbers and the no-new-order result.

Existing delivered read-only batch invocation1 passes2/2: CustomerSettings unrated dialog requires at least three target groups, its unrated submit stays disabled and no review POST occurs (11.608s); PartnerReadOnly exact rider trip shows the owned outlet, Delivered/date and positive payout (6.045s). Evidence57 retains both independent counts. They reuse60631296; no new checkout, review or refund write.

Existing OrderReviewsFlowTest#participantsReviewEachOther invocation1 is running on that same owned delivered order after the no-rating guard. It intentionally writes four dummy participant reviews through real UI and then verifies submitted targets read-only; old direct aggregate/cache methods remain disabled/deferred. Remaining: read-only settings/partner screens, restaurant earnings/statement/navigation/admin-money, final O4/O5 gates on UI509d084, metrics and final docs/checklists. Stop after O4/O5.

Updated 2026-10-02T17:13:01+05:30. Review: Pending. Implementation/next scope: Pending. Evidence: Not run.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

## Checkpoint32 (2026-10-03T09:47:00+05:30): reviewed and proven live

- Reviews validator (`RandomDocuments/ReviewsIntegration_2026-09-11/tools/validate_reviews_integration.py`) read 81/85 + 1 warning. All five were stale against the 2026-09-28 multi-actor design (ReviewsService 0227db8) or moved files, not regressions: outbox key now includes the author; access policy is private-by-default; the controller uses Spring `Authentication`; the UI refusal type is derived from the generated API; pins moved to `env_deployments/dev`. Checks updated to the current design: **85/85**. Each updated check was seen red on a reintroduced defect (key without author; DRIVER made public; self comparison neutered — which first exposed a weakness, then fixed; X-User-Id read; a refusal message removed), except the warn-level tag check (not mutated: it reads a live deploy pin).
- New `OrderReviewsFlowTest` (REVIEW-01..06) PASS on bb43e2a4; REVIEW-AGG-01, REVIEW-13 and the rider history read PASS against that data. SUPPORT-01..11 retired (their UI was deleted at checkpoint23; support is covered in support-and-refund-queues).
- Not covered: REVIEW-12 (pagination with many reviews).
