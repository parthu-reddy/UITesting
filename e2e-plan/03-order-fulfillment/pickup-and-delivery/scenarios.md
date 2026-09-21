# 03 — Pickup and Delivery — All Scenarios

Prerequisite: Rider has accepted a dispatch ping and is in the active job phase. Uses `DeliveryActiveJobPage`, `DeliveryOrderDetailsModalPage`, `MapTrackingPage`.

## Batch 1 — Navigate to restaurant (pickup phase)

| ID | Description | Action | Expected result |
|---|---|---|---|
| PICKUP-01 | Active job shows pickup phase | After accepting dispatch. | Active job page shows "Go to Restaurant" / pickup phase with restaurant address. |
| PICKUP-02 | View order details before pickup | Tap "View Details" / order details on active job. | `DeliveryOrderDetailsModalPage` shows itemised list, customer name, and total. |
| PICKUP-03 | Map link or directions link | Active job shows a map or navigation link. | Map/directions link opens (or copies address to clipboard) without error. |
| PICKUP-04 | "Arrived at Restaurant" button | While in pickup phase, `DeliveryActiveJobPage` shows "Arrived at Restaurant" button. | Button is visible and tappable. |
| PICKUP-05 | Mark arrived at restaurant | Tap "Arrived at Restaurant". | Phase transitions to "Waiting for food" or similar; restaurant is notified. |

## Batch 2 — Pickup OTP and SwipeAction confirmation

Uses `DeliveryActiveJobPage.enterPickupOtp()` and `DeliveryActiveJobPage.swipeToConfirmPickup()`.

| ID | Description | Action | Expected result |
|---|---|---|---|
| PICKUP-06 | Item list visible before pickup | After arriving at restaurant. | Item checklist or order summary visible so rider can verify items. |
| PICKUP-07 | Pickup OTP input appears | After arriving at restaurant. | `DeliveryActiveJobPage.isPickupPhase()` returns true; "Enter 6-digit pickup OTP" input is visible. |
| PICKUP-08 | Enter valid pickup OTP | Enter the 6-digit pickup OTP provided by the restaurant via `enterPickupOtp()`. | OTP field filled; no error message. |
| PICKUP-09 | Swipe to confirm pickup | After entering valid pickup OTP, swipe the slider via `swipeToConfirmPickup()`. | Phase transitions to delivery phase; `isDeliveryPhase()` returns true; drop-off address shown. |
| PICKUP-10 | Enter wrong pickup OTP | Enter "000000" as pickup OTP → try swipe to confirm. | Error message or swipe blocked; pickup not confirmed. |
| PICKUP-11 | Customer address shown after pickup | After confirming pickup via swipe. | Customer delivery address (area/locality) is displayed on the active job card. |

## Batch 3 — Delivery OTP and SwipeAction confirmation

Uses `DeliveryActiveJobPage.enterDeliveryOtp()` and `DeliveryActiveJobPage.swipeToConfirmDelivery()`.

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-01 | Drop-off phase indicators | After pickup confirmed. | Active job shows "Go to Customer" or "Deliver Order" phase with customer area; `isDeliveryPhase()` true. |
| DELIVERY-02 | Delivery OTP input appears | When at customer's location. | "Enter 6-digit delivery OTP" input is visible on the active job screen. |
| DELIVERY-03 | Customer provides delivery OTP | On customer tracker, `getDeliveryOtp()` returns a 6-digit code. | OTP visible on customer side to share with rider. |
| DELIVERY-04 | Enter valid delivery OTP | Rider enters the customer's OTP via `enterDeliveryOtp()`. | OTP field filled; no error. |
| DELIVERY-05 | Swipe to confirm delivery | After entering valid delivery OTP, swipe via `swipeToConfirmDelivery()`. | `hasCompletedDelivery()` returns true; order status "Delivered"; earnings update. |
| DELIVERY-06 | Enter wrong delivery OTP | Enter "000000" → try swipe. | Error; delivery not confirmed. |

## Batch 4 — Rider navigation and communication during active job

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-07 | Open navigation map | During pickup phase, tap navigation button via `openNavigationMap()`. | Map or navigation app/link opens; no crash. |
| DELIVERY-08 | Call customer from active job | During delivery phase, tap call button via `callCustomer()`. | `CallOverlayPage` opens with masked number. |

## Batch 5 — Post-delivery

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-09 | Rider returns to available state | After delivery complete. | Rider dashboard shows no active job; online toggle active; ready for next dispatch. |
| DELIVERY-10 | Completed trip in history | Open History tab after delivering. | Completed trip appears in `DeliveryHistoryPage` with correct date, restaurant name, and earnings. |
| DELIVERY-11 | Earnings update post-delivery | Open Earnings tab after delivering. | Today's earnings figure has increased by the delivery payout amount. |

## Batch 6 — Edge cases

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-12 | Reload during active delivery | While in drop-off phase, reload the page. | Active job state is restored; phase indicator shows correct phase (not reset to pickup). |
| DELIVERY-13 | Customer not found at address | Rider marks "Customer Not Found" if button exists. | System offers re-attempt or auto-cancellation flow; documents this UI state. |
