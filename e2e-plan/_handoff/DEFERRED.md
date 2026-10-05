# Deferred and assigned work

## 2026-10-05 — O4/O5 boundary

These are explicit unverified assertions, excluded from the passing UI counts.

| IDs | Deferred scope | Owner document |
|---|---|---|
| O4-INT-001–004 | Browser token/storage/internal API/DB/Redis/forced expiry setup and the listed synthetic/direct aggregate methods | [Internal assertions](../01-foundation-and-access/login-and-otp/DEFERRED-O4-INTERNAL-ASSERTIONS.md) |
| O4-PERF-001 | Comparison to unavailable pre-O4 Gateway histogram; current portal <=150ms and current measurements remain required | [Performance baseline](../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-O4-PERFORMANCE-BASELINE.md) |
| O5-CSP-001 | Deliberately break deployed CSP or intercept its response; positive five-portal CSP/header/network gate remains required | [CSP mutation](../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-O5-CSP-TESTS.md) |

Existing duration, deliberate Dev rate-limit, SSE and production-provider/load deferrals below continue to apply. No disabled method, empty fixture or local mocked test is a deployed pass.

## 2026-10-04T16:42:23.916935+05:30 — Legacy brand search and300-line exclusion closed

O3-UI-006 passed strengthened deployed UI invocation11; all four O3 methods passed invocation13 on the same d79c33a image. All eleven component violations are resolved with the unchanged full13/13 gate and no new exception. Earlier exclusion below is historical. UI-only/wait/rate/SSE deferrals remain; see checkpoint71.

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
| 05-partner-and-account-management/organisations-and-portal-access | [DEFERRED-UI-ONLY-TESTS.md](../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-UI-ONLY-TESTS.md) |
| 05-partner-and-account-management/organisations-and-portal-access | [DEFERRED-WAIT-TESTS.md](../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-WAIT-TESTS.md) |
| 05-partner-and-account-management/organisations-and-portal-access | [DEFERRED-RATE-LIMIT-TESTS.md](../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-RATE-LIMIT-TESTS.md) |

When starting another feature, add/update its own DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md before creating intentionally skipped cases. Run a final slow/limits pass only when separately requested with suitable isolated target settings. Do not automatically enable the opt-ins at the end of a fast audit.


Refund/payment recovery now has its own [duration defer list](../04-order-exceptions-and-support/refunds-and-payment-recovery/DEFERRED-WAIT-TESTS.md) and [rate-limit defer list](../04-order-exceptions-and-support/refunds-and-payment-recovery/DEFERRED-RATE-LIMIT-TESTS.md). Neither is executed or counted as passed.

## 2026-10-04T07:03:09+05:30 — O3 UI-only boundary

O3 adds a separate deferred list because the owner requires all browser E2E setup, actions and
assertions to use visible UI controls only. Direct endpoint/IDOR calls, browser `fetch`, DB/Redis/
SSH reads, local-storage injection and direct measurement loops are not alternate E2E proof. They
are listed as deferred, not passed. Embedded H2 remains restricted to local service tests; Docker
and external database fixtures are prohibited. See the three O3 files above before implementing or
running any partner-application E2E.

## 2026-10-04T14:16:06.678712+05:30 — O3-UI-006, known legacy brand search failure

Customer search by renamed brand with a different outlet name failed in UI invocation8. The old component filters outlet names; exact saved-outlet search still finds the approved renamed card. Owner forbids old-code changes. See [owning deferral](../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-LEGACY-UI-TESTS.md). This assertion is failed/deferred, never a pass; current onboarding visibility uses the real outlet query and unchanged authoritative approval filter.
