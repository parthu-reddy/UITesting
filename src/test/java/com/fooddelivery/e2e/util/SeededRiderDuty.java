package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.opentest4j.TestAbortedException;

import java.util.regex.Pattern;

/**
 * Keeps a seeded rider ready through the delivery portal that a rider actually sees.
 *
 * <p>This fixture deliberately has no backend shortcut. It treats the rendered duty control and
 * visible delivery warnings as the E2E contract. Service-level duty and telemetry details belong
 * in their embedded-H2 tests.</p>
 */
public final class SeededRiderDuty implements AutoCloseable {

    private static final Pattern CONNECTION_OR_LOCATION_WARNING = Pattern.compile(
            "Connection lost|Waiting for your location|your location stopped reaching us");

    private final Page page;
    private final DeliveryOnlineTogglePage toggle;
    private boolean restoreOffline;
    private boolean preserveOnlineForActiveDelivery;

    private SeededRiderDuty(Page page, String phone) {
        this.page = page;
        this.toggle = new DeliveryOnlineTogglePage(page);

        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).login(phone).openPortal(Portal.DELIVERY);
        new DeliveryDashboardPage(page).waitForDashboard();

        if (hasActiveDeliveryOrDispatch()) {
            throw new TestAbortedException(
                    "Seeded rider already has a visible delivery or dispatch; leaving that session untouched");
        }

        if (toggle.isOffline()) {
            restoreOffline = true;
            toggle.goOnline();
        } else if (!toggle.isOnline()) {
            throw new AssertionError("Rider portal did not render an Online Duty or Offline control after login");
        }

        assertReadyForCheckout();
        System.out.println("[RIDER] Online Duty is visibly enabled in the delivery portal");
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
        org.assertj.core.api.Assertions.assertThat(toggle.isOnline())
                .as("Rider portal must visibly show Online Duty immediately before checkout")
                .isTrue();
        org.assertj.core.api.Assertions.assertThat(hasActiveDeliveryOrDispatch())
                .as("Seeded rider must not already have a visible delivery or dispatch")
                .isFalse();
        org.assertj.core.api.Assertions.assertThat(page.getByText(CONNECTION_OR_LOCATION_WARNING).count())
                .as("Rider portal must not show a connection or location warning")
                .isZero();
    }

    void preserveOnlineForActiveDelivery() {
        preserveOnlineForActiveDelivery = true;
    }

    void markDeliveryCompleted() {
        preserveOnlineForActiveDelivery = false;
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

        toggle.goOffline();
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
        if (!restoreOffline || preserveOnlineForActiveDelivery || hasActiveDeliveryOrDispatch()) return;
        if (!toggle.isOnline()) return;

        toggle.goOffline();
        org.assertj.core.api.Assertions.assertThat(toggle.isOffline())
                .as("Rider portal must visibly return to Offline after restoring this test's setup")
                .isTrue();
        System.out.println("[RIDER] Restored seeded rider to Offline through the delivery portal");
    }
}
