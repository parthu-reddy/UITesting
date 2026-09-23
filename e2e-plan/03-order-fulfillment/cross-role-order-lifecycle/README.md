# cross-role-order-lifecycle

Status: CROSS-01 through CROSS-08 implemented in `HappyDeliveryFlowTest` and passed against the development UI on 2026-09-23. Later batches remain pending.

Scope: One coordinated customer-to-restaurant-to-rider journey and state agreement.

Implementation: [HappyDeliveryFlowTest](../../../src/test/java/com/fooddelivery/e2e/tests/flows/HappyDeliveryFlowTest.java). Run with `mvn -q -Dtest=HappyDeliveryFlowTest -Dheadless=true test` from `UITesting`. The app URL remains configurable with `-Dapp.url=...`. Customer, restaurant and rider phones are randomly selected from their seeded dummy-data ranges to spread OTP traffic. Any role can be pinned for reproduction with its `-Dcustomer.phone=...`, `-Drestaurant.phone=...`, or `-Drider.phone=...` override.

The test uses Home, chooses a displayed Brand 1 outlet under 5 km, and keeps separate customer, restaurant, and rider browser contexts. It verifies the exact order ID in the restaurant queue and in the rider's Delivered history. Restaurant acceptance and preparation precede dispatch; the rider accepts the short-lived ping before the test opens the pickup OTP. The expanded test also verifies the gross dispatch offer and details, accepted-job and delivery-phase reload recovery, pickup/drop-off details, return to available duty, positive completed-trip net payout and the corresponding today's-earnings increase.

Validation: the original full UI-only run passed on 2026-09-23. Expanded validation is in progress; see `PENDING.md` for the current delivery-availability failure. No backend setup or direct service calls are used.
