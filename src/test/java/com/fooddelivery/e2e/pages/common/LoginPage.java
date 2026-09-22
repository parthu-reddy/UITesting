package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Login screen.
 * <p>
 * Covers login for existing accounts with completed profiles.
 * Maps to: {@code LoginScreen.tsx → RoleSelector.tsx → AuthForm.tsx → CompleteProfileModal.tsx}
 * </p>
 * <p>
 * Also covers sub-components: {@code LoginHeader.tsx}, {@code LoginFooter.tsx},
 * {@code RoleCard.tsx}, {@code PersonRow.tsx}, {@code OtpNotification.tsx}
 * </p>
 */
public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    // ── Compound login action ────────────────────────────────────────────

    /**
     * Performs a complete login as the given role.
     *
     * @param roleLabel the button text for the role card (e.g., "Order Food", "Restaurant Partner")
     * @param phone     the test user phone number
     */
    public void loginAs(String roleLabel, String phone) {
        loginAs(roleLabel, phone, null, null);
    }

    /** Explicit opt-in for an account whose first-login profile setup is authorized. */
    public void loginAs(String roleLabel, String phone, String profileName, String profileEmail) {
        selectRole(roleLabel);
        fillPhoneNumber(phone);
        clickSendOtp();
        waitForOtpInput();
        clickAutofillCode();
        clickVerifyAndLogin();
        waitForLoginComplete(profileName, profileEmail);
    }

    // ── Role selection ───────────────────────────────────────────────────

    public void selectRole(String roleLabel) {
        Locator btn = page.locator("button:has-text('" + roleLabel + "'):visible").first();
        btn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        btn.click();
    }

    // ── Phone input ──────────────────────────────────────────────────────

    public void fillPhoneNumber(String phone) {
        Locator input = page.getByPlaceholder("9876543210");
        input.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        input.fill(phone);
    }

    // ── OTP flow ─────────────────────────────────────────────────────────

    public void clickSendOtp() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Send One-Time OTP")).click();
    }

    public void waitForOtpInput() {
        page.getByPlaceholder("- - - - - -").waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void fillOtp(String otp) {
        page.getByPlaceholder("- - - - - -").fill(otp);
    }

    public void clickAutofillCode() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Autofill Code")).click();
    }

    public void clickResendSmsCode() {
        page.locator("button:has-text('Resend SMS Code')").click();
    }

    public void clickVerifyAndLogin() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Verify & Secure Log In")).click();
    }

    // ── Post-login ───────────────────────────────────────────────────────

    private void waitForLoginComplete(String profileName, String profileEmail) {
        try {
            page.waitForCondition(() ->
                !page.url().contains("/login") || page.getByPlaceholder("Enter your full name").isVisible(),
                new Page.WaitForConditionOptions().setTimeout(10000));
        } catch (com.microsoft.playwright.TimeoutError e) {
            // proceed to standard state checks which will throw appropriate errors
        }

        if (page.getByPlaceholder("Enter your full name").isVisible()) {
            if (profileName != null && profileEmail != null) {
                CompleteProfileModalPage profile = new CompleteProfileModalPage(page);
                profile.fillName(profileName);
                profile.fillEmail(profileEmail);
                profile.submit();
            } else {
                throw new AssertionError("Account requires profile completion; no profile setup was configured.");
            }
        }

        page.waitForCondition(() -> {
            if (page.getByText("Session Limit Reached", new Page.GetByTextOptions().setExact(true)).isVisible()) {
                throw new AssertionError("Account session limit reached. Configure a dedicated test account.");
            }
            if (page.getByText("Session Limit Reached", new Page.GetByTextOptions().setExact(true)).isVisible()) {
                throw new AssertionError("Account session limit reached. Configure a dedicated test account.");
            }
            Locator error = page.locator("div:has(> svg.lucide-circle-alert) > span");
            if (error.isVisible()) throw new AssertionError("Login rejected: " + error.innerText());
            return !page.getByPlaceholder("- - - - - -").isVisible() && !page.getByPlaceholder("Enter your full name").isVisible();
        });
    }

    // ── Navigation helpers ───────────────────────────────────────────────

    public void clickBackButton() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back").setExact(true)).click();
    }

    public void clickToggleTheme() {
        page.locator("button:has(svg.lucide-moon), button:has(svg.lucide-sun)").first().click();
    }
}
