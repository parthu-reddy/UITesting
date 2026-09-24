# Failures and pending work

## Open: cart increment remains at one

MenuCartUiTest.addIncrementDecrementAndEmptyCart fails: expected quantity 2 after Add one, displayed 1. Reproduced with a normal-duration 100ms click; source useCustomerCart has a 50ms mutation guard, but accommodating it did not resolve the failure. Keep the test failing; no application code was changed to force a pass.

Reproduction: customer 8000000001 → existing Home → Brand1 → explicitly select nearest outlet below 5 km (observed Brand 1 Outlet 10) → ADD Brand 1 Item 1-1 → View Cart → Add one. Quantity stayed 1 in that drawer-specific run. Menu-level increment/decrement/removal and final-item empty-cart behavior now pass separately.

Evidence: UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.MenuCartUiTest.xml and target/screenshots/addIncrementDecrementAndEmptyCart___customer.png / .html. Latest full class run on 2026-09-23: four passes and two failures. This drawer-increment assertion is the confirmed product failure.

Resolved test defect: original locator depended on the ADD button, which disappears after adding. Fixed by retaining data-menu-item identity. The remaining quantity failure occurred after this correction.

Pending: application investigation of the historical drawer-specific increment failure and settled quote arithmetic for fees/taxes. No checkout/payment is performed by these cart tests. Cart state is isolated to the test browser and discarded on closure; no other browser's cart is modified. Browser resource errors remain unattributed.

## Independent cart cases

MenuCartUiTest: removeOnlyItemFromCart, menuQuantityControlsAndRemoval, cartSubtotalAndCloseReopen all passed. These separately validate final-item removal, menu increment/decrement/removal, single-item subtotal matching its displayed unit price, Home in the cart and close/reopen retention. They do not resolve the existing drawer increment failure. Successful cases remove added items through the UI; no checkout.

Evidence: UITesting/target/surefire-reports (reports are overwritten by focused reruns); failure screenshots/HTML in target/screenshots. Broader coverage remains pending.

The latest full MenuCartUiTest run passed these three cases plus the basic menu-rendering case. It failed the drawer increment case and the separately documented data-dependent out-of-stock case; the latter is not evidence of a product defect.

Two additional focused cases now pass. `twoDistinctItemsProduceExactSubtotal` adds two different available items, verifies both names in the drawer, and proves the subtotal equals the sum of their displayed menu prices. `cartPersistsAfterSettingsNavigation` adds one item, opens Account Settings, returns home, and proves the exact item remains at quantity 1. No checkout is performed.

The CART-14 through CART-16 conflict-dialog scenarios do not match the current application model. `useCustomerCart` stores independent carts by `restaurantId` within each location, and `addToCart` adds to that restaurant's cart without a clear/replace confirmation. No conflict-dialog test will be fabricated for behavior the UI does not implement; these scenarios should be replaced by multi-restaurant cart coverage if that product behavior is intended.

`cartTotalEqualsDisplayedSubtotalFeesAndTaxes` now implements the strict CART-10 through CART-12 arithmetic check. Its first live run did not reach settled pricing: delivery availability returned HTTP 409 and both tax lines remained `Calculating...`. The test remains active and fails instead of interpreting unresolved values as zero. When the quote settles, it requires Total to equal Subtotal + Platform Fee + Delivery Fee + SGST + CGST within ₹0.01.

## Independent multi-restaurant carts

The obsolete `CustomerCartTest` that referenced nonexistent `Test Brand` data has been replaced with the deployed product contract for CART-14 through CART-16. Its live run passed: one UI-added item from Brand 1 and one from Brand 2 remained in separate outlet sections, each retained its exact item and independent Checkout action, and no cart-replacement dialog appeared. Review endpoints returned HTTP 403 and delivery availability returned HTTP 409 during the run, but neither prevented this non-checkout cart behavior from being verified.

`fiveSequentialIncrementsReachQuantitySix` and the expanded `removeOnlyItemFromCart` live-passed. Five rendered increment-button activations changed quantity 1 to 6. Removing the only item displayed `Your cart is empty`, removed the quantity output, and after closing the drawer the View Cart trigger was hidden. These tests keep their cart state inside the disposable browser context.
