# 02 — Saved Address and Outlet Selection — All Scenarios

Customer always uses "Home" address. All outlet selections must be < 5 km. No new addresses are created. All scenarios via UI only using `SavedDeliveryAddressPage`, `NearbyOutletPage`, `CustomerOutletSelectorModalPage`.

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
| ADDRESS-06 | Outlet selector opens | On restaurant card, tap to open `CustomerOutletSelectorModalPage`. | Outlet list renders with at least one outlet. |
| ADDRESS-07 | Select near outlet | Select Brand1 outlet with distance < 5 km (prefer < 4 km). | Menu page loads; outlet name in header matches selected outlet. |
| ADDRESS-08 | Distance label visible | Each outlet in the selector shows a distance badge (km). | Implemented and live-passed: every displayed option has a numeric, nonnegative km value. Eligibility remains determined by the live backend/UI. |
| ADDRESS-09 | Far outlet not orderable | If an outlet > 5 km is shown, it should be greyed out or have a "Too far" label, not an active "Select" button. | No "Select" or "Order" CTA on > 5 km outlet. |
| ADDRESS-10 | Outlet switch clears cart | Add item from Brand1 Outlet A → open outlet selector → switch to Brand1 Outlet B. | Conflict dialog appears prompting to clear the cart; confirm → Outlet B menu loads; cart is empty. |

## Batch 3 — NearbyOutletPage explicit selection

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDRESS-11 | Nearby outlet page renders | Navigate to `NearbyOutletPage` for Brand1. | Page shows a list of nearby outlets with name, distance, and a select button. |
| ADDRESS-12 | Select from nearby list | Click the first outlet in the nearby list. | Redirected to that outlet's menu page. |
| ADDRESS-13 | No outlet message | If no outlets are within range (hypothetical / admin-toggled), verify an empty-state message appears. | "No outlets nearby" or equivalent message; not a crash or blank page. |

## Batch 4 — Persistence

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDRESS-14 | Address persists on reload | Select Home → reload page. | Implemented and live-passed: Home remains selected. Restaurant filtering is covered separately. |
| ADDRESS-15 | Outlet persists during session | Select outlet → navigate to cart → navigate back. | Same outlet selected; menu remains available without re-selecting. |
