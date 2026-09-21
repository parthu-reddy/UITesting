# Pending work and validation notes

SavedAddressOutletUiTest passed, including after extracting NearbyOutletPage. Existing Home and the nearest displayed Brand1 outlet below 5 km were selected; the selected checkmark was verified.

Initial failure: fresh browser session already displayed Select Delivery Location and blocked the header button. Corrected to use the active CustomerAddressSelectorModal via SavedDeliveryAddressPage, selecting Home without creating an address.

Pending: rider availability and backend distance enforcement are not verified by this selection test. Recheck rider availability before placing an order. Session/outlet runs logged HTTP 400/401/403/404/409 background responses whose endpoints and impact remain unverified.

Evidence: UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.SavedAddressOutletUiTest.xml; initial screenshots under target/screenshots. No orders or addresses created.
