package com.fooddelivery.e2e.base;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class TestConfigTest {
    @Test
    void propertyUrlTakesPrecedenceOverEnvironment() {
        assertThat(TestConfig.resolveAppUrl("https://property.example/", "https://environment.example/"))
                .isEqualTo("https://property.example/");
    }

    @Test
    void environmentUrlIsUsedWhenPropertyIsAbsent() {
        assertThat(TestConfig.resolveAppUrl(null, "https://environment.example/"))
                .isEqualTo("https://environment.example/");
    }

    @Test
    void centralDefaultIsUsedWhenNeitherOverrideExists() {
        assertThat(TestConfig.resolveAppUrl(null, null)).isEqualTo(TestConfig.DEFAULT_APP_URL);
    }

    @Test
    void phoneOverridesAllowDedicatedScenarioAccountsForEveryRole() {
        withPhoneProperties(Map.of("customer.phone", "8000000502", "restaurant.phone", "9000000013",
                "rider.phone", "7000000031", "admin.phone", "1000000002"), () -> {
            assertThat(TestConfig.customerPhone()).isEqualTo("8000000502");
            assertThat(TestConfig.restaurantPhone()).isEqualTo("9000000013");
            assertThat(TestConfig.riderPhone()).isEqualTo("7000000031");
            assertThat(TestConfig.adminPhone()).isEqualTo("1000000002");
        });
    }

    @Test
    void ordinaryDefaultsNeverChooseNegativeOrDisposableFixtures() {
        withPhoneProperties(Map.of(), () -> {
            for (int attempt = 0; attempt < 100; attempt++) {
                assertSeededPhone(TestConfig.customerPhone(), "8000000", 500);
                assertSeededPhone(TestConfig.restaurantPhone(), "9000000", 10);
                assertSeededPhone(TestConfig.riderPhone(), "7000000", 30);
            }
            assertThat(TestConfig.adminPhone()).isEqualTo("1000000001");
        });
    }

    private void assertSeededPhone(String phone, String prefix, int upperBound) {
        assertThat(phone).matches(prefix + "[0-9]{3}");
        assertThat(Integer.parseInt(phone.substring(7))).isBetween(1, upperBound);
    }

    private void withPhoneProperties(Map<String, String> overrides, Runnable check) {
        String[] keys = {"customer.phone", "restaurant.phone", "rider.phone", "admin.phone"};
        Map<String, String> previous = new HashMap<>();
        try {
            for (String key : keys) {
                previous.put(key, System.getProperty(key));
                if (overrides.containsKey(key)) System.setProperty(key, overrides.get(key));
                else System.clearProperty(key);
            }
            check.run();
        } finally {
            for (String key : keys) {
                if (previous.get(key) == null) System.clearProperty(key);
                else System.setProperty(key, previous.get(key));
            }
        }
    }
}
