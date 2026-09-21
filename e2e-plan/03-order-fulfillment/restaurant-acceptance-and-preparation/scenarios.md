# 03 — Restaurant Acceptance and Preparation — All Scenarios

Requires: seeded customer has placed an order (or order exists in "Incoming" state). Seeded restaurant (9000000001) is logged in. Uses `RestaurantDashboardPage`, `RestaurantOrderQueuePage`, `RestaurantOrderActionsPage`, `RestaurantOrderDetailsModalPage`.

## Batch 1 — Incoming order queue

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-01 | Incoming tab visible | Login as restaurant. | "Incoming" tab visible in the order queue navigation. |
| REST-ACCEPT-02 | Incoming order appears | After customer places an order for this restaurant. | Order card appears in the Incoming tab within a reasonable wait (60 s max via polling); card shows customer name, item list, and total. |
| REST-ACCEPT-03 | Order details modal | Click on the incoming order card. | `RestaurantOrderDetailsModalPage` opens; shows itemised list, delivery address district, and order ID. |
| REST-ACCEPT-04 | Modal dismissal | Open order details modal → tap close. | Modal closes; order card still in Incoming queue. |

## Batch 2 — Accepting an order

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-05 | Accept button visible | On incoming order, via `RestaurantOrderActionsPage`. | "Accept" button visible and enabled. |
| REST-ACCEPT-06 | Accept moves order to Preparation | Tap "Accept" on incoming order. | Order card disappears from Incoming tab; appears in "Preparation" (or "Preparing") tab. |
| REST-ACCEPT-07 | Customer UI updates on accept | Simultaneously observe customer context. | Customer tracker status changes to "Preparing" / "Restaurant accepted". |
| REST-ACCEPT-08 | Accept is idempotent UI-wise | Tap Accept → observe → reload restaurant page. | Order is still in Preparation tab; not duplicated. |

## Batch 3 — Rejecting an order

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-09 | Reject button visible | On incoming order. | "Reject" button visible alongside Accept. |
| REST-ACCEPT-10 | Reject moves order out of queue | Tap "Reject" (provide reason if prompted). | Order disappears from Incoming; customer tracker shows "Cancelled by Restaurant". |
| REST-ACCEPT-11 | Reject reason form (if applicable) | If rejection requires a reason, submit without typing a reason. | Form validation prevents submission; reason field gets focus or error shown. |

## Batch 4 — Preparation flow

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-12 | Preparation tab visible | After accepting. | "Preparation" or "Preparing" tab visible with the accepted order. |
| REST-ACCEPT-13 | Mark Ready button | On the order in Preparation. | "Mark Ready" (or "Food Ready") button visible. |
| REST-ACCEPT-14 | Mark Ready moves to Ready tab | Tap "Mark Ready". | Order appears in "Ready for Pickup" tab; disappears from Preparation. |
| REST-ACCEPT-15 | Rider notified on Ready | After marking ready (prep complete or < 15 min remaining). | Rider context receives dispatch ping (`DispatchPingPage` visible). |

## Batch 5 — Preparation time constraint

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-16 | Prep time indicator visible | On order in Preparation tab. | A timer or estimated-ready time is displayed (e.g. "Ready in 12 min"). |
| REST-ACCEPT-17 | Early ready attempt (> 15 min remaining) | If the system blocks "Mark Ready" when more than 15 min remains. | "Mark Ready" is disabled or shows a "Too early" message. *(Document actual behavior — may differ.)* |
| REST-ACCEPT-18 | 15-min threshold unlocks dispatch | After restaurant marks ready AND prep time is ≤ 15 min remaining (or complete). | Dispatch ping becomes visible to available rider. |

## Batch 6 — Restaurant queue tab states

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-ACCEPT-19 | All tabs render | Cycle through Incoming, Preparation, Ready for Pickup, Completed tabs. | Each tab renders without crash; empty-state message shown when no orders. |
| REST-ACCEPT-20 | Completed tab shows history | After a full order cycle, navigate to Completed tab. | Completed order appears with final status. |

## Batch 7 — Restaurant Dashboard tab navigation (`RestaurantDashboardPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| REST-NAV-01 | Live Kitchen Feed tab opens | `RestaurantDashboardPage.openOrdersTab()` (maps to "Live Kitchen Feed"). | Live Kitchen Feed view renders; incoming/active orders visible. |
| REST-NAV-02 | Menu tab opens | `RestaurantDashboardPage.openMenuTab()`. | Menu editor/stock toggles page renders. |
| REST-NAV-03 | Settings tab opens | `RestaurantDashboardPage.openSettingsTab()`. | Restaurant settings visible. |
| REST-NAV-04 | Earnings tab opens | `RestaurantDashboardPage.openEarningsTab()`. | Restaurant earnings summary renders. |
| REST-NAV-05 | Reviews tab opens | `RestaurantDashboardPage.openReviewsTab()`. | Customer reviews list visible. |
| REST-NAV-06 | Restaurant outlet selector | `selectOutlet("Outlet 1")`. | Dashboard refreshes to show data for the selected outlet; no crash. |
| REST-NAV-07 | Rapid tab switching | Rapidly switch between all 5 tabs. | No stale data; each tab shows its own content without bleed-through. |

