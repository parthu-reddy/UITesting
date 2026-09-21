# 06 — Support and Refund Queues — All Scenarios

Uses: `AdminSupportTicketsPage`, `AdminRefundQueuePage`.

## Batch 1 — Support ticket queue

| ID | Description | Action | Expected result |
|---|---|---|---|
| SUPPORT-QUEUE-01 | Support tickets tab accessible | Login as admin → navigate to Support Tickets. | `AdminSupportTicketsPage` renders; status tabs visible (Open, In Review, Resolved, Denied). |
| SUPPORT-QUEUE-02 | Open tickets listed | With open tickets in the system. | Ticket cards in "Open" tab; each shows ticket ID, customer name, order ID, issue category. |
| SUPPORT-QUEUE-03 | Ticket ID is non-empty | Each ticket card. | Ticket ID is a non-empty string/UUID. |
| SUPPORT-QUEUE-04 | Visit each status tab | Cycle through Open, In Review, Resolved, Denied. | Each tab renders its list or an empty-state; no crash. |
| SUPPORT-QUEUE-05 | Change ticket status to "In Review" | Select an Open ticket → change status to "In Review". | Ticket moves to "In Review" tab; no longer in "Open". |
| SUPPORT-QUEUE-06 | Change ticket status to "Resolved" | Select an "In Review" ticket → mark as "Resolved". | Ticket moves to "Resolved" tab. |
| SUPPORT-QUEUE-07 | Ticket search | If a search field exists, search by order ID or customer phone. | Matching ticket appears; non-matching disappears. |
| SUPPORT-QUEUE-08 | Empty Open queue message | With no open tickets. | Empty-state message in Open tab; not a blank page. |

## Batch 2 — Refund queue

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-QUEUE-01 | Refund queue accessible | Navigate to Refund Queue tab. | `AdminRefundQueuePage` renders; pending refund list visible. |
| REFUND-QUEUE-02 | Refund entry shows order details | Each pending refund entry. | Order ID, customer name, amount, and request reason visible. |
| REFUND-QUEUE-03 | Refund amount non-zero | All pending refund entries. | Amount > ₹0; no ₹0 refund requests in queue. |
| REFUND-QUEUE-04 | Approve refund | Tap "Approve" on a pending refund. | Refund status changes; entry moves to "Approved" section. |
| REFUND-QUEUE-05 | Deny refund | Tap "Deny" on a pending refund (provide reason if required). | Refund denied; entry moves to "Denied" section. |
| REFUND-QUEUE-06 | Approved refund credited to customer | After admin approval. | Customer's order shows "Refunded"; amount in customer payment history matches approved amount. |
| REFUND-QUEUE-07 | Filter by date | Filter refund queue by date range. | Only refund requests in the date range shown. |

## Batch 3 — Support ticket advanced features (`AdminSupportTicketsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| SUPPORT-ADV-01 | Resolve ticket with notes | Select ticket → `fillResolutionNotes("E2E resolution")` → `resolveTicket()`. | Ticket resolved; moved to Resolved tab with resolution note attached. |
| SUPPORT-ADV-02 | Reject ticket | Select ticket → `rejectTicket()`. | Ticket moved to Rejected tab. |
| SUPPORT-ADV-03 | Resolve with notes shortcut | `resolveTicketWithNotes("Quick resolution note")`. | Same as ADV-01 but via convenience method. |
| SUPPORT-ADV-04 | Open chat in ticket | Select ticket → `openChat()`. | Chat widget opens; can send message to the customer who filed the ticket. |
| SUPPORT-ADV-05 | Ticket detail panel | `selectTicket(0)` → `isTicketDetailOpen()`. | Detail panel shows full ticket info: issue, timestamps, order details. |
| SUPPORT-ADV-06 | Support pagination — next | With > 1 page of tickets, `nextPage()`. | Next page of tickets shown; different ticket IDs. |
| SUPPORT-ADV-07 | Support pagination — prev | After navigating forward, `prevPage()`. | Returns to previous page. |
| SUPPORT-ADV-08 | Open each status tab | `openOpenTickets()`, `openInReviewTickets()`, `openResolvedTickets()`, `openRejectedTickets()`. | Each tab renders correctly; no stale content from other tabs. |
| SUPPORT-ADV-09 | Ticket count per tab | On each tab. | `getTicketCount()` returns ≥ 0 and matches visible ticket rows. |

## Batch 4 — Refund queue ticket detail (`AdminRefundQueuePage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-ADV-01 | Open refund ticket detail | `openRefundTicket(0)`. | Refund ticket detail panel opens with order info, customer, and requested amount. |
| REFUND-ADV-02 | Refund count | On refund queue. | `getRefundCount()` matches visible refund entries. |
| REFUND-ADV-03 | Approve via detail panel | After opening ticket, `approveRefund()`. | Refund approved; entry status updated. |
| REFUND-ADV-04 | Reject via detail panel | After opening ticket, `rejectRefund()`. | Refund rejected; entry status updated. |

