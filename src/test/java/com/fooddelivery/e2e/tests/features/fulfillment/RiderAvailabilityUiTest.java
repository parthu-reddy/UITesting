package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class RiderAvailabilityUiTest extends TestBase {

    @Test
    @DisplayName("DISPATCH-01-04/16/18: Rider duty persists and dashboard history renders")
    void riderDutyAndDashboardState() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive",
                testRiderPhone);

        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();
        DeliveryOnlineTogglePage duty = new DeliveryOnlineTogglePage(riderPage);

        assertThat(duty.isOnline() || duty.isOffline())
                .as("Fresh login exposes one explicit duty state").isTrue();
        assertThat(riderPage.getByText("Today’s Earnings",
                new Page.GetByTextOptions().setExact(true)).isVisible()).isTrue();

        // Always re-register this browser's geolocation, including when the seeded rider was
        // already rendered Online from an earlier session.
        duty.goOnline();
        assertThat(duty.isOnline()).isTrue();
        riderPage.reload();
        dashboard.waitForDashboard();
        assertThat(duty.isOnline()).as("Online duty survives reload").isTrue();

        duty.goOffline();
        assertThat(duty.isOffline()).isTrue();
        riderPage.reload();
        dashboard.waitForDashboard();
        assertThat(duty.isOffline()).as("Offline duty survives reload").isTrue();

        duty.goOnline();
        assertThat(duty.isOnline()).as("Test restores the shared rider to Online").isTrue();

        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        assertThat(riderPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Completed Deliveries")).isVisible()).isTrue();
        assertThat(riderPage.getByText("Delivered", new Page.GetByTextOptions().setExact(true)).first().isVisible()
                || riderPage.getByText("No completed deliveries found.",
                new Page.GetByTextOptions().setExact(true)).isVisible()).isTrue();
    }
}
