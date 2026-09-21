# 06 — Ledger, Payouts, and Order Money — All Scenarios

**Financial Integrity Rule**: All ledger and payout amounts must reflect real transaction data. No hardcoded assertions. Missing amounts = test failure.

Uses: `AdminOperationsPage`, `AdminOrderMoneyPage`, `AdminPayoutsPage`, `TransactionHistoryTablePage`.

## Batch 1 — Ledger entries

| ID | Description | Action | Expected result |
|---|---|---|---|
| LEDGER-01 | Ledger tab accessible | Login as admin → navigate to Ledger Entries. | `AdminOperationsPage` Ledger section renders. |
| LEDGER-02 | Filters heading visible | On ledger page. | "Filters" heading or filter section visible. |
| LEDGER-03 | Enter filter string | Type a restaurant name in the restaurant filter field. | Text accepted; not cleared automatically. |
| LEDGER-04 | Enter date range filter | Enter start and end dates. | Dates accepted in the filter fields. |
| LEDGER-05 | Apply filters | Tap "Apply Filters". | Ledger list filters to matching entries; or shows "No results" if none match. |
| LEDGER-06 | Clear filters | Tap "Clear" / clear fields manually. | Both filter fields emptied; "Apply Filters" remains enabled. |
| LEDGER-07 | Ledger entry amounts non-null | On entries shown. | Every entry amount is ≥ ₹0 and not `null`; credit/debit labels present. |
| LEDGER-08 | Ledger entry has timestamp | Each entry. | Date/time column non-empty. |
| LEDGER-09 | Ledger entry has entity (restaurant/rider) | Each entry. | Source entity name non-empty. |

## Batch 2 — Order money panels

| ID | Description | Action | Expected result |
|---|---|---|---|
| MONEY-01 | Operations tabs render | Cycle through each tab in `AdminOrderMoneyPage` (e.g. Pending, Processing, Completed). | Each tab renders with appropriate panel; no blank panels. |
| MONEY-02 | Orders in each panel | Each money panel shows orders or an empty state. | No crash; empty-state message if no orders in that status. |
| MONEY-03 | Amount column non-null | In any money panel with orders. | Amount field ≥ ₹0; no `null` values. |

## Batch 3 — Payouts

| ID | Description | Action | Expected result |
|---|---|---|---|
| PAYOUT-01 | Payout history form | Navigate to payout History → open search form. | UUID input field visible; search prompt shown. |
| PAYOUT-02 | Search payout by UUID | Enter a known payout UUID → tap Search. | Matching payout record shown; or "Not found" message for unknown UUID. |
| PAYOUT-03 | Search payout with empty UUID | Tap Search with blank UUID field. | Validation error; no blank search submitted. |
| PAYOUT-04 | Return to pending queue | After viewing history, tap "Back" / "Pending Queue". | Returns to pending payout queue; no crash. |
| PAYOUT-05 | Pending payout list | Open pending queue. | List of pending payouts shown with entity (restaurant/rider) and amount. |
| PAYOUT-06 | Approve payout | Tap "Approve" on a pending payout. | Payout status changes to "Processing" or "Approved"; entry moves to appropriate tab. |
| PAYOUT-07 | Payout amount non-zero | All pending payouts. | Every payout amount > ₹0. Zero-amount payouts should not appear in the queue. |

## Batch 4 — Advanced Ledger Filters (`AdminLedgerPage`)

Full filter suite: transaction ID, owner ID, owner type, category, direction, date range, pagination, and statement detail panel.

| ID | Description | Action | Expected result |
|---|---|---|---|
| LEDGER-ADV-01 | Ledger view visible | Admin opens Ledger tab. | `AdminLedgerPage.isLedgerVisible()` returns true; filter inputs visible. |
| LEDGER-ADV-02 | Filter by transaction ID | Enter a known txnId via `filterByTransactionId()` → `applyFilter()`. | Results filtered to matching transaction only. |
| LEDGER-ADV-03 | Filter by owner ID | Enter a known ownerId via `filterByOwnerId()` → `applyFilter()`. | Results filtered to that owner's transactions only. |
| LEDGER-ADV-04 | Filter by owner type | Select "RESTAURANT" via `selectOwnerType("RESTAURANT")` → `applyFilter()`. | Only restaurant-owned transactions shown. |
| LEDGER-ADV-05 | Filter by category | Select a category (e.g., "ORDER_PAYMENT") via `selectCategory()` → `applyFilter()`. | Only transactions in that category shown. |
| LEDGER-ADV-06 | Filter by direction (credit/debit) | Select "CREDIT" via `selectDirection("CREDIT")` → `applyFilter()`. | Only credit transactions shown. |
| LEDGER-ADV-07 | Filter by date range | Set from/to dates via `selectDateRange()` → `applyFilter()`. | Only transactions within date range shown. |
| LEDGER-ADV-08 | Combined filters | Set owner type + category + direction → `applyFilter()`. | Results match all filter criteria simultaneously. |
| LEDGER-ADV-09 | Clear filters resets all | Click "Clear" via `clearFilters()`. | All filter fields reset; full unfiltered result set displayed. |
| LEDGER-ADV-10 | Transaction count | After applying filter. | `getTransactionCount()` returns count matching expectations (> 0 for valid filter). |
| LEDGER-ADV-11 | Open statement detail panel | Click a transaction row via `openStatement(0)`. | `isStatementPanelOpen()` returns true; statement entries/details visible. |
| LEDGER-ADV-12 | Ledger pagination — next page | With many records, click "Next" via `nextPage()`. | Page advances; `getPageInfo()` shows updated page number (e.g., "Page 2"). |
| LEDGER-ADV-13 | Ledger pagination — prev page | After navigating to page 2, click "Prev" via `prevPage()`. | Returns to page 1. |
| LEDGER-ADV-14 | No results state | Filter by a non-existent txnId → `applyFilter()`. | "No results" / empty state displayed; `getTransactionCount()` returns 0. |

## Batch 5 — Additional Payout actions (`AdminPayoutsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| PAYOUT-08 | Reject payout | Tap "Reject" on a pending payout via `rejectPayout()`. | Payout status changes to "Rejected"; entry removed from pending queue. |
| PAYOUT-09 | Create draft payout | Tap "Create Payout" → `createPayout()`. | Draft payout created; confirm dialog appears via `confirmCreateDraftPayout()`. |
| PAYOUT-10 | Force-create payout | Use `forceCreatePayout()` to bypass validation. | Payout created immediately; appears in pending queue. |
| PAYOUT-11 | Open payout history tab | `openHistory()` → `openHistoryPayout()`. | History tab shows processed payouts with timestamps and amounts. |
| PAYOUT-12 | Open pending payout | `openPendingQueue()` → `openPendingPayout()`. | Pending payout details visible with approve/reject buttons. |

