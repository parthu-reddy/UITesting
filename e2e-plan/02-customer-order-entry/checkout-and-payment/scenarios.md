# 02 — Checkout and Payment — All Scenarios

All scenarios require: seeded customer logged in, "Home" address selected, Brand1 outlet < 5 km selected, at least one item in cart. Uses `CustomerCartDrawerPage`, `PaymentModalPage`. Rider MUST be logged in and marked Available near the restaurant. The restaurant must be online.

## Batch 1 — Checkout initiation

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-01 | Checkout button appears with items | Add at least one item to cart → open cart drawer. | "Proceed to Checkout" (or equivalent) button is visible and enabled. |
| CHECKOUT-02 | Checkout button absent with empty cart | View cart with zero items. | No "Proceed to Checkout" button; empty-cart state shown. |
| CHECKOUT-03 | Checkout screen shows delivery address | Tap "Proceed to Checkout". | Checkout screen displays the selected "Home" address; address is non-empty. |
| CHECKOUT-04 | Checkout screen shows order summary | On checkout screen, verify item names, quantities, and subtotal match the cart. | Item list matches; subtotal correct. |
| CHECKOUT-05 | Checkout screen shows price breakdown | Delivery fee, tax, and grand total lines are all visible on the checkout screen. | Three separate line items visible; no line shows "₹null" or "₹undefined". |

## Batch 2 — Payment method selection

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-06 | Payment options visible | On checkout/payment step, verify at least one payment method option is visible (e.g. UPI, Card, Wallet). | Payment option list renders; no blank section. |
| CHECKOUT-07 | Select UPI | Tap "UPI / Netbanking" option. | Choice accepts the click and the common Pay action remains enabled. The current UI has no UPI detail form or QR step. |
| CHECKOUT-08 | Select Card | Tap "Credit Card" option. | Choice accepts the click and the common Pay action remains enabled. The current simulated payment UI has no card-entry fields. |
| CHECKOUT-09 | Select Wallet | Verify the Wallet option. | Wallet option renders; it may be disabled when the displayed balance is insufficient. |
| CHECKOUT-10 | Payment choice after closing | Select a method → close payment → re-enter checkout. | Document actual reset/persistence behavior; the current UI has no separate Back step. |

## Batch 3 — Successful checkout (integration)

| ID | Description | Prerequisite | Action | Expected result |
|---|---|---|---|---|
| CHECKOUT-11 | Full order placement | Rider available near restaurant; restaurant online. | Add item → select Home → checkout → select any available payment → confirm payment. | Order confirmation screen shows order ID; customer order tracker appears with "Order Placed" status. |
| CHECKOUT-12 | Order ID is non-empty | After successful checkout. | Inspect order confirmation. | Order ID is a non-empty string/UUID. |
| CHECKOUT-13 | Order confirmation screen dismissal | After order confirmation, tap "Track Order" or "Done". | Navigates to active order tracker or home screen; confirmation screen closes. |

## Batch 4 — Checkout abandonment

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-14 | Back from checkout preserves cart | Enter checkout → tap Back. | Cart items still present; no partial order created. |
| CHECKOUT-15 | Close payment modal | On payment modal, tap "X" or Cancel. | Modal closes; checkout screen still visible; cart intact. |
| CHECKOUT-16 | Reload during checkout | Enter checkout → reload page. | Implemented and live-passed: the unsubmitted payment modal closes, the authenticated dashboard renders, and the same cart item remains with checkout enabled. |

## Batch 5 — Payment failure handling

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-17 | Payment gateway timeout feedback | If the payment gateway returns an error/timeout, the UI shows an error message. | Error message visible; retry option shown; no duplicate order created. |
| CHECKOUT-18 | Declined payment feedback | If the gateway declines the payment, a specific decline message (not a generic crash) is shown. | Clear UI message; user can try a different method. |
