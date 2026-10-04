package com.fooddelivery.e2e.tests.features.organisations;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.util.GatewayApi;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

/** ORG-07..12: real Dev signup, current membership and outlet permissions after O2 rollout. */
@Tag("business-platform") @Tag("bp-o2")
public class OrganisationRestaurantAccessTest extends TestBase {
    @Test void organisationRestaurantAccess() throws Exception {
        assertThat(System.getProperty("bp.o2.preflight")).as("Use the O2 read-only deployment/phone preflight runner").isEqualTo("true");
        String phone=System.getProperty("bp.o2.phone");
        assertThat(phone).matches("9999[0-9]{6}");
        login(restaurantPage,"9000000001");
        var organisations=GatewayApi.get(restaurantPage,"/api/v1/organisations");
        assertThat(organisations.status()).isEqualTo(200);
        var rows=content(organisations);assertThat(rows).hasSize(1);
        var organisation=rows.get(0);assertThat(organisation.get("myRole")).isEqualTo("OWNER");
        String org=(String)organisation.get("id"),orgPath="/api/v1/organisations/"+org;
        var brands=listData(GatewayApi.get(restaurantPage,"/api/v1/brands"));assertThat(brands).hasSize(1);
        var brand=brands.get(0);assertThat(brand.get("name")).isEqualTo("Brand 1");
        assertThat(brand.get("organisationId")).isEqualTo(org);
        var outlets=listData(GatewayApi.get(restaurantPage,"/api/v1/outlets"));
        String outletName=System.getProperty("bp.o2.outlet","Brand 1 Outlet 3");
        var outlet=outlets.stream().filter(o -> outletName.equals(o.get("name"))).findFirst().orElseThrow();
        String outletId=(String)outlet.get("id");
        assertThat(outlet.get("brandId")).isEqualTo(brand.get("id"));
        var items=listData(GatewayApi.get(restaurantPage,"/api/v1/restaurants/"+outletId+"/catalog/items"));
        boolean resume=Boolean.getBoolean("bp.o2.resume");
        Path retained=Path.of("target/business-platform/o2/member-"+phone+".json");
        Map<?,?> previous=resume ? (Map<?,?>)restaurantPage.evaluate("text => JSON.parse(text)",Files.readString(retained)) : Map.of();
        var overrides=listData(GatewayApi.get(restaurantPage,"/api/v1/outlets/"+outletId+"/menu-overrides"));
        // Effective availability also respects category hours. Stock remains operable after close.
        var item=items.stream().filter(i -> resume ? previous.get("itemId").equals(i.get("id")) :
            overrides.stream().noneMatch(o -> i.get("id").equals(o.get("masterMenuItemId")) && Boolean.FALSE.equals(o.get("isAvailable"))))
            .findFirst().orElseThrow();
        String itemId=(String)item.get("id"),itemName=(String)item.get("name");
        String stockPath="/api/v1/outlets/"+outletId+"/menu-items/"+itemId+"/stock";
        var manifest=new LinkedHashMap<String,Object>();
        manifest.put("organisationId",org);manifest.put("outletId",outletId);manifest.put("itemId",itemId);
        manifest.put("phone",phone);manifest.put("dataPolicy","retain");manifest.put("cleanupPerformed",false);
        if(resume){
            assertThat(previous.get("organisationId")).isEqualTo(org);
            assertThat(previous.get("outletId")).isEqualTo(outletId);
            assertThat(previous.get("itemId")).isEqualTo(itemId);
            assertThat(previous.get("phone")).isEqualTo(phone);
            manifest.put("invitationId",previous.get("invitationId"));
            manifest.put("userId",previous.get("userId"));
            manifest.put("resumedFromRetainedFixture",true);
        }
        saveManifest(retained,manifest);

        if(resume){login(customerPage,phone);}else{
            customerPage.navigate(TestConfig.APP_URL);
            new LoginPage(customerPage).login(phone, "E2E O2 Staff", "o2_"+phone+"@test.com").openOnboarding(Portal.RESTAURANT);
            var invited=GatewayApi.post(restaurantPage,orgPath+"/invitations",Map.of("phoneNumber",phone,"role","STAFF"));
            assertThat(invited.status()).isEqualTo(201);
            String invitation=(String)invited.object().get("id");manifest.put("invitationId",invitation);saveManifest(retained,manifest);
            assertThat(GatewayApi.post(customerPage,"/api/v1/organisation-invitations/"+invitation+"/accept",null).status()).isEqualTo(200);
        }
        var member=content(GatewayApi.get(restaurantPage,orgPath+"/members")).stream()
            .filter(m -> phone.equals(m.get("phoneNumber"))).findFirst().orElseThrow();
        assertThat(member.get("role")).isEqualTo("STAFF");assertThat(member.get("status")).isEqualTo("ACTIVE");
        String userId=(String)member.get("userId");manifest.put("userId",userId);saveManifest(retained,manifest);

        customerPage.reload();
        var staffDashboard=new RestaurantDashboardPage(customerPage);
        staffDashboard.waitForDashboard();staffDashboard.selectOutlet(outletName);staffDashboard.openMenuTab();
        Locator stock=customerPage.getByRole(AriaRole.SWITCH,new Page.GetByRoleOptions().setName(itemName+" available").setExact(true));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(stock).isChecked();
        toggleStock(customerPage,stock,stockPath,false);assertStock(customerPage,outletId,itemId,false);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(stock).not().isChecked();
        toggleStock(customerPage,stock,stockPath,true);assertStock(customerPage,outletId,itemId,true);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(stock).isChecked();
        assertThat(GatewayApi.post(customerPage,"/api/v1/outlets/"+outletId+"/menu-overrides/"+itemId,
            Map.of("overriddenPrice",123.45,"isAvailable",true)).status()).isEqualTo(403);
        String moneyPath="/api/v1/money/restaurant/"+outletId;
        assertThat(GatewayApi.get(customerPage,moneyPath+"/refund-requests").status()).isEqualTo(200);
        assertThat(GatewayApi.get(customerPage,moneyPath+"/summary").status()).isEqualTo(403);
        assertThat(GatewayApi.get(customerPage,moneyPath+"/statement").status()).isEqualTo(403);
        assertThat(GatewayApi.get(customerPage,moneyPath+"/refunds").status()).isEqualTo(403);
        staffDashboard.openEarningsTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText("Not permitted",new Page.GetByTextOptions().setExact(true))).hasCount(3);

