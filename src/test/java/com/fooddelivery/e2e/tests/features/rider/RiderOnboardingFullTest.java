package com.fooddelivery.e2e.tests.features.rider;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Approved seed stays operational with reviewed settings visibly locked in the rider portal. */
@Tag("rider-onboarding")
@Tag("ui-only")
public class RiderOnboardingFullTest extends TestBase {

    @Test
    @DisplayName("Approved rider has the duty dashboard and locked reviewed settings")
    void approvedRiderHasOperationalDashboardAndFrozenDetails() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);

        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();
        assertThat(riderPage.getByTestId("delivery-application")).isHidden();

        dashboard.openSettingsTab();
        assertThat(riderPage.getByText(
                "Reviewed delivery details are locked. Contact support if they need to change.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.locator("fieldset")).isDisabled();
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save Profile Changes").setExact(true))).isDisabled();
    }
}
