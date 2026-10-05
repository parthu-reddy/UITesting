# Validation and pending work


## 2026-10-05T11:57:12+05:30 — O4/O5 scoped regression acceptance

The required current methods for this feature pass on the retained Dev fixtures. Canonical60631296 is delivered with exact receipt/posted earnings/18balanced ledger lines, selected-item quote without refund submission and four immutable reviews; actual participant chat round trips are retained from the same lifecycle. Settings15/15 distinct methods,partner10/10,restaurant earnings1/1,navigation2/2,admin-money1/1 and exact beneficiary queue1/1 pass. Original failed invocations remain separately recorded; no duplicate lifecycle or server cleanup. See the checkpoint113 release evidence and feature-specific artifacts. Unselected/outcome/provider/internal/routed/duration/rate/SSE cases remain outside this acceptance.
## 2026-10-05T07:02:09+05:30 — checkpoint107: exact money, selected-item quote and no-rating guard pass

Existing retained-delivered Happy branch invocation1 passes1/1 (23.304s),0failures/errors/skips. Rendered receipt and rider trip agree with actual admin money: CARD/SUCCESS,total₹72.81,food₹46.67,GST₹2.34,delivery₹18.80,platform₹5.00,restaurant net₹34.67,rider net₹21.16. All18 ledger lines balance; exact restaurant/rider posted payouts match. No refund exists and no refund request was submitted. Selected-item partial quote matches visible Items+GST, retains its context and enables the final request button without clicking it. Evidence56 preserves counts/numbers and the no-new-order result.

Existing delivered read-only batch invocation1 passes2/2: CustomerSettings unrated dialog requires at least three target groups, its unrated submit stays disabled and no review POST occurs (11.608s); PartnerReadOnly exact rider trip shows the owned outlet, Delivered/date and positive payout (6.045s). Evidence57 retains both independent counts. They reuse60631296; no new checkout, review or refund write.

Existing OrderReviewsFlowTest#participantsReviewEachOther invocation1 is running on that same owned delivered order after the no-rating guard. It intentionally writes four dummy participant reviews through real UI and then verifies submitted targets read-only; old direct aggregate/cache methods remain disabled/deferred. Remaining: read-only settings/partner screens, restaurant earnings/statement/navigation/admin-money, final O4/O5 gates on UI509d084, metrics and final docs/checklists. Stop after O4/O5.

## Local browser-routed coverage

`AdminLedgerAdvancedTest` now checks a supported category filter, owner-type validation before network access, API and row-level amount/date/account values, strict next/previous pagination, blank payout search prevention, unknown-payee empty results, and positive balances in the pending payout queue. Existing statement-panel coverage remains disabled because the ledger view has no detail panel.

`AdminLedgerMoneyReadOnlyRoutedUiTest` verifies ledger filters, pagination, payout history, and the routed order-money panel through a browser-local fixture. `AdminMoneyOperationsSafetyUiTest` verifies rejection resolution and payment/wallet retry success and failure contracts without touching shared financial records.

`AdminPayoutSafetyUiTest` uses a stateful browser-local fixture to cover draft creation, an explicit
unverified-bank override, ambiguous draft-create retry recovery, second-admin approval, draft
cancellation, mark-paid, mark-failed, and a rejected failure transition. It asserts each exact
endpoint, payload or query, idempotency header, and the resulting rendered state. This is local
UI-contract evidence; it is not proof of a deployed ledger or payment transition.

Deployed Dev validation: `AdminLedgerAdvancedTest` ran 13 tests with 0 failures, 0 errors, and 1 fixture-dependent skip. The map-related error is isolated to `AdminLiveOpsFleetTest`; no ledger or payout assertion failed.

## Source-verified scope corrections

- The ledger exposes transaction ID, owner ID/type, category, and direction filters. It has no date-range controls, entity names, direction column, or statement detail panel. The categories come from the backend enum; `ORDER_PAYMENT` is not a valid category.
- The four Money Operations tabs are Ledger Rejections, Reconciliation Runs, Payment DLQ, and Wallet DLQ.
- `AdminOrderMoney.tsx` is routed at `/admin/orders/:orderId/money` and is reachable from payout history. Browser-routed coverage verifies that route and its read-only rendering. A real order-money assertion still needs a controlled deployed order fixture.
- Payout history is searched by payee UUID and payee type. Search is disabled for a blank UUID. An unknown payee currently leaves the generic "Search Payouts" prompt; it does not show a distinct not-found message.
- Committed payout actions are covered only through disposable browser-local fixtures. A target-environment lifecycle run still requires an isolated payee, a second pre-provisioned administrator for the four-eyes approval, and safe financial cleanup.
