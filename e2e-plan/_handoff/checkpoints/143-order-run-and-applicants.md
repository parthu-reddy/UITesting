# Checkpoint 143 — order lifecycle + applicant tests; one more restaurant fix (deploy pending)

2026-10-10T06:30+05:30. Nothing running. Owner: "you try completing as much as you can".

| Run | Result | Dev records |
|---|---|---|
| HappyDeliveryFlowTest#completeOrderLifecycle (06:03 IST) | 1/1 in ~3 min; helper added a 14-min dish at Brand 1 Outlet 5 | order 5dc4e098 HANDED_OVER (delivered) |
| CustomerOrderHistoryUiTest + CheckoutUiTest | 15 pass, 2 fail | none |
| DeliveryApplicationApiTest (O3, admin 001) | 1/1 | 1 delivery applicant, approved |
| DeliveryOnboardingUiTest (O4/O5, admin 002) | 1/1 | 1 delivery applicant |
| RegistrationUiTest customer, rider | 1/1, 1/1 | 1 customer, 1 rider applicant |
| RestaurantApplicationApiTest (O3) | 0/1 — **real defect, fixed locally** | 1 organisation + draft application (no outlet) |

- Reorder tests (closedReorder…, missingReorderItems…) now PASS with the delivered order.
- Still failing, data only (reset Dev): historyPaginationPreservesRowsWithoutDuplicates (needs >1 history page),
  cancelledHistoryRowOpensExactTerminalTracker (needs a restaurant-cancelled order).
- **Defect:** `ApplicationOutletFactory` refused equal opening/closing times, so "Open 24 hours" on a partner
  application returned 400 at `POST .../application/outlets`. Fixed (equal = all day), `ApplicationOutletFactoryTest`
  red-first, gate P1.5 added (+ proof), Restaurant 189/0. **Needs a restaurant-service redeploy**, then rerun
  `scripts/run_partner_applications_o3_e2e.py --only restaurant` and `scripts/run_registration_e2e.py --only restaurant`.
- Test helper: `addQuickPrepItemToCart` now names the outlet from the menu heading (it printed "Change outlet").
