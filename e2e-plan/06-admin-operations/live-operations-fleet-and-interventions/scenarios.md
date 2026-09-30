# 06 — Live Operations, Fleet, and Interventions — All Scenarios

Uses: `AdminLiveOpsPage`, `AdminManualInterventionsPage`, `AdminPortalPage`.

## Batch 1 — Live orders map / board

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-OPS-01 | Live operations tab accessible | Login as admin → navigate to Live Operations. | `AdminLiveOpsPage` renders; active orders board or map visible. |
| ADMIN-OPS-02 | Active orders visible | With active orders in the system. | Cards show the first eight order ID characters, restaurant name, and NEEDS DRIVER or ASSIGNED. |
| ADMIN-OPS-03 | Active riders visible | On Fleet Map. | Rider records with coordinates produce rider or rider-offline markers; the legend shows total rider records, which can exceed plotted markers. |
| ADMIN-OPS-04 | No orders empty state | With no active orders. | Empty-state message shown; no crash or "undefined" markers on the map. |
| ADMIN-OPS-05 | Order card shows assignment state | Each active order card. | The card shows NEEDS DRIVER or ASSIGNED. The current list card does not expose customer area or the full order lifecycle status. |

## Batch 2 — Manual interventions

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-OPS-06 | Interventions panel accessible | Navigate to `AdminManualInterventionsPage`. | Panel renders with available intervention actions. |
| ADMIN-OPS-07 | Re-assign rider | Admin selects a delayed order → assigns to a different available rider. | Order reassigned; new rider's context receives dispatch ping; original rider context loses the active job. |
| ADMIN-OPS-08 | Re-assign with no available riders | Admin tries to re-assign with no other riders online. | Warning: "No available riders" shown; re-assignment blocked. |
| ADMIN-OPS-09 | Cancel and refund order | Admin selects an intervention and taps "Cancel & Refund (Normal)" in an isolated disposable fixture. | Order cancellation and refund state are verified across the customer, restaurant and rider views. Do not run against shared Dev data. |
| ADMIN-OPS-10 | Cancellation confirmation and reason | Enter a reason of at least five characters and request cancellation on a selected intervention. | An order-specific confirmation appears; canceling it sends no request. A confirmed action is covered only through an isolated fixture. |
| ADMIN-OPS-11 | Cancellation refund | After cancellation in an isolated paid-order fixture. | Refund queue shows the matching order and amount. Do not run against shared Dev data. |
| ADMIN-OPS-15 | Rejected cancellation recovery | Confirm a cancellation in a browser-local fixture that returns a rejection. | The intervention and typed reason remain visible, the control is enabled for correction, and no success notice or queue removal is shown. |

## Batch 3 — Fleet visibility

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-OPS-12 | Available driver count | Select an active order in Live Operations. | Assignment panel count matches its rendered Assign buttons, or shows "No available drivers nearby." |
| ADMIN-OPS-13 | Rider availability toggle reflected | Rider goes Online → refresh Fleet Map. | The rider legend/markers update after the map data reloads. Requires an isolated rider fixture. |
| ADMIN-OPS-14 | Active delivery shown on rider card | While rider is in active delivery. | The current order card shows assignment state; rider/order lifecycle detail is not shown in the list card. |

## Batch 4 — Fleet Map (`AdminFleetMapPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| FLEET-01 | Fleet tab accessible | Admin navigates to Fleet tab via `AdminPortalPage.openFleetTab()`. | `AdminFleetMapPage.isFleetMapVisible()` returns true; map renders. |
| FLEET-02 | Driver markers visible | With a rider that has coordinates. | Rider marker count is positive and each marker is tagged rider or rider-offline. If no plotted rider fixture exists, skip with a reason. |
| FLEET-03 | Select driver on map | Click a driver marker via `selectDriver(0)`. | `isDriverDetailVisible()` returns true; driver detail panel opens. |
| FLEET-04 | Driver name in detail panel | After selecting driver. | `getDriverName()` returns a non-empty string. |
| FLEET-05 | Refresh map updates markers | After a rider goes offline, click refresh via `refreshMap()`. | Driver marker count may decrease; map reloads without crash. |
| FLEET-06 | Empty fleet map | With no riders online. | `getDriverMarkerCount()` returns 0; empty-state or message shown. |

## Batch 5 — LiveOps Pagination and Refund actions (`AdminLiveOpsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| LIVEOPS-01 | LiveOps pagination — next | With > 1 page of orders, click "Next" via `nextPage()`. | Page advances; `getPageInfo()` shows "Page 2". |
| LIVEOPS-02 | LiveOps pagination — prev | After navigating to page 2, click "Prev" via `prevPage()`. | Returns to page 1. |
| LIVEOPS-03 | Select an active order | Select the first visible UUID-backed order card. | `isOrderSelected()` returns true and the assignment/refund panel appears. |
| LIVEOPS-04 | Refresh active orders | Click "Refresh" via `refresh()`. | Order list reloads; count may update; no crash. |
| LIVEOPS-05 | Available driver count | Select an order to open its assignment panel. | Heading count equals Assign buttons, or its explicit no-driver message is visible. |
| LIVEOPS-06 | Assign driver from LiveOps | Select an unassigned order in an isolated fixture. | Driver assignment is verified across the rider and order views. The current UI sends the mutation without a confirmation. |
| LIVEOPS-07 | Partial refund from LiveOps | Select an order in an isolated financial fixture. | Refund is verified in the refund queue and ledger. The current UI sends the mutation without a confirmation. |
| LIVEOPS-08 | Post-delivery refund from LiveOps | Select a delivered order in an isolated fixture. | Refund is verified in the refund queue and ledger. The current UI sends the mutation without a confirmation. |
| LIVEOPS-09 | Refund amount validation | Enter ₹0 in the selected-order panel. | Partial Refund and Post-Delivery remain disabled; no request is sent. |

## Batch 6 — Operations tabs: Reconciliation, DLQ (`AdminOperationsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| OPS-TAB-01 | Operations page visible | Navigate to operations. | `AdminOperationsPage.isOperationsVisible()` returns true. |
| OPS-TAB-02 | Rejections tab | Click `openRejectionsTab()`. | Rejections list rendered (or empty state). |
| OPS-TAB-03 | Reconciliation tab | Click `openReconciliationTab()`. | Reconciliation panel renders; no crash. |
| OPS-TAB-04 | Payment DLQ tab | Click `openPaymentDlqTab()`. | Payment dead-letter queue visible; shows failed payment records or empty state. |
| OPS-TAB-05 | Wallet DLQ tab | Click `openWalletDlqTab()`. | Wallet dead-letter queue visible; shows failed wallet records or empty state. |
| OPS-TAB-06 | Resolve rejection | Open a rejection in an isolated operations fixture. | Verify its audit note and resolved state. Do not resolve a shared financial record. |
| OPS-TAB-07 | Tab switching | Rapidly switch between all 4 tabs. | No stale data; each tab renders its own content without bleed-through. |
