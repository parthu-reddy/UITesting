package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.restaurant.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Restaurant-specific assertions on the happy test's order, without another lifecycle. */
public final class RestaurantAcceptanceChecks {
    private RestaurantAcceptanceChecks() {}
    public static void beforeAccept(Page page,String id,Map<?,?> order) {
        var actions=new RestaurantOrderActionsPage(page);
        Locator card=actions.orderCard(id);
        java.util.function.Consumer<Response> observe = response -> {
            String path = java.net.URI.create(response.url()).getPath();
            if (!response.request().method().equals("GET") || !path.endsWith("/fulfillment/orders/active")) return;
            System.out.println("[KITCHEN] UI active-order response status=" + response.status()
                    + " outlet=" + path.split("/")[4]);
            if (response.status() == 200) {
                Map<?,?> envelope = (Map<?,?>) page.evaluate("text => JSON.parse(text)", response.text());
                List<?> rows = (List<?>) envelope.get("data");
                var owned = rows.stream().map(row -> (Map<?,?>) row).filter(row -> id.equals(row.get("orderId"))).toList();
                System.out.println("[KITCHEN] rows=" + rows.size() + " ownedRows=" + owned.size()
                        + (owned.isEmpty() ? "" : " ownedStatus=" + owned.get(0).get("status")));
            }
        };
        page.onResponse(observe);
        try { assertThat(card).hasCount(1, new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions().setTimeout(30000)); }
        finally { page.offResponse(observe); }
        assertThat(card).hasAttribute("data-status","CREATED",
                new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
        assertThat(card).containsText("#"+id.substring(0,8).toUpperCase());
        org.assertj.core.api.Assertions.assertThat(order.get("customerName")).isNotNull();
        assertThat(card).containsText(order.get("customerName").toString());
        List<?> items=(List<?>)order.get("items");
        org.assertj.core.api.Assertions.assertThat(items).isNotEmpty();
        Locator dishes=card.getByRole(AriaRole.LIST,new Locator.GetByRoleOptions().setName("Dishes").setExact(true));
        assertThat(dishes.getByRole(AriaRole.LISTITEM)).hasCount(items.size());
        for(Object value:items) {
            var item=(Map<?,?>)value;
            Locator dish=dishes.getByRole(AriaRole.LISTITEM).filter(new Locator.FilterOptions().setHasText(item.get("name").toString()));
            assertThat(dish).hasCount(1);assertThat(dish).containsText(item.get("quantity")+"×");
        }
        assertThat(card).containsText("Order value");assertThat(card).containsText("Your payout");
        assertThat(card.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName(Pattern.compile("^Accept.*[0-9]+ min$")))).isEnabled();
        assertThat(card.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Reject").setExact(true))).isEnabled();
        actions.openOrderDetails(id);
        Locator modal=page.getByRole(AriaRole.DIALOG,new Page.GetByRoleOptions().setName("Order #"+id.substring(0,8)).setExact(true));
        assertThat(modal).isVisible();
        for(Object value:items)assertThat(modal).containsText(((Map<?,?>)value).get("name").toString());
        assertThat(modal.getByText("Transparent Financial Breakdown",new Locator.GetByTextOptions().setExact(true))).isVisible();
        new RestaurantOrderDetailsModalPage(page).close();assertThat(modal).isHidden();
        assertThat(card).hasAttribute("data-status","CREATED");
    }
    public static void afterAcceptReload(Page page,String id,String outlet) {
        Locator selection=page.getByRole(AriaRole.COMBOBOX,new Page.GetByRoleOptions().setName("Outlet").setExact(true));
        String selectedLabel=selection.innerText().trim();
        org.assertj.core.api.Assertions.assertThat(selectedLabel.equals(outlet) || selectedLabel.endsWith(" / "+outlet))
                .as("the grouped selector names the owned outlet before reload").isTrue();
        page.reload();new RestaurantDashboardPage(page).waitForDashboard();
        Locator card=new RestaurantOrderActionsPage(page).orderCard(id);
        assertThat(card).hasAttribute("data-status","ACCEPTED");assertThat(card).hasCount(1);
        assertThat(selection).hasText(selectedLabel);
        assertThat(card).containsText("ready by");
    }
}
