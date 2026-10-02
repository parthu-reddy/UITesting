# 02 — Saved Address and Outlet Selection — All Scenarios

Ordinary order setup uses existing Home; ADDRESS-21 deliberately selects the existing Work address to validate address changes and range rejection. All outlet selections must be < 5 km. No new addresses are created. All scenarios via UI only using `SavedDeliveryAddressPage`, `NearbyOutletPage`, `CustomerOutletSelectorModalPage`.

## Batch 1 — Address selection

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDRESS-01 | Home address default | Login as customer → tap address selector. | Implemented and live-passed: "Home" appears without creating an address. |
| ADDRESS-02 | Select Home | Tap "Home" in `SavedDeliveryAddressPage`. | Implemented and live-passed: dashboard shows `Home:`. |
| ADDRESS-03 | Address display on dashboard | After selecting Home, the delivery address strip on the dashboard shows a non-empty address label. | Implemented through the exact `Home:` header assertion. |
| ADDRESS-04 | Address modal dismissal | Open address selector → tap outside or press Escape. | Implemented and live-passed with Escape; previous Home selection remains. |
| ADDRESS-05 | Address modal re-open | Open and close address modal twice consecutively. | Implemented and live-passed; both opens and dismissals retain Home. |

## Batch 2 — Outlet selection and 5 km rule

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDRESS-06 | Outlet selector opens | Open a restaurant menu, then tap Change outlet. | Outlet list renders with at least one outlet. |
| ADDRESS-07 | Select near outlet | Select Brand1 outlet with distance < 5 km (prefer < 4 km). | Menu page loads; outlet name in header matches selected outlet. |
| ADDRESS-08 | Distance label visible | Each outlet in the selector shows a distance badge (km). | Implemented and live-passed: every displayed option has a numeric, nonnegative km value. Eligibility remains determined by the live backend/UI. |
| ADDRESS-09 | Far outlet not orderable | If an outlet > 5 km is shown, it should be greyed out or have a "Too far" label, not an active "Select" button. | No "Select" or "Order" CTA on > 5 km outlet. |
| ADDRESS-10 | Outlet switch clears cart | Add item from Brand1 Outlet A → open outlet selector → switch to Brand1 Outlet B. | Conflict dialog appears prompting to clear the cart; confirm → Outlet B menu loads; current outlet cart is cleared; independent other restaurant carts retain their exact items and quantities. |

## Batch 3 — NearbyOutletPage explicit selection

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDRESS-11 | Nearby outlet page renders | Use the NearbyOutletPage helper to open Brand1 menu and its outlet modal. | The existing outlet dialog shows names, numeric distances and outlet buttons. |
| ADDRESS-12 | Select from nearby list | Click the first outlet in the nearby list. | The selected outlet menu loads. |
| ADDRESS-13 | No outlet message | Route the nearby-discovery response to an empty list; verify the reachable feed empty state. | Out of Range, no restaurant cards, and Change Address opens the existing address dialog. |

## Batch 4 — Persistence

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDRESS-14 | Address persists on reload | Select Home → reload page. | Implemented and live-passed: Home remains selected. Restaurant filtering is covered separately. |
| ADDRESS-15 | Outlet persists during session | Select outlet → navigate to cart → navigate back. | Same outlet selected; menu remains available without re-selecting. |

## Current audit additions

ADDRESS-16: fallback after a stale selected address ID must update ID, label and coordinates together. ADDRESS-17: an empty server address list must invalidate persisted saved-address coordinates/ID. ADDRESS-18: cancelling an active-cart outlet switch keeps selection/item/quantity; confirming clears only that outlet's cart. ADDRESS-19: selecting the same outlet preserves cart without confirmation. ADDRESS-20: an empty nearby-discovery response gives the explicit feed empty state; an empty sibling list does not normally open an outlet modal. ADDRESS-21: alternate saved-address selection and fresh quote use the new ID; require confirmation if an active cart exists. ADDRESS-22: keyboard Home/End/wrap skips disabled far outlets. ADDRESS-18/19/22 and the reused ADDRESS-13/20 empty-area UI contract passed. ADDRESS-21 passed after dashboard setter deployment: Work rejected out of range using Work ID, and returning Home yielded a fresh successful quote with Home ID. ADDRESS-16/17 have local consistency proof; valid saved/GPS reload has deployed proof. GPS expiry/elapsed location-age and quotas remain deferred.
