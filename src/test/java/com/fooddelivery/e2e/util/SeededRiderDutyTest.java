package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit checks for the UI-only idle-duty cleanup; no server state is inspected or written directly. */
class SeededRiderDutyTest {

    @Test
    void offlineRiderNeedsNoUiClick() {
        DutyPage fixture = new DutyPage(false, false);

        SeededRiderDuty.finishOfflineIfIdle(fixture.page);

        assertThat(fixture.clicks).isZero();
        assertThat(fixture.online).isFalse();
    }

    @Test
    void onlineIdleRiderUsesTheVisibleDutyControl() {
        DutyPage fixture = new DutyPage(true, false);

        SeededRiderDuty.finishOfflineIfIdle(fixture.page);

        assertThat(fixture.clicks).isEqualTo(1);
        assertThat(fixture.online).isFalse();
    }

    @Test
    void visibleActiveDeliveryIsNotForcedOffline() {
        DutyPage fixture = new DutyPage(true, true);

        SeededRiderDuty.finishOfflineIfIdle(fixture.page);

        assertThat(fixture.clicks).isZero();
        assertThat(fixture.online).isTrue();
    }

    @Test
    void nonDeliveryPageIsIgnored() {
        DutyPage fixture = new DutyPage(true, false);
        fixture.url = "https://example.test/";

        SeededRiderDuty.finishOfflineIfIdle(fixture.page);

        assertThat(fixture.clicks).isZero();
        assertThat(fixture.online).isTrue();
    }

    private static final class DutyPage {
        final Page page;
        boolean online;
        final boolean activeDelivery;
        String url = TestConfig.APP_URL;
        int clicks;

        DutyPage(boolean online, boolean activeDelivery) {
            this.online = online;
            this.activeDelivery = activeDelivery;
            page = proxy(Page.class, (method, args) -> switch (method) {
                case "url" -> url;
                case "isClosed" -> false;
                case "locator" -> dutyLocator((String) args[0]);
                case "getByRole" -> roleLocator(args);
                case "waitForResponse" -> {
                    ((Runnable) args[args.length - 1]).run();
                    yield proxy(com.microsoft.playwright.Response.class, (name, values) -> switch (name) {
                        case "status" -> 200;
                        case "text" -> "{\"success\":true}";
                        default -> throw new AssertionError("Unexpected response method: " + name);
                    });
                }
                default -> throw new AssertionError("Unexpected Page method: " + method);
            });
        }

        private Locator dutyLocator(String selector) {
            if (selector.contains("Online Duty")) return locator(() -> online, () -> {
                clicks++;
                online = false;
            });
            if (selector.contains("Offline")) return locator(() -> !online, () -> { });
            throw new AssertionError("Unexpected selector: " + selector);
        }

        private Locator roleLocator(Object[] args) {
            String role = String.valueOf(args[0]);
            if (role.contains("HEADING")) return locator(() -> activeDelivery, () -> { });
            if (role.contains("ALERT")) return locator(() -> false, () -> { });
            throw new AssertionError("Unexpected role: " + role);
        }

        private Locator locator(java.util.function.BooleanSupplier visible, Runnable click) {
            return proxy(Locator.class, (method, args) -> switch (method) {
                case "isVisible" -> visible.getAsBoolean();
                case "click" -> { click.run(); yield null; }
                case "waitFor", "filter" -> null;
                default -> throw new AssertionError("Unexpected Locator method: " + method);
            });
        }
    }

    private interface Call { Object invoke(String method, Object[] args); }

    private static <T> T proxy(Class<T> type, Call call) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (self, method, args) -> {
            if (method.getName().equals("first") || method.getName().equals("filter")) return self;
            if (method.getName().equals("toString")) return type.getSimpleName() + " fixture";
            return call.invoke(method.getName(), args);
        }));
    }
}
