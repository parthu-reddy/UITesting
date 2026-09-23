# Validation and pending work

The first reload batch is UI-only and reversible. It does not place an order or simulate server-side expiry.

Live validation on 2026-09-23: all four `PageReloadRecoveryTest` cases passed with randomized seeded customers. Session, exact Home selection, exact cart item, and Settings URL/profile survived reload.

`NetworkRecoveryUiTest` also passed live: the loaded cart remained visible while the browser context was offline, and the exact item survived reconnect plus reload.

The expanded reload batch also passed its same-context second-tab case: the second tab reused the authenticated customer session and displayed the exact selected Home address.

Pending after this batch:

- active order, restaurant preparation and rider active-job reload scenarios require a clean order lifecycle;
- reconnect/status reconciliation scenarios require an active order and remain blocked by intermittent delivery-availability HTTP 409 responses;
- an explicit offline banner is not assumed; the implemented test proves loaded-page stability and cart recovery. Product-specific banner behavior remains pending.
