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

The current customer UI exposes history through Account Settings → History. It renders a defined empty state or clickable order summaries. The older standalone `CustomerOrderHistory` overlay remains unmounted.

| ID | Description | Action | Expected result |
|---|---|---|---|
| HISTORY-01 | Completed order in history | Implemented through the reachable settings History tab; the test accepts the defined empty state for seeded accounts without history. |
| HISTORY-02 | History item shows restaurant name | Implemented for every rendered row; restaurant must be nonblank and cannot contain `Unknown`, `null`, or `undefined`. |
| HISTORY-03 | History item shows date | Still pending: the reachable history row does not render a date. |
| HISTORY-04 | History item shows total | Implemented for every rendered row through an INR total and nonempty status assertion. |
| HISTORY-05 | Tap history item for details | Implemented: clicking a populated row must close settings and open the exact order tracker. |
| HISTORY-06 | Cancelled order in history | Status rendering is structurally covered; a deterministic cancelled-order history fixture remains pending. |

## Batch 5 — Reorder

| ID | Description | Action | Expected result |
|---|---|---|---|
| REORDER-01 | Reorder strip visible | On a completed order detail. | Still unreachable: settings history opens the tracker and renders no Reorder control. |
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
