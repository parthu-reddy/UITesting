package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Predicate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Offline teardown uses authoritative state; no live browser/server or time waits. */
class SeededRiderDutyTest {
    @Test void offlineRiderNeedsNoStatusWrite() {
        DutyPage fixture = new DutyPage(200, "OFFLINE");
        SeededRiderDuty.finishOfflineIfIdle(fixture.page);
        assertThat(fixture.clicks).isZero();assertThat(fixture.requests).isZero();
    }
    @Test void onlineIdleRiderGoesOfflineAndServerStateIsChecked() {
        DutyPage fixture = new DutyPage(200, "ONLINE", "OFFLINE");
        SeededRiderDuty.finishOfflineIfIdle(fixture.page);
        assertThat(fixture.clicks).isEqualTo(1);assertThat(fixture.requests).isEqualTo(1);
        assertThat(fixture.states).isEmpty();
    }
    @Test void activeDeliveryIsNotForcedOffline() {
        DutyPage fixture = new DutyPage(200, "ON_DELIVERY");
        SeededRiderDuty.finishOfflineIfIdle(fixture.page);
        assertThat(fixture.clicks).isZero();assertThat(fixture.requests).isZero();
    }
    @Test void rejectedOfflineRequestIsReported() {
        DutyPage fixture = new DutyPage(400, "ONLINE");
        assertThatThrownBy(() -> SeededRiderDuty.finishOfflineIfIdle(fixture.page)).isInstanceOf(AssertionError.class);
        assertThat(fixture.requests).isEqualTo(1);
    }
    @Test void optimisticButtonCannotReplaceServerOfflineConfirmation() {
        DutyPage fixture = new DutyPage(200, "ONLINE", "ONLINE");
        assertThatThrownBy(() -> SeededRiderDuty.finishOfflineIfIdle(fixture.page)).isInstanceOf(AssertionError.class);
    }

    private static class DutyPage {
        final Deque<String> states = new ArrayDeque<>();
        final Page page;
        int clicks, requests;
        @SuppressWarnings("unchecked")
        DutyPage(int status, String... initialStates) {
            states.addAll(Arrays.asList(initialStates));
            Request request = proxy(Request.class, (method, args) -> {
                if (method.equals("method")) return "POST";
                throw new AssertionError("Unexpected request method: " + method);
            });
            Response response = proxy(Response.class, (method, args) -> switch (method) {
                case "url" -> TestConfig.APP_URL.replaceAll("/$", "") + "/api/delivery/status";
                case "request" -> request;
                case "status" -> status;
                default -> throw new AssertionError("Unexpected response method: " + method);
            });
            Locator locator = proxy(Locator.class, (method, args) -> switch (method) {
                case "first" -> null; // The locator wrapper below resolves first() to itself.
                case "isVisible" -> true;
                case "click" -> {clicks++;yield null;}
                case "waitFor" -> null;
                default -> throw new AssertionError("Unexpected locator method: " + method);
            });
            page = proxy(Page.class, (method, args) -> switch (method) {
                case "url" -> TestConfig.APP_URL;
                case "isClosed" -> false;
                case "evaluate" -> states.removeFirst();
                case "locator" -> locator;
                case "waitForResponse" -> {
                    requests++;
                    assertThat(((Predicate<Response>) args[0]).test(response)).isTrue();
                    ((Runnable) args[1]).run();yield response;
                }
                default -> throw new AssertionError("Unexpected page method: " + method);
            });
        }
    }
    private interface Call { Object invoke(String method, Object[] args); }
    private static <T> T proxy(Class<T> type, Call call) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (self, method, args) -> {
            if (method.getName().equals("first")) return self;
            if (method.getName().equals("toString")) return type.getSimpleName() + " fixture";
            return call.invoke(method.getName(), args);
        }));
    }
}
