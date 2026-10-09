package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

/**
 * Page Object for the Delivery Dashboard shell.
 * Maps to: {@code DeliveryDashboard.tsx}
 * <p>
 * Also covers sub-components: {@code RiderHeader.tsx}, {@code RiderStatsBar.tsx},
 * {@code RiderNotices.tsx}, {@code RiderPrompts.tsx}, {@code DeliveryAvailableJobs.tsx}
 * </p>
 */
public class DeliveryDashboardPage {

    private final Page page;

    public DeliveryDashboardPage(Page page) {
        this.page = page;
    }

    public void waitForDashboard() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                        .setName(Pattern.compile("^(Offline|Online Duty)$")))
                .first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    public void goOnline() {
        new DeliveryOnlineTogglePage(page).goOnline();
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    /** Leaves Settings: the rider header toggles "Profile settings" / "Back to jobs" (RiderHeader); there is no Active tab. */
    public void backToJobs() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Back to jobs").setExact(true)).click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.getByRole(
                com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Profile settings").setExact(true))).isVisible();
    }

    public void openHistoryTab() {
        page.locator("button:has-text('Trips Completed')").first().click();
        page.waitForTimeout(300);
    }

    public void openSettingsTab() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
        page.waitForTimeout(300);
    }


    public void openWalletTab() {
        page.locator("button:has-text('Wallet'), [role='tab']:has-text('Wallet')").first().click();
        page.waitForTimeout(300);
    }
}
