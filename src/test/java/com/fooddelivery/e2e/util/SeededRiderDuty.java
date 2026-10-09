package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

/**
 * Keeps a seeded rider ready through the delivery portal that a rider actually sees.
 *
 * <p>Uses visible controls and passive observations of the actual UI's DUTY_STATUS and location
 * frames. No direct backend requests, database/Redis reads or browser-state injection.</p>
 */
public final class SeededRiderDuty implements AutoCloseable {

    private static final Pattern CONNECTION_OR_LOCATION_WARNING = Pattern.compile(
            "Connection lost|Connecting to dispatch|Waiting for your location|your location stopped reaching us");

    private final Page page;
    private final DeliveryOnlineTogglePage toggle;
    private final RiderDutyObservation observation = new RiderDutyObservation();
    private boolean restoreOffline;

    private SeededRiderDuty(Page page, String phone) {
        this.page = page;
        this.toggle = new DeliveryOnlineTogglePage(page);
        observation.observe(page);

        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).login(phone).openPortal(Portal.DELIVERY);
        new DeliveryDashboardPage(page).waitForDashboard();

        page.waitForCondition(() -> observation.status() != null,
                new Page.WaitForConditionOptions().setTimeout(20000));
        if ("ON_DELIVERY".equals(observation.status()) || hasActiveDeliveryOrDispatch()) {
            throw new AssertionError(
                    "Seeded rider already has a visible delivery or dispatch; leaving that session untouched");
        }

        if ("OFFLINE".equals(observation.status()) && toggle.isOffline()) {
            restoreOffline = true;
            toggle.goOnline();
        } else if (!toggle.isOnline()) {
            throw new AssertionError("Rider portal did not render an Online Duty or Offline control after login");
        }

        assertReadyForCheckout();
        System.out.println("[RIDER] Server DUTY_STATUS ONLINE and live UI location frames verified");
    }

    public static SeededRiderDuty ensureOnline(Page page, String phone) {
        SeededRiderDuty duty = null;
        try {
            duty = new SeededRiderDuty(page, phone);
            return duty;
        } catch (RuntimeException failure) {
            if (duty != null) duty.close();
            throw failure;
        }
    }

    /** Recheck the visible rider portal immediately before the customer starts checkout. */
    public void assertReadyForCheckout() {
        page.waitForCondition(() -> observation.ready(System.nanoTime()),
                new Page.WaitForConditionOptions().setTimeout(20000));
        org.assertj.core.api.Assertions.assertThat(toggle.isOnline())
                .as("Rider portal must visibly show Online Duty immediately before checkout")
                .isTrue();
        org.assertj.core.api.Assertions.assertThat(hasActiveDeliveryOrDispatch())
                .as("Seeded rider must not already have a visible delivery or dispatch")
                .isFalse();
        org.assertj.core.api.Assertions.assertThat(page.getByText(CONNECTION_OR_LOCATION_WARNING).count())
                .as("Rider portal must not show a connection or location warning")
                .isZero();
        try {
            var folder = java.nio.file.Path.of("target/business-platform/o45");
            java.nio.file.Files.createDirectories(folder);
            java.nio.file.Files.writeString(folder.resolve("rider-preflight-" + java.util.UUID.randomUUID() + ".json"),
                    (String) page.evaluate("data => JSON.stringify(data,null,2)", observation.evidence(System.nanoTime())));
        } catch (java.io.IOException failure) { throw new AssertionError("Cannot retain rider UI preflight evidence", failure); }
    }

    /**
     * Rider duty is the sole automatic UI reset requested for this E2E harness. It only clicks
     * the rendered duty control; a page outside the delivery portal or an active job is left alone.
     */
    public static void finishOfflineIfIdle(Page page) {
        if (page == null || page.isClosed() || !isApplicationPage(page)) return;

        DeliveryOnlineTogglePage toggle = new DeliveryOnlineTogglePage(page);
        if (hasActiveDeliveryOrDispatch(page)) {
            System.out.println("[RIDER] Leaving visible active delivery or dispatch unchanged");
            return;
        }
        if (toggle.isOffline()) return;
        if (!toggle.isOnline()) return;

        goOfflineWithServerConfirmation(page, toggle);
        org.assertj.core.api.Assertions.assertThat(toggle.isOffline())
                .as("Rider portal must visibly return to Offline after teardown")
                .isTrue();
    }

    private static boolean isApplicationPage(Page page) {
        String root = TestConfig.APP_URL.replaceAll("/+$", "");
        return page.url().startsWith(root);
    }

    private boolean hasActiveDeliveryOrDispatch() {
        return hasActiveDeliveryOrDispatch(page);
    }

    private static boolean hasActiveDeliveryOrDispatch(Page page) {
        boolean activeContract = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Active Contract").setExact(true)).isVisible();
        boolean dispatchOffer = page.getByRole(AriaRole.ALERT)
                .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("New Dispatch"))
                .isVisible();
        return activeContract || dispatchOffer;
    }

    @Override
    public void close() {
        if (!restoreOffline || "ON_DELIVERY".equals(observation.status()) || hasActiveDeliveryOrDispatch()) return;
        if (!toggle.isOnline()) return;

        goOfflineWithServerConfirmation(page, toggle);
        org.assertj.core.api.Assertions.assertThat(toggle.isOffline())
                .as("Rider portal must visibly return to Offline after restoring this test's setup")
                .isTrue();
        System.out.println("[RIDER] Restored seeded rider to Offline through the delivery portal");
    }
    private static void goOfflineWithServerConfirmation(Page page, DeliveryOnlineTogglePage toggle) {
        var response = page.waitForResponse(result -> result.request().method().equals("POST")
                && "/api/delivery/status".equals(com.fooddelivery.e2e.util.UrlPaths.path(result.url())), toggle::goOffline);
        org.assertj.core.api.Assertions.assertThat(response.status()).as("Visible offline action commits on the server").isEqualTo(200);
        org.assertj.core.api.Assertions.assertThat(response.text()).matches("(?s).*\"success\"\\s*:\\s*true.*");
    }
}
