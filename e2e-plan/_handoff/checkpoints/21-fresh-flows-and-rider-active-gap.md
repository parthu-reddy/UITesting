# Checkpoint21: fresh-order runs after reset, and the rider active-trip gap

Updated 2026-10-02T20:05:00+05:30. The user deployed checkpoint20 and reset/reseeded the Dev data, then asked to continue with fresh orders.

## Environment verified (read-only)

The deploy pins and running images are exactly the committed HEADs: customer dee93c0, payment 66f0913, chat 7cffcad, UI 71138db. All services are healthy. Flyway `20261002190000` applied (`sweep_attempts` exists). Before the runs there were 0 orders, refunds, intents, transactions and `refund_req` keys. Actors 8000000484 / 9000000001 / 7000000026 / 1000000001 exist. The live URL is still `https://gulf-strike-dark-extras.trycloudflare.com/`; `~/cloudflared.log` on the VM is from August and its hostname is dead.

## Results

| Flow | Result | Order |
|---|---|---|
| OrderCancellationFlowTest#customerCancelsBeforeAcceptance (fresh) | **PASS** 1/1, 0 skip, 88s | b7d01530-ac02-4da1-9be6-303b73bfedc5 |
| RestaurantRejectFlowTest#restaurantCancelsOrder (fresh) | **PASS** 1/1, 0 skip | 881ba9a0-1cbe-40a1-95d0-c47811e7ef48 |
| HappyDeliveryFlowTest#completeOrderLifecycle (fresh) | **FAIL** at line 581 after 68s | 7f7af6a5-1d86-4f29-b4cc-36eefe69a74b (retained, ASSIGNED) |

The refund flows were confirmed in the databases. Refunds COMPLETED on the first attempt (attempts 0, sweep_attempts 0). Customer and gateway payments are REFUNDED. Each gateway has one SUCCESS capture with `amount_refunded` 43.35 and a COMPLETED gateway refund. The ledger's REFUND lines balance its ORDER_TOTAL lines. The rider was OFFLINE afterwards. Evidence: [21-fresh-refund-flows.json](../evidence/21-fresh-refund-flows.json).

## Happy-flow failure: root cause (from source, logs and the database)

The rider accepted the dispatch at 14:09:10.738Z: the assignment was committed, the rider was ON_DELIVERY, and the UI showed the Active Contract with the restaurant address. Seconds later the UI showed "Scanning for dispatched contracts" and the delivery-address check failed.

`GET /api/v1/delivery/orders/active` answered only from the customer service's copy (`orders.delivery_executive_id`). That copy learns the driver when DRIVER_ASSIGNED is consumed, at 14:09:12.769Z (outbox lag ≈ 2s). Meanwhile `/available` was empty because the ping had been consumed. The rider UI's poll handler (`useDeliveryOrders.onData`) *replaces* its list, so a poll in that gap dropped the rider's own trip. The race predates checkpoint20; the delivery service hasn't changed since 07:18Z, before the last good run. Server state stayed consistent throughout.

## Fix (local, uncommitted)

- **CustomerApplication**: `GET /internal/orders/driver/{id}/active` accepts `confirmedOrderIds`. `findActiveOrdersForDriver` returns the driver's recorded orders *or* confirmed ids whose `delivery_executive_id IS NULL`, so it never exposes another rider's order. The `CustomerMcpService` caller was updated, plus a new contract `getActiveOrdersForDriverWithConfirmedAssignments.groovy`, the ContractTestBase stubs, and a regenerated `openapi.json`.
- **DeliveryExecutiveApplication**: `OrderAssignmentRepository.findByDriverIdAndState`. `ConfirmedDeliveryProgress.heldOrderIds` sends the rider's ASSIGNED orders. `apply` marks a held order as this driver's, with deliveryStatus at least ASSIGNED (forward-only; never over a cancellation; a RELEASED row claims nothing). The Feign client and fallback were updated.
- **Test-only fix**: delivery `OpenApiGenerationTest` had been red since `8a9c7b6`, because `RestaurantStatusStreamConnectionTest.StreamConfiguration` was `@Configuration` in the scanned package. The annotation was removed (that test registers the class explicitly).
- **FoodDeliveryAppUI**: regenerated API types. They include the customer parameter and the stale payment spec catch-up (`MANUAL_ASSIGNMENT_FAILED`).

## Verification

- Customer full `clean test`: 458 tests in 90 classes, 0 fail/error/skip, including the generated `InternalTest` (9) with the new contract.
- New real-JPA `ActiveOrdersForDriverQueryTest` (3) includes an empty-list case; Hibernate accepts an empty `IN`, so no placeholder value was needed.
- Customer stubs reinstalled locally. Delivery full `clean test`: 159 tests in 44 classes, 0 fail/error. One skip, the pre-existing generated `validate_getDeliveryStatus`. `DeliveryExecutiveContractConsumerTest` 5/5, including the new confirmed-assignments call.
- UI: typecheck and lint pass, 737/121 files.
- Guards seen red: the query without its other-driver guard fails `anotherDriversOrderIsNeverReturnedEvenIfNamed`; the query ignoring confirmed ids fails `anOrderConfirmedByTheDeliveryService…`; the overlay without its ASSIGNED default fails `aJustAcceptedOrderReadsAsThisRidersAssignedTrip`.

## Mistake recorded

My first caller enumeration searched only for the repository method and missed `CustomerMcpService`, which calls the controller method directly; the compiler caught it. Enumerate the callers of every changed signature: controllers, services, tests and Feign fallbacks.

## Next

1. The user deploys customer-service and delivery-service (and the UI for its regenerated types, optional at runtime). No migration.
2. Resume the retained order. Do **not** create another one:
   `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=HappyDeliveryFlowTest#completeOrderLifecycle' -Dapp.url=https://gulf-strike-dark-extras.trycloudflare.com/ -Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 -Dresume.order.id=7f7af6a5-1d86-4f29-b4cc-36eefe69a74b '-Dresume.outlet=Brand 1 Outlet 3' -DexcludedGroups=slow-auth,auth-rate-limit test`
   The rider is ON_DELIVERY on that order; keep it until delivery completes.
3. Then run a fresh full happy lifecycle once, to prove the accept→active gap closes live; the resume skips the accept step.

## Correction recorded 2026-10-02T23:20:00+05:30

The new contract `getActiveOrdersForDriverWithConfirmedAssignments.groovy` overlapped the existing `getActiveOrdersForDriver.groovy` (WireMock ignores the extra `confirmedOrderIds` parameter on the old stub). The local `DeliveryExecutiveContractConsumerTest` 5/5 pass depended on stub load order; CI Phase 2 contract testing failed for DeliveryExecutiveApplication. Fixed in the publish run by `priority 1` (specific) and `priority 2` (general). The claim above that the consumer contract was verified was stronger than the evidence. Lesson: [overlapping-stub-priority-mistake.md](../issue-notes/overlapping-stub-priority-mistake.md).
