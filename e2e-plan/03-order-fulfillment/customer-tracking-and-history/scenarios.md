# 03 — Customer Tracking and History — All Scenarios

Uses: `CustomerOrderTrackerPage`, `CustomerActiveOrdersCarouselPage`, `MapTrackingPage`, `CustomerOrderHistoryPage`, `ReorderStripPage`.

## Batch 1 — Active order tracking status updates

| ID | Description | Action | Expected result |
|---|---|---|---|
| TRACKING-01 | Order Placed status | Immediately after checkout. | Exact full order ID is retained; CREATED reconciles to PENDING_ACCEPTANCE and the rendered Waiting for Restaurant status. |
| TRACKING-02 | Restaurant Accepted status | After restaurant accepts the order. | Exact tracker reaches ACCEPTED without manual reload through current adaptive order polling; map coordinates use a separate SSE stream. |
| TRACKING-03 | Preparing status | After restaurant begins preparation. | Tracker shows "Preparing" with estimated time if available. |
| TRACKING-04 | Ready for Pickup status | After restaurant marks food ready. | Tracker shows "Ready for Pickup" or "Looking for rider". |
| TRACKING-05 | Rider Assigned status | After rider accepts dispatch. | Tracker shows rider name (or masked) and "On the way to restaurant". |
| TRACKING-06 | Picked Up status | After rider marks picked up. | Exact tracker shows Courier on the way and owned delivery code; rider phase is OUT_FOR_DELIVERY. |
| TRACKING-07 | Delivered / Completed status | After rider completes delivery. | Tracker shows "Delivered" / "Order Completed". |

## Batch 2 — Live map tracking

| ID | Description | Action | Expected result |
|---|---|---|---|
| TRACKING-08 | Map visible during active delivery | While rider is in transit to customer. | `MapTrackingPage` shows a map with a rider marker. |
| TRACKING-09 | Rider location non-zero | Inspect map marker coordinates. | Rendered courier coordinates are finite, in range and match the rider browser GPS. The unset(0,0)pair is absent; a single zero axis is valid. |
| TRACKING-10 | Map updates on rider movement | Rider advances; customer map marker should shift. | Map marker position changes within 30 s of rider location update. |

## Batch 3 — Active orders carousel

| ID | Description | Action | Expected result |
|---|---|---|---|
| TRACKING-11 | Carousel shows active order | Dismiss the selected tracker with Back while owned orders remain active. | `CustomerActiveOrdersCarouselPage` shows the active order card with current status. |
| TRACKING-12 | Tap order card opens tracker | Tap active order card. | `CustomerOrderTrackerPage` opens for that specific order. |
| TRACKING-13 | Multiple active orders | If two orders are placed simultaneously (edge case). | Carousel shows two cards; tapping each opens the correct tracker. |

## Batch 4 — Order history

The current customer UI exposes history through Account Settings → History. It renders a defined empty state or clickable order summaries. The older standalone `CustomerOrderHistory` overlay remains unmounted.

| ID | Description | Action | Expected result |
|---|---|---|---|
| HISTORY-01 | Completed order in history | Open Account Settings → History. | Requires actual populated retained history; successful-empty/loading/error contracts are separate tests and cannot satisfy the populated prerequisite. |
| HISTORY-02 | History item shows restaurant name | Open Account Settings → History. | Implemented for every rendered row; restaurant must be nonblank and cannot contain `Unknown`, `null`, or `undefined`. |
| HISTORY-03 | History item shows date | Open Account Settings → History. | Each populated row renders a visible readable time with a valid ISO datetime; the real populated branch is required. |
| HISTORY-04 | History item shows total | Open Account Settings → History. | Implemented for every rendered row through an INR total and nonempty status assertion. |
| HISTORY-05 | Tap history item for details | Open Account Settings → History. | Implemented: clicking a populated row must close settings and open the exact order tracker. |
| HISTORY-06 | Cancelled order in history | Open Account Settings → History. | The real retained restaurant-cancelled row opens its exact CANCELLED_BY_RESTAURANT details; dismissing via Back retains the row. |

## Batch 5 — Reorder

| ID | Description | Action | Expected result |
|---|---|---|---|
| REORDER-01 | Reorder strip visible | On the customer home feed with real completed history. | Order it again shows full-ID buttons with previous totals, deduplicated per outlet. |
| REORDER-02 | Reorder adds items to cart | Tap "Reorder". | Owned completed order resolves exact current eligible outlet/catalogue and current price; an address-bound quote validates all lines; original quantities restore atomically and cart opens. No order is submitted. |
| REORDER-03 | Reorder from unavailable outlet | If the original outlet is now > 5 km or closed. | UI shows a warning instead of silently adding items. |

## Batch 6 — Order Tracker advanced features (`CustomerOrderTrackerPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| TRACKER-ADV-01 | Total paid display | On the order tracker. | Exact-owned bill opens and Total paid equals the authoritative created order total; no guessed positive amount. |
| TRACKER-ADV-02 | Payment method display | On the order tracker. | Exact-owned bill shows the authoritative supported method, e.g. Paid via CARD. Current backend supports CARD/UPI/WALLET; Cash is not a valid expected fixture. |
| TRACKER-ADV-03 | Rider assigned indicator | After rider accepts dispatch. | `hasRiderAssigned()` returns true; rider name/status visible. |
| TRACKER-ADV-04 | Dismiss failed order | On a failed/cancelled order tracker, dismiss its details through the reachable Back button via `dismissFailedOrder()`. | Details close and customer returns to home; the order remains in history and server data is retained. |
| TRACKER-ADV-05 | Approve delay prompt | When restaurant requests delay, `approveDelay()`. | Visible confirmation sends approved=true for the exact order; real restaurant/customer convergence is verified in rejection-cancellation-and-delays, not inferred from local optimistic state. |
| TRACKER-ADV-06 | Reject delay (cancel order) | When restaurant requests delay, `rejectDelay()`. | Confirmation sends approved=false; server settles CANCELLED_BY_RESTAURANT with Customer rejected delay. Real convergence is verified in rejection-cancellation-and-delays. |

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

## Additional recovery and boundary contracts

History first-page failure remains distinct from empty and can retry; pagination failure keeps loaded rows; loaded IDs remain unique. Reorder confirms replacement and preserves an edited cart if declined; missing/invalid catalogue lines cannot create a partial cart; stale address/cart responses and duplicate clicks cannot commit outdated quantities. Unit/routed contracts are explicitly separate from actual Dev transactions. Map selection/mode changes release prior resources; invalid fixes never plot; HTTP503/non-SSE200/EOF trigger honest recovery and authorization refusal terminates retries. These immediate contracts do not execute quota exhaustion or expiry waits.
