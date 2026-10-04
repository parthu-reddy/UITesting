package com.fooddelivery.e2e.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RiderDutyObservationTest {
    private static final String FIX = "{\"driverId\":\"owned-rider\",\"lat\":12.975,\"lng\":77.604,\"timestamp\":\"2026-10-04T16:00:00Z\"}";
    @Test void offlineDashboardProfileAllowsOneUiActionButDoesNotProveSocketReadiness() {
        var observation = new RiderDutyObservation();
        observation.profile("{\"success\":true,\"data\":{\"status\":\"OFFLINE\"}}");
        assertThat(observation.status()).isEqualTo("OFFLINE"); assertThat(observation.ready(2L)).isFalse();
        observation.received("{\"type\":\"DUTY_STATUS\",\"status\":\"ONLINE\",\"reason\":\"CONNECTED\"}");
        observation.profile("{\"status\":\"OFFLINE\"}"); assertThat(observation.status()).isEqualTo("ONLINE");
    }
    @Test void onlineLabelNeedsARealServerSnapshotAndRecentUiFix() {
        var observation = new RiderDutyObservation();
        observation.sent(FIX, 1L); assertThat(observation.ready(2L)).isFalse();
        observation.received("{\"type\":\"DUTY_STATUS\",\"status\":\"ONLINE\",\"reason\":\"CONNECTED\"}");
        assertThat(observation.ready(2L)).isTrue();
        assertThat(observation.ready(16_000_000_000L)).isFalse();
    }
    @Test void onDeliveryIsPreservedButNeverAcceptedAsIdleReadiness() {
        var observation = new RiderDutyObservation(); observation.sent(FIX, 1L);
        observation.received("{\"type\":\"DUTY_STATUS\",\"status\":\"ON_DELIVERY\",\"reason\":\"CONNECTED\"}");
        assertThat(observation.status()).isEqualTo("ON_DELIVERY"); assertThat(observation.ready(2L)).isFalse();
    }
    @Test void lostLocationOrSocketInvalidatesReadiness() {
        var observation = new RiderDutyObservation(); observation.sent(FIX, 1L);
        observation.received("{\"type\":\"DUTY_STATUS\",\"status\":\"ONLINE\",\"reason\":\"CONNECTED\"}");
        observation.received("{\"type\":\"DUTY_STATUS\",\"status\":\"OFFLINE\",\"reason\":\"LOCATION_LOST\"}");
        assertThat(observation.ready(2L)).isFalse(); observation.disconnected();
        assertThat(observation.status()).isNull(); assertThat(observation.ready(2L)).isFalse();
    }
    @Test void missingSnapshotReasonAndInvalidCoordinatesCannotProveReadiness() {
        var observation = new RiderDutyObservation();
        observation.received("{\"type\":\"DUTY_STATUS\",\"status\":\"ONLINE\"}");
        assertThat(observation.status()).isNull();
        observation.received("{\"type\":\"DUTY_STATUS\",\"status\":\"ONLINE\",\"reason\":\"CONNECTED\"}");
        observation.sent(FIX.replace("12.975", "123.975"), 1L); assertThat(observation.ready(2L)).isFalse();
    }
}
