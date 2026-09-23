# 07 — Reload, Reconnect, and Recovery — All Scenarios

Uses all page objects as applicable. Tests browser-level resilience and state persistence.

## Batch 1 — Page reload persistence

| ID | Description | Action | Expected result |
|---|---|---|---|
| RECOVERY-01 | Session persists on reload | Customer logged in → hard reload. | Implemented with exact dashboard and phone assertions. |
| RECOVERY-02 | Cart persists on reload | Customer adds an item → reloads. | Implemented without checkout; exact item must remain in the cart. |
| RECOVERY-03 | Selected address persists on reload | Customer selects Home → reloads. | Implemented; exact Deliver to text remains and the address dialog stays closed. |
| RECOVERY-04 | Active order tracker persists on reload | Customer has active order → reloads tracker page. | Tracker shows same order status as before reload (possibly refreshed from backend). |
| RECOVERY-05 | Restaurant active order persists on reload | Restaurant has order in Preparation → reloads. | Order still visible in Preparation tab after reload. |
| RECOVERY-06 | Rider active job persists on reload | Rider is in active delivery phase → reloads. | Active job page reloads to the same delivery phase (pickup or drop-off). |

## Batch 2 — Network disconnect and reconnect

| ID | Description | Action | Expected result |
|---|---|---|---|
| RECOVERY-07 | Offline banner appears | While logged in, simulate network disconnect via Playwright (`page.context().setOffline(true)`). | UI shows "Offline" banner, toast, or error message within 10 s. |
| RECOVERY-08 | Offline banner disappears on reconnect | Restore network (`setOffline(false)`). | Offline banner disappears within 10 s; app resumes normal operation. |
| RECOVERY-09 | No crash on disconnect | With customer cart open, disconnect network. | Implemented; loaded UI and exact cart item remain visible. Active-order variant remains pending. |
| RECOVERY-10 | State reconciles after reconnect | Reconnect during active order. | Order tracker status is refreshed from backend; no stale status displayed. |
| RECOVERY-11 | Cart retained across offline period | Add to cart → go offline → come back online. | Implemented; exact item must survive reconnect and reload. |

## Batch 3 — Back-navigation recovery

| ID | Description | Action | Expected result |
|---|---|---|---|
| RECOVERY-12 | Back from checkout | Enter checkout → press browser Back. | Returns to cart; cart items intact; no partial order created. |
| RECOVERY-13 | Back from payment modal | Open payment modal → press Back. | Modal closes; checkout screen remains. |
| RECOVERY-14 | Back from order tracker | View active order tracker → press Back. | Returns to home/dashboard; active order still in progress. |
| RECOVERY-15 | Back from restaurant menu | Customer on restaurant menu → browser Back. | Returns to restaurant list; no cart cleared. |

## Batch 4 — Parallel tab recovery

| ID | Description | Action | Expected result |
|---|---|---|---|
| RECOVERY-16 | Two customer tabs | Open two tabs with the same customer session. | Implemented for shared authentication and exact Home selection; cross-tab cart consistency remains pending. |
| RECOVERY-17 | Order placed in one tab visible in other | Customer places order in Tab 1. | Tab 2's active orders section updates (or on next interaction/reload shows the new order). |
