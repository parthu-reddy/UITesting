# Pending work and validation notes

SavedAddressOutletUiTest passes four tests, including after extracting NearbyOutletPage. Existing Home and the nearest displayed Brand1 outlet below 5 km are selected; the selected checkmark is verified; the address modal reopens and dismisses twice while preserving Home; Home survives reload; and every displayed Brand1 outlet distance can be parsed as a nonnegative number.

Initial failure: fresh browser session already displayed Select Delivery Location and blocked the header button. Corrected to use the active CustomerAddressSelectorModal via SavedDeliveryAddressPage, selecting Home without creating an address.

The legacy `CustomerAddressTest.addNewAddress` silently created `Test Office` shared data and made no meaningful assertion. It has been replaced with a non-mutating draft-discard test plus strict existing-Home selection. Both rewritten tests live-passed on 2026-09-24; the draft is closed without saving and the Home count remains unchanged.

Pending: rider availability and backend distance enforcement are not verified by this selection test. The test deliberately does not assert that all listed outlets are within 5 km because the UI can list farther locations; it explicitly chooses the nearest displayed Brand1 outlet below 5 km. Recheck rider availability before placing an order. Session/outlet runs logged HTTP 400/401/403/404/409 background responses whose endpoints and impact remain unverified.

## ADDRESS-09 — far outlet remains selectable (2026-09-24)

The strict UI test `OutletSelectionTest.farOutletsCannotBeSelected` found `Brand 1 Outlet 5` at `5.1 km` rendered as an enabled button with no Too far, Unavailable, or Out of range label. The test remains failing as a regression signal because the project rule says an order cannot be placed above 5 km. Five companion tests passed: selector rendering, distance labels, nearby selection/menu load, outlet persistence through cart open/close, and Home persistence after reload.

Evidence: UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.SavedAddressOutletUiTest.xml; initial screenshots under target/screenshots. No orders or addresses created.

## UI fixes applied 2026-09-24 (not yet deployed or re-run)

**Outlet selector** -- both defects fixed in
`features/catalog/components/customer/CustomerOutletSelectorModal.tsx`:

- **ACCESS-05 keyboard navigation.** The options were plain buttons with no arrow handling, so
  Tab reached them but ArrowDown did nothing. The list now handles ArrowDown / ArrowUp / Home /
  End, wrapping at the ends and skipping disabled options.
- **ADDRESS-09 far outlets.** Outlets beyond 5 km rendered as ordinary enabled buttons, so a
  customer could select one and only meet `OUT_OF_SERVICE_AREA` at checkout. They are now
  `disabled`, dimmed, and labelled `Too far to deliver - over 5 km`.

These changes are applied to `FoodDeliveryAppUI` source and pass typecheck, lint and the 339
unit tests. **They are not verified**: nothing here is proven until the UI is deployed and the
owning test is re-run live. Do not mark anything validated on the strength of this note.
