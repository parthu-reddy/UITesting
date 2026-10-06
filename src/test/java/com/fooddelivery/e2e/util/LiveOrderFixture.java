package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Quoted real Dev order creation shared by the overlapping-order coverage. No setup DB writes. */
public final class LiveOrderFixture {
    public record Created(String id, String outlet) {}
    private LiveOrderFixture() {}
    public static Created readRetained(Page page, String id) throws java.io.IOException {
        Map<?, ?> data = (Map<?, ?>) page.evaluate("text => JSON.parse(text)",
                Files.readString(Path.of("target/lifecycle", id + ".json")));
        org.assertj.core.api.Assertions.assertThat(data.get("orderId")).isEqualTo(id);
        return new Created(id, (String) data.get("outlet"));
    }
    public record PendingResume(Created order, Map<?,?> details) {}
    /** Resume an owned unaccepted fixture through the normal UI read; never recreate it. */
    public static PendingResume resumePending(Page customer, Page restaurant, String id,
            String customerPhone, String restaurantPhone, String riderPhone) throws java.io.IOException {
        org.assertj.core.api.Assertions.assertThat(id).matches("[0-9a-fA-F-]{36}");
        Map<?,?> manifest=(Map<?,?>)customer.evaluate("text=>JSON.parse(text)",Files.readString(Path.of("target/lifecycle",id+".json")));
        org.assertj.core.api.Assertions.assertThat(manifest.get("orderId")).isEqualTo(id);
        org.assertj.core.api.Assertions.assertThat(manifest.get("customerPhone")).isEqualTo(customerPhone);
        org.assertj.core.api.Assertions.assertThat(manifest.get("restaurantPhone")).isEqualTo(restaurantPhone);
        org.assertj.core.api.Assertions.assertThat(manifest.get("riderPhone")).isEqualTo(riderPhone);
        Response response=customer.waitForResponse(r->r.request().method().equals("GET") && "/api/v1/orders/active".equals(com.fooddelivery.e2e.util.UrlPaths.path(r.url())),customer::reload);
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200);
        Map<?,?> body=(Map<?,?>)customer.evaluate("text=>JSON.parse(text)",response.text());
        java.util.List<?> rows=(java.util.List<?>)((Map<?,?>)body.get("data")).get("content");
        Map<?,?> details=rows.stream().map(x->(Map<?,?>)x).filter(x->id.equals(x.get("id"))).findFirst().orElseThrow(()->new AssertionError("Retained pending order is no longer active"));
        org.assertj.core.api.Assertions.assertThat(details.get("status")).isEqualTo("PENDING_ACCEPTANCE");
        org.assertj.core.api.Assertions.assertThat(details.get("paymentMethod")).isEqualTo("CARD");
        Locator owned=customer.locator("[data-testid='order-tracker'][data-order-id='"+id+"']");
        if(!owned.isVisible()) {
            if(customer.getByTestId("order-tracker").first().isVisible())customer.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Back").setExact(true)).click();
            new CustomerActiveOrdersCarouselPage(customer).openOrder(id);
        }
        assertThat(owned).hasAttribute("data-status","PENDING_ACCEPTANCE");
        String outlet=(String)manifest.get("outlet");new RestaurantDashboardPage(restaurant).selectOutlet(outlet);
        assertThat(new com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage(restaurant).orderCard(id)).hasAttribute("data-status","CREATED",new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
        return new PendingResume(new Created(id,outlet),details);
    }
    public static Created place(Page customer, Page restaurant, String customerPhone,
                                String restaurantPhone, String riderPhone, SeededRiderDuty duty) throws java.io.IOException {
        int brand = Integer.parseInt(restaurantPhone.substring(7));
        new NearbyOutletPage(customer).openBrandAndSelectNearby("Brand " + brand);
        CustomerMenuViewPage menu = new CustomerMenuViewPage(customer);menu.addQuickPrepItemToCart();
        String outlet = menu.getSelectedOutletName();
        new RestaurantDashboardPage(restaurant).selectOutlet(outlet);
        assertThat(restaurant.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Active").setExact(true)))
                .hasAttribute("aria-pressed", "true");
        menu.clickViewCart();new CustomerCartDrawerPage(customer).waitForCartOpen();
        duty.assertReadyForCheckout();
        CheckoutAvailability.requireDeliveryAvailable(CheckoutAvailability.clickCheckoutAndWaitForAvailability(customer));
        PaymentModalPage payment = new PaymentModalPage(customer);payment.waitForOpen();payment.waitForFinalQuote();
        Response response = customer.waitForResponse(r -> r.request().method().equals("POST")
                && com.fooddelivery.e2e.util.UrlPaths.path(r.url()).equals("/api/v1/orders"), () -> payment.placeOrder("Credit or debit card"));
        org.assertj.core.api.Assertions.assertThat(response.status()).isBetween(200, 299);
        Map<?, ?> body = (Map<?, ?>) customer.evaluate("text => JSON.parse(text)", response.text());
        org.assertj.core.api.Assertions.assertThat(body.get("success")).isEqualTo(true);
        Map<?, ?> data = (Map<?, ?>) body.get("data");String id = (String) data.get("id");
        org.assertj.core.api.Assertions.assertThat(id).matches("[0-9a-fA-F-]{36}");
        Map<?, ?> request = (Map<?, ?>) customer.evaluate("text => JSON.parse(text)", response.request().postData());
        org.assertj.core.api.Assertions.assertThat(request.get("paymentMethod")).isEqualTo("CARD");
        org.assertj.core.api.Assertions.assertThat((String) request.get("quoteId")).isNotBlank();
        Path path = Path.of("target/lifecycle", id + ".json");Files.createDirectories(path.getParent());
        Files.writeString(path, (String) customer.evaluate("data => JSON.stringify(data,null,2)", Map.of(
                "orderId", id, "customerPhone", customerPhone, "restaurantPhone", restaurantPhone,
                "riderPhone", riderPhone, "outlet", outlet, "dataPolicy", "retain", "cleanupPerformed", false)));
        assertThat(customer.locator("[data-testid='order-tracker'][data-order-id='" + id + "']")).isVisible();
        return new Created(id, outlet);
    }
}
