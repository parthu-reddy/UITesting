# Validation and pending work

## Restaurant earnings panel

Current deployed validation on 2026-09-23 fails because clicking Earnings returns to Live Kitchen; Net Earnings never becomes visible. This matches the restaurant dashboard route override recorded under restaurant acceptance/navigation. A previous run rendered Net Earnings, Pending Balance and Clawbacks, but that result is stale for the current deployment. Amount accuracy remains unverified.

Evidence: UITesting/target/surefire-reports (reports are overwritten by focused reruns); failure screenshots/HTML in target/screenshots. Broader coverage remains pending.

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

These changes are applied to `FoodDeliveryAppUI` source and pass typecheck, lint and the 339
unit tests. **They are not verified**: nothing here is proven until the UI is deployed and the
owning test is re-run live. Do not mark anything validated on the strength of this note.
