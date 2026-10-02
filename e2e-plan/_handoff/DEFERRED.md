# Deferred and assigned work

Excluded by the user: intentional duration waits, relaxed Dev rate-limit exhaustion, and public SSE. Existing opt-ins auth.slow.enabled/auth.limits.enabled must remain false/unset; normal selectors exclude slow-auth and auth-rate-limit. Skipped/deferred tests are not passes. Fake-clock/local boundary checks are allowed when no live waiting is involved.

Rider item visibility/checklist is withdrawn, not a future deferred requirement. Real WebRTC/media calling, large/concurrent chat history, telemetry security, real delay/unavailable delivery and financial recovery are assigned open work; no proof is inferred from normal UI navigation.

| Owning feature | Deferred file |
|---|---|
| 01-foundation-and-access/login-and-otp | [DEFERRED-RATE-LIMIT-TESTS.md](../01-foundation-and-access/login-and-otp/DEFERRED-RATE-LIMIT-TESTS.md) |
| 01-foundation-and-access/login-and-otp | [DEFERRED-WAIT-TESTS.md](../01-foundation-and-access/login-and-otp/DEFERRED-WAIT-TESTS.md) |
| 01-foundation-and-access/sessions-and-role-access | [DEFERRED-RATE-LIMIT-TESTS.md](../01-foundation-and-access/sessions-and-role-access/DEFERRED-RATE-LIMIT-TESTS.md) |
| 01-foundation-and-access/sessions-and-role-access | [DEFERRED-WAIT-TESTS.md](../01-foundation-and-access/sessions-and-role-access/DEFERRED-WAIT-TESTS.md) |
| 02-customer-order-entry/cart-and-pricing | [DEFERRED-RATE-LIMIT-TESTS.md](../02-customer-order-entry/cart-and-pricing/DEFERRED-RATE-LIMIT-TESTS.md) |
| 02-customer-order-entry/cart-and-pricing | [DEFERRED-WAIT-TESTS.md](../02-customer-order-entry/cart-and-pricing/DEFERRED-WAIT-TESTS.md) |
| 02-customer-order-entry/checkout-and-payment | [DEFERRED-RATE-LIMIT-TESTS.md](../02-customer-order-entry/checkout-and-payment/DEFERRED-RATE-LIMIT-TESTS.md) |
| 02-customer-order-entry/checkout-and-payment | [DEFERRED-WAIT-TESTS.md](../02-customer-order-entry/checkout-and-payment/DEFERRED-WAIT-TESTS.md) |
| 02-customer-order-entry/restaurant-discovery-and-menu | [DEFERRED-RATE-LIMIT-TESTS.md](../02-customer-order-entry/restaurant-discovery-and-menu/DEFERRED-RATE-LIMIT-TESTS.md) |
| 02-customer-order-entry/restaurant-discovery-and-menu | [DEFERRED-WAIT-TESTS.md](../02-customer-order-entry/restaurant-discovery-and-menu/DEFERRED-WAIT-TESTS.md) |
| 02-customer-order-entry/saved-address-and-outlet-selection | [DEFERRED-RATE-LIMIT-TESTS.md](../02-customer-order-entry/saved-address-and-outlet-selection/DEFERRED-RATE-LIMIT-TESTS.md) |
| 02-customer-order-entry/saved-address-and-outlet-selection | [DEFERRED-WAIT-TESTS.md](../02-customer-order-entry/saved-address-and-outlet-selection/DEFERRED-WAIT-TESTS.md) |
| 03-order-fulfillment/cross-role-order-lifecycle | [DEFERRED-RATE-LIMIT-TESTS.md](../03-order-fulfillment/cross-role-order-lifecycle/DEFERRED-RATE-LIMIT-TESTS.md) |
| 03-order-fulfillment/cross-role-order-lifecycle | [DEFERRED-WAIT-TESTS.md](../03-order-fulfillment/cross-role-order-lifecycle/DEFERRED-WAIT-TESTS.md) |
| 03-order-fulfillment/customer-tracking-and-history | [DEFERRED-RATE-LIMIT-TESTS.md](../03-order-fulfillment/customer-tracking-and-history/DEFERRED-RATE-LIMIT-TESTS.md) |
| 03-order-fulfillment/customer-tracking-and-history | [DEFERRED-WAIT-TESTS.md](../03-order-fulfillment/customer-tracking-and-history/DEFERRED-WAIT-TESTS.md) |
| 03-order-fulfillment/pickup-and-delivery | [DEFERRED-RATE-LIMIT-TESTS.md](../03-order-fulfillment/pickup-and-delivery/DEFERRED-RATE-LIMIT-TESTS.md) |
| 03-order-fulfillment/pickup-and-delivery | [DEFERRED-WAIT-TESTS.md](../03-order-fulfillment/pickup-and-delivery/DEFERRED-WAIT-TESTS.md) |
| 03-order-fulfillment/restaurant-acceptance-and-preparation | [DEFERRED-RATE-LIMIT-TESTS.md](../03-order-fulfillment/restaurant-acceptance-and-preparation/DEFERRED-RATE-LIMIT-TESTS.md) |
| 03-order-fulfillment/restaurant-acceptance-and-preparation | [DEFERRED-WAIT-TESTS.md](../03-order-fulfillment/restaurant-acceptance-and-preparation/DEFERRED-WAIT-TESTS.md) |

When starting another feature, add/update its own DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md before creating intentionally skipped cases. Run a final slow/limits pass only when separately requested with suitable isolated target settings. Do not automatically enable the opt-ins at the end of a fast audit.


Refund/payment recovery now has its own [duration defer list](../04-order-exceptions-and-support/refunds-and-payment-recovery/DEFERRED-WAIT-TESTS.md) and [rate-limit defer list](../04-order-exceptions-and-support/refunds-and-payment-recovery/DEFERRED-RATE-LIMIT-TESTS.md). Neither is executed or counted as passed.
