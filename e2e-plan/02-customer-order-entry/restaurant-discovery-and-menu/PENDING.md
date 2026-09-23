# Pending work and validation notes

The basic menu case passed: menu names/prices render and restaurant editing controls are absent. `allMenuItemsHaveNamesPositivePricesAndCategories` also passed against Brand 1 Outlet 9 at 0.6 km: every displayed item had a nonempty name, positive rupee-formatted price, no leaked `null`/`undefined`, and belonged to a nonempty category heading. `RestaurantDiscoveryUiTest` passes two tests: the Home browser shows at least two distinct restaurant brands, every rendered card exposes a numeric nonnegative distance, brand search filters the cards, a nonsense query renders `No Kitchens Found`, and Clear Filters restores the original count.

The out-of-stock case is implemented and remains active, but the latest randomized outlet had no visible item marked `Out of stock`, so the prerequisite assertion failed. This is a seeded-data dependency rather than evidence that unavailable items can be added. Re-run against an outlet containing an unavailable item; when present, the test asserts that neither ADD nor quantity controls exist.

MENU-05 live-passed: the test selected the nearest Brand1 outlet, found a different Brand1 outlet below 5 km, switched to it, verified menu rows rendered, and reopened the selector to confirm the selected checkmark moved to the second outlet.

Item-image fallback and description styling remain pending. Source inspection shows menu rows omit the image container entirely when `imageUrl` is absent rather than rendering a placeholder; a future UI assertion must follow the intended product contract instead of claiming a fallback currently exists. The current restaurant menu has no item-search field, so MENU-11 through MENU-13 cover the actual search control on the customer restaurant browser. UI checks do not establish server-side authorization.

The restaurant-browser category filter is now live-passed. A non-All category becomes `aria-pressed=true`, the displayed cards are reduced or unchanged according to data, and selecting All restores nonempty unfiltered results. The assertion accounts for the component's intentional six-card lazy-load reset after each filter change.

Evidence: UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.MenuCartUiTest.xml (four passes, drawer-increment failure, and missing out-of-stock fixture failure in the 2026-09-23 full class run), plus `RestaurantDiscoveryUiTest` (one focused pass). No inventory edits.
