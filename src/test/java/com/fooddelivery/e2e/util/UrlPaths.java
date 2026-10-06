package com.fooddelivery.e2e.util;

import java.net.URI;

/** Path of a request/response URL for matching; opaque URLs (data:, blob:) have no path and match nothing. */
public final class UrlPaths {
    private UrlPaths() {
    }

    public static String path(String url) {
        try {
            String path = URI.create(url).getPath();
            return path == null ? "" : path;
        } catch (IllegalArgumentException unparseable) {
            return "";
        }
    }
}
