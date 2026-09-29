# 06 — Support and Refund Queues — All Scenarios

Uses: `AdminSupportTicketsPage`, `AdminRefundQueuePage`.

## Batch 1 — Support ticket queue

| ID | Description | Action | Expected result |
|---|---|---|---|
| SUPPORT-QUEUE-01 | Support tickets tab accessible | Login as admin → navigate to Support Tickets. | `AdminSupportTicketsPage` renders; tabs are OPEN, IN REVIEW, RESOLVED, REJECTED. |
| SUPPORT-QUEUE-02 | Open tickets listed | With open tickets in the system. | Cards show order ID prefix, customer ID prefix, reason, and creation time. |
| SUPPORT-QUEUE-03 | Ticket ID is non-empty | Select a ticket and open its detail panel. | Ticket detail opens; the card itself does not display the ticket UUID. |
| SUPPORT-QUEUE-04 | Visit each status tab | Cycle through OPEN, IN REVIEW, RESOLVED, REJECTED. | Each status request succeeds and renders rows or "No tickets found." |
| SUPPORT-QUEUE-05 | Approve support request | Select a ticket in a disposable fixture → open Resolve Ticket confirmation. | Amount is present and named in the confirmation; cancel for shared Dev data. |
| SUPPORT-QUEUE-06 | Reject support request | Select an OPEN ticket → enter notes → open Reject Request confirmation. | Confirmation appears; cancel without closing the ticket in shared Dev. |
| SUPPORT-QUEUE-07 | Ticket search | — | The current support queue has no search field. |
| SUPPORT-QUEUE-08 | Empty queue message | With no tickets in the selected status. | "No tickets found" appears instead of a blank list. |

## Batch 2 — Refund queue

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-QUEUE-01 | Refund queue accessible | Navigate to Refund Queue tab. | `AdminRefundQueuePage` shows rows or the explicit "Queue Empty" state. |
| REFUND-QUEUE-02 | Refund entry shows request data | Each refund row. | Ticket/order UUID prefixes, reason, requested amount, status, and creation time are visible; customer name is not in the table. |
| REFUND-QUEUE-03 | Refund amount present | On all pending refund entries. | A requested amount is shown or the row says "-" for a missing amount; approval is blocked by source validation if the amount is unavailable. |
| REFUND-QUEUE-04 | Approve refund | Open a ticket in a disposable financial fixture → open approval confirmation. | Confirmation states the amount and ledger consequence; cancel against shared Dev. |
| REFUND-QUEUE-05 | Reject refund | Open a ticket in a disposable fixture → open rejection confirmation. | Confirmation appears; cancel against shared Dev. |
| REFUND-QUEUE-06 | Approved refund credited to customer | After approval in an isolated fixture. | Customer order, refund record, and ledger amount agree. |
| REFUND-QUEUE-07 | Filter by status | Choose OPEN, IN REVIEW, RESOLVED, REJECTED, or ALL. | Queue request uses the selected status and renders rows or the empty state. There is no date filter. |

## Batch 3 — Support ticket advanced features (`AdminSupportTicketsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| SUPPORT-ADV-01 | Approve ticket | Select a ticket in an isolated fixture → use Resolve Ticket. | Approval requires a valid refund amount and an amount-specific confirmation; final approval is not run on shared Dev. |
| SUPPORT-ADV-02 | Reject ticket | Select OPEN ticket → add notes → open reject confirmation. | Rejection confirmation appears; final rejection requires an isolated support fixture. |
| SUPPORT-ADV-03 | Resolve with notes | — | No convenience shortcut is exposed by the current UI/page object. |
| SUPPORT-ADV-04 | Open ticket chat | Select a ticket → open details/chat. | Chat view opens; sending a message requires an explicitly isolated test conversation. |
| SUPPORT-ADV-05 | Ticket detail panel | `selectTicket(0)` → `isTicketDetailOpen()`. | Ticket Details heading and the selected order ID render. |
| SUPPORT-ADV-06 | Support pagination — next | With more than one page, `nextPage()`. | Request changes to page 2 and the page label advances. |
| SUPPORT-ADV-07 | Support pagination — prev | After navigating forward, `prevPage()`. | Request returns to page 1. |
| SUPPORT-ADV-08 | Open each status tab | Open each of OPEN, IN REVIEW, RESOLVED, REJECTED. | The matching status request succeeds and the queue shows rows or an explicit empty state. |
| SUPPORT-ADV-09 | Ticket count per tab | On each status tab. | Header count matches the visible ticket buttons. |

## Batch 4 — Refund queue ticket detail (`AdminRefundQueuePage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| REFUND-ADV-01 | Open refund ticket detail | `openRefundTicket(0)`. | Refund ticket detail panel opens with order info, customer, and requested amount. |
| REFUND-ADV-02 | Refund count | On refund queue. | `getRefundCount()` matches visible refund entries. |
| REFUND-ADV-03 | Approve via detail panel | After opening a ticket, open approval confirmation and cancel. | Confirmation renders without a resolve POST. A committed approval needs an isolated fixture. |
| REFUND-ADV-04 | Reject via detail panel | After opening a ticket, open rejection confirmation and cancel. | Confirmation renders without a resolve POST. A committed rejection needs an isolated fixture. |
