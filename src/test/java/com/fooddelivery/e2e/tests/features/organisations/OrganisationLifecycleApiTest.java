package com.fooddelivery.e2e.tests.features.organisations;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.util.GatewayApi;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.*;
import java.util.*;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Two real customer sessions and gateway APIs; run only after the O1 deployment gate. */
@Tag("business-platform") @Tag("bp-o1")
public class OrganisationLifecycleApiTest extends TestBase {
    /** Read-only requests after a rollout; server histograms are captured separately over SSH. */
    @Test void readOnlyRequestsForServerLatencyMeasurements() {
        String existingPhone = System.getProperty("bp.o1.measurement.phone");
        assertThat(existingPhone).as("Use a retained successful O1 customer; never register here").matches("8999[0-9]{6}");
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", existingPhone);
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        for (int i = 0; i < 30; i++) {
            var organisations = GatewayApi.get(customerPage, "/api/v1/organisations");
            assertThat(organisations.status()).isEqualTo(200);
            assertThat(content(organisations)).isNotEmpty();
            assertThat(GatewayApi.get(restaurantPage, "/api/v1/brands").status()).isEqualTo(200);
            assertThat(GatewayApi.get(restaurantPage, "/api/v1/outlets").status()).isEqualTo(200);
        }
        System.out.println("Server histogram sample completed: 30 successful requests per organisation/brand/outlet list; no product writes");
    }

    @Test void organisationLifecycle() throws Exception {
        assertThat(System.getProperty("bp.o1.preflight")).as("Allocate unused phones with run_organisation_o1_e2e.py").isEqualTo("true");
        String phoneA=phone("a"),phoneB=phone("b");assertThat(phoneA).isNotEqualTo(phoneB);
        register(customerPage,phoneA,"A");register(restaurantPage,phoneB,"B");
        String name="E2E O1 "+UUID.randomUUID().toString().substring(0,8);
        var created=GatewayApi.post(customerPage,"/api/v1/organisations",Map.of("displayName",name));
        assertThat(created.status()).isEqualTo(201);assertThat(created.object().get("myRole")).isEqualTo("OWNER");
        String id=(String)created.object().get("id"),path="/api/v1/organisations/"+id;
        // Persist the owned resource before further mutations so a failed run remains inspectable.
        var folder=Paths.get("target/business-platform/o1");Files.createDirectories(folder);
        Files.writeString(folder.resolve(id+".json"),(String)customerPage.evaluate("manifest => JSON.stringify(manifest,null,2)",Map.of("organisationId",id,"name",name,"phoneA",phoneA,"phoneB",phoneB,"dataPolicy","retain","cleanupPerformed",false)));
        var list=GatewayApi.get(customerPage,"/api/v1/organisations");assertThat(list.status()).isEqualTo(200);
        assertThat(content(list).stream().anyMatch(o -> id.equals(o.get("id")))).isTrue();
        assertThat(GatewayApi.get(restaurantPage,path).status()).isEqualTo(404);
        String invitation=invite(customerPage,path,phoneB,"MANAGER");
        assertThat(GatewayApi.post(customerPage,path+"/invitations",Map.of("phoneNumber",phoneB,"role","MANAGER")).status()).isEqualTo(409);
        var mine=GatewayApi.get(restaurantPage,"/api/v1/organisation-invitations");assertThat(mine.status()).isEqualTo(200);
        assertThat(content(mine).stream().anyMatch(o -> invitation.equals(o.get("id")))).isTrue();
        assertThat(GatewayApi.post(restaurantPage,"/api/v1/organisation-invitations/"+invitation+"/accept",null).status()).isEqualTo(200);
        assertRole(restaurantPage,path,"MANAGER");
        assertThat(GatewayApi.post(restaurantPage,path+"/invitations",Map.of("phoneNumber",phoneA,"role","STAFF")).status()).isEqualTo(403);
        var members=GatewayApi.get(customerPage,path+"/members");assertThat(members.status()).isEqualTo(200);
        String userB=(String)content(members).stream().filter(m -> phoneB.equals(m.get("phoneNumber"))).findFirst().orElseThrow().get("userId");
        assertThat(GatewayApi.patch(customerPage,path+"/members/"+userB,Map.of("role","STAFF")).status()).isEqualTo(200);
        assertThat(GatewayApi.patch(restaurantPage,path,Map.of("displayName","Forbidden rename")).status()).isEqualTo(403);
        assertThat(GatewayApi.patch(customerPage,path+"/members/"+userB,Map.of("role","OWNER")).status()).isEqualTo(400);
        assertThat(GatewayApi.delete(customerPage,path+"/members/"+userB).status()).isEqualTo(200);
        assertThat(GatewayApi.get(restaurantPage,path).status()).isEqualTo(404);
        String second=invite(customerPage,path,phoneB,"ADMIN");
        assertThat(GatewayApi.post(restaurantPage,"/api/v1/organisation-invitations/"+second+"/accept",null).status()).isEqualTo(200);
        assertThat(GatewayApi.post(customerPage,path+"/ownership-transfer",Map.of("userId",userB)).status()).isEqualTo(200);
        assertRole(restaurantPage,path,"OWNER");assertRole(customerPage,path,"ADMIN");
        assertThat(GatewayApi.delete(customerPage,path+"/members/"+userB).status()).isEqualTo(403);
        System.out.println("O1 lifecycle completed; retained organisationId="+id);
    }
    private String phone(String label){String value=System.getProperty("bp.o1.phone."+label);assertThat(value).matches("8999[0-9]{6}");return value;}
    private void register(Page page,String phone,String label){page.navigate(TestConfig.APP_URL);new LoginPage(page).registerAs("Order Food",phone,"E2E O1 "+label,"o1_"+phone+"@test.com");}
    private String invite(Page page,String path,String phone,String role){var result=GatewayApi.post(page,path+"/invitations",Map.of("phoneNumber",phone,"role",role));assertThat(result.status()).isEqualTo(201);return (String)result.object().get("id");}
    private void assertRole(Page page,String path,String role){var result=GatewayApi.get(page,path);assertThat(result.status()).isEqualTo(200);assertThat(result.object().get("myRole")).isEqualTo(role);}
    private List<Map<?,?>> content(GatewayApi.Response response){List<Map<?,?>> rows=new ArrayList<>();for(Object row:(List<?>)response.object().get("content")){rows.add((Map<?,?>)row);}return rows;}
}
