# Checkpoint 142 — time-independent ordering deployed and verified on Dev (safe features)

2026-10-10T03:00+05:30. Nothing running. No orders on Dev (customer_db has 0 orders: Dev was reset with the deploy).

## Deploy verified

restaurant-service 09f905f, customer-service 30bfb63, food-delivery-app-ui 986e01c: DEPLOY_LOG (16:54Z) == pin == repo HEAD
== running image, all healthy. Committed HEADs contain the Phase 1–5 changes.

## Live checks (read-only)

- `validate_time_independence.py` P4.5: 20 Brand 1/2 outlets orderable every minute on Dev; no window ends at 23:59
  (123 all-day outlet rows, 201 all-day category rows).
- Public API at 02:40 IST: Brand 1 Outlet 10 and Brand 2 Outlet 7 open, 50/50 dishes available (7 and 10 under 15 min);
  Brand 7 Outlet 2 and Brand 9 Outlet 7 open with 0 available (their categories are closed at night — realistic seed).

## Safe E2E batch (changed features, impact A/B, slow excluded) — 90 pass, 3 fail

Evidence `evidence/time-independent-2026-10-10/safe`. Failures, none caused by opening hours:
- CustomerOrderHistoryUiTest closedReorder… + missingReorderItems…: no "Reorder" button — Dev has 0 orders after the
  reset. Needs one delivered order (an owner-approved order run) first.
- MenuCartUiTest#outOfStockItemsCannotBeAdded: stale test step. On a fresh Dev it took the "make a fixture" branch,
  switched Brand 1 Item 1-1 at Brand 1 Outlet 5 OUT OF STOCK (by design, never restored), then reloaded and expected
  the feed; the app reopens the same menu (URL route). Screenshot showed the menu with the dish out of stock. Test
  fixed to expect the reopened outlet; rerun 1/1 (via the early branch, since the fixture now exists).

## Dev records created by this batch

Brand 1 Outlet 5: Brand 1 Item 1-1 out of stock (retained fixture, by design). Rider 7000000001 set online by ensureOnline.

## Waiting on the owner

Approval for: the 4 applicant-creating tests (RegistrationUiTest, RestaurantApplicationApiTest — exercises the new
"Open 24 hours" switch —, DeliveryApplicationApiTest, DeliveryOnboardingUiTest) and one order run
(CustomerOrderPlacementTest#tippedOrderMatchesMockPaidTotal: proves the new order helper and gives the reorder tests
their delivered order). Commit UITesting (MenuCartUiTest fix).
