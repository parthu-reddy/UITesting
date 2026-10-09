package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * One administrator sign-in per test class.
 *
 * <p>Every admin sign-in costs an OTP step-up, and IdentityService allows 5 verifies per 5 min per admin phone, so a
 * fresh sign-in per method made admin runs ~90% waiting (2026-10-09: 20 methods, 2.6 min of testing in ~25 min).
 * The first {@code loginAsAdmin()} in a class signs in for real and {@link #capture captures} the session; later
 * methods get it {@link #inject injected} into their own fresh context, so routes, listeners and the viewport stay
 * per method. The UI keeps the admin token in sessionStorage (per tab) and the everyday token in localStorage
 * (FoodDeliveryAppUI lib/tokenStore.ts), so both are seeded into each new tab once, before the app runs.
 * The class session is signed out once, by {@link #signOut}, after the class.
 */
public final class ClassAdminSession {

    private record Snapshot(List<Cookie> cookies, Map<String, String> localStorage, String adminToken) { }

    private static final Map<Class<?>, Snapshot> BY_CLASS = new ConcurrentHashMap<>();
    /** Set in a tab once it has been seeded, so a sign-out later in the same tab is not undone by a navigation. */
    static final String SEEDED_MARKER = "e2e_class_admin_session";

    private ClassAdminSession() { }

    public static boolean has(Class<?> testClass) {
        return BY_CLASS.containsKey(testClass);
    }

    /** Called right after a real admin sign-in on {@code page}. */
    @SuppressWarnings("unchecked")
    public static void capture(Class<?> testClass, BrowserContext context, Page page) {
        String adminToken = (String) page.evaluate("() => sessionStorage.getItem('admin_auth_token')");
        Map<String, String> local = (Map<String, String>) page.evaluate(
                "() => Object.fromEntries(Object.entries(localStorage))");
        if (adminToken == null || !local.containsKey("auth_token")) {
            throw new AssertionError("Admin sign-in finished without both session tokens; nothing to reuse");
        }
        BY_CLASS.put(testClass, new Snapshot(context.cookies(), Map.copyOf(local), adminToken));
    }

    /** Seeds the class session into a fresh context; the next navigation to the app is signed in. */
    public static void inject(Class<?> testClass, BrowserContext context) {
        Snapshot s = BY_CLASS.get(testClass);
        if (s == null) throw new IllegalStateException("No admin session captured for " + testClass.getSimpleName());
        if (!s.cookies().isEmpty()) context.addCookies(s.cookies());
        String entries = s.localStorage().entrySet().stream()
                .map(e -> "[" + js(e.getKey()) + "," + js(e.getValue()) + "]")
                .collect(Collectors.joining(","));
        context.addInitScript("""
                (() => {
                  try {
                    if (location.origin !== %s || sessionStorage.getItem(%s)) return;
                    for (const [k, v] of [%s]) localStorage.setItem(k, v);
                    sessionStorage.setItem('admin_auth_token', %s);
                    sessionStorage.setItem(%s, '1');
                  } catch (e) { /* about:blank and other origins have no app storage */ }
                })();
                """.formatted(js(appOrigin()), js(SEEDED_MARKER), entries, js(s.adminToken()), js(SEEDED_MARKER)));
    }

    /** A captured session that no longer works (e.g. revoked) is dropped, so the next sign-in is a real one. */
    public static void forget(Class<?> testClass) {
        BY_CLASS.remove(testClass);
    }

    /** Ends the class session on the server, once, after the class. Prints statuses, never tokens. */
    public static void signOut(Class<?> testClass, Browser browser) {
        if (testClass == null || browser == null || !BY_CLASS.containsKey(testClass)) return;
        BrowserContext context = browser.newContext();
        try {
            inject(testClass, context);
            Page page = context.newPage();
            page.navigate(TestConfig.APP_URL);
            System.out.println("[E2E TEARDOWN] class admin session signed out " + SessionSignOut.signOut(page));
        } catch (RuntimeException e) {
            System.out.println("[E2E TEARDOWN] class admin session sign-out failed: " + e.getMessage());
        } finally {
            BY_CLASS.remove(testClass);
            context.close();
        }
    }

    static String appOrigin() {
        URI app = URI.create(TestConfig.APP_URL);
        return app.getScheme() + "://" + app.getAuthority();
    }

    /** A JavaScript string literal. */
    static String js(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '<' -> out.append("\\u003c");
                default -> {
                    if (c < 0x20 || c == 0x2028 || c == 0x2029) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }
}
