# 03 — Cross-Role Order Lifecycle — All Scenarios

**Prerequisite rule**: All three roles (Customer, Restaurant, Rider) must be logged in simultaneously in separate, isolated browser contexts. Rider MUST be Online and physically near (< 5 km) the selected restaurant. Customer uses "Home" address. Restaurant is online. Outlet selected is < 5 km from rider.

No backend state is manipulated directly — all actions go through the UI.

## Batch 1 — Full happy-path order lifecycle

| ID | Description | Action | Expected (all three contexts) |
|---|---|---|---|
| CROSS-01 | Setup three contexts | Login Customer (8000000001) in Context A; Restaurant (9000000001) in Context B; Rider (7000000001) in Context C. Make rider Online. | All three dashboards visible; rider shows Online status. |
| CROSS-02 | Customer places order | Customer selects Home, Brand1 outlet < 5 km, adds item, checks out. | Customer: "Order Placed" tracker. Restaurant (B): incoming order card appears. |
| CROSS-03 | Restaurant accepts | Restaurant taps "Accept" on incoming order. | Backend order becomes ACCEPTED; the exact customer tracker has ACCEPTED through polling without manual reload. The visible delivery headline depends on its delivery state; assert the owning status attribute rather than guessing a shared label. Restaurant retains the exact accepted order; the separate Start Cooking action moves it to PREPARING. Rider dispatch may start when estimated preparation is within15 minutes, including immediately after acceptance; readiness is not a universal dispatch gate. |
| CROSS-04 | Restaurant marks Ready | Restaurant taps "Mark Ready". | Order becomes READY_FOR_PICKUP; customer tracker shows "Packed and ready" without manual reload. Rider dispatch follows estimated-completion timing and may already have appeared at acceptance; bind the exact order. |
| CROSS-05 | Rider accepts dispatch | Rider taps "Accept" on ping. | Customer (A): "Courier assigned" on the exact owned tracker. Rider (C): Active job shows pickup phase. |
| CROSS-06 | Rider arrives at restaurant | Rider taps "Arrived at Restaurant". | Customer (A): "Courier at restaurant" on the exact owned tracker. Any optional restaurant notification belongs to separate notification coverage. |
| CROSS-07 | Rider marks picked up | Rider taps "Picked Up". | Customer (A): "Courier on the way" on the exact owned tracker; live map telemetry belongs to the live-tracking feature. Restaurant (B): exact order progresses to its source-defined dispatched/terminal view; no assumed Completed tab. |
| CROSS-08 | Rider completes delivery | Rider taps "Delivery Complete". | Customer (A): receipt heading "Order delivered" for the exact owned order. Rider (C): earnings updated; active job closed. |

## Batch 2 — Verify real-time sync (no manual reloads)

| ID | Description | Condition | Expected result |
|---|---|---|---|
| CROSS-09 | Customer sees each status change in real-time | Between CROSS-03 and CROSS-08. | Each required customer transition appears automatically without reload; verify actual polling/live transport in source rather than assuming SSE/WebSocket. |
| CROSS-10 | Restaurant queue updates in real-time | Between CROSS-02 and CROSS-06. | Orders move between Incoming → Preparation → Ready without reload. |
| CROSS-11 | Rider ping appears without reload | At CROSS-04. | Dispatch ping arrives on rider dashboard without rider reloading. |

## Batch 3 — Post-lifecycle state checks

| ID | Description | Action | Expected result |
|---|---|---|---|
| CROSS-12 | Order in customer history | After CROSS-08, customer opens Order History. | Completed order appears with correct restaurant name, items, and total. |
| CROSS-13 | Earnings in rider history | After CROSS-08, rider opens History tab. | Completed trip appears with correct earnings figure. |
| CROSS-14 | Restaurant completed order | After CROSS-08, restaurant verifies the exact order in its terminal/history view (there is no separate Completed tab in the current Kanban). | Exact order is absent from active queue and present in outlet history as Delivered with the correct charged total. |

## Batch 4 — Overlapping independent orders

| ID | Description | Action | Expected result |
|---|---|---|---|
| CROSS-15 | Two overlapping orders | Customer places two orders from different brand outlets in separate cart sessions (or two customers if additional accounts are available). | Both server records are pending together. Complete the first while the second remains pending, then complete the second. Verify exact IDs/outlets, independent receipts and authorised streams. A single rider serves them sequentially; this is overlapping lifecycle isolation, not parallel transaction/load stress proof. |
