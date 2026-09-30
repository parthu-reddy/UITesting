# Validation and pending work

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
