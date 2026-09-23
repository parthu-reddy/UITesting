# Pending: customer tracker after delivery

On 2026-09-23, a UI-only run of `HappyDeliveryFlowTest` completed restaurant acceptance, rider pickup, and delivery. Rider history showed the exact new order as **Delivered**, but the customer's still-open tracker continued to show its earlier active state for 90 seconds and never rendered `rate-order-prompt`. This happened in repeated full-flow runs. The completed lifecycle test therefore asserts the exact order in rider history, while customer terminal-state synchronization remains unverified.

When starting this functionality folder, investigate the customer's live update and history UI for the same order ID. Add a dedicated UI assertion for the terminal customer state after the UI behavior is reliable. The test itself should remain UI-only; Oracle server logs may help diagnose the synchronization failure.

Active-order recovery is also affected. After order `e6b39e43-f1c9-4d0e-bb63-e5d6333b2cf8` was accepted and picked up, logging the same customer (`8000000092`) in again and reloading did not restore the `Secure Delivery Verification` panel. The rider UI correctly restored the delivery phase, but the customer delivery OTP was unreachable through the UI, preventing UI-only completion of that interrupted order.

Source investigation found that `getActiveOrders` correctly returns HANDED_OVER / OUT_FOR_DELIVERY orders, but `CustomerDashboard` kept the selected `trackingOrder` only in component memory. After login or reload it rendered an active-order card and left the full tracker, including the delivery OTP, closed. UI commit `fa844ff` now selects the newest in-flight order once after the initial active-order fetch. It does not reopen after the customer deliberately closes it. Type checking, 339 UI tests and the production build pass. Source is pushed, but deployment and live resume verification remain pending.

## Customer history is not reachable

Source inspection on 2026-09-23 found `CustomerOrderHistory.tsx` defines the history overlay, rows, totals, statuses and pagination, but no source file renders `<CustomerOrderHistory>`. `CustomerDashboard.tsx` routes every customer path through `path="*"` to `CustomerMainView`. Consequently, HISTORY-01 through HISTORY-06 and REORDER-01 through REORDER-03 cannot be exercised through the deployed UI. No backend call or storage injection will be used to bypass this missing UI entry point.

HOME-05 and HOME-06 live-passed. With one UI-added menu item, the free-delivery tracker exposes `role=progressbar`, `aria-valuenow` between 0 and 100, and either `Add ₹… for Free Delivery!` or `Free Delivery Unlocked!`. HOME-07 also live-passed by incrementing the same item through the rendered controls until the progress reached 100 and the unlocked message appeared; no checkout occurred.
