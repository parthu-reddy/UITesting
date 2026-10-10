package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.pages.common.Portal;
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
@Tag("feature-rider-delivery")
public class RiderAvailabilityUiTest extends TestBase {

    @Test
    @DisplayName("DISPATCH-01-04/16/18: Rider duty persists and dashboard history renders")
    void riderDutyAndDashboardState() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);

        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();
        DeliveryOnlineTogglePage duty = new DeliveryOnlineTogglePage(riderPage);

        assertThat(duty.isOnline() || duty.isOffline())
                .as("Fresh login exposes one explicit duty state").isTrue();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                riderPage.getByText(Pattern.compile("^(Today’s earnings|Paid today)$"))).isVisible();

        boolean initiallyOnline = duty.isOnline();
        // Online setup is idempotent; do not cycle a rider already on duty.
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
        assertThat(duty.isOnline()).as("Rider can return Online after an explicit offline action").isTrue();

        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        // The history panel opens after the click and loads its trips; wait for it rather than reading once.
        riderPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Completed Deliveries"))
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(15000));
        riderPage.getByText("Delivered", new Page.GetByTextOptions().setExact(true)).first()
                .or(riderPage.getByText("No completed deliveries found.", new Page.GetByTextOptions().setExact(true)))
                .first()
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(15000));
        if (!initiallyOnline) duty.goOffline();
    }
}
