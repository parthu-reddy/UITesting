package com.fooddelivery.e2e.util;

import java.net.URI;

/**
 * Requests a browser-routed test must let through to Dev even when it fixtures "every /api/v1/ call".
 * They are the signed-in shell, not page data:
 *  - /api/v1/auth/**            sign-in, step-up, sessions and TEARDOWN logout (a fixture 405 here left
 *                               admin sessions open on Dev: "admin signed out [admin=405]", P0-2 run 3);
 *  - /api/v1/users/profile      the shell's own-profile read and the chat socket's token check
 *                               (answering it locally fakes authentication);
 *  - /api/v1/chat/webrtc/ice-servers  call setup the admin shell loads on a full page load.
 */
public final class FixtureShell {
    private FixtureShell() { }

    public static boolean isShell(String url) {
        String path = URI.create(url).getPath();
        return path.startsWith("/api/v1/auth/")
                || path.equals("/api/v1/users/profile")
                || path.equals("/api/v1/chat/webrtc/ice-servers");
    }

    /** The catch-all fixture predicate: every API call except the shell's own. */
    public static boolean isFixturedApi(String url) {
        return url.contains("/api/v1/") && !isShell(url);
    }
}
