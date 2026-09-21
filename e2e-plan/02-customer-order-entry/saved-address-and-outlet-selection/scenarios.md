# 02 — Saved Address and Outlet Selection — All Scenarios

Customer always uses "Home" address. All outlet selections must be < 5 km. No new addresses are created. All scenarios via UI only using `SavedDeliveryAddressPage`, `NearbyOutletPage`, `CustomerOutletSelectorModalPage`.

## Batch 1 — Address selection

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDRESS-01 | Home address default | Login as customer → tap address selector. | "Home" entry appears in the address list without creating a new one. |
| ADDRESS-02 | Select Home | Tap "Home" in `SavedDeliveryAddressPage`. | Dashboard updates to show "Deliver to: Home" or equivalent; restaurant list refreshes. |
| ADDRESS-03 | Address display on dashboard | After selecting Home, the delivery address strip on the dashboard shows a non-empty address label. | Label is not `null`, `undefined`, or an empty string. |
| ADDRESS-04 | Address modal dismissal | Open address selector → tap outside or press Escape. | Modal closes; previous selection retained. |
| ADDRESS-05 | Address modal re-open | Open and close address modal twice consecutively. | Modal opens and closes cleanly each time; no stale state. |

## Batch 2 — Outlet selection and 5 km rule

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDRESS-06 | Outlet selector opens | On restaurant card, tap to open `CustomerOutletSelectorModalPage`. | Outlet list renders with at least one outlet. |
| ADDRESS-07 | Select near outlet | Select Brand1 outlet with distance < 5 km (prefer < 4 km). | Menu page loads; outlet name in header matches selected outlet. |
| ADDRESS-08 | Distance label visible | Each outlet in the selector shows a distance badge (km). | Every outlet row has a numeric distance ≥ 0 km and ≤ 5 km to be orderable. |
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
| ADDRESS-14 | Address persists on reload | Select Home → reload page. | Home is still selected; restaurant list is filtered to Home's area. |
| ADDRESS-15 | Outlet persists during session | Select outlet → navigate to cart → navigate back. | Same outlet selected; menu remains available without re-selecting. |
