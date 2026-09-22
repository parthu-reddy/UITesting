package com.fooddelivery.e2e.tests.smoke;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("admin-ui")
public class AdminReadOnlyUiTest extends TestBase {
    @BeforeEach void login() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone);
        assertThat(adminPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Admin").setExact(true))).isVisible();
    }
    private void tab(String name) { adminPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true)).click(); }
    @Test void ledgerFiltersCanBeCleared() {
        tab("Ledger Entries");
        assertThat(adminPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Filters").setExact(true))).isVisible();
        adminPage.getByPlaceholder("Transaction ID").fill("e2e-unsent-filter");
        adminPage.getByPlaceholder("Owner ID").fill("e2e-unsent-owner");
        tab("Clear");
        assertThat(adminPage.getByPlaceholder("Transaction ID")).hasValue("");
        assertThat(adminPage.getByPlaceholder("Owner ID")).hasValue("");
        assertThat(adminPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Apply Filters"))).isEnabled();
    }
    @Test void categoriesEditorOpensWithoutChangingData() {
        tab("Categories");
        assertThat(adminPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Existing Categories"))).isVisible();
        assertThat(adminPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Global Categories"))).isVisible();
        assertThat(adminPage.getByPlaceholder("e.g. Italian, Vegan, Burgers")).isEditable();
        assertThat(adminPage.getByPlaceholder("Brief description of the category...")).isEditable();
    }
    @Test void supportStatusNavigation() {
        tab("Support Tickets");
        for (String status : new String[]{"IN REVIEW", "RESOLVED", "REJECTED", "OPEN"}) {
            tab(status);
            assertThat(adminPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Support Tickets").setExact(true))).isVisible();
            // The selected status is also rendered in the list heading.
            assertThat(adminPage.getByRole(AriaRole.HEADING).filter(new Locator.FilterOptions().setHasText(status)).first()).isVisible();
        }
    }
    @Test void findExistingCustomerByPhone() {
        tab("User Management");
        adminPage.getByPlaceholder("User ID / Phone").fill(testCustomerPhone);
        tab("Search");
        assertThat(adminPage.getByText(testCustomerPhone, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
    }
    @Test void reviewModerationIsReadOnly() {
        tab("Review Moderation");
        assertThat(adminPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Review Moderation"))).isVisible();
        assertThat(adminPage.getByText("Unredacted. Reviews cannot be edited or removed — this is for investigation.")).isVisible();
    }

    @Test void moneyOperationsTabsRender() {
        tab("Money Operations");
        String[][] cases = {{"Ledger Rejections","Rejected Ledger Movements"},
                {"Reconciliation Runs","Recent Reconciliation Runs"},
                {"Payment DLQ","Failed Payment Webhooks"},{"Wallet DLQ","Wallet Outbox DLQ"}};
        for (String[] item : cases) {
            Locator selected = adminPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName(item[0]).setExact(true));
            selected.click();
            assertThat(selected).hasAttribute("aria-selected","true");
            assertThat(adminPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName(item[1]).setExact(true))).isVisible();
        }
    }
    @Test void payoutHistorySearchForm() {
        tab("Pending Payouts");
        assertThat(adminPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Payouts").setExact(true))).isVisible();
        tab("History");
        assertThat(adminPage.getByPlaceholder("Enter UUID...")).isEditable();
        assertThat(adminPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Search Payouts").setExact(true))).isVisible();
        tab("Pending Queue");
        assertThat(adminPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Pending Payouts Queue").setExact(true))).isVisible();
    }
}
