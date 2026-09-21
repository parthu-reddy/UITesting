# 02 — Cart and Pricing — All Scenarios

All scenarios use `CustomerCartDrawerPage`, `CustomerMenuViewPage`, `CustomerFreeDeliveryTrackerPage`. The seeded customer selects "Home" address and a Brand1 outlet < 5 km. No orders are placed (no payment step). No prices are mutated.

## Batch 1 — Add to cart

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-01 | Add single item | Login → select Home → open Brand1 menu → click "Add" on first available item. | Cart icon badge shows "1"; cart drawer shows the item with quantity 1. |
| CART-02 | Add multiple distinct items | Add two different items from the same restaurant. | Cart shows both items; subtotal = sum of individual prices. |
| CART-03 | Item price in cart matches menu price | Note the price of an item on the menu page → add to cart → open cart. | Cart item price exactly matches the menu price (no rounding difference). |
| CART-04 | Add unavailable item | Attempt to tap "Add" on an out-of-stock item. | No "Add" button present on out-of-stock item; tapping the row does nothing. |

## Batch 2 — Quantity stepper

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-05 | Increment quantity | Add item → open cart → tap "+" stepper. | Quantity changes from 1 → 2; subtotal doubles for that item. |
| CART-06 | Decrement to 1 | With quantity 2, tap "–". | Quantity returns to 1; item remains in cart. |
| CART-07 | Decrement to 0 removes item | With quantity 1, tap "–". | Item is removed from cart; cart badge decrements; if cart is now empty, empty-cart state shown. |
| CART-08 | Rapid increment | Tap "+" 5 times quickly. | Quantity reaches 6; no duplicate network errors; total updates correctly. |

## Batch 3 — Pricing accuracy

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-09 | Subtotal calculation | Add two items (price A and price B). | Subtotal displayed = A + B (no rounding error > ₹0.01). |
| CART-10 | Tax display | Verify that tax amount is shown separately if the UI shows a tax line. | Tax ≥ ₹0; label is non-empty (e.g. "GST" or "Tax"). |
| CART-11 | Delivery fee display | Cart or checkout screen shows a delivery fee line. | Delivery fee ≥ ₹0; label non-empty; no hardcoded ₹0 fallback when backend is healthy. |
| CART-12 | Total = subtotal + tax + delivery | Grand total on cart = subtotal + tax + delivery fee. | Total matches arithmetic (within ₹0.01 floating-point tolerance). |
| CART-13 | Free delivery tracker | If `CustomerFreeDeliveryTrackerPage` is visible, verify progress bar advances as cart value increases. | Progress bar non-zero after adding sufficient items. |

## Batch 4 — Cross-restaurant cart conflict

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-14 | Cross-restaurant conflict dialog | Add item from Brand1 → tap "Add" on a different restaurant (Brand2). | Conflict dialog: "Your cart has items from [Brand1]. Do you want to clear it?" |
| CART-15 | Accept conflict → cart replaced | In the conflict dialog, tap "Yes, start fresh". | Brand1 items removed; Brand2 item added; cart shows Brand2 item only. |
| CART-16 | Dismiss conflict → cart unchanged | In the conflict dialog, tap "Cancel". | Brand1 items remain; Brand2 item not added; user stays on Brand1 menu. |

## Batch 5 — Empty cart state

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-17 | Empty cart message | Open cart with no items. | Empty-state message shown (e.g. "Your cart is empty"); no total or item rows displayed. |
| CART-18 | Cart badge absent when empty | With no items, the cart icon badge should show "0" or be hidden. | No misleading non-zero badge. |

## Batch 6 — Cart persistence

| ID | Description | Action | Expected result |
|---|---|---|---|
| CART-19 | Cart persists across navigation | Add item → navigate to order history → navigate back to menu. | Cart still contains the item; quantity unchanged. |
| CART-20 | Cart persists on page reload | Add item → reload page. | Cart item and quantity are retained (session/localStorage persistence). |
