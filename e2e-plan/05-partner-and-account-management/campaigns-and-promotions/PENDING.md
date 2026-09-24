# Scenarios and validation

## Unsaved campaign cancellation

Current deployed validation on 2026-09-23 fails before the draft opens. Clicking the Ad Campaigns tab returns to Live Kitchen, so the New Campaign button never appears and the test times out. This matches the restaurant dashboard route override recorded under restaurant acceptance/navigation. No campaign mutation occurred. A previous run had passed the draft-cancellation assertions, but that result does not describe the current deployment.

`PartnerOperationsUiTest.verifyCampaignManagement` is now a strict CAMPAIGN-01 check rather than a no-op boolean lookup. Its live run fails because `Ad Spending History` never becomes visible after clicking `Ad Campaigns`; the browser also reports HTTP 403 for `/api/v1/brands/stream`. The test remains active so the navigation defect cannot be reported as a pass.

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
