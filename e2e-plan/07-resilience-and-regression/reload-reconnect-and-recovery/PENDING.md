# Validation and pending work

The first reload batch is UI-only and reversible. It does not place an order or simulate server-side expiry.

Live validation on 2026-09-23: all four `PageReloadRecoveryTest` cases passed with randomized seeded customers. Session, exact Home selection, exact cart item, and Settings URL/profile survived reload.

`NetworkRecoveryUiTest` also passed live: the loaded cart remained visible while the browser context was offline, and the exact item survived reconnect plus reload.

The expanded reload batch also passed its same-context second-tab case: the second tab reused the authenticated customer session and displayed the exact selected Home address.

RECOVERY-16 cross-tab cart consistency live-passed on 2026-09-24. Tab 1 added an item through the UI, Tab 2 reused the authenticated session and Home address, reloaded, opened View Cart, and displayed the exact same item. The disposable browser context contained the cart state.

RECOVERY-15 is implemented as an active strict test and failed live on 2026-09-24. After opening a Brand1 restaurant menu, browser Back did not restore the restaurant browser or its search field. This confirms that the component-state menu transition does not participate in browser history as the scenario requires.

RECOVERY-12/13 are implemented as one strict test: after opening payment, browser Back must close the payment dialog and reveal the existing cart with the exact item. The first live run could not reach payment because every attempted nearby outlet returned HTTP 409 from delivery availability. This is a prerequisite/environment failure, not evidence about Back behavior.

Pending after this batch:

- active order, restaurant preparation and rider active-job reload scenarios require a clean order lifecycle;
- reconnect/status reconciliation scenarios require an active order and remain blocked by intermittent delivery-availability HTTP 409 responses;
- an explicit offline banner is not assumed; the implemented test proves loaded-page stability and cart recovery. Product-specific banner behavior remains pending.
