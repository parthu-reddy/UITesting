package com.fooddelivery.e2e.tests.smoke;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
@Tag("appearance-ui")
public class LoginThemeUiTest extends TestBase {
    @ParameterizedTest(name="{0}px: theme switch works through role-form navigation")
    @ValueSource(ints={390,1280})
    void themeSwitchAndRoleNavigation(int width) {
        customerPage.setViewportSize(width,844);
        customerPage.navigate(TestConfig.APP_URL);
        customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch to dark mode").setExact(true)).click();
        assertThat(customerPage.locator(".app-background")).hasClass(Pattern.compile(".*\\bdark\\b.*"));
        if (width < 1024) customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Order Food").setExact(true)).click();
        LoginPage login=new LoginPage(customerPage); login.selectRole("Order Food");
        assertThat(customerPage.getByPlaceholder("9876543210")).isVisible();
        customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch to light mode").setExact(true)).click();
        assertThat(customerPage.locator(".app-background")).not().hasClass(Pattern.compile(".*\\bdark\\b.*"));
        login.clickBackButton();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch to dark mode").setExact(true))).isVisible();
    }
}
