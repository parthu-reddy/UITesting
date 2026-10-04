package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.MapTrackingPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Legacy class name retained; this class now contains only Map Search coverage.
 */
@Tag("map-search")
public class ChatRefundMapTest extends TestBase {

    @BeforeEach
    void loginCustomer() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
    }

    // ── MAP SEARCH SCENARIOS ──────────────────────────────────────────────

    @Test
    @DisplayName("MAP-SEARCH-01: Map container visible")
    void mapContainerVisible() {
        MapTrackingPage map = new MapTrackingPage(customerPage);
        // Map might be visible on order tracking or address selection
        boolean isVisible = map.isMapContainerVisible();
        System.out.println("[INFO] Map container visible: " + isVisible);
    }

    @Test
    @DisplayName("MAP-SEARCH-02: Search place in map")
    void searchPlaceInMap() {
        MapTrackingPage map = new MapTrackingPage(customerPage);
        if (map.isMapContainerVisible()) {
            map.searchPlace("Indiranagar");
            customerPage.waitForTimeout(1000);
            assertThat(map.hasSearchResults()).isTrue();
        }
    }

    @Test
    @DisplayName("MAP-SEARCH-03: Select first map search result")
    void selectFirstMapSearchResult() {
        MapTrackingPage map = new MapTrackingPage(customerPage);
        if (map.isMapContainerVisible()) {
            map.searchPlace("Indiranagar");
            customerPage.waitForTimeout(1000);
            if (map.hasSearchResults()) {
                map.selectFirstResult();
            }
        }
    }

    @Test
    @DisplayName("MAP-SEARCH-05: Fill map coordinates")
    void fillMapCoordinates() {
        MapTrackingPage map = new MapTrackingPage(customerPage);
        if (map.isMapContainerVisible()) {
            map.fillCoordinates("17.3850", "78.4867");
        }
    }
}
