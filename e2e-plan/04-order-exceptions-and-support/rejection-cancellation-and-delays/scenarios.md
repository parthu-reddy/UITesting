# 04 — Rejection, Cancellation, and Delays — All Scenarios

Uses: `RestaurantOrderActionsPage`, `CustomerOrderTrackerPage`, `DeliveryActiveJobPage`, `CustomerOrderHistoryPage`.

## Batch 1 — Restaurant rejects order

| ID | Description | Action | Expected result |
|---|---|---|---|
| CANCEL-01 | Restaurant sees reject button | With incoming order. | "Reject" button visible alongside "Accept". |
| CANCEL-02 | Reject triggers customer cancellation status | Restaurant taps "Reject". | Customer tracker shows "Cancelled by Restaurant" or "Order Rejected"; no "Delivered" label. |
| CANCEL-03 | Reject with reason (if required) | Tap Reject → provide reason → submit. | Rejection submitted; reason stored (visible to admin in support queue). |
| CANCEL-04 | Reject without reason blocked | Tap Reject → submit empty reason (if required). | Validation prevents blank reason submission. |
| CANCEL-05 | Rejected order in customer history | After rejection. | Order in history shows "Cancelled" status with restaurant as cancellation source. |
| CANCEL-06 | Refund initiated on rejection | After restaurant reject (paid order). | Customer notified of refund initiation; refund status visible in order details. |

## Batch 2 — Customer cancels order

| ID | Description | Action | Expected result |
|---|---|---|---|
| CANCEL-07 | Cancel button visible before restaurant accepts | Customer places order → immediately opens tracker. | "Cancel Order" button visible while status is "Order Placed". |
| CANCEL-08 | Cancel before accept removes from restaurant queue | Customer cancels → check restaurant context. | Order disappears from restaurant Incoming queue. |
| CANCEL-09 | Cancel button hidden after acceptance | Restaurant accepts → customer checks tracker. | "Cancel Order" button is no longer visible (too late to cancel). |
| CANCEL-10 | Customer cancel reason (if required) | Customer taps Cancel → provides reason. | Reason submitted; refund initiated. |
| CANCEL-11 | Cancelled order in history | After customer cancellation. | History shows the order as "Cancelled" with customer as initiator. |

## Batch 3 — Rider abandons / delays

| ID | Description | Action | Expected result |
|---|---|---|---|
| CANCEL-12 | Rider declines dispatch | Active order dispatched → rider declines ping. | Order re-dispatched to another available rider or enters support queue. Customer tracker shows appropriate delay message. |
| CANCEL-13 | Rider goes offline mid-delivery | Rider accepts job → toggles Offline. | Customer sees "Rider unavailable" / delay status; system re-assigns or escalates. |
| CANCEL-14 | No riders available | Order placed with no riders online (admin scenario). | Customer tracker shows "Searching for rider" for an extended period; no false "Rider found" status. |

## Batch 4 — Timeout / auto-cancellation

| ID | Description | Action | Expected result |
|---|---|---|---|
| CANCEL-15 | Restaurant timeout auto-cancel | Restaurant never accepts within system timeout (≥ 5 min). | Order auto-cancelled; customer notified; refund initiated. *(Requires env config — document timeout value.)* |
| CANCEL-16 | Order timeout UI | While waiting for restaurant for a long time. | Customer tracker shows a delay indicator or "This is taking longer than usual". |
