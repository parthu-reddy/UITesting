package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.WebSocket;
import com.microsoft.playwright.WebSocketFrame;
import org.opentest4j.TestAbortedException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads rider duty from the server's DUTY_STATUS WebSocket snapshot and restores only a duty
 * transition this test made itself.
 */
public final class SeededRiderDuty implements AutoCloseable {

    private static final Pattern STATUS = Pattern.compile("\"status\"\\s*:\\s*\"(OFFLINE|ONLINE|ON_DELIVERY)\"");
    private static final Pattern LAT = Pattern.compile("\"lat\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern LNG = Pattern.compile("\"lng\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

    private final Page page;
    private final DeliveryOnlineTogglePage toggle;
    private boolean restoreOffline;
    private final AtomicReference<String> profileStatus = new AtomicReference<>();
    private final AtomicReference<String> latestStatus = new AtomicReference<>();
    private final AtomicReference<WebSocket> riderSocket = new AtomicReference<>();
    private final AtomicInteger statusMessages = new AtomicInteger();
    private final AtomicInteger profileResponses = new AtomicInteger();
    private final AtomicReference<Boolean> locationTelemetrySent = new AtomicReference<>(false);
    private final Consumer<WebSocket> socketListener;
    private final Consumer<Response> profileListener;

    private SeededRiderDuty(Page page, String phone) {
        this.page = page;
        this.toggle = new DeliveryOnlineTogglePage(page);
        this.socketListener = socket -> {
            if (!socket.url().contains("/api/delivery/tracking")) return;
            riderSocket.set(socket);
            socket.onFrameReceived(this::observeDutyStatus);
            socket.onFrameSent(this::observeLocationTelemetry);
        };
        this.profileListener = response -> {
            if (!response.url().contains("/api/delivery/profile")) return;
            try {
                Matcher matcher = STATUS.matcher(response.text());
                if (response.status() == 200 && matcher.find()) {
                    profileStatus.set(matcher.group(1));
                    profileResponses.incrementAndGet();
                }
            } catch (RuntimeException unreadableProfile) {
                System.out.println("[RIDER] Could not read profile response: " + unreadableProfile.getMessage());
            }
        };

        page.onWebSocket(socketListener);
        page.onResponse(profileListener);
        try {
            page.navigate(TestConfig.APP_URL);
            new LoginPage(page).loginAs("Delivery Executive", phone);
            new DeliveryDashboardPage(page).waitForDashboard();

            // The rider app intentionally does not open its tracking WebSocket while OFFLINE.
            // It learns the initial server status from /api/delivery/profile, and only opens the
            // socket after the server has accepted an Online action. Waiting for a socket frame
            // here deadlocks an offline seeded rider before the UI can bring it online.
            require(() -> profileStatus.get() != null,
                    "Rider profile did not expose an authoritative server duty status after login");
            String initialStatus = profileStatus.get();
            if ("ON_DELIVERY".equals(initialStatus)) {
                throw new TestAbortedException("Seeded rider is already carrying an order; leaving that session untouched");
            }
            if (!"ONLINE".equals(initialStatus) && !"OFFLINE".equals(initialStatus)) {
                throw new AssertionError("Seeded rider server status was not ONLINE or OFFLINE: " + initialStatus);
            }

            boolean startedOffline = "OFFLINE".equals(initialStatus);
            this.restoreOffline = startedOffline;
            if (startedOffline) {
                int beforeClick = statusMessages.get();
                toggle.goOnline();
                require(() -> statusMessages.get() > beforeClick && "ONLINE".equals(latestStatus.get()),
                        "Rider did not receive the server ONLINE status after the UI action opened its tracking socket");
            }

            require(() -> Boolean.TRUE.equals(locationTelemetrySent.get())
                            && riderSocket.get() != null
                            && !riderSocket.get().isClosed()
                            && "ONLINE".equals(latestStatus.get()),
                    "Rider has no current location telemetry on a connected socket with server status ONLINE");
            System.out.println("[RIDER] Server DUTY_STATUS=ONLINE with current location telemetry");
        } catch (RuntimeException failure) {
            try {
                close();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
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

    private void observeDutyStatus(WebSocketFrame frame) {
        String text = frame.text();
        if (text == null || !text.contains("\"DUTY_STATUS\"")) return;
        Matcher matcher = STATUS.matcher(text);
        if (matcher.find()) {
            latestStatus.set(matcher.group(1));
            statusMessages.incrementAndGet();
        }
    }

    private void observeLocationTelemetry(WebSocketFrame frame) {
        String text = frame.text();
        if (text == null) return;
        Matcher lat = LAT.matcher(text);
        Matcher lng = LNG.matcher(text);
        if (lat.find() && lng.find()) {
            double latitude = Double.parseDouble(lat.group(1));
            double longitude = Double.parseDouble(lng.group(1));
            if (latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180) {
                locationTelemetrySent.set(true);
            }
        }
    }

    private void require(java.util.function.BooleanSupplier condition, String message) {
        try {
            page.waitForCondition(condition, new Page.WaitForConditionOptions().setTimeout(15000));
        } catch (TimeoutError timeout) {
            throw new AssertionError(message, timeout);
        }
    }

    @Override
    public void close() {
        if (!restoreOffline || !toggle.isOnline()) {
            page.offWebSocket(socketListener);
            page.offResponse(profileListener);
            return;
        }
        try {
            toggle.goOffline();
            int beforeRefresh = profileResponses.get();
            page.reload();
            new DeliveryDashboardPage(page).waitForDashboard();
            require(() -> profileResponses.get() > beforeRefresh && "OFFLINE".equals(profileStatus.get()),
                    "Rider profile did not confirm OFFLINE after restoring this test's temporary duty change");
            System.out.println("[RIDER] Restored seeded rider to OFFLINE");
        } finally {
            page.offWebSocket(socketListener);
            page.offResponse(profileListener);
        }
    }
}
