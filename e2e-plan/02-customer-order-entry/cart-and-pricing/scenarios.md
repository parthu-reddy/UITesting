Current execution: 13 unique live invocations passed without failures/errors/skips; 5 local rendered threshold checks passed separately. No orders/payments were submitted. Real quote cases keep a dedicated authoritatively ONLINE rider nearby and restore only idle rider OFFLINE. Long-duration/rate-limit checks are excluded.

# 02 — Cart and Pricing — All Scenarios

All scenarios use `CustomerCartDrawerPage`, `CustomerMenuViewPage`, `CustomerFreeDeliveryTrackerPage`. The seeded customer selects "Home" address and a Brand1 outlet < 5 km. No orders are placed (no payment step). No prices are mutated.

## Batch 1 — Add to cart

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-01 | Add single item | Login → select Home → open Brand1 menu → click "Add" on first available item. | Cart icon badge shows "1"; cart drawer shows the item with quantity 1. |
| CART-02 | Add multiple distinct items | Add two different items from the same restaurant. | Implemented and live-passed: both exact item names render in the drawer. |
| CART-03 | Item price in cart matches menu price | Note the price of two items on the menu page → add to cart → open cart. | Each item’s displayed cart unit price matches its menu price in integer paise; both quantities are 1, and subtotal is their exact sum. |
| CART-04 | Add unavailable item | Attempt to tap "Add" on an out-of-stock item. | No "Add" button present on out-of-stock item; tapping the row does nothing. |

## Batch 2 — Quantity stepper

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-05 | Increment quantity | Add item → use menu "+" stepper. | Implemented and current live-passed in both menu and drawer: quantity changes from 1 to 2. |
| CART-06 | Decrement to 1 | With menu quantity 2, tap "–". | Implemented and live-passed: quantity returns to 1 and item remains. |
| CART-07 | Decrement to 0 removes item | With quantity 1, tap "–". | Implemented and live-passed: ADD returns, View Cart disappears, and the separate drawer case shows the empty state. |
| CART-08 | Sequential increment | Activate "+" 5 times through the rendered control. | Implemented and live-passed: quantity changes from 1 to 6. |

## Batch 3 — Pricing accuracy

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-09 | Subtotal calculation | Add two items (price A and price B). | Implemented and live-passed: displayed subtotal equals A + B exactly. |
| CART-10 | Tax display | Open Checkout after authoritative rider readiness; wait for final quote. | Combined GST & restaurant charges is a settled INR amount on the checkout sheet; the cart drawer shows Item total only. |
| CART-11 | Delivery fee display | Checkout sheet shows the final delivery fee line. | Implemented in the strict arithmetic test; `FREE` is treated as ₹0 and an INR fee is parsed otherwise. |
| CART-12 | Total = subtotal + fees + taxes | Compare every displayed pricing component. | Current live-passed: Total equals Item total + Delivery fee + optional Platform fee + GST & restaurant charges within ₹0.01 after a successful final quote. |
| CART-13 | Free delivery tracker | Increase cart value through UI controls. | Current live-passed: progress matches actual successful quote subtotal/threshold and derived quantity reaches the unlocked state. Exact below/at/above boundaries have separate local rendered-UI proof. |

## Batch 4 — Cross-restaurant cart conflict

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-14 | Cross-restaurant cart behavior | Implemented for the current product contract: add one item from each of two nearby brands and open the combined cart. | Two independent restaurant sections and checkout actions render; no replacement dialog appears. |
| CART-15 | First cart is retained | After adding from the second restaurant, increment the first cart. | The first cart quantity changes 1→2; the second item, quantity 1 and subtotal stay unchanged. |
| CART-16 | Second cart remains independent | Remove the first cart through its quantity control. | The second outlet/item/quantity/subtotal remain unchanged with exactly one Checkout action; no cart replaces the other. |

## Batch 5 — Empty cart state

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-17 | Empty cart message | Remove the only cart item. | Implemented and live-passed: `Your cart is empty` renders and no quantity output remains. |
| CART-18 | Cart trigger absent when empty | Close the empty drawer after removing its only item. | Implemented and live-passed: View Cart is hidden. |

## Batch 6 — Cart persistence

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-19 | Cart persists across navigation | Add item → navigate to settings → return home → reopen cart. | Implemented and live-passed: exact item remains at quantity 1. History-specific navigation remains covered separately. |
| CART-20 | Cart persists on page reload | Add item → increment to quantity 2 → reload page. | Implemented and live-passed in `PageReloadRecoveryTest`: the exact item and quantity 2 are retained. |
