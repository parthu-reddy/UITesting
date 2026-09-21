# 03 — Rider Availability and Dispatch — All Scenarios

**Critical rule**: Order is only visible to the rider AFTER the restaurant accepts it AND preparation is either ≤ 15 minutes from completion OR already complete. Rider MUST be located near the restaurant (< 5 km) to receive a dispatch.

Uses: `DeliveryDashboardPage`, `DeliveryOnlineTogglePage`, `DispatchPingPage`, `DeliveryActiveJobPage`.

## Batch 1 — Rider availability toggle

| ID | Description | Action | Expected result |
|---|---|---|---|
| DISPATCH-01 | Go Online | Login as rider → tap "Go Online" / availability toggle via `DeliveryOnlineTogglePage`. | Toggle shows "Online" / green state; dashboard indicates available status. |
| DISPATCH-02 | Go Offline | While online, tap toggle to go Offline. | Toggle shows "Offline" / grey state; no dispatch pings received while offline. |
| DISPATCH-03 | Online toggle persists on reload | Go Online → reload page. | Rider is still shown as Online without re-toggling. |
| DISPATCH-04 | Offline by default (fresh login) | Login as rider without toggling. | Default state is documented (Online or Offline); verify no assumption that rider is always Online. |

## Batch 2 — Receiving dispatch ping

| ID | Description | Prerequisite | Action | Expected result |
|---|---|---|---|---|
| DISPATCH-05 | Dispatch ping appears | Rider Online, restaurant has marked food Ready, prep time constraint met. | Wait up to 90 s. | `DispatchPingPage` modal/card appears showing restaurant name, pick-up address, customer area, estimated distance. |
| DISPATCH-06 | Ping shows order details | On dispatch ping. | Verify restaurant name, estimated payout, and distance are non-empty. | All three fields non-empty. |
| DISPATCH-07 | Ping has accept and decline buttons | On dispatch ping modal. | — | "Accept" and "Decline" (or "Reject") buttons both visible. |
| DISPATCH-08 | Ping timeout | If rider does not respond to ping within the system timeout. | Ping dismisses automatically; order is re-dispatched to another rider or enters exception state. | Ping card disappears after timeout; no JS error. |

## Batch 3 — Accepting a dispatch

| ID | Description | Action | Expected result |
|---|---|---|---|
| DISPATCH-09 | Accept dispatch | Tap "Accept" on ping. | `DeliveryActiveJobPage` opens; shows "Navigate to Restaurant" phase with restaurant address. |
| DISPATCH-10 | Active job shows restaurant address | On active job after accept. | Restaurant name and approximate address visible; not empty. |
| DISPATCH-11 | Active job phase indicator | After accept. | Phase label shows "Pickup" or "Going to Restaurant" or equivalent. |
| DISPATCH-12 | Accept is idempotent | Accept dispatch → reload rider page. | Active job still shown; no duplicate assignment. |

## Batch 4 — Declining a dispatch

| ID | Description | Action | Expected result |
|---|---|---|---|
| DISPATCH-13 | Decline dispatch | Tap "Decline" on ping. | Ping dismissed; rider returns to available dashboard; order is re-dispatched or enters exception queue. |
| DISPATCH-14 | Decline reason form (if applicable) | If decline requires a reason, submit blank. | Validation prevents blank submission. |
| DISPATCH-15 | Multiple declines | Decline 2 consecutive pings. | After N declines (platform threshold), rider may be marked as temporarily unavailable — document actual behavior. |

## Batch 5 — Dashboard earnings and stats

| ID | Description | Action | Expected result |
|---|---|---|---|
| DISPATCH-16 | Today's Earnings visible | Login as rider. | "Today's Earnings" figure visible on `DeliveryDashboardPage`. |
| DISPATCH-17 | Earnings tab opens | Tap "Earnings" tab via `DeliveryDashboardPage.openEarningsTab()`. | `RiderEarningsPage` renders with earnings summary; no blank panel. |
| DISPATCH-18 | History tab opens | Tap "History" tab. | `DeliveryHistoryPage` renders; completed trip list or empty state shown. |
| DISPATCH-19 | Active tab shows current job | While a job is active, tap "Active" tab. | Current active job card is visible with pickup/drop-off details. |

## Batch 5 — Rider blocked state and wallet

| ID | Description | Action | Expected result |
|---|---|---|---|
| DISPATCH-20 | Rider isBlocked check | If rider's KYC is rejected or account is suspended. | `DeliveryOnlineTogglePage.isBlocked()` returns true; toggle to go online is disabled; message shown. |
| DISPATCH-21 | Rider wallet tab opens | Tap "Wallet" tab via `DeliveryDashboardPage.openWalletTab()`. | `RiderWalletPage` renders with wallet balance visible. |
| DISPATCH-22 | Rider wallet balance non-null | On wallet tab. | `RiderWalletPage.getWalletBalance()` returns a value ≥ ₹0; not `null`. |

