# Pending rider dispatch coverage

- DISPATCH-08 is a real timeout case and is deferred with other long-wait scenarios.
- DISPATCH-13 through DISPATCH-15 change dispatch assignment and may leave a created order without a rider. Implement them as a coordinated cross-role flow with cleanup.
- DISPATCH-17, DISPATCH-19, DISPATCH-21 and DISPATCH-22 use names from an older tab layout. The current rider dashboard exposes earnings as a stats tile, completed trips as a panel, and wallet inside Profile settings. Reconcile these scenarios with the current UI before implementing them.
- DISPATCH-20 needs an existing suspended or KYC-rejected rider. Do not create or mutate that backend state directly; run it when suitable UI-visible test data exists.

## Active assignment lock investigation (2026-09-23)

Oracle logs for order `e6b39e43-f1c9-4d0e-bb63-e5d6333b2cf8` showed the stale-driver sweeper mark the assigned rider OFFLINE after location heartbeats stopped. `RedisLockReaperTask` then treated OFFLINE as proof that the assignment was orphaned and deleted both `driver:active_order:*` and `order:driver:lock:*`. That violated the one-active-order invariant because OFFLINE can be a temporary disconnect during an active delivery.

Delivery service commit `ef34907` preserves both locks for OFFLINE and ON_DELIVERY riders; only ONLINE riders are eligible for crash-recovery cleanup. Regression tests and the full delivery-service Maven suite pass. Source is pushed, but deployment and live verification remain pending.

## Rider verification availability (2026-09-24)

A UI-only resume attempt for order `f5a010f8-542d-44a9-9a57-196f48223e70` could not open the rider dashboard because `/api/delivery/verification/status` returned HTTP 503 twice. The failure occurred before order assertions or UI actions. Retry after the deployed delivery verification endpoint is healthy; no backend state was changed.

A fresh focused `RiderAvailabilityUiTest` run later on 2026-09-24 reproduced the same two HTTP 503 responses. The duty control did not render within 15 seconds, confirming this currently blocks all rider-dependent order setup rather than one saved order or one test class.
