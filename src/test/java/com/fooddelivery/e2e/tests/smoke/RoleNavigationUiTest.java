package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("login-ui")
public class RoleNavigationUiTest extends TestBase {
    @ParameterizedTest(name = "{0}px: select {1}, return and select another role")
    @CsvSource({
        "1280,Order Food,Customer", "1280,Restaurant Partner,Restaurant Partner",
        "1280,Delivery Executive,Delivery Rider", "1280,System Admin,System Admin",
        "390,Order Food,Customer", "390,Restaurant Partner,Restaurant Partner",
        "390,Delivery Executive,Delivery Rider", "390,System Admin,System Admin"
    })
    void roleSelectionAndBack(int width, String role, String label) {
        customerPage.setViewportSize(width, 844);
        customerPage.navigate(TestConfig.APP_URL);
        if (width < 1024) {
            customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(role).setExact(true)).click();
            assertThat(customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(role).setExact(true)))
                    .hasAttribute("aria-selected", "true");
        }
        LoginPage login = new LoginPage(customerPage);
        login.selectRole(role);
        assertThat(customerPage.getByText(label, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        assertThat(customerPage.getByPlaceholder("9876543210")).isVisible();
        login.fillPhoneNumber("123");
        login.clickBackButton();
        if (width < 1024) customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Order Food").setExact(true)).click();
        login.selectRole("Order Food");
        assertThat(customerPage.getByPlaceholder("9876543210")).hasValue("");
        assertThat(customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send One-Time OTP"))).isVisible();
    }
}
