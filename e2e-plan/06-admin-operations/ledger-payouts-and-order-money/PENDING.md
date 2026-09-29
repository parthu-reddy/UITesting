# Validation and pending work

## Added read-only E2E coverage

`AdminLedgerAdvancedTest` now checks a supported category filter, owner-type validation before network access, API and row-level amount/date/account values, strict next/previous pagination, blank payout search prevention, unknown-payee empty results, and positive balances in the pending payout queue. Existing statement-panel coverage remains disabled because the ledger view has no detail panel.

`AdminLiveOpsFleetTest` checks the four Money Operations panels by selected tab, heading, and either rendered records or the panel's explicit empty state.

Validation performed: `mvn -q -DskipTests test-compile` passed. These browser E2E cases have **not** been executed against Dev in this turn: the configured Oracle tunnel hostnames do not resolve from this workstation. No deployed pass/fail result is inferred from compilation.

## Source-verified scope corrections

- The ledger exposes transaction ID, owner ID/type, category, and direction filters. It has no date-range controls, entity names, direction column, or statement detail panel. The categories come from the backend enum; `ORDER_PAYMENT` is not a valid category.
- The four Money Operations tabs are Ledger Rejections, Reconciliation Runs, Payment DLQ, and Wallet DLQ.
- `AdminOrderMoney.tsx` has component-level UI tests but is not imported or routed by `AdminPortal.tsx`; an admin E2E cannot open that breakdown today. Add a portal entry point before treating MONEY-02/03 as an end-to-end scenario.
- Payout history is searched by payee UUID and payee type. Search is disabled for a blank UUID. An unknown payee currently leaves the generic "Search Payouts" prompt; it does not show a distinct not-found message.
- Approve/reject/create/force-create payout scenarios still require a disposable financial fixture. The tests only open read-only views and confirmations that can be canceled.
