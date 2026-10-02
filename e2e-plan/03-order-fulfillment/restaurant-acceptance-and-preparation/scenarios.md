# 03 — Restaurant Acceptance and Preparation — All Scenarios

Requires: seeded customer has placed an order (or order exists in "Incoming" state). Seeded restaurant (9000000001) is logged in. Uses `RestaurantDashboardPage`, `RestaurantOrderQueuePage`, `RestaurantOrderActionsPage`, `RestaurantOrderDetailsModalPage`.

## Batch 1 — Incoming order queue

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-01 | Incoming region visible | Log in as restaurant. | Incoming Kanban region renders with its order count; there is no Incoming tab. |
| REST-ACCEPT-02 | Exact incoming order | Create a canonical quoted Dev order or resume its guarded owned manifest. | Restaurant CREATED card matches the full ID, customer, dish quantities and money labels; the customer service calls the same phase PENDING_ACCEPTANCE. |
| REST-ACCEPT-03 | Restaurant itemized order details | Open the exact card’s Order details button. | The named dialog contains this restaurant order’s dishes and financial breakdown. Restaurants require dish contents to prepare the package; riders do not. |
| REST-ACCEPT-04 | Modal dismissal | Open order details modal → tap close. | Modal closes; order card still in Incoming queue. |

## Batch 2 — Accepting an order

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-05 | Accept button visible | On incoming order, via `RestaurantOrderActionsPage`. | "Accept" button visible and enabled. |
| REST-ACCEPT-06 | Accept enters kitchen | Accept the exact incoming order. | Own accept POST200; card moves from Incoming to In the kitchen with ACCEPTED status. |
| REST-ACCEPT-07 | Customer sees acceptance | Observe the exact customer tracker through ordinary polling. | Own tracker reaches ACCEPTED and hides Cancel order. SSE transport is separately deferred. |
| REST-ACCEPT-08 | Acceptance survives reload | Accept, then reload the restaurant. | Exactly one full-ID card remains ACCEPTED in the selected outlet. This proves UI persistence, not duplicate-request concurrency. |

## Batch 3 — Rejecting an order

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-09 | Reject control | Inspect the exact incoming card. | Reject is enabled alongside Accept. |
| REST-ACCEPT-10 | Reject exact owned order | Submit Item out of stock. | Own reject POST200; card leaves queue; own customer tracker reaches CANCELLED_BY_RESTAURANT with visible reason and remains terminal after reload. |
| REST-ACCEPT-11 | Required rejection reason | Open rejection; leave blank, enter whitespace, then Back. | Confirm Cancel is disabled for blank/whitespace; no reject POST; Back retains CREATED. The current required-field constraint is UI validation; backend direct-request policy is separate. |

## Batch 4 — Preparation flow

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-12 | Kitchen region | Accept the order. | In the kitchen region contains the exact ACCEPTED card and Start cooking. |
| REST-ACCEPT-13 | Mark ready control | Start cooking via own prepare POST200. | Exact PREPARING card exposes enabled Mark ready. |
| REST-ACCEPT-14 | Ready for pickup | Mark ready via own ready POST200. | Exact READY_FOR_PICKUP card enters Ready for pickup; handover code available. |
| REST-ACCEPT-15 | Rider dispatch | Accept quick-prep order and observe rider. | Exact assigned dispatch ping when preparation is within15min or complete. Ready is not the only trigger. |

## Batch 5 — Preparation time constraint

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-16 | Promised prep indicator | Inspect incoming Accept label, then accepted card. | Accept displays promised minutes; accepted card displays ready-by time. No countdown wait needed. |
| REST-ACCEPT-17 | Early ready is allowed | Local state test marks PREPARING ready with30min remaining. | Ready succeeds, persists READY_FOR_PICKUP and publishes one event; there is no greater-than15min prohibition. Duplicate ready is rejected without another event. |
| REST-ACCEPT-18 | 15min dispatch scheduling | Local mocked acceptance events with14min and30min remaining. | Within15min dispatches immediately; greater-than15min schedules at readyAt−15min. Actual elapsed browser threshold coverage is deferred, not claimed by the mock. |

## Batch 6 — Restaurant queue tab states

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-19 | Six Kanban columns | Log in without creating an order. | Incoming, In the kitchen, Waiting on customer, Ready for pickup, Out for delivery and Refund requests show consistent counts and defined zero-count states. |
| REST-ACCEPT-20 | Completed order history | Complete the accepted order, open settings Order History. | The exact completed order appears Delivered. Completed is not a Kanban tab. |

## Batch 7 — Restaurant Dashboard tab navigation (`RestaurantDashboardPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-NAV-01 | Orders section | Open Orders tab. | Current six-column kitchen board renders. |
| REST-NAV-02 | Menu section | Open Menu tab. | Today’s menu renders; kitchen board disappears. |
| REST-NAV-03 | Settings section | Open Restaurant registration and menu settings. | Restaurant Console Settings renders; Back to Kitchen Feed works. |
| REST-NAV-04 | Earnings section | Open Earnings tab. | Net Earnings renders. |
| REST-NAV-05 | Reviews section | Open Reviews tab. | What customers said region renders; this is navigation proof, not populated review evidence. |
| REST-NAV-06 | Outlet selection | Select two existing owned outlets and reload. | Selected label survives reload and changes between outlets. No outlet activation, stock or profile mutation. |
| REST-NAV-07 | Rapid section switching | Switch Menu, Campaigns, Earnings, Reviews, then Orders. | Final kitchen region renders and obsolete menu/reviews panels are hidden. |


## Current audit2026-10-02

See [audit11](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/11-restaurant-acceptance-and-preparation.md). Local/navigation/queue evidence and retained-command flows must be reported separately. No cleanup. Intentional duration and quota checks remain in the separate deferred files.
