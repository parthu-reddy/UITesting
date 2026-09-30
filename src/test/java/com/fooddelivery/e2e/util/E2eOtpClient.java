package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a login OTP outside the browser after the normal UI has initiated it.
 *
 * <p>The dedicated runner secret lives only in the test process. It is never passed to Playwright,
 * stored in browser state, logged, or embedded in the UI bundle.</p>
 */
public final class E2eOtpClient {

    private static final String OTP_LOOKUP_PATH = "/api/v1/internal/e2e/auth/otp";
    private static final String RUNNER_SECRET_HEADER = "X-E2E-Runner-Secret";
    private static final Pattern OTP_RESPONSE =
            Pattern.compile("\\\"data\\\"\\s*:\\s*\\\"(\\d{6})\\\"");
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private E2eOtpClient() {
    }

    public static String getOtp(String phoneNumber, String serviceName) {
        if (!TestConfig.e2eOtpEnabled()) {
            throw new IllegalStateException(
                    "The E2E runner-secret OTP feature is disabled; use the selected Dev login flow");
        }
        if (phoneNumber == null || !phoneNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("E2E login requires a ten-digit seeded phone number");
        }
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("E2E login requires a supported portal service name");
        }

        URI endpoint = endpointFor(TestConfig.APP_URL, phoneNumber, serviceName);
        String runnerSecret = TestConfig.e2eRunnerSecret();

        for (int attempt = 1; attempt <= 5; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(15))
                    .header(RUNNER_SECRET_HEADER, runnerSecret)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response;
            try {
                response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception exception) {
                throw new IllegalStateException("Could not reach the E2E OTP harness", exception);
            }

            if (response.statusCode() != 200) {
                throw new IllegalStateException("E2E OTP harness rejected or could not route the request (HTTP "
                        + response.statusCode() + ")");
            }

            Matcher matcher = OTP_RESPONSE.matcher(response.body());
            if (matcher.find()) {
                return matcher.group(1);
            }

            if (attempt < 5) {
                waitForOtpCache();
            }
        }

        throw new IllegalStateException("The E2E OTP harness did not find an active OTP");
    }

    static URI endpointFor(String appUrlValue, String phoneNumber, String serviceName) {
        try {
            URI appUrl = URI.create(appUrlValue);
            if (!usesSecureTransport(appUrl)) {
                throw new IllegalStateException(
                        "E2E_APP_URL must use HTTPS unless it is a loopback HTTP address");
            }
            String query = "phoneNumber=" + URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8)
                    + "&serviceName=" + URLEncoder.encode(serviceName, StandardCharsets.UTF_8);
            return new URI(appUrl.getScheme(), null, appUrl.getHost(), appUrl.getPort(),
                    OTP_LOOKUP_PATH, query, null);
        } catch (Exception exception) {
            throw new IllegalStateException("E2E_APP_URL is not a valid application URL", exception);
        }
    }

    /** The runner credential must not cross a non-local plaintext connection. */
    private static boolean usesSecureTransport(URI appUrl) {
        if ("https".equalsIgnoreCase(appUrl.getScheme())) {
            return appUrl.getHost() != null;
        }
        if (!"http".equalsIgnoreCase(appUrl.getScheme())) {
            return false;
        }
        String host = appUrl.getHost();
        return host != null && (host.equalsIgnoreCase("localhost")
                || host.equals("127.0.0.1")
                || host.equals("::1")
                || host.equals("[::1]"));
    }

    private static void waitForOtpCache() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the E2E OTP", exception);
        }
    }
}
