# Pending restaurant acceptance and navigation coverage

## Restaurant section navigation failure

`RestaurantNavigationUiTest` fails after clicking **Menu Stock Toggles**: the deployed UI stays on **Live Kitchen Feed** and `In-Stock Dish Toggles` never appears. Reproduced twice on 2026-09-23. The initial Live Kitchen panel and its `New Placed` column render correctly.

Current UI source explains the behavior. `RestaurantDashboard` supplies this tab handler:

```tsx
onChange={(key) => { setActiveTab(key); setShowSettings(false); }}
```

`setActiveTab(key)` navigates to `/restaurant/<key>`, then `setShowSettings(false)` immediately navigates to `/restaurant`, replacing the selected route. This affects Menu, Ad Campaigns, Earnings and Reviews navigation through the main tab set. No application change or deployment was made during E2E work.

## Remaining scenario work

- REST-ACCEPT-01 through REST-ACCEPT-04 need current queue/detail terminology reconciled with the Kanban UI.
- REST-ACCEPT-07 customer real-time acceptance state remains affected by the separately recorded customer tracker synchronization issue.
- REST-ACCEPT-08 reload/idempotency and REST-ACCEPT-09 through REST-ACCEPT-11 rejection need dedicated order flows and cleanup.
- REST-ACCEPT-16 and REST-ACCEPT-17 need the current preparation-time display and early-ready business behavior inspected during a created order.
- REST-ACCEPT-19 and REST-NAV-02 through REST-NAV-05/07 are blocked by the navigation defect above.
- REST-ACCEPT-20 should assert the exact completed order after a lifecycle run.

## Acceptance and preparation validation (2026-09-24)

`RestaurantFulfillmentTest.restaurantCanAcceptAndPrepareOrder` live-passed against order `f452042b-9963-4592-9f7c-c7f46f1d7b88` at `Brand 5 Outlet 10`. It now verifies the exact incoming card, customer and money summaries, enabled Accept/Cancel controls, order-details dialog, acceptance, reload persistence without duplication, Start Cook, the customer's visible `Kitchen is Cooking...` state, Mark Prepared, READY state, and a six-digit handover OTP. The test can resume an existing order so a failed assertion does not require creating another order.

The details UI recovered from HTTP 500 on the restaurant-money endpoint by rendering its documented available-data fallback. That endpoint failure remains a backend/data issue; it did not prevent restaurant fulfillment.

## Restaurant rejection synchronization (2026-09-24)

`RestaurantFulfillmentTest.restaurantRejectsIncomingOrder` created order `45cbc0f5-af46-4eb2-9c4d-7cbc8c9546b7` at `Brand 8 Outlet 6`. The restaurant cancellation form accepted `Item out of stock` and the exact card disappeared from the restaurant queue, but after customer reload the exact tracker still showed `Waiting for Restaurant...`, `Order Received`, and an active Cancel Order button for more than 30 seconds. The expected terminal rejection headline and reason never appeared. The strict test remains failing for REST-ACCEPT-10.

REST-ACCEPT-11 is also a confirmed UI gap from source and rendered controls: Confirm Cancel is enabled with an empty reason and the drawer closes immediately on submission. There is no client-side required-field validation to assert until the product adds it.

## UI fixes applied 2026-09-24 (not yet deployed or re-run)

The **restaurant navigation route override** is fixed in
`src/pages/restaurant/RestaurantDashboard.tsx`.

Cause, confirmed in source: `view`, `showSettings` and `activeTab` are all derived from
`location.pathname`, and `setActiveTab` / `setView` / `setShowSettings` are all `navigate()`
calls. Three handlers called two of them in sequence, so the second navigation cancelled the
first:

```js
onChange={(key) => { setActiveTab(key); setShowSettings(false); }}
//         -> /restaurant/menu        -> /restaurant   (wins)
```

`setShowSettings(false)` was never needed -- navigating to `/restaurant/<tab>` already leaves the
settings view, because `showSettings` is derived from the path. The redundant second navigation
was removed from the tab handler, from `onToggleProfile` (the reason restaurant **Log Out** was
unreachable) and from `onToggleSettings`.

