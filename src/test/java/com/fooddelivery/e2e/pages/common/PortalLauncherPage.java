package com.fooddelivery.e2e.pages.common;

import com.fooddelivery.e2e.base.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PortalLauncherPage {
    private final Page page;
    public PortalLauncherPage(Page page) { this.page = page; }
    public void open() {
        // Customer entry may open its ordinary location prompt before the launcher is usable.
        // Close that visible dialog rather than clicking through it or injecting an address.
        Locator location = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Select Delivery Location").setExact(true));
        if (java.net.URI.create(page.url()).getPath().startsWith("/customer")) {
            try { location.waitFor(new Locator.WaitForOptions().setTimeout(3000)); }
            catch (com.microsoft.playwright.TimeoutError ignored) { /* A selected address needs no prompt. */ }
        }
        if (location.isVisible()) {
            location.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Close dialog").setExact(true)).click();
            assertThat(location).isHidden();
        }
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Switch portal").setExact(true)).click();
        assertThat(tile(Portal.CUSTOMER)).isVisible();
    }
    private Locator choices() { return page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Switch portal").setExact(true)); }
    public Locator tile(Portal portal) { return choices().getByTestId("portal-" + portal.name().toLowerCase()); }
    public void state(Portal portal, String state) { assertThat(tile(portal)).containsText(state); }
    public void choose(Portal portal) {
        tile(portal).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(portal.label).setExact(true)).click();
        page.waitForURL(Pattern.compile(".*/" + portal.path.substring(1) + "(?:[/?#].*)?$"));
    }
    public void start(Portal portal) {
        String label = portal == Portal.DELIVERY ? "Get started with deliveries" : "Get started with a restaurant";
        tile(portal).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(label).setExact(true)).click();
    }
    public void close() { choices().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Close dialog").setExact(true)).click(); }
    public void all() { choices().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("All portals").setExact(true)).click(); }
    public void selectOrganisation(Portal portal, String name) {
        tile(portal).getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName(portal.label + " organisation").setExact(true)).click();
        choices().getByRole(AriaRole.OPTION, new Locator.GetByRoleOptions().setName(name).setExact(true)).click();
    }
    public static void showAll(Page page) { page.navigate(TestConfig.APP_URL + "/portals"); assertThat(page.getByTestId("portal-choices")).isVisible(); }
}
