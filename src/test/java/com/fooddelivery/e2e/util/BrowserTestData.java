package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;

/** Local O3 browser-run metadata only; it never queries or changes product state. */
public final class BrowserTestData {
    private BrowserTestData() { }

    public static String phone(String key, String prefix) {
        assertThat(System.getProperty("bp.o3.preflight"))
                .as("Use the O3 browser runner's retained local phone allocation")
                .isEqualTo("true");
        assertThat(TestConfig.APP_URL).matches("https://[a-z0-9-]+\\.trycloudflare\\.com/?");
        String phone = System.getProperty("bp.o3.phone." + key);
        assertThat(phone).matches(prefix + "[0-9]{6}");
        return phone;
    }

    public static String applicantName(String scenario, String phone) {
        assertThat(scenario).matches("[a-z0-9-]+$");
        return "E2E O3 " + scenario + " " + phone;
    }
}
