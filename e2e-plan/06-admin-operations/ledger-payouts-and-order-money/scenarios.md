# 06 — Ledger, Payouts, and Order Money — All Scenarios

**Financial Integrity Rule**: All ledger and payout amounts must reflect real transaction data. No hardcoded assertions. Missing amounts = test failure.

Uses: `AdminOperationsPage`, `AdminOrderMoneyPage`, `AdminPayoutsPage`, `TransactionHistoryTablePage`.

## Batch 1 — Ledger entries

| ID | Description | Action | Expected result |
|---|---|---|---|
| LEDGER-01 | Ledger tab accessible | Login as admin → navigate to Ledger Entries. | `AdminOperationsPage` Ledger section renders. |
| LEDGER-02 | Filters heading visible | On ledger page. | "Filters" heading or filter section visible. |
| LEDGER-03 | Enter owner ID | Type a valid UUID in the Owner ID field. | Text is retained; the UI requires an owner type before sending the request. |
| LEDGER-04 | Select ledger filters | Select owner type, category, or CREDIT/DEBIT direction. | The selected backend enum value is retained and sent as a filter. |
| LEDGER-05 | Apply filters | Apply one or more supported filters. | A successful response renders matching rows or the explicit empty state. |
| LEDGER-06 | Clear filters | Tap "Clear" / clear fields manually. | Both filter fields emptied; "Apply Filters" remains enabled. |
| LEDGER-07 | Ledger entry amounts non-null | On entries shown. | The API amount is present and each displayed amount is a non-negative INR value. |
| LEDGER-08 | Ledger entry has timestamp | Each entry. | Date/time column non-empty. |
| LEDGER-09 | Ledger entry has account values | Each entry. | From/to cells contain an account UUID or the explicit "External / Unknown" state; the table does not show entity names or direction labels. |

## Batch 2 — Order money panels

| ID | Description | Action | Expected result |
|---|---|---|---|
| MONEY-01 | Money Operations tabs render | Open Money Operations and select Ledger Rejections, Reconciliation Runs, Payment DLQ, and Wallet DLQ. | Each selected tab renders its matching heading. |
| MONEY-02 | Order money breakdown | Open an order-specific admin money panel. | **Not currently reachable from `AdminPortal.tsx`:** `AdminOrderMoney.tsx` has no portal route or import, so this scenario cannot be driven through the deployed admin UI. |
| MONEY-03 | Order money amounts | Verify order money amounts and ledger trace. | Blocked with MONEY-02 until the admin UI exposes the order breakdown; the standalone component has UI unit coverage only. |

## Batch 3 — Payouts

| ID | Description | Action | Expected result |
|---|---|---|---|
| PAYOUT-01 | Payout history form | Navigate to payout History → open search form. | UUID input field visible; search prompt shown. |
| PAYOUT-02 | Search payout history by payee UUID | Enter a payee UUID → tap Search. | The request is scoped to that payee and type; matching payouts appear, or the current UI retains the generic "Search Payouts" prompt when none are found. |
| PAYOUT-03 | Search payout with empty UUID | Inspect the Search button with a blank UUID. | Search is disabled; no request can be submitted. |
| PAYOUT-04 | Return to pending queue | After viewing history, tap "Back" / "Pending Queue". | Returns to pending payout queue; no crash. |
| PAYOUT-05 | Pending payout list | Open pending queue. | Pending payees and unsettled balance are shown, or "All Caught Up!" appears. Each listed balance must be positive. |
| PAYOUT-06 | Approve payout | Approve a payout in an isolated disposable fixture only. | Payout status changes to "Processing" or "Approved"; entry moves to appropriate tab. Do not run against shared Dev data. |
| PAYOUT-07 | Payout amount non-zero | All pending payouts. | Every payout amount > ₹0. Zero-amount payouts should not appear in the queue. |

## Batch 4 — Advanced Ledger Filters (`AdminLedgerPage`)

Full filter suite: transaction ID, owner ID, owner type, category, direction, date range, pagination, and statement detail panel.

| ID | Description | Action | Expected result |
|---|---|---|---|
| LEDGER-ADV-01 | Ledger view visible | Admin opens Ledger tab. | `AdminLedgerPage.isLedgerVisible()` returns true; filter inputs visible. |
| LEDGER-ADV-02 | Filter by transaction ID | Enter a known txnId via `filterByTransactionId()` → `applyFilter()`. | Results filtered to matching transaction only. |
| LEDGER-ADV-03 | Owner ID/type validation | Apply an owner type without an owner ID. | UI shows validation and does not send a ledger request. |
| LEDGER-ADV-04 | Filter by owner type | Select a supported account type such as `RESTAURANT_PAYABLE` with an owner ID. | Request succeeds and any returned rows match the account filter. |
| LEDGER-ADV-05 | Filter by category | Select a category defined in `ChargeCategory` (for example `DELIVERY_FEE`). | Request succeeds and returned rows match the category. `ORDER_PAYMENT` is not a backend category. |
| LEDGER-ADV-06 | Filter by direction (credit/debit) | Select "CREDIT" via `selectDirection("CREDIT")` → `applyFilter()`. | Only credit transactions shown. |
| LEDGER-ADV-07 | Filter by date range | — | Not supported by the current ledger form or request model. |
| LEDGER-ADV-08 | Combined filters | Set owner type + category + direction → `applyFilter()`. | Results match all filter criteria simultaneously. |
| LEDGER-ADV-09 | Clear filters resets all | Click "Clear" via `clearFilters()`. | All filter fields reset; full unfiltered result set displayed. |
| LEDGER-ADV-10 | Transaction count | After applying filter. | `getTransactionCount()` returns count matching expectations (> 0 for valid filter). |
| LEDGER-ADV-11 | Open statement detail panel | — | Not supported by the current ledger view; rows only offer transaction-ID copy. Tracked in `e2e-plan/NOT-DEFECTS/README.md`. |
| LEDGER-ADV-12 | Ledger pagination — next page | With many records, click "Next" via `nextPage()`. | Page advances; `getPageInfo()` shows updated page number (e.g., "Page 2"). |
| LEDGER-ADV-13 | Ledger pagination — prev page | After navigating to page 2, click "Prev" via `prevPage()`. | Returns to page 1. |
| LEDGER-ADV-14 | No results state | Filter by a non-existent txnId → `applyFilter()`. | "No results" / empty state displayed; `getTransactionCount()` returns 0. |

## Batch 5 — Additional Payout actions (`AdminPayoutsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| PAYOUT-08 | Reject payout | Reject a payout in an isolated disposable fixture only. | Payout status changes to "Rejected"; entry removed from pending queue. Do not run against shared Dev data. |
| PAYOUT-09 | Create draft payout | Create a draft payout in an isolated disposable fixture only. | Draft payout appears with its persisted status. Do not run against shared Dev data. |
| PAYOUT-10 | Force-create payout | Exercise only with an isolated disposable fixture and an approved test plan. | Payout is created with a recorded reason and appears in the queue. Do not run against shared Dev data. |
| PAYOUT-11 | Open payout history tab | `openHistory()` → `openHistoryPayout()`. | History tab shows processed payouts with timestamps and amounts. |
| PAYOUT-12 | Open pending payout | `openPendingQueue()` → `openPendingPayout()`. | Pending payout details visible with approve/reject buttons. |
