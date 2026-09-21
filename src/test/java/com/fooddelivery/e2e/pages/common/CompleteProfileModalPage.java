package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Real profile-completion form; failed saves must never be treated as successful login. */
public class CompleteProfileModalPage {
    private final Page page;
    public CompleteProfileModalPage(Page page) { this.page = page; }

    public boolean isProfilePromptVisible() {
        return page.getByPlaceholder("Enter your full name").isVisible();
    }
    public void fillName(String name) { page.getByPlaceholder("Enter your full name").fill(name); }
    public void fillEmail(String email) { page.getByPlaceholder("Enter your email address").fill(email); }

    public void submit() {
        Response response = page.waitForResponse(
                r -> r.url().contains("/api/v1/users/profile") && r.request().method().equals("PUT"),
                () -> page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Save Profile & Continue").setExact(true)).click());
        if (!response.ok()) {
            throw new AssertionError("Profile save failed: HTTP " + response.status()
                    + ". Check profile API permissions and account setup.");
        }
        assertThat(page.getByPlaceholder("Enter your full name")).isHidden();
    }
}
