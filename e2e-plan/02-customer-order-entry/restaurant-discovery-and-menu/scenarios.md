## Current deployed validation — 2026-10-01

The user deployed menu recovery. Both routed UI cases passed with zero failures/errors/skips (04-recovery-deployed.xml): the forced catalog 502 displays Couldn't load menu, UI retry fetches 200 and restores rows; a successful empty catalog displays Menu unavailable without a retry/error. Total required discovery/menu/outlet coverage is now 23 unique passed invocations, selected in 04-fast-results.json. No deployment is pending for this feature. Earlier pending notes below describe the predeployment checkpoint. Backend-outage recovery remains distinct from routed UI proof.

# 02 — Restaurant Discovery and Menu — All Scenarios

Seeded customer always uses "Home" address. Restaurant outlet must be < 5 km from the Home address. Discovery/menu display actions are read-only. MENU-02 reuses retained stock; if none exists, it creates one unavailable item through the restaurant UI and retains that override. No price changes.

## Batch 1 — Restaurant listing and outlet selection

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-01 | Basic menu load | Login as customer → select Home address via `SavedDeliveryAddressPage` → open Brand1 outlet via `NearbyOutletPage` (< 5 km). | Menu items appear; first item has a non-empty name and rupee price; no restaurant editing controls visible. |
| MENU-02 | Out-of-stock items | On the same menu, verify out-of-stock items. | Out-of-stock rows are visible but have no "Add" button and no quantity stepper. Requires at least one seeded unavailable item. |
| MENU-03 | Multiple brands listed | On the customer home page, verify that at least two distinct brand names are shown in the restaurant list. | Implemented and live-passed; at least two cards with distinct nonempty brand names render, and all lazy-loaded cards fit the declared discovery radius; covers render loaded images or visible named fallbacks. |
| MENU-04 | Distance filter respect | All discovery cards fit the radius requested by the app (currently 10 km); delivery eligibility is separately limited to 5 km. | All lazy-loaded cards match the server count and have distances within the declared discovery radius (rounding tolerance 0.05 km). Brand1 selector options above 5 km are disabled and labelled too far. |
| MENU-05 | Outlet switching | Open nearest Brand1 outlet → switch to a different Brand1 outlet below 5 km. | Implemented and live-passed: menu rows rerender and the selector checkmark identifies the second outlet. |

## Batch 2 — Menu item display

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-06 | Item name is non-empty | For each visible menu item, assert `name.length > 0`. | Implemented and live-passed across every item in the selected outlet. |
| MENU-07 | Price format | All visible item prices include a rupee symbol (₹) and a numeric value > 0. | Implemented and live-passed; no ₹0 or missing price. |
| MENU-08 | Item image fallback | If an item has no image, a placeholder or brand logo should be shown. | Implemented as a strict active test: every row renders a loaded decorative image, loaded brand fallback or visible placeholder when both sources fail. |
| MENU-09 | Item description | If an item has a description, it is rendered below the name in smaller text. | Implemented: seeded descriptions must be nonblank, smaller than item names, two-line clamped, and free of leaked null values. |
| MENU-10 | Category headers | Menu items are grouped under category headers (e.g. "Starters", "Mains"). Verify headers are visible and non-empty. | Implemented and live-passed; at least one category and every rendered category heading is nonempty. |

## Batch 3 — Search and filtering (if applicable)

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-11 | Search restaurant | On the customer home browser, type an existing brand name. | Implemented and live-passed: one or more matching cards remain and all displayed brand headings match. The current menu view has no item-search field. |
| MENU-12 | Clear search | After searching, clear through the no-results action. | Implemented and live-passed: query empties and nonempty original results return after the six-card lazy-load reset. |
| MENU-13 | No results state | Type a name that matches no restaurant (e.g. "xyzqwerty123-no-kitchen"). | Implemented and live-passed: `No Kitchens Found` and Clear Filters render without a crash. |

Additional filter coverage: selecting a non-All craving category marks it pressed and selecting All restores nonempty unfiltered results; live-passed with lazy-loaded card counts.

## Batch 4 — Customer vs. restaurant role UI gate

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-14 | No edit controls for customer | Logged in as customer, on a menu page, assert that toggle switches ("Available"/"Unavailable") are absent. | No stock-toggle inputs or menu-editor buttons visible in customer view. |
| MENU-15 | No edit controls on outlet page | The outlet selector does not show any "Edit Outlet" or "Manage" links that would be visible to a restaurant partner. | Such controls are absent. |

Additional presentation coverage validates that rendered preparation times are positive and dietary markers expose only `Vegetarian` or `Non-vegetarian` accessible names. Unclassified items may correctly omit a marker.

## Additional gaps covered in the current audit

| ID | Scenario | Implementation / proof |
|---|---|---|
| MENU-16 | Search by actual cuisine | cuisineSearchMatchesTheRenderedCuisine; live passed. |
| MENU-17 | Cover image failure | failedCoverImagesRenderNamedFallbacks; routed UI passed. |
| MENU-18 | Nearby API fails or returns empty | nearbyFailureShowsRetryAndRecoversWithoutClaimingOutOfRange and emptyNearbyAreaOffersAnAddressChange; routed UI passed, outage distinguished from Out of Range. |
| MENU-19 | Selected catalog fails and retry recovers | catalogFailureOffersRetryAndRecoversTheSelectedOutlet; compiled, recovery fix locally passed, live deployment pending. |
| MENU-20 | Successful empty catalog | successfulEmptyCatalogShowsAnExplicitEmptyState; compiled, explicit empty state locally passed, live deployment pending. |

MENU-05 maps to SavedAddressOutletUiTest#switchBetweenTwoNearbyBrand1Outlets. MENU-15 maps to MenuCartUiTest#customerOutletSelectorExposesNoManagementActions. Distance eligibility also maps to four strict OutletSelectionTest checks. Current 21-case durable evidence is in RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/04-fast-results.json.
