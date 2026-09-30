package com.fooddelivery.e2e.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

class E2eOtpClientTest {

    @Test
    void usesTheGatewayOriginRatherThanTheBrowserRoute() {
        URI endpoint = E2eOtpClient.endpointFor(
                "https://example.test/customer", "8000000001", "CUSTOMER");

        assertEquals("https://example.test/api/v1/internal/e2e/auth/otp?phoneNumber=8000000001&serviceName=CUSTOMER",
                endpoint.toString());
    }

    @Test
    void refusesToSendTheRunnerCredentialOverRemotePlaintextHttp() {
        assertThrows(IllegalStateException.class,
                () -> E2eOtpClient.endpointFor("http://example.test", "8000000001", "CUSTOMER"));
    }

    @Test
    void permitsPlainHttpOnlyForLocalDevelopment() {
        URI endpoint = E2eOtpClient.endpointFor("http://localhost:8080/customer", "8000000001", "CUSTOMER");

        assertEquals("http://localhost:8080/api/v1/internal/e2e/auth/otp?phoneNumber=8000000001&serviceName=CUSTOMER",
                endpoint.toString());
    }
}
