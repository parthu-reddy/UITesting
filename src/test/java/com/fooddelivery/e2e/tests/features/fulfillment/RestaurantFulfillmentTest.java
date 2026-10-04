package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Queue-only assertions; happy/rejection flows own lifecycle outcomes without duplicate orders. */
@Tag("ui-only")
public class RestaurantFulfillmentTest extends TestBase {
    @BeforeEach void loginRestaurant() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
    }
    @Test @DisplayName("REST-ACCEPT-19: Six Kanban regions and counts without creating an order")
    void restaurantQueueTabStates(){
        String[] names={"Incoming","In the kitchen","Waiting on customer","Ready for pickup","Out for delivery","Refund requests"};
        String[] empty={"No new orders","Nothing cooking","No delayed orders","Nothing waiting","No orders in transit","No refund requests"};
        for(int i=0;i<names.length;i++){
            Locator region=restaurantPage.getByRole(AriaRole.REGION,new Page.GetByRoleOptions().setName(Pattern.compile("^"+names[i]+", [0-9]+ orders?$")));assertThat(region).isVisible();
            int count=region.locator("[data-testid='restaurant-order-card']").count();assertThat(region).hasAttribute("aria-label",names[i]+", "+count+" order"+(count==1?"":"s"));
            if(count==0)assertThat(region.getByRole(AriaRole.HEADING,new Locator.GetByRoleOptions().setName(empty[i]).setExact(true))).isVisible();
        }
    }
}
