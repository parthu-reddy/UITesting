package com.fooddelivery.e2e.pages.common;

import com.fooddelivery.e2e.base.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** One person login, then an explicit rendered portal choice; no API or browser-state setup. */
public class LoginPage {
    private final Page page;
    private String freshOrganisationName;
    public LoginPage(Page page) { this.page = page; }

    public LoginPage login(String phone) { return login(phone, null, null); }
    /** Profile completion is explicit for a retained new test person. */
    public LoginPage login(String phone, String profileName, String profileEmail) {
        return loginPerson(phone, profileName, profileEmail, false);
    }
    public LoginPage loginNewPerson(String phone, String profileName, String profileEmail) {
        freshOrganisationName = profileName + " Team " + phone;
        return loginPerson(phone, profileName, profileEmail, true);
    }
    private LoginPage loginPerson(String phone, String profileName, String profileEmail, boolean requireFresh) {
        page.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/login");
        fillPhoneNumber(phone); clickSendOtp(); waitForOtpInput(); clickAutofillCode();
        clickVerifyAndLogin(); waitForLoginComplete(profileName, profileEmail, requireFresh); return this;
    }
    public void fillPhoneNumber(String phone) {
        page.getByLabel("PHONE NUMBER", new Page.GetByLabelOptions().setExact(true)).fill(phone);
    }
    public void clickSendOtp() { button("Send One-Time OTP").click(); }
    public void waitForOtpInput() { page.getByLabel("ENTER SECURE CODE").waitFor(); }
    public void fillOtp(String otp) { page.getByLabel("ENTER SECURE CODE").fill(otp); }
    /** Uses the exact Dev Autofill control a person clicks manually. */
    public void clickAutofillCode() {
        page.getByTestId("dev-otp-autofill").click();
        assertThat(page.getByLabel("ENTER SECURE CODE")).hasValue(Pattern.compile("[0-9]{6}"));
    }
    public void clickResendSmsCode() { button("Resend SMS Code").click(); }
    public void clickVerifyAndLogin() { button("Verify & Secure Log In").click(); }

    public LoginPage openPortal(Portal portal) {
        page.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/portals");
        button(portal.label).click();
        page.waitForURL(Pattern.compile(".*/" + portal.path.substring(1) + "(?:[/?#].*)?$"));
        if (portal == Portal.ADMIN) stepUpAdmin();
        return this;
    }
    /** Partner setup uses an authenticated application page, before operational approval. */
    public LoginPage openOnboarding(Portal portal) {
        if (portal == Portal.CUSTOMER) return openPortal(portal);
        page.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/portals");
        String label = portal == Portal.DELIVERY ? "Get started with deliveries" : "Get started with a restaurant";
        button(label).click();
        if (portal == Portal.RESTAURANT) {
            page.waitForURL(Pattern.compile(".*/business(?:[/?#].*)?$"));
            if (freshOrganisationName == null) throw new AssertionError("Choose or create an explicitly owned organisation through BusinessHubPage.");
            new com.fooddelivery.e2e.pages.business.BusinessHubPage(page).create(freshOrganisationName);
        }
        assertThat(page.getByTestId(portal == Portal.DELIVERY ? "delivery-application" : "restaurant-application")).isVisible();
        return this;
    }
    public void stepUpAdmin() {
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Administrator verification").setExact(true));
        Locator heading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Admin").setExact(true));
        page.waitForCondition(() -> dialog.isVisible() || heading.isVisible());
        if (dialog.isVisible()) {
            stepUpCall("/api/v1/auth/admin-session/otp", () -> dialog.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Send administrator code").setExact(true)).click());
            page.getByTestId("dev-admin-otp-autofill").click();
            assertThat(page.getByLabel("Administrator code")).hasValue(Pattern.compile("[0-9]{6}"));
            stepUpCall("/api/v1/auth/admin-session", () -> dialog.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Verify administrator access").setExact(true)).click());
        }
        assertThat(heading).isVisible();
    }
    /**
     * Identity hard-limits admin step-up per phone (AuthService.sendOtp/verifyOtp: 10 sends / 10 min,
     * 5 verifies / 5 min). A 429 fails here at once with the cause, instead of a 60 s locator timeout
     * on every following step (P0-2 batch 1 lost an hour that way). Pace admin classes with
     * e2e-plan/_handoff/tools/run_e2e_batch.py.
     */
    private void stepUpCall(String path, Runnable click) {
        Response response = page.waitForResponse(r -> r.request().method().equals("POST")
                && com.fooddelivery.e2e.util.UrlPaths.path(r.url()).equals(path), click);
        if (response.status() == 429)
            throw new AssertionError("ADMIN_STEP_UP_RATE_LIMITED: " + path + " returned 429. Identity allows 10 code sends per 10 min "
                    + "and 5 verifies per 5 min per admin phone; run admin classes through run_e2e_batch.py, which paces them.");
    }
    private void waitForLoginComplete(String name, String email, boolean requireFresh) {
        page.waitForCondition(() -> !page.url().contains("/login")
                || page.getByPlaceholder("Enter your full name").isVisible()
                || page.getByRole(AriaRole.ALERT).isVisible()
                || page.getByText("Session Limit Reached", new Page.GetByTextOptions().setExact(true)).isVisible(),
                new Page.WaitForConditionOptions().setTimeout(TestConfig.DEFAULT_TIMEOUT));
        if (requireFresh && !page.getByPlaceholder("Enter your full name").isVisible())
            throw new AssertionError("Candidate is not a fresh person; fresh profile completion was not shown. Retain this candidate manifest.");
        if (page.getByPlaceholder("Enter your full name").isVisible()) {
            if (name == null || email == null) throw new AssertionError("Profile completion requires an explicitly allocated person and profile.");
            var profile = new CompleteProfileModalPage(page); profile.fillName(name); profile.fillEmail(email); profile.submit();
        }
        if (page.getByText("Session Limit Reached", new Page.GetByTextOptions().setExact(true)).isVisible())
            throw new AssertionError("Session limit reached; use an owned session fixture and its visible replacement control.");
        if (page.url().contains("/login") && page.getByRole(AriaRole.ALERT).isVisible())
            throw new AssertionError("Login rejected: " + page.getByRole(AriaRole.ALERT).innerText());
        page.waitForCondition(() -> !page.url().contains("/login"), new Page.WaitForConditionOptions().setTimeout(TestConfig.DEFAULT_TIMEOUT));
    }
    private Locator button(String label) { return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(label).setExact(true)); }
    public void clickBackButton() { button("Back").click(); }
    public void clickToggleTheme() { page.locator("button:has(svg.lucide-moon), button:has(svg.lucide-sun)").first().click(); }
}
