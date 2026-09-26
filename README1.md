# Triage of the 2026-09-26 E2E report

This file checks another agent's E2E report against the source. It records which of their claims are **not defects** (or were wrong) and which small fixes went in. Larger open work is in
[RandomDocuments/E2EFailureTriage_2026-09-26](../RandomDocuments/E2EFailureTriage_2026-09-26/README.md).

Nothing was committed. No new tests were written.

## Claims that are not defects, or were wrong

| Claim | Verdict | Evidence (read in source) |
| --- | --- | --- |
| `RiderFulfillmentTest` is "incorrectly tagged `ui-only`" | **Wrong as stated.** `ui-only` is defined in `e2e-plan/README.md` ("UI-only execution update") as *all actions go through the deployed UI*. It does not mean *reversible*. `pom.xml` does not filter on the tag (Surefire only has a `-Dgroups` comment), so the tag gates nothing. The real defect was the unseeded random rider phone (fixed below). | `RestaurantFulfillmentTest`, `CustomerOrderPlacementTest` and `DelayApprovalFlowTest` also place real orders under `ui-only`. |
| Delivery availability `409` "prevents checkout" | **Possibly expected.** The 409 is `DeliveryPartnerUnavailableException`, the designed response when MapsIntegration finds no online rider within the fleet radius. `TEST-DATA.md` requires a rider to be online near the outlet before ordering. The report does not show that a rider was online, and the broad run was stopped before `RiderFulfillmentTest` brought one online. It still needs a live check with a rider online. See plan Phase 2, which also covers a real masking bug in the same handler. | `CustomerApplication/.../CustomerRestaurantController.java:122-160` |
| "Two `MenuCartUiTest` classes" is the only duplicate | Also `PaymentModalPage` (in `pages/common` and `pages/customer`). These are page objects, not test classes, so Surefire `-Dtest` selection is not affected. Left as is. | `find src -name '*.java'` basename check |
| `NOT-DEFECTS/README.md` classifications | No disagreement; not re-audited here. | — |

## Fixed now (small)

| Issue | Fix | Verified by |
| --- | --- | --- |
| Customer settings/session tests click a hidden control. `DashboardHeader`'s "Profile Settings" button is `lg:hidden`; tests run at 1280 px, where `CustomerNavRail` shows "Account" instead. | New `CustomerDashboardPage.openProfileSettings(Page)` picks the rail's "Account" at ≥1024 px and the header button below that. All 9 call sites use it: `SessionUiTest`, `CustomerSettingsUiTest`, smoke `MenuCartUiTest`, `CrossRoleSessionIsolationTest`, `SessionManagementTest`, `CustomerOrderHistoryUiTest`, `CustomerAddressTest`, `CustomerRoutingUiTest` (its force-click on the hidden button is gone), and `CustomerDashboardPage.openSettingsTab`. | `mvn -DskipTests test-compile` passes. After the change, a grep for `"Profile Settings"` finds only the helper. **Not run live.** |
| `PartnerOperationsUiTest.verifyRiderEarnings` asserted with no wait | Waits up to 15 s for `Today’s Earnings` (text confirmed in `RiderStatsBar.tsx:43`). | test-compile. **Not run live.** |
| `RiderFulfillmentTest` generated a random unseeded `70xxxxxxxx` rider, which then went through onboarding | Uses the seeded `testRiderPhone` (7000000001–030, `TEST-DATA.md`). | test-compile. |
| Order-placing tests could not be excluded from a "reversible" batch | Added `@Tag("flow")` (as `DelayApprovalFlowTest` already has) to `RiderFulfillmentTest`, `RestaurantFulfillmentTest` and `CustomerOrderPlacementTest`. Exclude them with `-DexcludedGroups=flow`. | test-compile. |
| Two test classes named `MenuCartUiTest` | Renamed `tests/features/customer/MenuCartUiTest` to `MenuCartFeatureTest`. Updated the REGR-02 reference in `e2e-plan/07-.../suite-isolation-and-regression/scenarios.md`. The smoke class keeps its name, which the READMEs and PENDING files refer to. | The duplicate-basename check lists only `PaymentModalPage`. |
| **Product: browser Back from a menu does not return to the restaurant list** | Cause: clicking a card pushes `/customer/restaurant/<card>`, then choosing an outlet pushes `/customer/restaurant/<outlet>` again. Back therefore lands on the first menu. Fix in `FoodDeliveryAppUI/src/features/customer-orders/model/useCustomerRoute.ts`: when a menu is already open, choosing an outlet uses `navigate(..., { replace: true })`. | `npm run typecheck` (tsc --noEmit) and eslint on the file pass. **Not verified on the deployment, which needs a UI redeploy.** |
| **Product: seeded menus show zero dietary markers** | Cause: the customer menu DTO is built at `RestaurantApplication/.../CatalogService.java:197` without `.isVeg(...)`, so `isVeg` is always null. `VegMarker` correctly renders nothing for unknown, and the column defaults to `FALSE`, so data exists. Added `.isVeg(master.getIsVeg())`. | `mvn -o clean test -Dtest='Catalog*Test*'`: CatalogServiceTest 1/1. **Full `build_verify` and contract tests were not run.** Needs a RestaurantApplication redeploy. |

## Out of scope, noticed

- `useCustomerCart.ts` still reads the old `food_delivery_carts` (V1) localStorage key as a migration fallback. That contradicts the no-backward-compatibility rule. Not touched.
- `e2e-plan/02-.../cart-and-pricing/PENDING.md` and `restaurant-discovery-and-menu/PENDING.md` still describe the dietary and Back failures as open. Update them after a live rerun on the redeployed build.
