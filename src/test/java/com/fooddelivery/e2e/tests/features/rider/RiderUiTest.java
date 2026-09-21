package com.fooddelivery.e2e.tests.features.rider;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class RiderUiTest extends TestBase {

    @Test
    @DisplayName("RIDER-01: Verify Rider Dashboard and Go Online functionality")
    void verifyRiderDashboardAndOnlineStatus() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", TestConfig.RIDER_PHONE);
        
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();

        DeliveryOnlineTogglePage toggle = new DeliveryOnlineTogglePage(riderPage);
        // Sometimes it starts as online from a previous test run
        if (!toggle.isOnline()) {
            toggle.goOnline();
        }
        
        assertThat(toggle.isOnline()).isTrue();
    }
}
