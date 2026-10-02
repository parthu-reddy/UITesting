package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.util.concurrent.atomic.AtomicInteger;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Owned visible rejection checks shared by the two existing rejection tests. */
public final class RestaurantRejectionScenario {
    private RestaurantRejectionScenario() {}
    public static void reject(Page restaurant, Page customer, String id) {
        Locator card=new RestaurantOrderActionsPage(restaurant).orderCard(id);
        assertThat(card).hasAttribute("data-status", "CREATED");
        Locator reject=card.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Reject").setExact(true));
        assertThat(reject).isEnabled();
        AtomicInteger posts=new AtomicInteger();
        restaurant.onResponse(r->{if(r.request().method().equals("POST") && r.url().endsWith("/orders/"+id+"/reject"))posts.incrementAndGet();});
        reject.click();
        Locator reason=card.getByPlaceholder("e.g. Out of stock, Kitchen busy...",new Locator.GetByPlaceholderOptions().setExact(true));
        Locator confirm=card.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Confirm Cancel").setExact(true));
        assertThat(reason).hasAttribute("required", "");assertThat(confirm).isDisabled();
        reason.fill("   ");assertThat(confirm).isDisabled();
        card.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Back").setExact(true)).click();
        assertThat(reason).isHidden();assertThat(card).hasAttribute("data-status","CREATED");
        org.assertj.core.api.Assertions.assertThat(posts.get()).as("Blank reason and Back do not submit").isZero();
        reject.click();reason.fill("Item out of stock");assertThat(confirm).isEnabled();
        Response response=restaurant.waitForResponse(r->r.request().method().equals("POST") && r.url().endsWith("/orders/"+id+"/reject"),confirm::click);
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200);
        assertThat(card).isHidden();
        Locator tracker=customer.locator("[data-testid='order-tracker'][data-order-id='"+id+"']");
        assertThat(tracker).hasAttribute("data-status","CANCELLED_BY_RESTAURANT",new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
        assertThat(tracker.getByTestId("terminal-headline")).hasText("The restaurant could not fulfil this order.");
        assertThat(tracker.getByTestId("cancellation-reason")).containsText("Item out of stock");
        assertThat(tracker.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Cancel order").setExact(true))).isHidden();
        customer.reload();
        com.fooddelivery.e2e.pages.customer.CustomerDashboardPage.openProfileSettings(customer);
        customer.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        new com.fooddelivery.e2e.pages.customer.CustomerOrderHistoryPage(customer).waitForHistoryLoad();
        customer.locator("[data-testid='customer-history-order'][data-order-id='"+id+"']").click();
        assertThat(tracker).hasAttribute("data-status","CANCELLED_BY_RESTAURANT");
        assertThat(tracker.getByTestId("cancellation-reason")).containsText("Item out of stock");
    }
}
