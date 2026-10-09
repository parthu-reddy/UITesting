package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.ArrayList;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** One person form on both viewports. Portal selection follows successful authentication. */
@Tag("feature-auth")
public class RoleNavigationUiTest extends TestBase {
    @ParameterizedTest(name="{0}px: one accessible person login") @ValueSource(ints={1280,390})
    void onePersonLoginOnBothViewports(int width) {
        customerPage.setViewportSize(width,844); customerPage.navigate(TestConfig.APP_URL);
        assertThat(customerPage.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Send One-Time OTP").setExact(true))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Order Food").setExact(true))).hasCount(0);
        assertThat(customerPage.getByRole(AriaRole.TAB)).hasCount(0);
        new LoginPage(customerPage).fillPhoneNumber("123"); assertThat(customerPage.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).hasValue("123");
    }
    @org.junit.jupiter.api.Test void phoneEditingDoesNotRequestAnOtpOrCreateAPageError() {
        customerPage.setViewportSize(390,844); var requests=new ArrayList<String>(); var errors=new ArrayList<String>();
        customerPage.onPageError(errors::add); customerPage.onRequest(request -> { if(request.url().endsWith("/api/v1/auth/otp")) requests.add(request.method()); });
        customerPage.navigate(TestConfig.APP_URL); LoginPage login=new LoginPage(customerPage);
        for(String value:new String[]{"1","12345","abc1234567890123",""}) login.fillPhoneNumber(value);
        org.assertj.core.api.Assertions.assertThat(requests).isEmpty(); org.assertj.core.api.Assertions.assertThat(errors).isEmpty();
    }
}
