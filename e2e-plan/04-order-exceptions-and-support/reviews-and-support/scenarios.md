# 04 — Reviews and Support — All Scenarios

Prerequisite: at least one completed order in the customer's history. Uses: `RateOrderModalPage`, `CustomerReviewsPage`, `PostDeliverySupportModalPage`, `AdminSupportTicketsPage`.

## Batch 1 — Participants review order-related targets

| ID | Description | Action | Expected result |
|---|---|---|---|
| REVIEW-01 | Customer sees eligible targets | Customer opens a delivered order's review action. | The shared review dialog lists only that order's restaurant, assigned delivery partner, and purchased dishes. |
| REVIEW-02 | Customer submits ratings | Rate one or more targets and optionally comment on each rated target. | Only rated targets are submitted; success is shown. |
| REVIEW-03 | Customer review is immutable | Reopen the same order's review action. | Saved ratings/comments are visible without rating controls, text fields, or submit action. |
| REVIEW-04 | Restaurant reviews participants | Restaurant opens a delivered order and its review action. | Only Customer and Delivery partner targets are available; submit and reopen as read-only. |
| REVIEW-05 | Delivery partner reviews participants | Assigned rider opens the delivered order and its review action. | Only Customer and Restaurant targets are available; submit and reopen as read-only. |
| REVIEW-06 | Empty submission blocked | Open an eligible review dialog without selecting stars. | Submit stays disabled; a review cannot be saved without a rating. |
| REVIEW-07 | Ineligible target rejected | Attempt self-review, review of an unrelated dish, a target outside the actor's role matrix, or an order the actor did not participate in. | Backend rejects the request; no review is created. Covered by service/controller tests; not posted to the live immutable review endpoint. |

## Batch 2 — Restaurant sees review

| ID | Description | Action | Expected result |
|---|---|---|---|
| REVIEW-09 | Restaurant reviews tab | Login as restaurant → open Reviews tab. | Reviews list renders; most recent review visible with star count and text. |
| REVIEW-10 | Review shows customer name or "Anonymous" | On the review entry. | Customer identifier is non-empty ("Customer" or name); not `null`. |
| REVIEW-11 | Review timestamp | Each review entry shows a date/time. | Date is non-empty and in a readable format. |
| REVIEW-12 | Multiple reviews visible | If more than one review exists. | List shows all reviews; pagination works if many reviews exist. |

## Batch 3 — Post-delivery support ticket

| ID | Description | Action | Expected result |
|---|---|---|---|
| SUPPORT-01 | Help button on completed order | Customer opens a completed order. | "Help" / "Need Support" button visible. |
| SUPPORT-02 | Support modal opens | Tap "Help". | `PostDeliverySupportModalPage` opens with issue categories. |
| SUPPORT-03 | Select "Missing Item" category | Tap "Missing Item". | Category selected; text field for details appears. |
| SUPPORT-04 | Submit support ticket | Fill in details → tap Submit. | Success message; ticket created with a ticket ID. |
| SUPPORT-05 | Ticket ID is non-empty | After submission. | Ticket ID shown to customer; non-empty string. |
| SUPPORT-06 | Submit ticket without category blocked | Open support modal → tap Submit without selecting a category. | Validation prevents submission. |
| SUPPORT-07 | Support ticket in customer ticket list | After submission. | Ticket appears in customer's "My Tickets" or "Support History" list. |

## Batch 4 — Admin sees support ticket

| ID | Description | Action | Expected result |
|---|---|---|---|
| SUPPORT-08 | Admin support queue | Login as admin → open Support Tickets. | Ticket submitted in SUPPORT-04 appears in the queue. |
| SUPPORT-09 | Ticket shows correct category | On admin side. | Ticket category is "Missing Item" matching what customer selected. |
| SUPPORT-10 | Ticket shows order ID | On admin side. | Order ID on ticket matches the customer's order. |
| SUPPORT-11 | Admin can change ticket status | Admin selects a status (e.g. "In Review", "Resolved"). | Status updates and ticket moves to corresponding tab. |

## Batch 5 — Customer review history

| ID | Description | Action | Expected result |
|---|---|---|---|
| REVIEW-13 | My Reviews resolves | Customer opens Account Settings → My Reviews. | At least one review article or the defined empty state renders. Focused deployed check passed on 2026-09-29; older HTTP 403 is historical. |
