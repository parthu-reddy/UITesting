# 04 — Reviews and Support — All Scenarios

Prerequisite: at least one completed order in the customer's history. Uses: `RateOrderModalPage`, `CustomerReviewsPage`, `PostDeliverySupportModalPage`, `AdminSupportTicketsPage`.

## Batch 1 — Customer submits review

| ID | Description | Action | Expected result |
|---|---|---|---|
| REVIEW-01 | Rate Order button visible on completed order | Customer opens a completed order in history. | "Rate Order" / "Leave a Review" button visible. |
| REVIEW-02 | Review modal opens | Tap "Rate Order". | `RateOrderModalPage` opens with star rating for food and delivery. |
| REVIEW-03 | Star rating for food | Tap 4 stars on "Food" rating. | 4 stars highlighted. |
| REVIEW-04 | Star rating for delivery | Tap 5 stars on "Delivery" rating. | 5 stars highlighted. |
| REVIEW-05 | Text review field | Type "Great food, fast delivery!" in the text field. | Text appears in the field. |
| REVIEW-06 | Submit review | Tap "Submit". | Success toast/message shown; modal closes. |
| REVIEW-07 | Submit without rating blocked | Open review modal → tap Submit without selecting any stars. | Validation prevents submission; error shown asking for rating. |
| REVIEW-08 | Rate Order button hidden after review | After submitting a review. | "Rate Order" button is no longer visible for that order (already reviewed). |

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
| REVIEW-13 | My Reviews resolves | Customer opens Account Settings → My Reviews. | Implemented and active: at least one review article or the defined empty state renders. Live deployment currently fails because `/api/v1/reviews/me` returns HTTP 403; see `PENDING.md`. |
