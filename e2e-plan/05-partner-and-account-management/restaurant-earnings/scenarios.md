# 05 — Restaurant Earnings — All Scenarios

**Financial Integrity Rule**: Earnings amounts must be derived from actual completed orders. No hardcoded expected amounts. If the earnings figure is unavailable, the test FAILS — it does not assert ₹0.

Uses: `RestaurantEarningsPage`.

## Batch 1 — Earnings summary display

| ID | Description | Action | Expected result |
|---|---|---|---|
| EARNINGS-01 | Earnings tab accessible | Login as restaurant → tap "Earnings" tab. | `RestaurantEarningsPage` renders without crash. |
| EARNINGS-02 | Summary fields visible | On earnings page. | At least one summary field visible (e.g. "Total Earnings", "This Week", "This Month"). |
| EARNINGS-03 | Earnings amount is non-negative | All earnings figures displayed. | Every figure ≥ ₹0; no negative amounts shown to partner. |
| EARNINGS-04 | Earnings amount is non-null | All earnings figures. | No `null`, `undefined`, or "₹null" rendered anywhere. |
| EARNINGS-05 | Earnings after completed order | After at least one order is completed. | Earnings figure increases by the order's restaurant payout amount. |

## Batch 2 — Earnings breakdown / history

| ID | Description | Action | Expected result |
|---|---|---|---|
| EARNINGS-06 | Order-level earnings history | Navigate to earnings history (if available). | List of completed orders with individual payout amounts. |
| EARNINGS-07 | Date filter on earnings | If a date range filter exists, apply "Last 7 days". | Earnings filtered to the selected range; total updates. |
| EARNINGS-08 | Clear date filter | Clear the date filter. | All-time earnings restored. |
| EARNINGS-09 | Individual order payout matches total | Sum of individual order payouts in history ≈ total earnings shown. | Arithmetic difference ≤ ₹0.01. |

## Batch 3 — Payout request (if UI available)

| ID | Description | Action | Expected result |
|---|---|---|---|
| EARNINGS-10 | Payout request button | If "Request Payout" button exists. | Button visible; tapping opens payout form. |
| EARNINGS-11 | Payout form shows available balance | On payout form. | Available balance = current earnings; non-zero after orders. |
| EARNINGS-12 | Payout with zero balance blocked | If balance is ₹0, tap Request. | Validation prevents ₹0 payout request. |
| EARNINGS-13 | Payout amount entry | Enter a valid payout amount ≤ available balance → submit. | Payout request recorded; balance updates to available minus requested amount. |
