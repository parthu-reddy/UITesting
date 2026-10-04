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
        // Customer entry may show its ordinary location prompt. Its Close control must
        // remain respected when the initial empty-address response arrives afterward.
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
    /** Producer decisions reach the Identity projection through the two-second outbox job. */
    public void awaitChangedState(Portal portal, String state) {
        long started = System.nanoTime(); int refreshActions = 0;
        while (true) {
            try {
                assertThat(tile(portal)).containsText(state,
                        new com.microsoft.playwright.assertions.LocatorAssertions.ContainsTextOptions().setTimeout(1000));
                break;
            } catch (AssertionError notProjectedYet) {
                if (System.nanoTime() - started >= 10_000_000_000L) throw notProjectedYet;
                choices().getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Refresh portal availability").setExact(true)).click();
                refreshActions++;
            }
        }
        System.out.printf("[PORTALS] %s %s confirmed after %d visible refresh actions in %d ms%n",
                portal, state, refreshActions, (System.nanoTime() - started) / 1_000_000);
    }
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
