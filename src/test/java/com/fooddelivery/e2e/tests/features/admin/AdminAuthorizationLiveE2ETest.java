package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
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
        assertCustomerDashboard();

        AtomicInteger clientAdminRequests = new AtomicInteger();
        customerPage.onRequest(request -> recordAdminRequest(request, clientAdminRequests));
        customerPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/admin/ledger");
        customerPage.waitForURL("**/customer**");
        assertCustomerDashboard();

        assertThat(clientAdminRequests.get())
                .as("RoleGuard must redirect before an admin screen can request protected data")
                .isZero();
        assertThat(URI.create(customerPage.url()).getPath()).startsWith("/customer");

        Object statusValue = customerPage.evaluate("""
                async (path) => {
                  const token = localStorage.getItem('auth_token');
                  if (!token) throw new Error('Authenticated customer token is missing');
                  const response = await fetch(path, {
                    headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' },
                    credentials: 'omit'
                  });
                  return response.status;
                }
                """, ADMIN_INTERVENTIONS_PATH);
        int status = ((Number) statusValue).intValue();
        assertThat(status)
                .as("an authenticated CUSTOMER must be denied the read-only administrator endpoint")
                .isEqualTo(403);
    }

    private void assertCustomerDashboard() {
        // Authorization does not require a saved address or delivery-location selection.
        assertThat(URI.create(customerPage.url()).getPath()).startsWith("/customer");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                customerPage.getByText("Deliver to", new com.microsoft.playwright.Page.GetByTextOptions()
                        .setExact(true)).first()).isVisible();
        assertThat(customerPage.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).role"))
                .isEqualTo("CUSTOMER");
    }

    private static void recordAdminRequest(Request request, AtomicInteger count) {
        if (URI.create(request.url()).getPath().startsWith("/api/v1/internal/admin/")) {
            count.incrementAndGet();
        }
    }
}
