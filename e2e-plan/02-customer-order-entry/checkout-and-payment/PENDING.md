# Pending work and confirmations

The first reversible UI-only checkout batch is implemented and passing. No backend fixture creation or state manipulation is used.

- CHECKOUT-02 cannot currently be reached through normal UI navigation: the `View Cart` trigger is rendered only when `totalCartItems > 0`, so an empty cart cannot be opened.
- CHECKOUT-07 through CHECKOUT-10 assumed separate UPI/card forms and back-navigation persistence. The current payment modal only provides three simulated method tiles (Credit Card, UPI / Netbanking and Wallet) with one Pay action. It has no card fields, QR view, or intermediate checkout page. The passing test verifies that all three choices render and an available choice can be selected.
- CHECKOUT-13 assumes a separate confirmation screen with Track Order or Done. Current successful payment transitions directly to the order tracker after a short success state.
- CHECKOUT-16 is implemented and live-passed: reloading from the payment modal does not resume the unsubmitted payment UI, and the same cart item remains available with checkout enabled.
- CHECKOUT-17 and CHECKOUT-18 require a naturally failing payment response or a UI control that selects a failure outcome. Do not intercept or mutate backend traffic to manufacture these while the project is UI-only.

The focused CHECKOUT-16 run passed. A later two-method class run produced one pass and one environment/data error: repeated delivery-availability HTTP 409 responses left every inspected Brand1 outlet without an orderable Add control. During that run, the helper's fallback was also corrected to consider every untried outlet below 5 km and to track selected outlet names, matching the project distance rule. This is not recorded as a checkout-behavior failure because the test never reached checkout in the failing case.

Reuse Home, explicitly select a nearby Brand1 outlet below 5 km, and make the seeded rider available through the UI. Do not substitute new addresses or fabricated backend state for missing prerequisites.
