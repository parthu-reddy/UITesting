package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import java.util.ArrayList;
import java.util.List;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("ui-only")
@Tag("session-isolation")
public class CrossRoleSessionIsolationTest extends TestBase {

    private final List<String> pageErrors = new ArrayList<>();



    @BeforeEach
    void loginThreeRoles() {
        for (Page page : List.of(customerPage, restaurantPage, riderPage)) page.onPageError(pageErrors::add);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
    }

    @Test
    @DisplayName("SESSION-12/13/14/16/17/18: Three roles remain isolated across reload")
    void simultaneousRolesRemainIsolatedAcrossReload() {
        assertCustomerRoleOnly();
        assertRestaurantRoleOnly();
        assertRiderRoleOnly();
        assertIdentities();

        customerPage.reload();
        restaurantPage.reload();
        riderPage.reload();
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
        new DeliveryDashboardPage(riderPage).waitForDashboard();

        assertCustomerRoleOnly();
        assertRestaurantRoleOnly();
        assertRiderRoleOnly();
        assertIdentities();
        org.assertj.core.api.Assertions.assertThat(pageErrors).isEmpty();
    }

    @Test
    @DisplayName("SESSION-12/13/14: Customer logout leaves restaurant and rider authenticated")
    void customerLogoutDoesNotAffectRestaurantSession() {
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Log Out").setExact(true)).click();
        assertThat(customerPage.getByLabel("PHONE NUMBER", new Page.GetByLabelOptions().setExact(true))).isVisible();

        restaurantPage.reload();
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
        assertRestaurantRoleOnly();
        riderPage.reload();
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        assertRiderRoleOnly();
        assertIdentity(restaurantPage, testRestaurantPhone, "RESTAURANT");
        assertIdentity(riderPage, testRiderPhone, "DELIVERY");
        org.assertj.core.api.Assertions.assertThat(pageErrors).isEmpty();
    }


    @Test
    @Disabled("O4-INT-002: forged-header API and storage probes are deferred under the owner UI-only policy")
    @DisplayName("SESSION-23: unsigned user headers cannot switch session ownership or evict another test user")
    void forgedHeadersCannotChangeSessionOwnership() {
        java.util.Map<?, ?> peer = (java.util.Map<?, ?>) restaurantPage.evaluate("""
                () => {
                    const c = JSON.parse(atob(localStorage.getItem('auth_token').split('.')[1]
                        .replace(/-/g, '+').replace(/_/g, '/')));
                    return {userId: c.sub, sessionId: c.sessionId};
                }
                """);
        java.util.Map<?, ?> result = (java.util.Map<?, ?>) customerPage.evaluate("""
                async peer => {
                    const token = localStorage.getItem('auth_token');
                    const claims = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
                    const headers = {Authorization: `Bearer ${token}`, Accept: 'application/json',
                        'X-User-Id': peer.userId, 'X-Session-Id': peer.sessionId, 'X-User-Roles': 'ADMIN'};
                    const list = await fetch('/api/v1/internal/auth/sessions', {headers, credentials: 'omit'});
                    const body = await list.json();
                    const removal = await fetch(`/api/v1/internal/auth/sessions/${peer.sessionId}`, {
                        method: 'DELETE', headers, credentials: 'omit'
                    });
                    return {listStatus: list.status, ownListed: (body.data || []).some(s => s.sessionId === claims.sessionId),
                        peerListed: (body.data || []).some(s => s.sessionId === peer.sessionId), removeStatus: removal.status};
                }
                """, peer);
        org.assertj.core.api.Assertions.assertThat(((Number) result.get("listStatus")).intValue()).isEqualTo(200);
        org.assertj.core.api.Assertions.assertThat(result.get("ownListed")).isEqualTo(true);
        org.assertj.core.api.Assertions.assertThat(result.get("peerListed")).isEqualTo(false);
        // Current API uses idempotent 200 when the selected ID is not in the caller's list.
        org.assertj.core.api.Assertions.assertThat(((Number) result.get("removeStatus")).intValue()).isEqualTo(200);
        java.util.Map<?, ?> unchanged = (java.util.Map<?, ?>) restaurantPage.evaluate("""
                async peer => {
                    const response = await fetch('/api/v1/internal/auth/sessions', {
                        headers: {Authorization: `Bearer ${localStorage.getItem('auth_token')}`}, credentials: 'omit'
                    });
                    const body = await response.json();
                    return {status: response.status, sessionPreserved: (body.data || []).some(s => s.sessionId === peer.sessionId)};
                }
                """, peer);
        org.assertj.core.api.Assertions.assertThat(((Number) unchanged.get("status")).intValue()).isEqualTo(200);
        org.assertj.core.api.Assertions.assertThat(unchanged.get("sessionPreserved")).isEqualTo(true);
        assertIdentities();
    }

    private void assertIdentities() {
        assertIdentity(customerPage, testCustomerPhone, "CUSTOMER");
        assertIdentity(restaurantPage, testRestaurantPhone, "RESTAURANT");
        assertIdentity(riderPage, testRiderPhone, "DELIVERY");
    }

    private void assertIdentity(Page page, String phone, String role) {
        if (role.equals("CUSTOMER")) CustomerDashboardPage.openProfileSettings(page);
        else page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
        var input = page.locator("input[type=tel]");
        assertThat(input).hasValue(phone);
        assertThat(input).isDisabled();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close settings").setExact(true)).click();
    }

    private void assertRiderRoleOnly() {
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        assertThat(riderPage.getByText("Trips Completed", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByRole(AriaRole.REGION, INCOMING_COLUMN)).hasCount(0);
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).hasCount(0);
        assertThat(riderPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true))).hasCount(0);
    }

    /** The restaurant board's first column: a <section> named "Incoming, <n> orders" (KanbanColumn.tsx). */
    private static final Page.GetByRoleOptions INCOMING_COLUMN = new Page.GetByRoleOptions()
            .setName(Pattern.compile("^Incoming, \\d+ orders?$"));

    private void assertCustomerRoleOnly() {
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))).first()).containsText("Home:");
        assertThat(customerPage.getByRole(AriaRole.REGION, INCOMING_COLUMN)).hasCount(0);
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Accept Order").setExact(true))).hasCount(0);
    }

    private void assertRestaurantRoleOnly() {
        assertThat(restaurantPage.getByRole(AriaRole.REGION, INCOMING_COLUMN)).isVisible();
        assertThat(restaurantPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).hasCount(0);
        assertThat(restaurantPage.getByText("View Cart",
                new Page.GetByTextOptions().setExact(true))).hasCount(0);
    }
}
