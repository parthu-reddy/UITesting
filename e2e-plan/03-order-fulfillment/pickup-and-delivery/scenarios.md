# 03 — Pickup and Delivery — All Scenarios

Prerequisite: Rider has accepted a dispatch ping and is in the active job phase. Uses `DeliveryActiveJobPage`, `DeliveryOrderDetailsModalPage`, `MapTrackingPage`.

## Batch 1 — Navigate to restaurant (pickup phase)

| ID | Description | Action | Expected result |
|---|---|---|---|
| PICKUP-01 | Active job shows pickup phase | After accepting dispatch. | Implemented and live-validated: exact Active Contract order ID, selected outlet, customer address and pickup controls render. |
| PICKUP-02 | View order details before pickup | Tap "View Details" / order details on active job. | `DeliveryOrderDetailsModalPage` shows itemised list, customer name, and total. |
| PICKUP-03 | Map link or directions link | Active job shows a map or navigation link. | Map/directions link opens (or copies address to clipboard) without error. |
| PICKUP-04 | "Arrived at Restaurant" button | While in pickup phase, `DeliveryActiveJobPage` shows "Arrived at Restaurant" button. | Implemented and live-validated before clicking. |
| PICKUP-05 | Mark arrived at restaurant | Tap "Arrived at Restaurant". | Implemented and live-validated through the following pickup OTP interaction. |

## Batch 2 — Pickup OTP and SwipeAction confirmation

Uses `DeliveryActiveJobPage.enterPickupOtp()` and `DeliveryActiveJobPage.swipeToConfirmPickup()`.

| ID | Description | Action | Expected result |
|---|---|---|---|
| PICKUP-06 | Item list visible before pickup | After arriving at restaurant. | Item checklist or order summary visible so rider can verify items. |
| PICKUP-07 | Pickup OTP input appears | After arriving at restaurant. | Implemented and live-validated, including restoration after accepted-job reload. |
| PICKUP-08 | Enter valid pickup OTP | Enter the 6-digit pickup OTP provided by the restaurant via `enterPickupOtp()`. | Implemented and live-validated with the restaurant-rendered six-digit OTP. |
| PICKUP-09 | Swipe to confirm pickup | After entering valid pickup OTP, swipe the slider via `swipeToConfirmPickup()`. | Implemented and live-validated: Step 2, Package Picked Up, customer address and delivery OTP input render. |
| PICKUP-10 | Enter wrong pickup OTP | Enter "000000" as pickup OTP → try swipe to confirm. | Error message or swipe blocked; pickup not confirmed. |
| PICKUP-11 | Customer address shown after pickup | After confirming pickup via swipe. | Implemented and live-validated against the exact address captured from the dispatch card. |

## Batch 3 — Delivery OTP and SwipeAction confirmation

Uses `DeliveryActiveJobPage.enterDeliveryOtp()` and `DeliveryActiveJobPage.swipeToConfirmDelivery()`.

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-01 | Drop-off phase indicators | After pickup confirmed. | Implemented and live-validated with Step 2 and Package Picked Up indicators. |
| DELIVERY-02 | Delivery OTP input appears | When at customer's location. | Implemented and live-validated before and after delivery-phase reload. |
| DELIVERY-03 | Customer provides delivery OTP | On customer tracker, `getDeliveryOtp()` returns a 6-digit code. | Implemented and live-validated from the customer context. |
| DELIVERY-04 | Enter valid delivery OTP | Rider enters the customer's OTP via `enterDeliveryOtp()`. | Implemented and live-validated. |
| DELIVERY-05 | Swipe to confirm delivery | After entering valid delivery OTP, swipe via `swipeToConfirmDelivery()`. | Implemented and live-validated through the exact Delivered history row; earnings comparison is implemented but awaits a clean rerun after environment failures. |
| DELIVERY-06 | Enter wrong delivery OTP | Enter "000000" → try swipe. | Error; delivery not confirmed. |

## Batch 4 — Rider navigation and communication during active job

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-07 | Open navigation map | During pickup phase, tap navigation button via `openNavigationMap()`. | Map or navigation app/link opens; no crash. |
| DELIVERY-08 | Call customer from active job | During delivery phase, tap call button via `callCustomer()`. | `CallOverlayPage` opens with masked number. |

## Batch 5 — Post-delivery

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-09 | Rider returns to available state | After delivery complete. | Implemented and live-validated: Active Contract disappears and Online Duty renders. |
| DELIVERY-10 | Completed trip in history | Open History tab after delivering. | Implemented and live-validated for the exact order, selected restaurant, Delivered status and positive net payout. |
| DELIVERY-11 | Earnings update post-delivery | Open Earnings tab after delivering. | Implemented against the dashboard Today's Earnings tile using the exact history net payout; final assertion awaits a clean rerun after environment failures. |

## Batch 6 — Edge cases

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-12 | Reload during active delivery | While in drop-off phase, reload the page. | Implemented and live-validated: exact order ID, Step 2 and delivery OTP input are restored rather than reset to pickup. |
| DELIVERY-13 | Customer not found at address | Rider marks "Customer Not Found" if button exists. | System offers re-attempt or auto-cancellation flow; documents this UI state. |
