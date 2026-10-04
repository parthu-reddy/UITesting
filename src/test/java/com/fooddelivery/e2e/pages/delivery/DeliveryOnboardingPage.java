package com.fooddelivery.e2e.pages.delivery;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
public final class DeliveryOnboardingPage {
    private final Page page;
    public DeliveryOnboardingPage(Page page) { this.page = page; }
    public void submit(String name, String phone) { assertThat(page.getByTestId("delivery-application")).isVisible(); new RiderOnboardingWizardPage(page).completeDevModeOnboarding(name, "KA" + phone); }
    public void refresh(String status) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Refresh status").setExact(true)).click();
        assertThat(page.getByText(status, new Page.GetByTextOptions().setExact(true))).isVisible();
    }
}
