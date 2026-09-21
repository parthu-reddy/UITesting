# 02 — Restaurant Discovery and Menu — All Scenarios

Seeded customer always uses "Home" address. Restaurant outlet must be < 5 km from the Home address. All scenarios are read-only — no inventory or price changes.

## Batch 1 — Restaurant listing and outlet selection

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-01 | Basic menu load | Login as customer → select Home address via `SavedDeliveryAddressPage` → open Brand1 outlet via `NearbyOutletPage` (< 5 km). | Menu items appear; first item has a non-empty name and rupee price; no restaurant editing controls visible. |
| MENU-02 | Out-of-stock items | On the same menu, verify out-of-stock items. | Out-of-stock rows are visible but have no "Add" button and no quantity stepper. Requires at least one seeded unavailable item. |
| MENU-03 | Multiple brands listed | On the customer home page, verify that at least two distinct brand names are shown in the restaurant list. | Two or more brand cards visible with distinct names and cover images. |
| MENU-04 | Distance filter respect | All outlets shown on the customer home page have distance labels ≤ 5 km (enforced by backend); verify no outlet beyond 5 km appears with an "Order" button. | Every visible outlet either has a distance ≤ 5 km or is marked "Too far" / not orderable. |
| MENU-05 | Outlet switching | Open Brand1 Outlet A → note first menu item name → close → open Brand1 Outlet B. | Menu contents update to reflect the different outlet; first item name may differ. |

## Batch 2 — Menu item display

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-06 | Item name is non-empty | For each visible menu item, assert `name.length > 0`. | All item names are non-empty strings. |
| MENU-07 | Price format | All visible item prices include a rupee symbol (₹) and a numeric value > 0. | No ₹0 or missing price. |
| MENU-08 | Item image fallback | If an item has no image, a placeholder or brand logo is shown; no broken `<img>` with `alt` missing. | No broken images; alt text present on every image. |
| MENU-09 | Item description | If an item has a description, it is rendered below the name in smaller text. | Description text visible when available; no `null` or `undefined` literals rendered. |
| MENU-10 | Category headers | Menu items are grouped under category headers (e.g. "Starters", "Mains"). Verify headers are visible and non-empty. | At least one category header visible. |

## Batch 3 — Search and filtering (if applicable)

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-11 | Search for item | If a search/filter field is present on the menu page, type an item name. | Matching items appear; non-matching items disappear. |
| MENU-12 | Clear search | After searching, clear the field. | Full menu list restored. |
| MENU-13 | No results state | Type a name that matches no items (e.g. "xyzqwerty123"). | A "no items found" or empty state message is shown; no crash. |

## Batch 4 — Customer vs. restaurant role UI gate

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-14 | No edit controls for customer | Logged in as customer, on a menu page, assert that toggle switches ("Available"/"Unavailable") are absent. | No stock-toggle inputs or menu-editor buttons visible in customer view. |
| MENU-15 | No edit controls on outlet page | The outlet selector does not show any "Edit Outlet" or "Manage" links that would be visible to a restaurant partner. | Such controls are absent. |
