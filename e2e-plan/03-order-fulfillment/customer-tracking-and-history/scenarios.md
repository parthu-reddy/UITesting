# 03 — Customer Tracking and History — All Scenarios

Uses: `CustomerOrderTrackerPage`, `CustomerActiveOrdersCarouselPage`, `MapTrackingPage`, `CustomerOrderHistoryPage`, `ReorderStripPage`.

## Batch 1 — Active order tracking status updates

| ID | Description | Action | Expected result |
|---|---|---|---|
| TRACKING-01 | Order Placed status | Immediately after checkout. | Tracker shows "Order Placed" status; order ID visible. |
| TRACKING-02 | Restaurant Accepted status | After restaurant accepts the order. | Tracker updates to "Restaurant Accepted" or "Preparing" — no manual reload needed (real-time via SSE/WebSocket). |
| TRACKING-03 | Preparing status | After restaurant begins preparation. | Tracker shows "Preparing" with estimated time if available. |
| TRACKING-04 | Ready for Pickup status | After restaurant marks food ready. | Tracker shows "Ready for Pickup" or "Looking for rider". |
| TRACKING-05 | Rider Assigned status | After rider accepts dispatch. | Tracker shows rider name (or masked) and "On the way to restaurant". |
| TRACKING-06 | Picked Up status | After rider marks picked up. | Tracker shows "On the way to you" or "Rider collected your order". |
| TRACKING-07 | Delivered / Completed status | After rider completes delivery. | Tracker shows "Delivered" / "Order Completed". |

## Batch 2 — Live map tracking

| ID | Description | Action | Expected result |
|---|---|---|---|
| TRACKING-08 | Map visible during active delivery | While rider is in transit to customer. | `MapTrackingPage` shows a map with a rider marker. |
| TRACKING-09 | Rider location non-zero | Inspect map marker coordinates. | Marker coordinates are non-zero / not at 0,0 origin. |
| TRACKING-10 | Map updates on rider movement | Rider advances; customer map marker should shift. | Map marker position changes within 30 s of rider location update. |

## Batch 3 — Active orders carousel

| ID | Description | Action | Expected result |
|---|---|---|---|
| TRACKING-11 | Carousel shows active order | While an order is in progress. | `CustomerActiveOrdersCarouselPage` shows the active order card with current status. |
| TRACKING-12 | Tap order card opens tracker | Tap active order card. | `CustomerOrderTrackerPage` opens for that specific order. |
| TRACKING-13 | Multiple active orders | If two orders are placed simultaneously (edge case). | Carousel shows two cards; tapping each opens the correct tracker. |

## Batch 4 — Order history

Current blocker: `CustomerOrderHistory` is implemented but not mounted anywhere in the customer UI; all scenarios in this batch remain unreachable through UI interaction or direct routing.

| ID | Description | Action | Expected result |
|---|---|---|---|
| HISTORY-01 | Completed order in history | After delivery completes. | Order appears in `CustomerOrderHistoryPage` with status "Completed" and correct total. |
| HISTORY-02 | History item shows restaurant name | In order history list. | Each order card shows the restaurant/brand name; no "Unknown" or `null`. |
| HISTORY-03 | History item shows date | Each history entry shows the order date/time. | Date is non-empty and formatted readably. |
| HISTORY-04 | History item shows total | Each history entry shows the final paid total. | Total ≥ ₹0; formatted with rupee symbol. |
| HISTORY-05 | Tap history item for details | Tap a completed order in history. | Order detail view or modal opens showing itemised breakdown. |
| HISTORY-06 | Cancelled order in history | After a cancellation. | Cancelled order appears in history with status "Cancelled"; no "Completed" label. |

## Batch 5 — Reorder

| ID | Description | Action | Expected result |
|---|---|---|---|
| REORDER-01 | Reorder strip visible | On a completed order detail. | `ReorderStripPage` shows a "Reorder" button. |
| REORDER-02 | Reorder adds items to cart | Tap "Reorder". | Same items as the original order are added to the cart; cart drawer opens. |
| REORDER-03 | Reorder from unavailable outlet | If the original outlet is now > 5 km or closed. | UI shows a warning instead of silently adding items. |

## Batch 6 — Order Tracker advanced features (`CustomerOrderTrackerPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| TRACKER-ADV-01 | Total paid display | On the order tracker. | `getTotalPaid()` returns a non-null, non-zero value (e.g., "₹250"). |
| TRACKER-ADV-02 | Payment method display | On the order tracker. | `getPaymentMethod()` returns "Paid via Cash" or "Paid via Wallet" — not blank. |
| TRACKER-ADV-03 | Rider assigned indicator | After rider accepts dispatch. | `hasRiderAssigned()` returns true; rider name/status visible. |
| TRACKER-ADV-04 | Dismiss failed order | On a failed/cancelled order tracker, tap "Dismiss" via `dismissFailedOrder()`. | Failed order notification cleared; customer returns to home/orders view. |
| TRACKER-ADV-05 | Approve delay prompt | When restaurant requests delay, `approveDelay()`. | Delay accepted; order continues; status updates accordingly. |
| TRACKER-ADV-06 | Reject delay (cancel order) | When restaurant requests delay, `rejectDelay()`. | Order cancelled; cancellation confirmation shown to customer. |

## Batch 7 — Customer Home Page (`CustomerHomePage`) and Free Delivery Tracker

| ID | Description | Action | Expected result |
|---|---|---|---|
| HOME-01 | Customer home search restaurant | Search for a displayed brand. | Implemented and live-passed in `RestaurantDiscoveryUiTest`; every remaining card matches. |
| HOME-02 | Restaurant count | On home page. | Implemented and live-passed; at least two cards and two distinct brands render. |
| HOME-03 | Restaurant visibility check | Find Brand1 after selecting Home. | Implemented through nearby Brand1 selection; an eligible outlet below 5 km is required. |
| HOME-04 | Open restaurant from home | Open Brand1 and select a nearby outlet. | Implemented and live-passed; menu rows render. |
| HOME-05 | Free delivery tracker visible | Add an orderable item through the menu UI. | Implemented and live-passed: named progressbar renders with a bounded value from 0 to 100. |
| HOME-06 | Free delivery remaining amount | On tracker. | Implemented and live-passed: UI shows `Add ₹… for Free Delivery!` or `Free Delivery Unlocked!`. |
| HOME-07 | Free delivery unlocked state | Increment an orderable item until the progress reaches its configured threshold. | Implemented and live-passed: progress reaches 100 and `Free Delivery Unlocked!` renders without checkout. |
