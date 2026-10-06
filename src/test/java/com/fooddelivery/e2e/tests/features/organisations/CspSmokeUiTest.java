package com.fooddelivery.e2e.tests.features.organisations;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
/** Passive console/origin/header observation while operating the deployed portal UI. */
@Tag("business-platform") @Tag("bp-o5") @Tag("ui-only")
public class CspSmokeUiTest extends TestBase {
    @Test void securityHeadersAndConsoleHoldAcrossEveryCurrentPortal() throws Exception {
        Set<String> origins = new TreeSet<>(); List<String> violations = new ArrayList<>(); List<String> mainHeaders = new ArrayList<>();
        List<String> headerFailures = new ArrayList<>();
        List<String> networkFailures = new ArrayList<>();
        for (Page page : List.of(customerPage, restaurantPage, riderPage, adminPage)) {
            page.onConsoleMessage(message -> {
                String text = message.text(); if (text.toLowerCase().contains("content security policy") || text.toLowerCase().contains("content-security-policy")) violations.add("CSP violation on " + com.fooddelivery.e2e.util.UrlPaths.path(page.url()));
            });
            page.onRequest(request -> recordOrigin(origins, request.url()));
            page.onWebSocket(socket -> recordOrigin(origins, socket.url()));
            page.onResponse(response -> {
                URI responseUri = URI.create(response.url());
                if (response.status() >= 400 && responseUri.getHost() != null)
                    networkFailures.add(response.request().method() + " " + responseUri.getHost() + responseUri.getPath()
                            .replaceAll("[a-fA-F0-9-]{36}", ":id") + " " + response.status());
                if (!response.request().isNavigationRequest() || !URI.create(response.url()).getHost().equals(URI.create(TestConfig.APP_URL).getHost())) return;
                String csp = response.headerValue("content-security-policy");
                if (csp == null || !csp.contains("default-src 'self'") || !csp.contains("frame-ancestors 'none'") || csp.contains("unsafe-eval"))
                    headerFailures.add("Missing or invalid Content-Security-Policy");
                if (!"nosniff".equals(response.headerValue("x-content-type-options"))) headerFailures.add("Missing nosniff");
                if (!"strict-origin-when-cross-origin".equals(response.headerValue("referrer-policy"))) headerFailures.add("Missing strict referrer policy");
                String permissions = response.headerValue("permissions-policy");
                if (permissions == null || !permissions.contains("geolocation=")) headerFailures.add("Missing geolocation policy");
                mainHeaders.add("navigation response observed");
            });
        }
        Path evidence = Path.of("target/business-platform/o45/csp-origins-" + UUID.randomUUID() + ".txt");
        try {
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new CustomerDashboardPage(customerPage).waitForDashboard();
        new LoginPage(restaurantPage).login("9000000001").openPortal(Portal.RESTAURANT);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
        PortalLauncherPage launcher = new PortalLauncherPage(restaurantPage); launcher.open(); launcher.choose(Portal.BUSINESS);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByTestId("organisation-role")).isVisible();
        new LoginPage(riderPage).login("7000000001").openPortal(Portal.DELIVERY);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(riderPage.getByText("Trips Completed", new Page.GetByTextOptions().setExact(true))).isVisible();
        new LoginPage(adminPage).login(testAdminPhone).openPortal(Portal.ADMIN);
        new AdminPortalPage(adminPage).waitForPortal();
        for (Page page : List.of(customerPage, restaurantPage, riderPage, adminPage))
            page.waitForFunction("document.fonts.status === 'loaded' && Array.from(document.images).every(image => image.complete && image.naturalWidth > 0)");
        } finally {
        Files.createDirectories(evidence.getParent());
        Files.writeString(evidence, "Observed public UI origins (no paths or credentials)\n" + String.join("\n", origins)
                + "\nCSP violations: " + violations.size() + "\nHeader failures: " + headerFailures.size() + "\n");
        Files.writeString(evidence, "Network 4xx/5xx: " + networkFailures.size() + "\n"
                + String.join("\n", networkFailures) + "\n", java.nio.file.StandardOpenOption.APPEND);
        }
        assertThat(mainHeaders).hasSizeGreaterThanOrEqualTo(4);
        assertThat(headerFailures).as("required browser security headers").isEmpty();
        assertThat(violations).as("CSP violations during normal portal UI navigation").isEmpty();
        assertThat(networkFailures).as("4xx/5xx during normal permitted portal UI navigation").isEmpty();
    }
    private static void recordOrigin(Set<String> origins, String url) {
        URI uri = URI.create(url); if (uri.getHost() != null && Set.of("http", "https", "ws", "wss").contains(uri.getScheme())) origins.add(uri.getScheme() + "://" + uri.getHost());
    }
}
