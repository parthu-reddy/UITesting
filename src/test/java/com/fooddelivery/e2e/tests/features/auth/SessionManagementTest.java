package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.util.ArrayList;
import java.util.List;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** UI-only owned device replacement/removal; three sessions per person across all purposes. */
@Tag("session-management") @Tag("ui-only")
public class SessionManagementTest extends TestBase {
    private SessionManagementPage sessions;
    private final List<BrowserContext> devices=new ArrayList<>();
    private Page secondPage,thirdPage,fourthPage;
    @AfterEach void closeBrowserResources() { devices.forEach(BrowserContext::close); devices.clear(); }
    @BeforeEach void openCustomerSettings(TestInfo info) {
        if(info.getTestMethod().orElseThrow().isAnnotationPresent(EnabledIfSystemProperty.class)) {
            testCustomerPhone=System.getProperty("registration.customer.phone");
            org.assertj.core.api.Assertions.assertThat(System.getProperty("customer.phone")).isEqualTo(testCustomerPhone);
            com.fooddelivery.e2e.util.FreshCustomerFixture.registerWithAddress(customerPage,testCustomerPhone,"Home");
        } else {
            new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        }
        CustomerDashboardPage.openProfileSettings(customerPage); sessions=new SessionManagementPage(customerPage); sessions.waitForSessions();
    }
    @Test void currentSessionIsVisible() {
        assertThat(sessions.section()).isVisible(); assertThat(sessions.section()).containsText("This device");
        org.assertj.core.api.Assertions.assertThat(sessions.getSessionCount()).isBetween(1,3);
    }
    @Test void sessionActionsMatchCurrentUiContract() {
        assertThat(sessions.section().getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Remove").setExact(true))).hasCount(sessions.getSessionCount());
        assertThat(sessions.section().getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Refresh devices").setExact(true))).isVisible();
    }
    @Test @EnabledIfSystemProperty(named="session.fixture.case",matches="device-removal")
    void removeOwnSecondDeviceRevokesOnlyThatSession() {
        secondPage=loginDevice("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0");
        customerPage.reload(); sessions.waitForSessions(); assertThat(sessions.sessionRows()).hasCount(2);
        Locator row=sessions.sessionRows().filter(new Locator.FilterOptions().setHasText("Windows · Firefox")); assertThat(row).hasCount(1);
        var response=customerPage.waitForResponse(r -> r.request().method().equals("DELETE")&&r.url().contains("/api/v1/auth/sessions/"),() -> {
            row.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Remove").setExact(true)).click();
            customerPage.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Remove device").setExact(true)).click();
        });
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200); assertThat(sessions.sessionRows()).hasCount(1);
        secondPage.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/portals"); assertThat(secondPage.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).isVisible();
        assertThat(sessions.section()).containsText("This device");
    }
    @Test @EnabledIfSystemProperty(named="session.fixture.case",matches="limit-cancel")
    void sessionLimitCancelPreservesExistingDevices() {
        Locator modal=reachSessionLimit(); modal.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Close dialog").setExact(true)).click();
        assertThat(fourthPage.getByLabel("ENTER SECURE CODE")).isVisible();
        for(Page page:List.of(customerPage,secondPage,thirdPage)) {
            page.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/portals"); assertThat(page.getByTestId("portal-customer")).isVisible();
        }
    }
    @Test @EnabledIfSystemProperty(named="session.fixture.case",matches="limit-replacement")
    void sessionLimitReplacementRevokesSelectedDeviceOnly() {
        Locator modal=reachSessionLimit();
        var response=fourthPage.waitForResponse(r -> r.url().endsWith("/api/v1/auth/session")&&r.request().method().equals("POST"),
            () -> modal.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Replace Windows Firefox session").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200);
        fourthPage.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/portals"); assertThat(fourthPage.getByTestId("portal-customer")).isVisible();
        secondPage.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/portals"); assertThat(secondPage.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).isVisible();
        for(Page page:List.of(customerPage,thirdPage)) {
            page.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/portals"); assertThat(page.getByTestId("portal-customer")).isVisible();
        }
    }
    private Locator reachSessionLimit() {
        assertThat(sessions.sessionRows()).hasCount(1);
        secondPage=loginDevice("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0");
        thirdPage=loginDevice("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/605.1.15 Version/17.0 Safari/605.1.15");
        fourthPage=newDevice("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7; rv:121.0) Gecko/20100101 Firefox/121.0");
        fourthPage.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/login"); LoginPage login=new LoginPage(fourthPage);
        login.fillPhoneNumber(testCustomerPhone); login.clickSendOtp(); login.waitForOtpInput(); login.clickAutofillCode();
        var response=fourthPage.waitForResponse(r -> r.url().endsWith("/api/v1/auth/session")&&r.request().method().equals("POST"),login::clickVerifyAndLogin);
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(409);
        Locator modal=fourthPage.getByRole(AriaRole.DIALOG,new Page.GetByRoleOptions().setName("Active sessions").setExact(true)); assertThat(modal).isVisible();
        assertThat(modal).containsText("3 active sessions"); return modal;
    }
    private Page loginDevice(String userAgent) {
        Page page=newDevice(userAgent); new LoginPage(page).login(testCustomerPhone).openPortal(Portal.CUSTOMER); return page;
    }
    private Page newDevice(String userAgent) {
        BrowserContext context=browser.newContext(new Browser.NewContextOptions().setViewportSize(1280,900).setUserAgent(userAgent)
            .setPermissions(List.of("geolocation","notifications")).setGeolocation(TestConfig.GEO_LAT,TestConfig.GEO_LNG));
        devices.add(context); Page page=context.newPage(); page.setDefaultTimeout(TestConfig.DEFAULT_TIMEOUT); return page;
    }
}
