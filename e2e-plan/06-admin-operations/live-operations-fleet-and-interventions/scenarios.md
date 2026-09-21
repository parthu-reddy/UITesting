# 06 — Live Operations, Fleet, and Interventions — All Scenarios

Uses: `AdminLiveOpsPage`, `AdminManualInterventionsPage`, `AdminPortalPage`.

## Batch 1 — Live orders map / board

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-OPS-01 | Live operations tab accessible | Login as admin → navigate to Live Operations. | `AdminLiveOpsPage` renders; active orders board or map visible. |
| ADMIN-OPS-02 | Active orders visible | With active orders in the system. | Active order cards visible with order ID, restaurant, customer area, and rider assignment status. |
| ADMIN-OPS-03 | Active riders visible | On the live map (if map view available). | Rider markers visible on map; rider count ≥ online riders in the system. |
| ADMIN-OPS-04 | No orders empty state | With no active orders. | Empty-state message shown; no crash or "undefined" markers on the map. |
| ADMIN-OPS-05 | Order card shows status | Each active order card. | Status label shows current phase (e.g. "Preparing", "Rider En Route"). |

## Batch 2 — Manual interventions

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-OPS-06 | Interventions panel accessible | Navigate to `AdminManualInterventionsPage`. | Panel renders with available intervention actions. |
| ADMIN-OPS-07 | Re-assign rider | Admin selects a delayed order → assigns to a different available rider. | Order reassigned; new rider's context receives dispatch ping; original rider context loses the active job. |
| ADMIN-OPS-08 | Re-assign with no available riders | Admin tries to re-assign with no other riders online. | Warning: "No available riders" shown; re-assignment blocked. |
| ADMIN-OPS-09 | Force cancel order | Admin selects an active order → taps "Force Cancel". | Order cancelled from all contexts; customer tracker shows "Cancelled by Admin"; restaurant and rider lose the active order. |
| ADMIN-OPS-10 | Force cancel confirmation dialog | Tap "Force Cancel". | Confirmation dialog appears before cancellation is executed. |
| ADMIN-OPS-11 | Force cancel triggers refund | After force cancel on a paid order. | Refund entry appears in admin's refund queue for the cancelled order amount. |

## Batch 3 — Fleet visibility

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-OPS-12 | Rider online count | On live operations page. | Online rider count matches the number of riders who are toggled Online. |
| ADMIN-OPS-13 | Rider availability toggle reflected | Rider goes Online → admin page updates rider count. | Count increases by 1 without admin page reload (if real-time) or after refresh. |
| ADMIN-OPS-14 | Active delivery shown on rider card | While rider is in active delivery. | Admin live ops shows rider's current delivery status. |

## Batch 4 — Fleet Map (`AdminFleetMapPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| FLEET-01 | Fleet tab accessible | Admin navigates to Fleet tab via `AdminPortalPage.openFleetTab()`. | `AdminFleetMapPage.isFleetMapVisible()` returns true; map renders. |
| FLEET-02 | Driver markers visible | With riders online. | `getDriverMarkerCount()` ≥ 1; driver marker pins visible on map. |
| FLEET-03 | Select driver on map | Click a driver marker via `selectDriver(0)`. | `isDriverDetailVisible()` returns true; driver detail panel opens. |
| FLEET-04 | Driver name in detail panel | After selecting driver. | `getDriverName()` returns a non-empty string. |
| FLEET-05 | Refresh map updates markers | After a rider goes offline, click refresh via `refreshMap()`. | Driver marker count may decrease; map reloads without crash. |
| FLEET-06 | Empty fleet map | With no riders online. | `getDriverMarkerCount()` returns 0; empty-state or message shown. |

## Batch 5 — LiveOps Pagination and Refund actions (`AdminLiveOpsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| LIVEOPS-01 | LiveOps pagination — next | With > 1 page of orders, click "Next" via `nextPage()`. | Page advances; `getPageInfo()` shows "Page 2". |
| LIVEOPS-02 | LiveOps pagination — prev | After navigating to page 2, click "Prev" via `prevPage()`. | Returns to page 1. |
| LIVEOPS-03 | Select order by ID prefix | Use `selectOrderById("ORD-")`. | Order selected; `isOrderSelected()` returns true; details panel visible. |
| LIVEOPS-04 | Refresh active orders | Click "Refresh" via `refresh()`. | Order list reloads; count may update; no crash. |
| LIVEOPS-05 | Available driver count | On order detail with assignment panel. | `getAvailableDriverCount()` ≥ 0; reflects actual online riders. |
| LIVEOPS-06 | Assign driver from LiveOps | Select an unassigned order → `assignDriver(0)`. | Driver assigned; order card updates to show assigned rider. |
| LIVEOPS-07 | Partial refund from LiveOps | Select order → `initiatePartialRefund("50")`. | Partial refund of ₹50 processed; confirmation shown. |
| LIVEOPS-08 | Post-delivery refund from LiveOps | Select a delivered order → `initiatePostDeliveryRefund("100")`. | Post-delivery refund of ₹100 processed; appears in refund queue. |
| LIVEOPS-09 | Refund amount validation | Enter ₹0 → click partial refund. | Error: "Amount must be > 0"; refund blocked. |

## Batch 6 — Operations tabs: Reconciliation, DLQ (`AdminOperationsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| OPS-TAB-01 | Operations page visible | Navigate to operations. | `AdminOperationsPage.isOperationsVisible()` returns true. |
| OPS-TAB-02 | Rejections tab | Click `openRejectionsTab()`. | Rejections list rendered (or empty state). |
| OPS-TAB-03 | Reconciliation tab | Click `openReconciliationTab()`. | Reconciliation panel renders; no crash. |
| OPS-TAB-04 | Payment DLQ tab | Click `openPaymentDlqTab()`. | Payment dead-letter queue visible; shows failed payment records or empty state. |
| OPS-TAB-05 | Wallet DLQ tab | Click `openWalletDlqTab()`. | Wallet dead-letter queue visible; shows failed wallet records or empty state. |
| OPS-TAB-06 | Resolve rejection | On Rejections tab, `resolveRejection(0, "E2E test resolution note")`. | Rejection resolved; entry removed from rejections list. |
| OPS-TAB-07 | Tab switching | Rapidly switch between all 4 tabs. | No stale data; each tab renders its own content without bleed-through. |

