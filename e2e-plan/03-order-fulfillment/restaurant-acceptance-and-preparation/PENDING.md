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
