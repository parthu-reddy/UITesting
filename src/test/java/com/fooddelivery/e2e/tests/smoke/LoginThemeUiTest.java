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
        var phone = customerPage.getByPlaceholder("9876543210");
        assertThat(phone).isVisible();
        phone.fill("8000000001");
        double contrast = ((Number) phone.evaluate("""
                input => {
                  const rgb = value => (value.match(/[0-9.]+/g) || []).slice(0, 3).map(Number);
                  const lum = color => {
                    const c = rgb(color).map(v => v / 255).map(v => v <= .03928 ? v / 12.92 : Math.pow((v + .055) / 1.055, 2.4));
                    return .2126 * c[0] + .7152 * c[1] + .0722 * c[2];
                  };
                  let node = input;
                  let background = 'rgba(0, 0, 0, 0)';
                  while (node && /rgba?\\(0, 0, 0(?:, 0)?\\)/.test(background)) {
                    background = getComputedStyle(node).backgroundColor;
                    node = node.parentElement;
                  }
                  const foregroundLum = lum(getComputedStyle(input).color);
                  const backgroundLum = lum(background);
                  return (Math.max(foregroundLum, backgroundLum) + .05) /
                         (Math.min(foregroundLum, backgroundLum) + .05);
                }
                """)).doubleValue();
        org.assertj.core.api.Assertions.assertThat(contrast)
                .as("dark-mode phone input text contrast")
                .isGreaterThanOrEqualTo(4.5);
        customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch to light mode").setExact(true)).click();
        assertThat(customerPage.locator(".app-background")).not().hasClass(Pattern.compile(".*\\bdark\\b.*"));
        login.clickBackButton();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch to dark mode").setExact(true))).isVisible();
    }
}
