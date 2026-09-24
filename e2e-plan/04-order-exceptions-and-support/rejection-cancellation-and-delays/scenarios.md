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
| CANCEL-08 | Cancel before accept removes from restaurant queue | Implemented in `OrderCancellationFlowTest`; the exact order is first observed in Incoming, then must disappear after customer cancellation. |
| CANCEL-09 | Cancel button hidden after acceptance | Implemented in `RestaurantFulfillmentTest`; the customer reloads after exact-order acceptance and must not see Cancel Order. |
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

## Implemented batch 5 — Delay approval decision

| ID | Description | Result |
|---|---|---|
| DELAY-01 | Restaurant opens delay control for the exact incoming order | Implemented in `DelayApprovalFlowTest`. |
| DELAY-02 | Restaurant requests a 15-minute delay with a reason | Implemented using the rendered `+15 Min`, reason, and Submit Delay controls. |
| DELAY-03 | Customer sees the delay prompt | Implemented with exact prompt and Approve Delay assertions. |
| DELAY-04 | Customer approves delay | Implemented; verifies the prompt closes, customer status advances, and restaurant order becomes accepted. |
| DELAY-05 | Customer can reject the delay | Implemented through the prompt's Cancel Order action. |
| DELAY-06 | Rejected delay terminates customer order | Implemented with the exact tracked order's terminal headline. |
| DELAY-07 | Rejected delay leaves restaurant queue | Implemented; verifies the exact order card disappears after reload. |
