package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.microsoft.playwright.Page;
import java.net.URI;
import java.util.*;

/**
 * Ends a page's sessions the way the app's own logout does: one POST /api/v1/auth/logout per stored token
 * (administrator step-up in sessionStorage, everyday sign-in in localStorage). Without it every test leaves a
 * 30-day session behind and seeded people reach the per-person session limit. Never returns or logs a token.
 */
public final class SessionSignOut {
    private SessionSignOut() { }

    /** Returns "scope=status" per token found; empty when the page holds none or is not on the app origin. */
    public static List<String> signOut(Page page) {
        if (page == null || page.isClosed() || !onAppOrigin(page.url())) return List.of();
        var result = (List<?>) page.evaluate("""
            async () => {
              const stored = [['admin', () => sessionStorage.getItem('admin_auth_token')],
                              ['everyday', () => localStorage.getItem('auth_token')]];
              const out = [];
              for (const [scope, read] of stored) {
                let token = null;
                try { token = read(); } catch { continue; }
                if (!token) continue;
                try {
                  const response = await fetch('/api/v1/auth/logout', {method: 'POST', credentials: 'same-origin',
                    redirect: 'error', headers: {Authorization: 'Bearer ' + token}, signal: AbortSignal.timeout(5000)});
                  out.push(scope + '=' + response.status);
                } catch (e) { out.push(scope + '=failed'); }
              }
              return out;
            }
            """);
        return result.stream().map(String::valueOf).toList();
    }

    /** Tokens go only to the app's own gateway, never to a page left on another origin (e.g. a payment page). */
    private static boolean onAppOrigin(String url) {
        try {
            URI page = URI.create(url), app = URI.create(TestConfig.APP_URL);
            return Objects.equals(page.getScheme(), app.getScheme()) && Objects.equals(page.getAuthority(), app.getAuthority());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
