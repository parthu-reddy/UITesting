package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import com.fooddelivery.e2e.pages.delivery.*;
import com.fooddelivery.e2e.util.LiveOrderFixture;
import com.fooddelivery.e2e.util.SeededRiderDuty;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.assertThat;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Exact retained Dev handover flows. Each method proves its scenario then completes that order. */
@Tag("pickup-delivery")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PickupDeliveryOtpTest extends TestBase {
    private SeededRiderDuty duty;
    private LiveOrderFixture.Created order;
    private String pickupOtp;
    private String resumeId = System.getProperty("pickup.resume.order.id", "").trim();
    private String activeRestaurantStatus;
    private final List<Map<String,Object>> confirmations = new ArrayList<>();
    private int pickupPosts;
    private int deliveryPosts;

    @BeforeEach
    void loginAllActors() throws java.io.IOException {
        if (!resumeId.isEmpty()) {
            assertThat(resumeId).matches("[0-9a-fA-F-]{36}");
            Map<?,?> retained=(Map<?,?>)riderPage.evaluate("text=>JSON.parse(text)",Files.readString(Path.of("target/lifecycle",resumeId+".json")));
            assertThat(retained.get("orderId")).isEqualTo(resumeId);
            assertThat(retained.get("customerPhone")).isEqualTo(testCustomerPhone);
            assertThat(retained.get("restaurantPhone")).isEqualTo(testRestaurantPhone);
            assertThat(retained.get("riderPhone")).isEqualTo(testRiderPhone);
            order=new LiveOrderFixture.Created(resumeId,(String)retained.get("outlet"));
        }
        riderPage.onResponse(response -> {
            String path = java.net.URI.create(response.url()).getPath();
            if (path == null) return;
            if (path.endsWith("/status") && response.request().method().equals("POST") && response.request().postData()!=null) {
                String body=response.request().postData();
                if (body.contains("\"OUT_FOR_DELIVERY\"")) pickupPosts++;
                if (body.contains("\"DELIVERED\"")) deliveryPosts++;
            }
            if (order != null && path.equals("/api/v1/delivery/orders/active") && response.ok()) {
                Map<?,?> root=(Map<?,?>)riderPage.evaluate("text=>JSON.parse(text)",response.text());
                Object data=root.containsKey("data")?root.get("data"):root;
                Object content=data instanceof Map<?,?> map?map.get("content"):data;
                if (content instanceof List<?> list) for(Object row:list) {
                    if(row instanceof Map<?,?> map && order.id().equals(map.get("id"))) activeRestaurantStatus=(String)map.get("status");
                }
            }
        });
        if (resumeId.isEmpty()) duty = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone);
        else {
            riderPage.navigate(TestConfig.APP_URL);
            new LoginPage(riderPage).loginAs("Delivery Executive",testRiderPhone);
            new DeliveryDashboardPage(riderPage).waitForDashboard();
            assertOwnedJob();
            assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isVisible();
        }
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food",testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner",testRestaurantPhone);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
    }

    @AfterEach
    void retainEvidence(TestInfo info) throws java.io.IOException {
        try {
            if(order!=null) {
                Path folder=Path.of("target/pickup-delivery");Files.createDirectories(folder);
                Files.writeString(folder.resolve(info.getTestMethod().orElseThrow().getName()+"-"+order.id()+".json"),
                    (String)riderPage.evaluate("data=>JSON.stringify(data,null,2)",Map.of(
                        "orderId",order.id(),"outlet",order.outlet(),"customerPhone",testCustomerPhone,
                        "riderPhone",testRiderPhone,"dataPolicy","retain","cleanupPerformed",false,"confirmations",confirmations)));
            }
        } finally {if(duty!=null)duty.close();}
    }

    private DeliveryActiveJobPage prepareAndArrive() throws java.io.IOException {
        if (!resumeId.isEmpty()) {
            new RestaurantDashboardPage(restaurantPage).selectOutlet(order.outlet());
            RestaurantOrderActionsPage actions=new RestaurantOrderActionsPage(restaurantPage);
            assertThat(actions.orderCard(order.id())).hasAttribute("data-status","READY_FOR_PICKUP");
            pickupOtp=actions.getPickupOtp(order.id());assertThat(pickupOtp).matches("[0-9]{6}");
            assertOwnedJob();
            assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isVisible();
            return new DeliveryActiveJobPage(riderPage);
        }
        order=LiveOrderFixture.place(customerPage,restaurantPage,testCustomerPhone,testRestaurantPhone,testRiderPhone,duty);
        RestaurantOrderActionsPage actions=new RestaurantOrderActionsPage(restaurantPage);
        actions.acceptOrder(order.id());
        assertThat(actions.orderCard(order.id())).hasAttribute("data-status","ACCEPTED");
        DispatchPingPage ping=new DispatchPingPage(riderPage);ping.waitForPing(order.outlet());
        assertThat(riderPage.getByText("ORDER CONTRACT #"+order.id().substring(0,8),new Page.GetByTextOptions().setExact(true))).isVisible();
        ping.acceptDispatch(order.id());assertOwnedJob();
        // Subscribe before the ready command, and observe the ordinary authenticated active-order
        // polling response. Public SSE is explicitly deferred; no synthetic stream or time sleep.
        actions.startCooking(order.id());assertThat(actions.orderCard(order.id())).hasAttribute("data-status","PREPARING");
        actions.markPrepared(order.id());assertThat(actions.orderCard(order.id())).hasAttribute("data-status","READY_FOR_PICKUP");
        riderPage.bringToFront();
        riderPage.waitForCondition(()->"READY_FOR_PICKUP".equals(activeRestaurantStatus),new Page.WaitForConditionOptions().setTimeout(45000));
        pickupOtp=actions.getPickupOtp(order.id());assertThat(pickupOtp).matches("[0-9]{6}");
        DeliveryActiveJobPage job=new DeliveryActiveJobPage(riderPage);
        assertThat(riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Mark Arrived at Restaurant").setExact(true))).isVisible();
        expectStatus("AT_RESTAURANT",job::markArrivedAtRestaurant,true);
        assertOwnedJob();assertThat(riderPage.getByText("Step 1: Collect food packages",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isVisible();
        return job;
    }

    private void assertOwnedJob() {
        assertThat(riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Active Contract").setExact(true))).isVisible();
        assertThat(riderPage.getByText("#"+order.id(),new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByText("RESTAURANT ADDRESS",new Page.GetByTextOptions().setExact(true)).locator("..")).containsText(order.outlet());
        assertThat(riderPage.getByText("DELIVERY ADDRESS",new Page.GetByTextOptions().setExact(true)).locator("..").locator("p")).not().isEmpty();
    }

    private Response expectStatus(String status,Runnable action,boolean success) {
        Response response=riderPage.waitForResponse(r->r.request().method().equals("POST") && r.url().endsWith("/orders/"+order.id()+"/status")
                && r.request().postData()!=null && r.request().postData().contains("\""+status+"\""),new Page.WaitForResponseOptions().setTimeout(20000),action);
        confirmations.add(Map.of("orderId",order.id(),"status",status,"httpStatus",response.status(),"expectedSuccess",success));
        if(success) assertThat(response.ok()).as("%s exact order status confirmation HTTP%s",status,response.status()).isTrue();
        else assertThat(response.status()).as("Incorrect %s OTP must be rejected by the server",status).isEqualTo(400);
        return response;
    }

    private void pickUp(DeliveryActiveJobPage job) {
        job.enterPickupOtp(pickupOtp);expectStatus("OUT_FOR_DELIVERY",job::swipeToConfirmPickup,true);
        assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isHidden();
        assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();
        assertThat(riderPage.getByText("Step 2: Deliver to door",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByText("Package Picked Up",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertOwnedJob();
    }

    private String deliveryOtp() {
        Locator tracker=customerPage.locator("[data-testid='order-tracker'][data-order-id='"+order.id()+"']");
        assertThat(tracker).isVisible();assertThat(tracker.getByTestId("delivery-code")).isVisible();
        String code=tracker.getByTestId("delivery-code").innerText().trim();assertThat(code).matches("[0-9]{6}");return code;
    }

    private void complete(DeliveryActiveJobPage job) { complete(job, false); }

    private void complete(DeliveryActiveJobPage job, boolean offlineAfter) {
        if (offlineAfter) riderPage.getByLabel("Go offline after delivery", new Page.GetByLabelOptions().setExact(true)).check();
        job.enterDeliveryOtp(deliveryOtp());Response delivered=expectStatus("DELIVERED",job::swipeToConfirmDelivery,true);
        Map<?,?> command=(Map<?,?>)riderPage.evaluate("text=>JSON.parse(text)",delivered.request().postData());
        assertThat(command.get("goOfflineAfter")).isEqualTo(offlineAfter);
        assertThat(riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Active Contract").setExact(true))).isHidden();
        assertThat(riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(offlineAfter ? "Offline" : "Online Duty").setExact(true))).isVisible();
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator row=riderPage.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText("ORDER #"+order.id().substring(0,8))).filter(new Locator.FilterOptions().setHasText("Delivered"));
        assertThat(row).hasCount(1);assertThat(row).containsText(order.outlet());
        java.util.regex.Matcher amount=Pattern.compile("₹\\s*([0-9,]+(?:\\.[0-9]{1,2})?)").matcher(row.innerText());
        assertThat(amount.find()).as("Completed trip exposes INR payout").isTrue();assertThat(Double.parseDouble(amount.group(1).replace(",",""))).isPositive();
        assertThat(customerPage.locator("[data-testid='order-tracker'][data-order-id='"+order.id()+"']").getByRole(AriaRole.HEADING,new Locator.GetByRoleOptions().setName("Order delivered").setExact(true)))
            .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(30000));
        // This is explicit successful completion, not an order-deleting/resetting teardown.
        riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Completed Deliveries").setExact(true)).locator("..").getByRole(AriaRole.BUTTON).click();
    }

    private static String wrongCode(String actual) {return "000000".equals(actual)?"000001":"000000";}

    @Test @Order(1) @DisplayName("PICKUP-07: Numeric input, short-code validation and restored pickup phase")
    void pickupOtpInputAppears() throws java.io.IOException {
        DeliveryActiveJobPage job=prepareAndArrive();
        riderPage.reload();assertOwnedJob();assertThat(riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Mark Arrived at Restaurant").setExact(true))).isHidden();
        job.enterPickupOtp("12");int before=pickupPosts;job.swipeToConfirmPickup();
        assertThat(riderPage.getByText("OTP must be exactly 6 digits",new Page.GetByTextOptions().setExact(true))).isVisible();assertThat(pickupPosts).isEqualTo(before);
        riderPage.getByPlaceholder("Enter 6-digit pickup OTP").fill("12a345678");assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).hasValue("123456");
        pickUp(job);complete(job);
    }
    @Test @Order(2) @DisplayName("PICKUP-08/09/11: Valid restaurant code confirms exact pickup and destination")
    void enterValidPickupOtpAndSwipeToConfirm() throws java.io.IOException {DeliveryActiveJobPage job=prepareAndArrive();pickUp(job);complete(job);}
    @Test @Order(3) @DisplayName("PICKUP-10: Wrong code receives owned400, restores phase, then same order succeeds")
    void wrongPickupOtpBlocked() throws java.io.IOException {
        DeliveryActiveJobPage job=prepareAndArrive();job.enterPickupOtp(wrongCode(pickupOtp));expectStatus("OUT_FOR_DELIVERY",job::swipeToConfirmPickup,false);
        assertThat(riderPage.getByText("Invalid pickup OTP.",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isVisible();assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isHidden();
        riderPage.reload();assertOwnedJob();assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isVisible();pickUp(job);complete(job);
    }
    @Test @Order(4) @DisplayName("DELIVERY-01/02/03/12: Exact drop-off phase survives reload with customer code")
    void deliveryPhaseWithOtp() throws java.io.IOException {DeliveryActiveJobPage job=prepareAndArrive();pickUp(job);riderPage.reload();assertOwnedJob();assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isHidden();deliveryOtp();complete(job);}
    @Test @Order(5) @DisplayName("DELIVERY-04/05/09/10: Correct code completes exact order and retained history")
    void completeDeliveryWithOtp() throws java.io.IOException {DeliveryActiveJobPage job=prepareAndArrive();pickUp(job);complete(job,true);}
    @Test @Order(6) @DisplayName("DELIVERY-06: Short validation and wrong code reject without completion, then recover")
    void wrongDeliveryOtpBlocked() throws java.io.IOException {
        DeliveryActiveJobPage job=prepareAndArrive();pickUp(job);job.enterDeliveryOtp("12");int before=deliveryPosts;job.swipeToConfirmDelivery();
        assertThat(riderPage.getByText("OTP must be exactly 6 digits",new Page.GetByTextOptions().setExact(true))).isVisible();assertThat(deliveryPosts).isEqualTo(before);
        job.enterDeliveryOtp(wrongCode(deliveryOtp()));expectStatus("DELIVERED",job::swipeToConfirmDelivery,false);
        assertThat(riderPage.getByText("Invalid delivery OTP.",new Page.GetByTextOptions().setExact(true))).isVisible();assertOwnedJob();
        riderPage.reload();assertOwnedJob();assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();complete(job);
    }
    @Test @Order(7) @DisplayName("PICKUP-03/DELIVERY-07: Accessible restaurant directions opens validated navigation target")
    void openNavigationMapDuringJob() throws java.io.IOException {
        DeliveryActiveJobPage job=prepareAndArrive();
        // Verify the external navigation URL only; external map service behavior is a boundary contract.
        riderPage.context().route("https://www.google.com/maps/**",route->route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setContentType("text/html").setBody("<p>External navigation boundary</p>")));
        try(Page popup=riderPage.waitForPopup(()->job.openNavigationMap())) {
            popup.waitForURL("https://www.google.com/maps/**");java.net.URI uri=java.net.URI.create(popup.url());assertThat(uri.getHost()).isEqualTo("www.google.com");
            assertThat(uri.getPath()).isEqualTo("/maps/dir/");assertThat(uri.getQuery()).contains("api=1");
            java.util.regex.Matcher coords=Pattern.compile("destination=(-?[0-9.]+),(-?[0-9.]+)").matcher(uri.getQuery());assertThat(coords.find()).isTrue();
            assertThat(Double.parseDouble(coords.group(1))).isBetween(-90.0,90.0);assertThat(Double.parseDouble(coords.group(2))).isBetween(-180.0,180.0);
        }
        pickUp(job);complete(job);
    }
}
