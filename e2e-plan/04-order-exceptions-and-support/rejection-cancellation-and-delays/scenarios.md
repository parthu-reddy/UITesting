# 04 — Rejection, Cancellation, and Delays — All Scenarios

Uses: `RestaurantOrderActionsPage`, `CustomerOrderTrackerPage`, `DeliveryActiveJobPage`, `CustomerOrderHistoryPage`.

## Implemented batch — Customer cancels before acceptance

| ID | Description | Result |
|---|---|---|
| CANCEL-07 | Cancel button visible before restaurant accepts | Implemented in `OrderCancellationFlowTest`. |
| CANCEL-11 | Cancelled exact order reaches terminal customer view | Implemented in `OrderCancellationFlowTest`; verifies the exact new order ID and terminal message. |
| CANCEL-02 | Restaurant rejection reaches customer | Implemented in `RestaurantRejectFlowTest`; live validation blocked before order creation. |
| CANCEL-03 | Restaurant supplies cancellation reason | Implemented with exact-order targeting and customer reason assertion; live validation pending. |

## Planned batch 1 — Restaurant rejects order

| ID | Description | Action | Expected result |
|---|---|---|---|
| CANCEL-01 | Restaurant sees reject button | With incoming order. | "Reject" button visible alongside "Accept". |
| CANCEL-04 | Reject without reason blocked | Tap Reject → submit empty reason (if required). | Validation prevents blank reason submission. |
| CANCEL-05 | Rejected order in customer history | After rejection. | Order in history shows "Cancelled" status with restaurant as cancellation source. |
| CANCEL-06 | Refund initiated on rejection | After restaurant reject (paid order). | Customer notified of refund initiation; refund status visible in order details. |

## Planned batch 2 — Remaining customer cancellation behavior

| ID | Description | Action | Expected result |
|---|---|---|---|
| CANCEL-08 | Cancel before accept removes from restaurant queue | Customer cancels → check restaurant context. | Order disappears from restaurant Incoming queue. |
| CANCEL-09 | Cancel button hidden after acceptance | Restaurant accepts → customer checks tracker. | "Cancel Order" button is no longer visible (too late to cancel). |
| CANCEL-10 | Customer cancel reason (if required) | Customer taps Cancel → provides reason. | Reason submitted; refund initiated. |

## Planned batch 3 — Rider abandons / delays

| ID | Description | Action | Expected result |
|---|---|---|---|
| CANCEL-12 | Rider declines dispatch | Active order dispatched → rider declines ping. | Order re-dispatched to another available rider or enters support queue. Customer tracker shows appropriate delay message. |
| CANCEL-13 | Rider goes offline mid-delivery | Rider accepts job → toggles Offline. | Customer sees "Rider unavailable" / delay status; system re-assigns or escalates. |
| CANCEL-14 | No riders available | Order placed with no riders online (admin scenario). | Customer tracker shows "Searching for rider" for an extended period; no false "Rider found" status. |

## Deferred batch 4 — Timeout / auto-cancellation

| ID | Description | Action | Expected result |
|---|---|---|---|
| CANCEL-15 | Restaurant timeout auto-cancel | Restaurant never accepts within system timeout (≥ 5 min). | Order auto-cancelled; customer notified; refund initiated. *(Requires env config — document timeout value.)* |
| CANCEL-16 | Order timeout UI | While waiting for restaurant for a long time. | Customer tracker shows a delay indicator or "This is taking longer than usual". |
