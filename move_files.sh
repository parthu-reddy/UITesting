#!/bin/bash
set -e
cd "/Users/parthureddy/Documents/Food Delivery.nosync/UITesting/src/test/java/com/fooddelivery/e2e/tests/features"

mkdir -p admin auth customer exceptions fulfillment resilience restaurant rider

declare -A FILES
FILES=(
  ["AdminCampaignTest.java"]="admin"
  ["AdminLedgerAdvancedTest.java"]="admin"
  ["AdminLiveOpsFleetTest.java"]="admin"
  ["AdminSupportRefundQueueTest.java"]="admin"
  ["AdminSupportUserReviewTest.java"]="admin"
  ["AdminUiTest.java"]="admin"
  ["AdminUserOpsTest.java"]="admin"
  ["SessionManagementTest.java"]="auth"
  ["ProfileSettingsTest.java"]="auth"
  ["SettingsTest.java"]="auth"
  ["CheckoutUiTest.java"]="customer"
  ["CustomerAddressTest.java"]="customer"
  ["CustomerCartTest.java"]="customer"
  ["CustomerHomeAddressTest.java"]="customer"
  ["MenuCartUiTest.java"]="customer"
  ["OutletSelectionTest.java"]="customer"
  ["ChatCommunicationTest.java"]="exceptions"
  ["ChatRefundMapTest.java"]="exceptions"
  ["ExceptionsSupportUiTest.java"]="exceptions"
  ["OrderCancellationTest.java"]="exceptions"
  ["RefundRequestTest.java"]="exceptions"
  ["OrderFulfillmentUiTest.java"]="fulfillment"
  ["PickupDeliveryOtpTest.java"]="fulfillment"
  ["ResilienceRegressionUiTest.java"]="resilience"
  ["ResponsiveAccessibilityTest.java"]="resilience"
  ["PartnerOperationsUiTest.java"]="restaurant"
  ["RestaurantMenuManagementTest.java"]="restaurant"
  ["RestaurantUiTest.java"]="restaurant"
  ["RiderOnboardingFullTest.java"]="rider"
  ["RiderOnboardingTest.java"]="rider"
  ["RiderUiTest.java"]="rider"
)

for file in "${!FILES[@]}"; do
  dir="${FILES[$file]}"
  if [ -f "$file" ]; then
    mv "$file" "$dir/"
    perl -pi -e "s/package com\.fooddelivery\.e2e\.tests\.features;/package com.fooddelivery.e2e.tests.features.${dir};/g" "$dir/$file"
    echo "Moved $file to $dir and updated package."
  fi
done
