# cross-role-order-lifecycle

Fast CROSS01–15 verified in corrected deployed invocations. Backend/config SSE pool fix and UI acceptance/restored receipt fixes deployed. Intentional expiry, long scheduled preparation and quota exhaustion remain explicitly deferred. The 28-feature suite is unfinished.

`HappyDeliveryFlowTest#completeOrderLifecycle` verifies CROSS01–14 including every automatic customer state, exact restaurant queue transitions, immediate eligible dispatch, authorised initial/restored streams, pickup/delivery, both rider reload phases and all three histories. `#overlappingOrdersRemainIndependent` verifies CROSS15 with the same seeded customer in two isolated cart contexts, different brands and a sequentially assigned rider. Both exact receipts remain independent. These are two real Dev-order methods; mocked Dev payment produces retained orders without actual charging.

Run one method at a time from UITesting with app.url, customer.phone=8000000484, restaurant.phone=9000000001, rider.phone=7000000027, concurrent.restaurant.phone=9000000002, headless=true, slow.mo=0, record.video=false and excludedGroups=slow-auth,auth-rate-limit. Follow TEST-DATA.md. No register/provider setup or destructive cleanup. Only idle riders becomeOFFLINE. Retained assigned orders have explicit resume.order.id/resume.outlet; a still-pending retained pair may use concurrent.resume.pair. Never resurrect a terminal order through resume.

Exact reports/current verification are in RandomDocuments/E2ECoverageAudit_2026-10-01/08-cross-role-order-lifecycle.md. Intentional waits and quotas stay in the per-feature deferred files. History is retained in PENDING.md.
