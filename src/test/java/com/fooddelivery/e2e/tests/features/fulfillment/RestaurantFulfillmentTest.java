package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.restaurant.*;
import com.fooddelivery.e2e.pages.delivery.*;
import com.fooddelivery.e2e.util.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Canonical retained Dev orders; queue-only coverage has no customer/rider/order setup. */
@Tag("flow") @Tag("ui-only")
public class RestaurantFulfillmentTest extends TestBase {
    private LiveOrderFixture.Created order;
    private SeededRiderDuty duty;
    private Map<?,?> details;
    private String riderOrderStatus;
    private final List<Map<String,Object>> commands=new ArrayList<>();

    @BeforeEach void loginRestaurant() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner",testRestaurantPhone);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
    }
    @AfterEach void retain(TestInfo info) throws java.io.IOException {
        try {
            if(order!=null){Path folder=Path.of("target/restaurant-fulfillment");Files.createDirectories(folder);
                Files.writeString(folder.resolve(info.getTestMethod().orElseThrow().getName()+"-"+order.id()+".json"),
                    (String)restaurantPage.evaluate("x=>JSON.stringify(x,null,2)",Map.of("orderId",order.id(),"outlet",order.outlet(),"customerPhone",testCustomerPhone,"restaurantPhone",testRestaurantPhone,"riderPhone",testRiderPhone,"commands",commands,"dataPolicy","retain","cleanupPerformed",false)));}
        }finally{if(duty!=null)duty.close();}
    }
    private void createOrder() throws java.io.IOException {
        duty=SeededRiderDuty.ensureOnline(riderPage,testRiderPhone);
        customerPage.onResponse(r->{String path=java.net.URI.create(r.url()).getPath();
            if("/api/v1/orders".equals(path)&&r.request().method().equals("POST")&&r.ok()){
                Map<?,?> body=(Map<?,?>)customerPage.evaluate("text=>JSON.parse(text)",r.text());details=(Map<?,?>)body.get("data");}});
        riderPage.onResponse(r->{String path=java.net.URI.create(r.url()).getPath();
            if(order!=null&&"/api/v1/delivery/orders/active".equals(path)&&r.ok()){
                Map<?,?> root=(Map<?,?>)riderPage.evaluate("text=>JSON.parse(text)",r.text());Object data=root.containsKey("data")?root.get("data"):root;
                Object content=data instanceof Map<?,?> map?map.get("content"):data;
                if(content instanceof List<?> rows)for(Object row:rows)if(row instanceof Map<?,?> map&&order.id().equals(map.get("id")))riderOrderStatus=(String)map.get("status");}});
        customerPage.navigate(TestConfig.APP_URL);new LoginPage(customerPage).loginAs("Order Food",testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        String resume=System.getProperty("restaurant.resume.order.id", "").trim();
        if(resume.isEmpty())order=LiveOrderFixture.place(customerPage,restaurantPage,testCustomerPhone,testRestaurantPhone,testRiderPhone,duty);
        else {LiveOrderFixture.PendingResume retained=LiveOrderFixture.resumePending(customerPage,restaurantPage,resume,testCustomerPhone,testRestaurantPhone,testRiderPhone);order=retained.order();details=retained.details();}
        org.assertj.core.api.Assertions.assertThat(details).isNotNull();
        org.assertj.core.api.Assertions.assertThat(details.get("id")).isEqualTo(order.id());
    }
    private Locator card(){return new RestaurantOrderActionsPage(restaurantPage).orderCard(order.id());}
    private Locator tracker(){return customerPage.locator("[data-testid='order-tracker'][data-order-id='"+order.id()+"']");}
    private void command(String action,Runnable run){
        Response r=restaurantPage.waitForResponse(x->x.request().method().equals("POST")&&x.url().endsWith("/orders/"+order.id()+"/"+action),run);
        commands.add(Map.of("orderId",order.id(),"action",action,"httpStatus",r.status()));
        org.assertj.core.api.Assertions.assertThat(r.status()).as("Exact restaurant %s command",action).isEqualTo(200);
    }
    private void status(String status){assertThat(card()).hasAttribute("data-status",status,new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));}
    private void customerStatus(String status){assertThat(tracker()).hasAttribute("data-status",status,new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));}
    private void delivery(String status,Runnable run){Response r=riderPage.waitForResponse(x->x.request().method().equals("POST")&&x.url().endsWith("/orders/"+order.id()+"/status")&&x.request().postData()!=null&&x.request().postData().contains("\""+status+"\""),run);commands.add(Map.of("orderId",order.id(),"action",status,"httpStatus",r.status()));org.assertj.core.api.Assertions.assertThat(r.status()).isEqualTo(200);}

    // REST-ACCEPT-01..08/12..18/20 are consolidated into HappyDeliveryFlowTest.
    // This class retains only the distinct rejection branch and read-only queue checks.
    @Test @DisplayName("REST-ACCEPT-09-11: Required rejection reason, Back and exact customer terminal result")
    void restaurantRejectsIncomingOrder() throws java.io.IOException {createOrder();RestaurantRejectionScenario.reject(restaurantPage,customerPage,order.id());commands.add(Map.of("orderId",order.id(),"action","reject","httpStatus",200));}
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
