# checkout-and-payment

Status: UI-only batch validated on 2026-09-23. `CheckoutUiTest` covers CHECKOUT-01, CHECKOUT-03 through CHECKOUT-06, CHECKOUT-14 through CHECKOUT-16 without placing an order. CHECKOUT-11 and CHECKOUT-12 are also exercised by the passing `HappyDeliveryFlowTest`.

Scope: Checkout, payment UI and order creation.

Implementation: `CheckoutUiTest` uses the existing Home address, a displayed Brand 1 outlet below 5 km, and the seeded rider made Online through the UI. It verifies the cart item and enabled checkout action, keyboard activation with Enter, delivery address, item quantity, subtotal, delivery fee, taxes, total, Card/UPI/Wallet choices, closing payment, and reload behavior. Reload closes the unsubmitted payment modal while preserving the cart item and checkout readiness.

Validation command: `mvn -q -Dtest=CheckoutUiTest -Dheadless=true -Dslow.mo=0 -Drecord.video=false test`.

See [pending work, failures and confirmations](PENDING.md).
