package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.microsoft.playwright.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live authorization proof for the client guard and the service boundary around administrator UI.
 *
 * <p>This uses the seeded customer through the normal Dev autofill login. It performs only a
 * customer login and one read-only forbidden request; no customer, order, or administrator data
 * is created or changed.</p>
 */
@Tag("admin")
@Tag("admin-authorization")
public class AdminAuthorizationLiveE2ETest extends TestBase {

    private static final String SEEDED_CUSTOMER_PHONE = "8000000001";
    private static final String ADMIN_INTERVENTIONS_PATH = "/api/v1/internal/admin/orders/intervention";

    @Test
    @DisplayName("ADMIN-AUTH-01: a customer deep link cannot render admin UI or read an administrator endpoint")
    void customerCannotOpenAdminRoutesOrReadAdminInterventions() {
        customerPage.setViewportSize(1280, 900);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", SEEDED_CUSTOMER_PHONE);
        new CustomerDashboardPage(customerPage).waitForDashboard();

        AtomicInteger clientAdminRequests = new AtomicInteger();
        customerPage.onRequest(request -> recordAdminRequest(request, clientAdminRequests));
        customerPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/admin/ledger");
        customerPage.waitForURL("**/customer**");
        new CustomerDashboardPage(customerPage).waitForDashboard();

        assertThat(clientAdminRequests.get())
                .as("RoleGuard must redirect before an admin screen can request protected data")
                .isZero();
        assertThat(URI.create(customerPage.url()).getPath()).startsWith("/customer");

        Object statusValue = customerPage.evaluate("""
                async (path) => {
                  const response = await fetch(path, { credentials: 'same-origin' });
                  return response.status;
                }
                """, ADMIN_INTERVENTIONS_PATH);
        int status = ((Number) statusValue).intValue();
        assertThat(status)
                .as("an authenticated CUSTOMER must be denied the read-only administrator endpoint")
                .isEqualTo(403);
    }

    private static void recordAdminRequest(Request request, AtomicInteger count) {
        if (URI.create(request.url()).getPath().startsWith("/api/v1/internal/admin/")) {
            count.incrementAndGet();
        }
    }
}
