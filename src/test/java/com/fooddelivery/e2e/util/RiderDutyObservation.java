package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.WebSocket;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Observes only the socket and location frames emitted by the real rider UI. */
final class RiderDutyObservation {
    private static final Set<String> STATUSES = Set.of("OFFLINE", "ONLINE", "ON_DELIVERY");
    private static final Set<String> REASONS = Set.of("CONNECTED", "RIDER_REQUEST", "LOCATION_LOST", "SUSPENDED", "ACCOUNT_DEACTIVATED");
    private WebSocket socket;
    private String status;
    private String reason;
    private boolean connected;
    private long fixNanos;
    private int fixes;
    private double lat;
    private double lng;

    void observe(Page page) {
        // Offline riders have no tracking socket. Observe their real dashboard profile response
        // before deciding whether to click Online; live readiness still requires the socket.
        page.onResponse(response -> {
            if (response.ok() && response.request().method().equals("GET")
                    && "/api/delivery/profile".equals(java.net.URI.create(response.url()).getPath())) {
                try { profile(response.text()); }
                catch (com.microsoft.playwright.PlaywrightException ignored) { /* No profile proof was obtained. */ }
            }
        });
        page.onWebSocket(next -> {
            if (!"/api/delivery/tracking".equals(java.net.URI.create(next.url()).getPath())) return;
            socket = next; status = null; connected = false; fixNanos = 0;
            next.onFrameReceived(frame -> { if (socket == next) received(frame.text()); });
            next.onFrameSent(frame -> { if (socket == next) sent(frame.text(), System.nanoTime()); });
            next.onClose(closed -> { if (socket == next) disconnected(); });
            next.onSocketError(error -> { if (socket == next) disconnected(); });
        });
    }
    void profile(String text) {
        String state = field(text, "status");
        if (status == null && STATUSES.contains(state)) { status = state; reason = "DASHBOARD_PROFILE"; }
    }
    void received(String text) {
        if (!"DUTY_STATUS".equals(field(text, "type"))) return;
        String state = field(text, "status"), cause = field(text, "reason");
        if (!STATUSES.contains(state) || !REASONS.contains(cause)) return;
        status = state; reason = cause; connected = true;
    }
    void sent(String text, long now) {
        if (text == null || field(text, "driverId").isEmpty() || field(text, "timestamp").isEmpty()) return;
        Double latitude = number(text, "lat"), longitude = number(text, "lng");
        if (latitude == null || longitude == null || Math.abs(latitude) > 90 || Math.abs(longitude) > 180) return;
        lat = latitude; lng = longitude; fixNanos = now; fixes++;
    }
    void disconnected() { connected = false; status = null; fixNanos = 0; }
    String status() { return status; }
    boolean ready(long now) { return connected && "ONLINE".equals(status) && fixNanos > 0 && now - fixNanos <= 15_000_000_000L; }
    Map<String, Object> evidence(long now) {
        return Map.of("status", status == null ? "UNOBSERVED" : status, "reason", reason == null ? "UNOBSERVED" : reason,
                "socketConnected", connected, "locationFixCount", fixes, "lat", lat, "lng", lng,
                "locationAgeMs", fixNanos == 0 ? -1L : (now - fixNanos) / 1_000_000, "serverReady", ready(now));
    }
    private static String field(String text, String key) {
        if (text == null) return "";
        var match = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(text);
        return match.find() ? match.group(1) : "";
    }
    private static Double number(String text, String key) {
        var match = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)(?=\\s*[,}])").matcher(text);
        return match.find() ? Double.valueOf(match.group(1)) : null;
    }
}
