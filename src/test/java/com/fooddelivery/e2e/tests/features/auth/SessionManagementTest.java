package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.SessionManagementPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Non-destructive checks for the inline Logged-in Devices settings section. */
@Tag("session-management")
@Tag("ui-only")
public class SessionManagementTest extends TestBase {

    private SessionManagementPage sessions;

    @BeforeEach
    void openCustomerSettings() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        sessions = new SessionManagementPage(customerPage);
        assertThat(sessions.section()).isVisible();
    }

    @Test
    @DisplayName("SESSION-MGMT-01/02: Logged-in Devices renders current session")
    void currentSessionIsVisible() {
        assertThat(sessions.hasDefinedState()).isTrue();
        assertThat(sessions.getSessionCount())
                .as("The browser that loaded settings must appear as an active session")
                .isGreaterThanOrEqualTo(1);

        Locator current = sessions.sessionRows().first();
        assertThat(current).containsText("Last Active:");
        assertThat(current.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true))).isVisible();
        assertThat(current.innerText()).doesNotContain("undefined", "null");
    }

    @Test
    @DisplayName("SESSION-MGMT-05: Session list exposes only explicit per-device removal")
    void sessionActionsMatchCurrentUiContract() {
        assertThat(sessions.section().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true)).first()).isVisible();
        assertThat(sessions.section().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Terminate All").setExact(true))).hasCount(0);
        assertThat(sessions.section().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("End All Sessions").setExact(true))).hasCount(0);
    }
}
