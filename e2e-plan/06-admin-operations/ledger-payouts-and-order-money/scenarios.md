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
| MONEY-01 | Money Operations tabs render | Open Money Operations and select Ledger Rejections, Failed Refunds (added 2026-10-03), Reconciliation Runs, Payment DLQ, and Wallet DLQ. | Each selected tab renders its matching heading. |
| MONEY-02 | Order money breakdown | Open an order-specific admin money panel from a payout-history entry. | `/admin/orders/:orderId/money` renders the selected order's read-only money panel. Browser-routed coverage verifies the route and request contract. |
| MONEY-03 | Order money amounts | Verify order money amounts and ledger trace with a controlled order fixture. | Amounts and ledger references agree with the authoritative fixture. A real target check remains fixture-gated. |
| MONEY-04 | Failed DLQ retry | In a browser-local fixture, retry a payment webhook or wallet outbox event that returns a controlled conflict. | The error is visible, the original queue item remains, and retry becomes available again. A persisted target mutation remains fixture-gated. |
| MONEY-05 | Order money per outcome (2026-10-03) | `AdminOrderMoneyOutcomesTest`, `-Dmoney.outcomes=<id>:<DELIVERED|PARTIAL|CANCELLED|REJECTED>,...` on owned orders, read-only. | The panel shows payment method and status and every refund row (status, amount). Delivered: SUCCESS, no refunds, each payee booked its quoted payout. Partial restaurant-fault refund: PARTIALLY_REFUNDED, restaurant booked its payout less the clawback. Cancelled/rejected: REFUNDED, one COMPLETED full refund, neither payee booked anything. |

## Batch 3 — Payouts

| ID | Description | Action | Expected result |
|---|---|---|---|
| PAYOUT-01 | Payout history form | Navigate to payout History → open search form. | UUID input field visible; search prompt shown. |
| PAYOUT-02 | Search payout history by payee UUID | Enter a payee UUID → tap Search. | The request is scoped to that payee and type; matching payouts appear, or the current UI retains the generic "Search Payouts" prompt when none are found. |
| PAYOUT-03 | Search payout with empty UUID | Inspect the Search button with a blank UUID. | Search is disabled; no request can be submitted. |
| PAYOUT-04 | Return to pending queue | After viewing history, tap "Back" / "Pending Queue". | Returns to pending payout queue; no crash. |
| PAYOUT-05 | Pending payout list | Open pending queue. | Pending payees and unsettled balance are shown, or "All Caught Up!" appears. Each listed balance must be positive. |
| PAYOUT-06 | Approve payout | Approve a draft using an isolated disposable fixture that represents a permitted second administrator. | Payout status changes to `APPROVED`; Mark Paid and Fail Payout become available. Do not run against shared Dev data. |
| PAYOUT-07 | Payout amount non-zero | All pending payouts. | Every payout amount > ₹0. Zero-amount payouts should not appear in the queue. |

## Batch 4 — Advanced Ledger Filters (`AdminLedgerPage`)

Implemented filter suite: transaction ID, owner ID, owner type, category, direction, and pagination. The current ledger view has no date-range controls or statement-detail panel.

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
| PAYOUT-08 | Cancel draft payout | Cancel a draft in an isolated disposable fixture only. | Payout status changes to `CANCELLED`; approval and terminal actions disappear. Do not run against shared Dev data. |
| PAYOUT-09 | Create draft payout | Create a draft payout in an isolated disposable fixture only. | Draft payout appears with its persisted status. Do not run against shared Dev data. |
| PAYOUT-10 | Force-create payout | Exercise only with an isolated disposable fixture and an approved test plan. | Payout is created with a recorded reason and appears in the queue. Do not run against shared Dev data. |
| PAYOUT-11 | Open payout history tab | `openHistory()` → `openHistoryPayout()`. | History tab shows processed payouts with timestamps and amounts. |
| PAYOUT-12 | Open pending payout | `openPendingQueue()` → `openPendingPayout()`. | Pending payout details and a create-draft flow are visible; a draft detail exposes Approve and Cancel as appropriate. |
| PAYOUT-13 | Explicit unverified-bank override | Open a named but unverified payee in a browser-local fixture. | Create remains unavailable until the checkbox is selected; the intercepted request sends `force: true`. A persisted target override remains fixture-gated. |
| PAYOUT-14 | Ambiguous draft-create retry | Simulate a committed draft whose first response is lost, then retry from the same dialog. | The retry sends the original idempotency key and receives the existing draft rather than becoming a new request. |
| PAYOUT-15 | Four-eyes approval guard | Open a fixture draft whose `createdBy` matches the authenticated admin. | Approve stays visible but disabled and sends no payout write. |
