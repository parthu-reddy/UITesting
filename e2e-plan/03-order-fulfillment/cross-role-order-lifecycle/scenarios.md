# 03 — Cross-Role Order Lifecycle — All Scenarios

**Prerequisite rule**: All three roles (Customer, Restaurant, Rider) must be logged in simultaneously in separate, isolated browser contexts. Rider MUST be Online and physically near (< 5 km) the selected restaurant. Customer uses "Home" address. Restaurant is online. Outlet selected is < 5 km from rider.

No backend state is manipulated directly — all actions go through the UI.

## Batch 1 — Full happy-path order lifecycle

| ID | Description | Action | Expected (all three contexts) |
|---|---|---|---|
| CROSS-01 | Setup three contexts | Login Customer (8000000001) in Context A; Restaurant (9000000001) in Context B; Rider (7000000001) in Context C. Make rider Online. | All three dashboards visible; rider shows Online status. |
| CROSS-02 | Customer places order | Customer selects Home, Brand1 outlet < 5 km, adds item, checks out. | Customer: "Order Placed" tracker. Restaurant (B): incoming order card appears. |
| CROSS-03 | Restaurant accepts | Restaurant taps "Accept" on incoming order. | Customer (A): status updates to "Preparing" without reload. Restaurant (B): order moves to Preparation tab. Rider (C): no ping yet (prep not complete). |
| CROSS-04 | Restaurant marks Ready | Restaurant taps "Mark Ready". | Customer (A): status updates to "Looking for Rider" / "Ready for Pickup". Rider (C): `DispatchPingPage` appears. |
| CROSS-05 | Rider accepts dispatch | Rider taps "Accept" on ping. | Customer (A): "Rider on the way to restaurant". Rider (C): Active job shows pickup phase. |
| CROSS-06 | Rider arrives at restaurant | Rider taps "Arrived at Restaurant". | Customer (A): status updated. Restaurant (B): notification if applicable. |
| CROSS-07 | Rider marks picked up | Rider taps "Picked Up". | Customer (A): "Rider heading to you" / map shows rider moving. Restaurant (B): order moves to Completed/Dispatched state. |
| CROSS-08 | Rider completes delivery | Rider taps "Delivery Complete". | Customer (A): "Order Delivered". Rider (C): earnings updated; active job closed. |

## Batch 2 — Verify real-time sync (no manual reloads)

| ID | Description | Condition | Expected result |
|---|---|---|---|
| CROSS-09 | Customer sees each status change in real-time | Between CROSS-03 and CROSS-08. | Status on customer tracker changes automatically (SSE/WebSocket) without page.reload() calls. |
| CROSS-10 | Restaurant queue updates in real-time | Between CROSS-02 and CROSS-06. | Orders move between Incoming → Preparation → Ready without reload. |
| CROSS-11 | Rider ping appears without reload | At CROSS-04. | Dispatch ping arrives on rider dashboard without rider reloading. |

## Batch 3 — Post-lifecycle state checks

| ID | Description | Action | Expected result |
|---|---|---|---|
| CROSS-12 | Order in customer history | After CROSS-08, customer opens Order History. | Completed order appears with correct restaurant name, items, and total. |
| CROSS-13 | Earnings in rider history | After CROSS-08, rider opens History tab. | Completed trip appears with correct earnings figure. |
| CROSS-14 | Restaurant completed order | After CROSS-08, restaurant opens Completed tab. | Order appears in Completed queue. |

## Batch 4 — Concurrent orders (stress)

| ID | Description | Action | Expected result |
|---|---|---|---|
| CROSS-15 | Two simultaneous orders | Customer places two orders from different brand outlets in separate cart sessions (or two customers if additional accounts are available). | Each order tracked independently; no status bleed between orders on customer tracker. |
