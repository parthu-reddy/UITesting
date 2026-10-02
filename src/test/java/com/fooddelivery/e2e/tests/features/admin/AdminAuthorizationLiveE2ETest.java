package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;
import java.util.stream.Stream;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live authorization proof for the client guard and the service boundary around administrator UI.
 *
 * <p>This uses seeded customer, restaurant and rider accounts through the normal Dev autofill login. It performs only a
 * role login and one read-only forbidden request; no customer, order, or administrator data
 * is created or changed.</p>
 */
@Tag("admin")
@Tag("admin-authorization")
public class AdminAuthorizationLiveE2ETest extends TestBase {

    enum Account {
        CUSTOMER("Order Food", "/customer"), RESTAURANT("Restaurant Partner", "/restaurant"), DELIVERY("Delivery Executive", "/delivery");
        final String label, path;
        Account(String label, String path) { this.label = label; this.path = path; }
    }

    static Stream<Account> accounts() {
        return Arrays.stream(System.getProperty("admin.auth.roles", "CUSTOMER,RESTAURANT").split(","))
                .map(String::trim).map(Account::valueOf);
    }

    private Page page;
    private Account account;
    private static final String ADMIN_INTERVENTIONS_PATH = "/api/v1/internal/admin/orders/intervention";

    @ParameterizedTest(name = "{0}: admin deep links and reads are denied")
    @MethodSource("accounts")
    @DisplayName("ADMIN-AUTH-01: non-admin deep links cannot render admin UI or read an administrator endpoint")
    void customerCannotOpenAdminRoutesOrReadAdminInterventions(Account selected) {
        account = selected;
        page = switch (selected) {
            case CUSTOMER -> customerPage;
            case RESTAURANT -> restaurantPage;
            case DELIVERY -> riderPage;
        };
        String phone = switch (selected) {
            case CUSTOMER -> testCustomerPhone;
            case RESTAURANT -> testRestaurantPhone;
            case DELIVERY -> testRiderPhone;
        };
        if (selected == Account.DELIVERY) {
            assertThat(phone).as("Use the dedicated preflighted offline authorization fixture")
                    .isEqualTo("7000000033");
        }
        page.setViewportSize(1280, 900);
        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).loginAs(selected.label, phone);
        assertRoleDashboard();

        AtomicInteger clientAdminRequests = new AtomicInteger();
        page.onRequest(request -> recordAdminRequest(request, clientAdminRequests));
        page.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/admin/ledger");
        page.waitForURL("**" + account.path.substring(1) + "**");
        assertRoleDashboard();

        assertThat(clientAdminRequests.get())
                .as("RoleGuard must redirect before an admin screen can request protected data")
                .isZero();
        assertThat(URI.create(page.url()).getPath()).startsWith(account.path);

        Object statusValue = page.evaluate("""
                async (path) => {
                  const token = localStorage.getItem('auth_token');
                  if (!token) throw new Error('Authenticated non-admin token is missing');
                  const response = await fetch(path, {
                    headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' },
                    credentials: 'omit'
                  });
                  return response.status;
                }
                """, ADMIN_INTERVENTIONS_PATH);
        int status = ((Number) statusValue).intValue();
        assertThat(status)
                .as("an authenticated non-admin must be denied the read-only administrator endpoint")
                .isEqualTo(403);
    }

    private void assertRoleDashboard() {
        assertThat(URI.create(page.url()).getPath()).startsWith(account.path);
        if (account == Account.CUSTOMER) {
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                    page.getByText("Deliver to", new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        } else if (account == Account.RESTAURANT) {
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                    page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                            new Page.GetByRoleOptions().setName("Orders").setExact(true))).isVisible();
        } else {
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Offline").setExact(true)))
                    .hasAttribute("aria-pressed", "false");
        }
        assertThat(page.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).role"))
                .isEqualTo(account.name());
    }



    private static void recordAdminRequest(Request request, AtomicInteger count) {
        if (URI.create(request.url()).getPath().startsWith("/api/v1/internal/admin/")) {
            count.incrementAndGet();
        }
    }
}