        var ownerSummary=GatewayApi.get(restaurantPage,moneyPath+"/summary");assertThat(ownerSummary.status()).isEqualTo(200);
        assertThat(GatewayApi.patch(restaurantPage,orgPath+"/members/"+userId,Map.of("role","MANAGER")).status()).isEqualTo(200);
        // Earlier STAFF decisions may remain fresh for five seconds. Retry through the actual UI.
        customerPage.waitForTimeout(5100);
        customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Check access again").setExact(true)).click();
        var managerSummary=GatewayApi.get(customerPage,moneyPath+"/summary");assertThat(managerSummary.status()).isEqualTo(200);
        for(String field:List.of("netEarnings","pendingBalance","clawbacks")){
            assertThat(new BigDecimal(managerSummary.object().get(field).toString())).isEqualByComparingTo(ownerSummary.object().get(field).toString());
        }
        for(var card:Map.of("Net Earnings","netEarnings","Pending Balance","pendingBalance","Clawbacks","clawbacks").entrySet()){
            Locator value=customerPage.getByText(card.getKey(),new Page.GetByTextOptions().setExact(true)).locator("..").locator("h3");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(value).hasText(new DecimalFormat("₹#,##0.00").format(new BigDecimal(ownerSummary.object().get(card.getValue()).toString())));
        }
        assertThat(GatewayApi.delete(restaurantPage,orgPath+"/members/"+userId).status()).isEqualTo(200);
        long removedAt=System.nanoTime();customerPage.waitForTimeout(5100);
        assertThat(GatewayApi.put(customerPage,stockPath,Map.of("inStock",false)).status()).isEqualTo(403);
        double elapsed=(System.nanoTime()-removedAt)/1_000_000d;
        assertThat(elapsed).as("Revoked stock access within six seconds of membership removal").isLessThanOrEqualTo(6000);
        assertThat(GatewayApi.get(customerPage,moneyPath+"/refund-requests").status()).isEqualTo(403);
        manifest.put("finalMembershipStatus","REMOVED");manifest.put("revocationObservedMs",elapsed);saveManifest(retained,manifest);
        login(adminPage,"9000000002");
        assertThat(GatewayApi.put(adminPage,stockPath,Map.of("inStock",false)).status()).isEqualTo(403);
        assertThat(GatewayApi.get(adminPage,moneyPath+"/refund-requests").status()).isEqualTo(403);
        for(String internal:List.of("/api/v1/internal/restaurants/users/"+userId+"/outlets?permission=ORG_VIEW",
            "/api/v1/internal/restaurants/outlets/"+outletId+"/organisation")){
            assertThat(GatewayApi.get(customerPage,internal).status()).isEqualTo(403);
        }
        assertStock(restaurantPage,outletId,itemId,true);
        manifest.put("completed",true);saveManifest(retained,manifest);
        System.out.println("O2 staff permissions verified; retained membership fixture "+retained);
    }
    private void login(Page page,String phone){page.navigate(TestConfig.APP_URL);new LoginPage(page).login(phone).openPortal(Portal.RESTAURANT);}
    private void assertStock(Page page,String outlet,String item,boolean available){
        page.waitForCondition(() -> listData(GatewayApi.get(page,"/api/v1/outlets/"+outlet+"/menu-overrides")).stream()
            .anyMatch(row -> item.equals(row.get("masterMenuItemId")) && Boolean.valueOf(available).equals(row.get("isAvailable"))),
            new Page.WaitForConditionOptions().setTimeout(10_000));
    }
    private void toggleStock(Page page,Locator control,String path,boolean available){
        var response=page.waitForResponse(r -> r.url().endsWith(path) && r.request().method().equals("PUT"),control::click);
        assertThat(response.status()).isEqualTo(200);
        Map<?,?> body=(Map<?,?>)page.evaluate("text => JSON.parse(text)",response.text());
        assertThat(((Map<?,?>)body.get("data")).get("isAvailable")).isEqualTo(available);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(control).isEnabled();
    }
    private List<Map<?,?>> content(GatewayApi.Response response){assertThat(response.status()).isEqualTo(200);return rows(response.object().get("content"));}
    private List<Map<?,?>> listData(GatewayApi.Response response){assertThat(response.status()).isEqualTo(200);return rows(response.object().get("data"));}
    private List<Map<?,?>> rows(Object body){assertThat(body).isInstanceOf(List.class);var result=new ArrayList<Map<?,?>>();for(Object row:(List<?>)body){result.add((Map<?,?>)row);}return result;}
    private void saveManifest(Path path,Map<String,Object> manifest) throws Exception{
        Files.createDirectories(path.getParent());Files.writeString(path,(String)restaurantPage.evaluate("value => JSON.stringify(value,null,2)",manifest)+"\n");
    }
}
