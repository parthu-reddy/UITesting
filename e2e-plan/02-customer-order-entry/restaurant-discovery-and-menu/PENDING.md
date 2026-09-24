# Pending work and validation notes

The basic menu case passed: menu names/prices render and restaurant editing controls are absent. `allMenuItemsHaveNamesPositivePricesAndCategories` also passed against Brand 1 Outlet 9 at 0.6 km: every displayed item had a nonempty name, positive rupee-formatted price, no leaked `null`/`undefined`, and belonged to a nonempty category heading. `RestaurantDiscoveryUiTest` passes two tests: the Home browser shows at least two distinct restaurant brands, every rendered card exposes a numeric nonnegative distance, brand search filters the cards, a nonsense query renders `No Kitchens Found`, and Clear Filters restores the original count.

The out-of-stock case is implemented and remains active, but the latest randomized outlet had no visible item marked `Out of stock`, so the prerequisite assertion failed. This is a seeded-data dependency rather than evidence that unavailable items can be added. Re-run against an outlet containing an unavailable item; when present, the test asserts that neither ADD nor quantity controls exist.

MENU-05 live-passed: the test selected the nearest Brand1 outlet, found a different Brand1 outlet below 5 km, switched to it, verified menu rows rendered, and reopened the selector to confirm the selected checkmark moved to the second outlet.

MENU-09 live-passed on 2026-09-24: every seeded description was nonblank, smaller than its item name, two-line clamped, and free of leaked `null`/`undefined` values.

MENU-08 is implemented as a strict regression test and failed live on 2026-09-24. Menu row 25 rendered an `img`, but the image did not load (`naturalWidth = 0`), so the deployed UI left a broken visual instead of a loaded item image or fallback. The failure remains active in `MenuCartUiTest.everyMenuItemHasLoadedImageOrFallback`.

Dietary/preparation metadata validation is implemented and failed its seeded-data prerequisite on 2026-09-24 because the selected deployed menu exposed zero accessible dietary markers. The test permits unclassified individual items but requires at least one classified seeded item to exercise the contract; when markers exist it accepts only `Vegetarian` or `Non-vegetarian`. Preparation times, when rendered, must be positive. Keep this test active and either seed at least one dietary classification or fix the UI if classifications already exist in the response.

The current restaurant menu has no item-search field, so MENU-11 through MENU-13 cover the actual search control on the customer restaurant browser. UI checks do not establish server-side authorization.

The restaurant-browser category filter is now live-passed. A non-All category becomes `aria-pressed=true`, the displayed cards are reduced or unchanged according to data, and selecting All restores nonempty unfiltered results. The assertion accounts for the component's intentional six-card lazy-load reset after each filter change.

Restaurant cover-image coverage live-passed on 2026-09-24. Every rendered restaurant card had exactly one loaded image (`naturalWidth > 0`) and a nonempty alt value; this exercises the configured fallback when a restaurant image is absent.

Evidence: UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.MenuCartUiTest.xml (four passes, drawer-increment failure, and missing out-of-stock fixture failure in the 2026-09-23 full class run), plus `RestaurantDiscoveryUiTest` (one focused pass). No inventory edits.