Re-run the tests in this folder after deployment.

**Accept Order was dead** and is fixed in
`features/restaurant-orders/components/RestaurantOrderCard.tsx`. `RestaurantOrderActions`
destructured `setIsSubmitting`, but the card never passed it, so it was `undefined` and
`onClick={() => { setIsSubmitting(true); handleStatusTransition(order); }}` threw
`TypeError: setIsSubmitting is not a function` on its FIRST line -- before the transition ran.
Accept Order, Start Cooking and Mark Ready were all dead, silently, because a throw inside a
React event handler surfaces nothing. Confirmed live against a real PENDING_ACCEPTANCE order on
2026-09-19; the order then auto-cancelled at 10m50s via `RestaurantTimeoutSweeper`.

TypeScript could not see it: the props were `Record<string, any> & { order: Order }` with
`no-explicit-any` disabled above them, so an omitted callback type-checked. The props are now a
real interface.

These changes are applied to `FoodDeliveryAppUI` source and pass typecheck, lint and the 339
unit tests. **They are not verified**: nothing here is proven until the UI is deployed and the
owning test is re-run live. Do not mark anything validated on the strength of this note.


## Restaurant tab navigation — STILL OPEN after four fixes (2026-09-24)

The earlier note in this file says the route override is fixed. That part is true and deployed,
but **the tabs still do not switch panels**. Correcting the record rather than leaving it.

### What is verified fixed
- The double-navigate (`setActiveTab(key); setShowSettings(false);` — both `navigate()` calls, the
  second cancelling the first). Deployed in `222d450`. The URL now sticks at `/restaurant/menu`.
- The catch-all `<Routes><Route path="*">` wrapper around the panels, removed in `94036a8`.
- A genuinely duplicated AnimatePresence key: `RestaurantOrderQueue`'s root carried
  `key="orders-panel"`, identical to the `motion.div` wrapping it in `RestaurantTabPanels`.
  Removed in `671985c`; confirmed in the deployed chunk (occurrences 2 -> 1).

### What is still broken
Measured on the deployed build `671985c`, bundle freshness confirmed against `index.html`:

| action | path | selected tab | Kanban | Stock panel |
|---|---|---|---|---|
| start | `/restaurant/orders` | Live Kitchen Feed | yes | no |
| click Menu Stock Toggles | `/restaurant/menu` | **Menu Stock Toggles** | **yes** | no |
| click Earnings | `/restaurant/earnings` | **Earnings** | **yes** | no |

### The constraints any explanation must satisfy
1. `Tabs` is **fully controlled** (`aria-selected={item.key === value}`, no internal state), and its
   highlight follows the click — so the parent re-renders with the new `activeTab`.
2. `RestaurantTabPanels` and `Tabs` are handed the **same variable in the same render**.
3. `RestaurantOrderQueue` is rendered from exactly one place: the orders branch. `RestaurantTabPanels`
   from exactly one place: `RestaurantDashboard`. Enumerated, not assumed.
4. A **full page load renders every panel correctly** — the defect only affects client-side transitions.
5. The incoming panel never enters the DOM at all (`In-Stock Dish Toggles` absent from `innerHTML`),
   while the outgoing one stays. That is the signature of `AnimatePresence mode="wait"` holding an
   exit that never resolves.

### Ruled out
Missing keys on the menu/settings branches. They were never missing: `RestaurantMenuTogglesView`
and `RestaurantSettingsShell` each own the presence key on their own motion root, which is the
pattern every branch follows. Keys added there in `9d8fc1b` **duplicated** them and were reverted.

### Suggested next step
The remaining suspect is `AnimatePresence mode="wait"` in `RestaurantTabPanels` never receiving
exit completion from the outgoing branch. The cheapest decisive experiment is to drop `mode="wait"`
(or `AnimatePresence` entirely) and see whether switching works; if it does, the fault is in the
exit lifecycle, not in the routing or the props. That was not attempted here deliberately — four
speculative fixes on one defect is already two too many.
