# 05 — Rider Wallet, Earnings, and History — All Scenarios

**Financial Integrity Rule**: All earnings and wallet amounts must be derived from actual completed deliveries. No hardcoded ₹0 assertions. Missing amounts = test failure.

Uses: `RiderEarningsPage`, `RiderWalletPage`, `DeliveryHistoryPage`.

## Batch 1 — Rider earnings

| ID | Description | Action | Expected result |
|---|---|---|---|
| RIDER-EARN-01 | Earnings tab renders | Login as rider → tap "Earnings" tab. | `RiderEarningsPage` renders; summary visible. |
| RIDER-EARN-02 | Today's earnings visible | On dashboard. | "Today's Earnings" figure is ≥ ₹0; non-null. |
| RIDER-EARN-03 | Earnings increase after delivery | After completing a delivery. | Earnings figure increases by the delivery payout; not ₹0 increase if payout > ₹0. |
| RIDER-EARN-04 | Weekly earnings visible | On Earnings page, weekly total shown. | Weekly figure ≥ ₹0; non-null. |
| RIDER-EARN-05 | Monthly earnings visible | Monthly total shown. | Monthly figure ≥ ₹0; non-null. |

## Batch 2 — Trip history

| ID | Description | Action | Expected result |
|---|---|---|---|
| RIDER-HISTORY-01 | History tab renders | Tap "History" / "Trips Completed" tab. | `DeliveryHistoryPage` renders; completed trip list or empty state. |
| RIDER-HISTORY-02 | Date filter entry | Enter "2000-01-01" in date filter. | Date value reflected in filter field. |
| RIDER-HISTORY-03 | Clear date filter | Clear date filter field. | Field empties; all trips shown. |
| RIDER-HISTORY-04 | Trip entry shows restaurant name | Each trip in history. | Restaurant name visible; non-empty. |
| RIDER-HISTORY-05 | Trip entry shows earnings | Each trip in history. | Earnings per trip visible; ≥ ₹0 and non-null. |
| RIDER-HISTORY-06 | Trip entry shows date | Each trip. | Date formatted readably; not `null`. |
| RIDER-HISTORY-07 | Empty state message | If no trips in selected date range. | "No trips" / empty-state message shown; no crash. |

## Batch 3 — Rider wallet

| ID | Description | Action | Expected result |
|---|---|---|---|
| RIDER-WALLET-01 | Wallet section renders | Open Profile settings → Wallet section. | `RiderWalletPage` or wallet sub-section renders; bank account / UPI fields visible. |
| RIDER-WALLET-02 | Bank account field | Bank account or UPI ID field visible. | Field shows current value or "Not set" — not `null`. |
| RIDER-WALLET-03 | Wallet balance visible | On wallet page. | Balance ≥ ₹0; formatted with ₹ symbol. |
| RIDER-WALLET-04 | Withdraw form (if available) | Tap "Withdraw" or "Request Payout". | Withdraw amount form opens; available balance shown. |
| RIDER-WALLET-05 | Withdraw zero blocked | Enter ₹0 → tap Withdraw. | Validation error; ₹0 withdrawal not permitted. |
| RIDER-WALLET-06 | Withdraw exceeds balance blocked | Enter amount > wallet balance. | Validation error; withdrawal capped at available balance. |

## Batch 4 — Document verification status

| ID | Description | Action | Expected result |
|---|---|---|---|
| RIDER-DOC-01 | Document status visible | On rider profile settings. | Aadhaar and license status shown (Pending / Verified / Rejected); not blank. |
| RIDER-DOC-02 | Verified status unlocks earnings | If KYC verified, full earnings functionality available. | Earnings and wallet tabs fully functional. |
