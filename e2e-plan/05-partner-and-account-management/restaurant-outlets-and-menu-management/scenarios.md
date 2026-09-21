# 05 — Restaurant Outlets and Menu Management — All Scenarios

Uses: `RestaurantMenuEditorPage`, `RestaurantDashboardPage`, `OutletRegistrationPage`, `RestaurantBrandRegistrationPage`.

No prices are mutated. No items are permanently deleted. Stock toggles may be changed and then reverted.

## Batch 1 — Menu stock management

| ID | Description | Action | Expected result |
|---|---|---|---|
| STOCK-01 | Menu Stock Toggles heading | Login as restaurant → navigate to Menu Stock Toggles. | Heading "Menu Stock Toggles" (or equivalent) visible; item list renders. |
| STOCK-02 | Each item has a toggle | On the stock management page. | Every item row has an accessible switch/toggle showing current availability state. |
| STOCK-03 | Toggle state is readable | Check the ARIA state of a toggle. | `aria-checked` attribute or equivalent reflects "true" (available) or "false" (unavailable). |
| STOCK-04 | Mark item unavailable | Toggle an available item to "Unavailable". | Toggle reflects new state; item visible to customer as out-of-stock (MENU-02 in customer scenarios). |
| STOCK-05 | Revert item to available | Toggle the same item back to "Available". | Toggle returns to "Available" state; item orderable again by customer. |
| STOCK-06 | Multiple toggles | Toggle three items quickly. | All three toggle states updated correctly; no race condition mixes the states. |

## Batch 2 — Menu item editor

| ID | Description | Action | Expected result |
|---|---|---|---|
| MENU-EDIT-01 | Menu editor opens | Restaurant opens `RestaurantMenuEditorPage`. | Item list renders with categories. |
| MENU-EDIT-02 | Item name visible | Each item row shows name. | No blank names in editor. |
| MENU-EDIT-03 | Item price visible | Each item row shows price. | Price ≥ ₹0; not `null`. |
| MENU-EDIT-04 | Edit item name field | Tap on an item to edit → modify name field. | Name field becomes editable. |
| MENU-EDIT-05 | Save item name change | Modify name → save. | Updated name appears in editor list; reflects on customer menu (MENU-06). |
| MENU-EDIT-06 | Cancel item edit | Modify name → cancel. | Original name restored; no change persisted. |
| MENU-EDIT-07 | Add new item | Tap "Add Item" → fill name, price, category. | New item appears in the menu editor list. |
| MENU-EDIT-08 | Add item without name blocked | Tap "Add Item" → leave name blank → save. | Validation error; no item created. |
| MENU-EDIT-09 | Add item without price blocked | Tap "Add Item" → leave price blank → save. | Validation error; no item created. |

## Batch 3 — Outlet management

| ID | Description | Action | Expected result |
|---|---|---|---|
| OUTLET-01 | Outlet list visible | Restaurant opens Outlets section. | List of current outlets with name and status. |
| OUTLET-02 | Register new outlet form | Tap "Add Outlet" / "Register Outlet". | `OutletRegistrationPage` form renders with name, address, and location fields. |
| OUTLET-03 | Submit outlet without name blocked | Leave name blank → submit. | Validation error. |
| OUTLET-04 | Outlet status toggle | On an existing outlet, toggle open/closed status. | Status reflects the toggle; customers may not be able to order from a closed outlet. |

## Batch 4 — Brand registration

| ID | Description | Action | Expected result |
|---|---|---|---|
| BRAND-01 | Brand registration page accessible | Restaurant navigates to Brand management. | `RestaurantBrandRegistrationPage` renders. |
| BRAND-02 | Form fields visible | On brand registration form. | Brand name, logo upload, cuisine type fields visible. |
| BRAND-03 | Submit without brand name blocked | Leave brand name empty → submit. | Validation error. |
