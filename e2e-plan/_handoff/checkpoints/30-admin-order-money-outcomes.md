# Checkpoint30: the admin order-money panel could not tell outcomes apart

Updated 2026-10-03T09:05:00+05:30. Priority item: admin order-money views per outcome.

## Finding

`/admin/orders/:orderId/money` (`AdminOrderMoney.tsx`) showed the price breakdown, the quoted restaurant and rider payouts, and the ledger trace, but **no payment status, no refunds and nothing about what was actually booked**. The API already returns payment method, status, gateway and every refund. So a cancelled or rejected order showed "Net Payout" figures as if earned, and a refunded order looked unrefunded. For a delivered order the quoted figures equal the ledger nets (bb43e2a4: 19.11 / 18.86), so a booked-amount line is a real check.

## Fix (FoodDeliveryAppUI, local)

A **Payment and Refunds** section: method, gateway, a payment status pill, and refund rows with status, amount, destination, fault and completed time or failure reason ("No refunds." otherwise). Each payout card gains **Posted to ledger**: the payee's ledger credits minus debits, or "Not posted". Labels are unchanged, so existing tests and page objects still hold. Tests: +4 (direction mutation red); vitest 758/121, typecheck, lint.

## Live fixtures for every outcome

Fresh `OrderCancellationFlowTest` → **19359711** and `RestaurantRejectFlowTest` → **bf109947**, both PASS. Each is REFUNDED with one COMPLETED ₹53.53 refund, and its ledger holds only the capture and the refund. Riders OFFLINE.

## New test, red as expected

`AdminOrderMoneyOutcomesTest` (MONEY-05) over bb43e2a4 DELIVERED, d3acfc93 PARTIAL, cf608115 PARTIAL, 19359711 CANCELLED, bf109947 REJECTED: **5/5 fail on UI 766b214** because the payment section isn't there. Expected green after the UI deploy. (Its first attempt errored on a JUnit argument-binding mistake in the test, fixed before the counted run.)

## Pending

Deploy **food-delivery-app-ui**, then rerun MONEY-05 (command in NEXT-STEPS). Evidence: [30-order-money-outcomes.json](../evidence/30-order-money-outcomes.json).
