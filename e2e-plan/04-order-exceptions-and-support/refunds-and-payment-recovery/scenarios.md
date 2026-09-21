# 04 — Refunds and Payment Recovery — All Scenarios

**Financial Integrity Rule**: Never assert a ₹0 fallback or hardcoded refund amount. All refund amounts must be derived from the actual order total. If the refund amount is unavailable, the test must FAIL — not silently pass with ₹0.

Uses: `AdminRefundQueuePage`, `AdminSupportTicketsPage`, `RefundModalPage`, `RefundRequestModalPage`, `TransactionHistoryTablePage`.

## Batch 1 — Admin processes full refund

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-01 | Admin opens refund queue | Login as admin → navigate to Refunds / Support Tickets. | `AdminRefundQueuePage` renders; pending refund requests listed. |
| REFUND-02 | Locate ticket by order ID | Search or filter for the support ticket created in SUPPORT-04. | Ticket found; shows order ID, customer name, amount, and issue category. |
| REFUND-03 | Refund amount matches order total | On the refund form for a full-refund request. | Pre-filled amount equals the customer's original order total (no hardcoded value). |
| REFUND-04 | Approve full refund | Tap "Approve Refund". | Success message; ticket status changes to "Refund Approved" / "Resolved". |
| REFUND-05 | Customer sees Refunded status | Customer opens the order in history. | Order status shows "Refunded"; refund amount matches what admin approved. |
| REFUND-06 | Customer payment history updated | Customer opens wallet/payment history. | Refund entry appears with correct amount and date. |

## Batch 2 — Admin processes partial refund

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-07 | Open partial refund form | Admin selects "Partial Refund" option for a ticket. | Amount field pre-filled with full amount; editable. |
| REFUND-08 | Enter partial amount | Admin changes amount to half of the order total. | Amount field accepts numeric input; no negative or zero amount allowed. |
| REFUND-09 | Approve partial refund | Tap "Approve". | Success message; ticket resolved with partial amount. |
| REFUND-10 | Customer sees partial refund | Customer checks order. | Order shows "Partially Refunded"; amount matches the admin-approved partial amount. |

## Batch 3 — Refund rejection / denial

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-11 | Admin denies refund | Admin selects "Deny" on a refund request. | Ticket status changes to "Denied"; reason recorded if required. |
| REFUND-12 | Customer informed of denial | After denial. | Customer's order status does NOT show "Refunded"; no payment is credited. |

## Batch 4 — Refund amount validation (financial integrity)

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-13 | Zero-amount refund blocked | Admin enters ₹0 in refund amount → tap Approve. | Validation error prevents ₹0 refund submission. |
| REFUND-14 | Refund exceeds order total blocked | Admin enters amount > order total → tap Approve. | Validation error; refund capped at original order total. |
| REFUND-15 | Negative amount blocked | Admin enters -100 → tap Approve. | Validation error; negative refund rejected. |

## Batch 5 — Payment recovery after failed order

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-16 | Auto-refund on restaurant reject | After restaurant rejects a paid order. | Refund is initiated automatically; customer tracker shows "Refund Initiated". |
| REFUND-17 | Auto-refund on customer cancel | After customer cancels a paid order. | Refund initiated; amount correct; appears in customer payment history. |
| REFUND-18 | Transaction history table | Customer opens `TransactionHistoryTablePage` (if available). | Table shows credits, debits, refunds with accurate amounts and timestamps. |
