package com.fooddelivery.e2e.tests.features.organisations;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.business.BusinessHubPage;
import com.fooddelivery.e2e.util.BusinessPlatformFixture;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
@Tag("ui-only")
@Tag("feature-organisations")
public class BusinessHubOrganisationUiTest extends TestBase {
    @Test void invitationRoleChangesAndRemovalAreVisibleToBothPeople() {
        String owner = BusinessPlatformFixture.phone("hub.owner", "9999"), member = BusinessPlatformFixture.phone("hub.member", "8999");
        String name = "E2E O45 Team " + owner;
        BusinessPlatformFixture.fresh(restaurantPage, owner, "E2E O45 Owner").openPortal(Portal.BUSINESS);
        BusinessHubPage hub = new BusinessHubPage(restaurantPage); hub.create(name); hub.members(); hub.invite(member, "MANAGER");
        BusinessPlatformFixture.fresh(customerPage, member, "E2E O45 Invitee").openPortal(Portal.BUSINESS);
        BusinessHubPage colleague = new BusinessHubPage(customerPage); colleague.accept(name); colleague.open(name); colleague.members();
        assertThat(customerPage.getByTestId("organisation-role")).containsText("Manager");
        assertThat(customerPage.getByRole(AriaRole.FORM, new Page.GetByRoleOptions().setName("Invite a colleague").setExact(true))).hasCount(0);
        restaurantPage.reload(); hub.members(); hub.changeRole(member, "STAFF"); customerPage.reload(); colleague.members();
        assertThat(customerPage.getByTestId("organisation-role")).containsText("Staff");
        // Returning to the hub revalidates the cached list; no manual refresh control exists.
        hub.remove(member); colleague.allOrganisations();
        assertThat(colleague.organisation(name)).hasCount(0);
    }
}
