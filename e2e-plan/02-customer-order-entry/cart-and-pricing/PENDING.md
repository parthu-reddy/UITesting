# Failures and pending work

## Open: cart increment remains at one

MenuCartUiTest.addIncrementDecrementAndEmptyCart fails: expected quantity 2 after Add one, displayed 1. Reproduced with a normal-duration 100ms click; source useCustomerCart has a 50ms mutation guard, but accommodating it did not resolve the failure. Keep the test failing; no application code was changed to force a pass.

Reproduction: customer 8000000001 → existing Home → Brand1 → explicitly select nearest outlet below 5 km (observed Brand 1 Outlet 10) → ADD Brand 1 Item 1-1 → View Cart → Add one. Quantity stays 1. Decrement and empty-cart steps were not reached and remain unvalidated.

Evidence: UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.MenuCartUiTest.xml and target/screenshots/addIncrementDecrementAndEmptyCart___customer.png / .html. Latest class run: two independent menu passes, one cart failure, no errors/skips.

Resolved test defect: original locator depended on the ADD button, which disappears after adding. Fixed by retaining data-menu-item identity. The remaining quantity failure occurred after this correction.

Pending: application investigation, quantity removal validation, fees/taxes/discounts. No checkout/payment performed. Cart state is isolated to the test browser and discarded on closure after failure; no other browser's cart is modified. Browser resource errors remain unattributed.

## Independent cart cases

MenuCartUiTest: removeOnlyItemFromCart, menuQuantityControlsAndRemoval, cartSubtotalAndCloseReopen all passed. These separately validate final-item removal, menu increment/decrement/removal, single-item subtotal matching its displayed unit price, Home in the cart and close/reopen retention. They do not resolve the existing drawer increment failure. Successful cases remove added items through the UI; no checkout.

Evidence: UITesting/target/surefire-reports (reports are overwritten by focused reruns); failure screenshots/HTML in target/screenshots. Broader coverage remains pending.

The latest focused MenuCartUiTest XML contains the three independent passing cases only; it does not resolve or replace the earlier failing drawer-increment result recorded here.
