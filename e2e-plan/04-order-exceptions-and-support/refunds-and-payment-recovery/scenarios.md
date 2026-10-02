# 04 — Refunds and Payment Recovery — All Scenarios

**Financial Integrity Rule**: Never assert a ₹0 fallback or hardcoded refund amount. Full-order refund amounts must be derived from the original payment and remaining refundable balance; selected-item amounts must use the authoritative itemized quote, including eligible taxes. If the refund amount is unavailable, the test must FAIL — not silently pass with ₹0.

Uses: `AdminRefundQueuePage`, `AdminSupportTicketsPage`, `RefundModalPage`, `RefundRequestModalPage`, `TransactionHistoryTablePage`.

## Batch 1 — Admin processes full refund

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-01 | Admin opens refund queue | Login as admin → navigate to Refunds / Support Tickets. | `AdminRefundQueuePage` renders; pending refund requests listed. |
| REFUND-02 | Locate ticket by order ID | Search or filter for the support ticket created in SUPPORT-04. | Ticket found; shows order ID, customer name, amount, and issue category. |
| REFUND-03 | Refund amount matches order total | On the refund form for a full-refund request. | For a fully eligible untouched order, the quote equals the original refundable amount. Existing refunds/selected quantities constrain the remaining award; no hardcoded total/fallback. |
| REFUND-04 | Approve full refund | Tap "Approve Refund". | Success message; ticket becomes RESOLVED and the refund request is persisted. Payment completion is verified separately. |
| REFUND-05 | Customer sees Refunded status | Customer opens the order in history. | Order retains its terminal lifecycle status. The separate refund record shows its actual status and approved amount; COMPLETED is required before claiming money returned. |
| REFUND-06 | Customer payment history updated | Customer opens wallet/payment history. | Refund entry appears with correct amount and date. |

## Batch 2 — Admin processes partial refund

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-07 | Open partial refund form | Admin reviews a selected-item ticket; the controller supports a positive override at or below its quote. | Current UI confirms the stored quote; it has no partial-amount input. Test reduced overrides at the controller/service boundary; record editable UI as an unimplemented product capability, not an existing control. |
| REFUND-08 | Enter partial amount | Submit a controlled reduced amount at the existing API boundary, bounded by the ticket quote. | The existing API validates the numeric override; negative/zero and amounts above the quoted or remaining balance are rejected. No editable partial-amount UI control is claimed. |
| REFUND-09 | Approve partial refund | Tap "Approve". | Ticket RESOLVED with the approved partial amount; separate refund/payment completion verified. |
| REFUND-10 | Customer sees partial refund | Customer checks order. | Order retains its lifecycle status; refund amount and status match the approved partial award, and only completed money is shown as returned. |

## Batch 3 — Refund rejection / denial

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-11 | Admin denies refund | Admin selects "Deny" on a refund request. | Ticket status changes to REJECTED; resolution notes persisted. |
| REFUND-12 | Customer informed of denial | After denial. | Customer order lifecycle status stays unchanged; no refund or payment credit is created. |

## Batch 4 — Refund amount validation (financial integrity)

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-13 | Zero-amount refund blocked | Admin enters ₹0 in refund amount → tap Approve. | Validation error prevents ₹0 refund submission. |
| REFUND-14 | Refund exceeds order total blocked | Admin enters amount > order total → tap Approve. | Validation error; the request is rejected above the original ticket quote or remaining refundable payment balance. The service must not silently clamp a mismatched amount. |
| REFUND-15 | Negative amount blocked | Admin enters -100 → tap Approve. | Validation error; negative refund rejected. |

## Batch 5 — Payment recovery after failed order

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-16 | Auto-refund on restaurant reject | After restaurant rejects a paid order. | Refund is initiated automatically; customer tracker shows the authoritative refund amount, destination and current status without claiming completion prematurely. |
| REFUND-17 | Auto-refund on customer cancel | After customer cancels a paid order. | Refund initiated; amount correct; appears in customer payment history. |
| REFUND-18 | Transaction history table | Customer opens `TransactionHistoryTablePage` (if available). | Table shows credits, debits, refunds with accurate amounts and timestamps. |

Source contract checked 2026-10-02: `AdminRefundController#resolveTicket`, `RefundService`, `AdminSupportTickets.tsx`. Current admin UI supports quote approval/rejection; reduced override validation exists in the backend. Customer refund rendering requires a separate review before completion sign-off.


## Reused money boundaries and exact recovery

| ID | Description | Action / owning check | Expected result / current proof |
|---|---|---|---|
| REFUND-19 | Exact callback amount and refund remainder | Extend existing ConfirmedCaptureRefundPersistenceTest after its partial and full refund, using the same committed fixture. | Missing/malformed/zero/negative/sub-cent/over-remaining callbacks fail with unchanged intent/transaction balances, refund rows and outbox; duplicate valid callbacks stay idempotent. Local pass in checkpoint 16; new payment-gateway guard deployed/healthy; no live recovery proof. |
| REFUND-20 | Retry selects one original refund | Existing admin exact-refund retry endpoint, on an owned FAILED fixture only after safe downstream recovery is implemented. | Same refund/order/payment identity; atomic selected-row operation, correct destination, remaining cap, no unrelated global sweep, no duplicate provider initiation. Checkpoint18 implements and locally verifies selected-row atomic queueing, original identity/destination/cap and duplicate rejection. Deployment/runtime proof and safe downstream gateway recovery remain open; queueing is not completion. |
| REFUND-21 | Initiation differs from completion | Reuse retained cancellation/rejection resume helpers. | Accepted/PROCESSING is not returned money. Exact original refund must be COMPLETED with completedAt; customer/payment/ledger amount and destination agree. Current owned fixtures FAILED; whole flows remain unverified. |

| REFUND-22 | Recover missing capture from stored confirmation | ADMIN selects exact original processed payment confirmation; no caller money/receipt data. | Checkpoint19 local unit/committed concurrency/method security proof passes. Validate matching order/gateway/amount/time/balance, deny nonadmin/anonymous, duplicate no-op, unchanged refunds/intents/outbox/other orders. Deployment and owned live proof pending; capture recovery is not returned money. |

See AUDIT-STATUS.md and PENDING.md for current ownership/deployment gates. Do not create another happy delivery or perform database cleanup to satisfy these scenarios. Actual duration/rate-limit/SSE cases remain excluded.

