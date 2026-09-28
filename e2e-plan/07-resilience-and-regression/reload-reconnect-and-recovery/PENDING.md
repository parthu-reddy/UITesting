# Validation and pending work

The first reload batch is UI-only and reversible. It does not place an order or simulate server-side expiry.

Live validation on 2026-09-23: all four `PageReloadRecoveryTest` cases passed with randomized seeded customers. Session, exact Home selection, exact cart item, and Settings URL/profile survived reload.

`NetworkRecoveryUiTest` also passed live: the loaded cart remained visible while the browser context was offline, and the exact item survived reconnect plus reload.

The expanded reload batch also passed its same-context second-tab case: the second tab reused the authenticated customer session and displayed the exact selected Home address.

RECOVERY-16 cross-tab cart consistency live-passed on 2026-09-24. Tab 1 added an item through the UI, Tab 2 reused the authenticated session and Home address, reloaded, opened View Cart, and displayed the exact same item. The disposable browser context contained the cart state.

RECOVERY-15 originally failed because selecting an outlet added a second menu history entry. The route now replaces the already-open menu entry, and the live `CustomerRoutingUiTest` passed on 2026-09-27: browser Back returns to the customer restaurant browser rather than the intermediate menu.

RECOVERY-12/13 are implemented as one strict test: after opening payment, browser Back must close the payment dialog and reveal the existing cart with the exact item. Checkout now reaches a final quote with a seeded rider online; the four non-submitting checkout scenarios pass live. A dedicated browser-Back assertion remains part of the broader recovery validation rather than an open product defect.

Pending after this batch:

- active order, restaurant preparation and rider active-job reload scenarios require a clean order lifecycle;
- reconnect/status reconciliation scenarios require an active order and remain blocked by intermittent delivery-availability HTTP 409 responses;
- an explicit offline banner is not assumed; the implemented test proves loaded-page stability and cart recovery. Product-specific banner behavior remains pending.


## 2026-09-28 focused five-class rerun

`PageReloadRecoveryTest` ran all 8 cases successfully in the focused five-class run. `browserBackFromPaymentPreservesCart` was exercised and passed after the rider-duty preflight helper established live server status and telemetry. Background HTTP 409 availability checks during outlet discovery did not block the selected checkout.
