# Pending: customer tracker after delivery

On 2026-09-23, a UI-only run of `HappyDeliveryFlowTest` completed restaurant acceptance, rider pickup, and delivery. Rider history showed the exact new order as **Delivered**, but the customer's still-open tracker continued to show its earlier active state for 90 seconds and never rendered `rate-order-prompt`. This happened in repeated full-flow runs. The completed lifecycle test therefore asserts the exact order in rider history, while customer terminal-state synchronization remains unverified.

When starting this functionality folder, investigate the customer's live update and history UI for the same order ID. Add a dedicated UI assertion for the terminal customer state after the UI behavior is reliable. The test itself should remain UI-only; Oracle server logs may help diagnose the synchronization failure.

Active-order recovery is also affected. After order `e6b39e43-f1c9-4d0e-bb63-e5d6333b2cf8` was accepted and picked up, logging the same customer (`8000000092`) in again and reloading did not restore the `Secure Delivery Verification` panel. The rider UI correctly restored the delivery phase, but the customer delivery OTP was unreachable through the UI, preventing UI-only completion of that interrupted order.

Source investigation found that `getActiveOrders` correctly returns HANDED_OVER / OUT_FOR_DELIVERY orders, but `CustomerDashboard` kept the selected `trackingOrder` only in component memory. After login or reload it rendered an active-order card and left the full tracker, including the delivery OTP, closed. UI commit `fa844ff` now selects the newest in-flight order once after the initial active-order fetch. It does not reopen after the customer deliberately closes it. Type checking, 339 UI tests and the production build pass. Source is pushed, but deployment and live resume verification remain pending.

## Customer history reachability changed

The standalone `CustomerOrderHistory.tsx` overlay remains unmounted, but the current UI now exposes order history through Account Settings → History via `SettingsHistoryTab`. `CustomerOrderHistoryUiTest` covers the reachable empty/populated states, validates every populated summary, and opens the exact tracker from a row. HISTORY-03 remains pending because no date is rendered. A deterministic cancelled row is still needed for HISTORY-06. The reachable tracker exposes no Reorder action, so REORDER-01 through REORDER-03 remain unavailable through the UI.

The three new history tests live-passed on 2026-09-24 with a randomized seeded customer. That account rendered the explicit empty-history state. A targeted rerun with customer `8000000362`, whose earlier delivery completed in the rider UI, also returned zero history rows and `No order history found.` The populated-row and exact-tracker branches compile and remain active, but customer history persistence/synchronization must be fixed or a seeded customer with visible history must be provided before those branches can execute live.

HOME-05 and HOME-06 live-passed. With one UI-added menu item, the free-delivery tracker exposes `role=progressbar`, `aria-valuenow` between 0 and 100, and either `Add ₹… for Free Delivery!` or `Free Delivery Unlocked!`. HOME-07 also live-passed by incrementing the same item through the rendered controls until the progress reached 100 and the unlocked message appeared; no checkout occurred.

## UI fixes applied 2026-09-24 (not yet deployed or re-run)

**REORDER-01 through REORDER-03 are no longer unavailable.** The note that "the reachable
tracker exposes no Reorder action" was accurate when written; it is not now.

The redesign added an **"Order it again"** strip to the customer home
(`features/customer-orders/components/ReorderStrip.tsx`, wired into
`CustomerRestaurantBrowser`), backed by `useReorderSuggestions` reading
`GET /api/v1/orders/history`. Each suggestion is a real `<button>` reading `Reorder - <total>`,
deduplicated to the most recent DELIVERED order per restaurant.

`ReorderStripPage.java` already existed and its locator `button:has-text('Reorder')` now
resolves; its doc comment has been corrected (the strip is on the home feed, not order history).

**Prerequisite:** the strip renders nothing without completed orders, so it needs a customer
with delivery history -- the same blocker as the populated-history branches. An absent strip is a
missing prerequisite, not a failure.

These changes are applied to `FoodDeliveryAppUI` source and pass typecheck, lint and the 339
unit tests. **They are not verified**: nothing here is proven until the UI is deployed and the
owning test is re-run live. Do not mark anything validated on the strength of this note.
