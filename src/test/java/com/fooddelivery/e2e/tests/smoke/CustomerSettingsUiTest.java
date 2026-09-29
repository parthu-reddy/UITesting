package com.fooddelivery.e2e.tests.smoke;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.util.concurrent.atomic.AtomicInteger;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("customer-settings-ui")
public class CustomerSettingsUiTest extends TestBase {
    @BeforeEach void openSettings() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food",testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        assertThat(customerPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Account Settings"))).isVisible();
    }
    @Test void profilePhoneIsReadOnlyAndCloseReturnsHome() {
        Locator phone=customerPage.locator("input[type=tel]");
        assertThat(phone).hasValue(testCustomerPhone);
        assertThat(phone).isDisabled();
        customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Close settings")).click();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("Deliver to")))).containsText("Home:");
    }
    @Test void keyboardTabsReachHistoryAndWallet() {
        Locator profile=customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Profile").setExact(true));
        profile.focus(); profile.press("ArrowRight");
        assertThat(customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("History").setExact(true)))
                .hasAttribute("aria-selected","true");
        customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Store Credit").setExact(true)).click();
        assertThat(customerPage.getByText("Available Balance",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Transaction History"))).isVisible();
        customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Profile").setExact(true)).click();
        assertThat(customerPage.locator("input[type=tel]")).hasValue(testCustomerPhone);
    }

    @Test void everyCustomerSettingsTabCanBeSelectedWithoutLosingSettings() {
        for (String name : java.util.List.of("Profile", "History", "Addresses", "My Reviews", "Store Credit")) {
            Locator tab = customerPage.getByRole(AriaRole.TAB,
                    new Page.GetByRoleOptions().setName(name).setExact(true));
            tab.click();
            assertThat(tab).hasAttribute("aria-selected", "true");
            assertThat(customerPage.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Account Settings"))).isVisible();
        }
    }
    @Test void profileIdentityAndStoreCreditBalanceRender() {
        Locator name = customerPage.locator("input[type=text]").first();
        assertThat(name).not().hasValue("");
        Locator email = customerPage.locator("input[type=email]").first();
        assertThat(email).not().hasValue("");

        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Store Credit").setExact(true)).click();
        Locator balance = customerPage.getByText("Available Balance",
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("h2");
        assertThat(balance).hasText(java.util.regex.Pattern.compile("^₹[0-9,]+(?:\\.[0-9]{2})?$"));
        String amount = balance.innerText().trim().substring(1).replace(",", "");
        org.assertj.core.api.Assertions.assertThat(Double.parseDouble(amount)).isGreaterThanOrEqualTo(0);
    }
    @Test void currentLoggedInDeviceIsListedWithoutRemovingIt() {
        Locator sessions = customerPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Logged-in Devices")).locator("..").locator("..");
        assertThat(sessions).isVisible();
        assertThat(sessions.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true)).first()).isVisible();
        assertThat(sessions).containsText("Last Active:");
    }
    @Test void savedHomeAddressIsVisibleWithoutEditingIt() {
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Addresses").setExact(true)).click();
        assertThat(customerPage.getByText("Home", new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add / Manage Addresses").setExact(true))).isVisible();
    }

    @Test void everyVisibleSettingsButtonHasAnAccessibleName() {
        Locator buttons = customerPage.locator("button:visible");
        org.assertj.core.api.Assertions.assertThat(buttons.count()).isGreaterThan(0);
        for (int index = 0; index < buttons.count(); index++) {
            Locator button = buttons.nth(index);
            String accessibleName = (String) button.evaluate("element => "
                    + "(element.getAttribute('aria-label') || element.getAttribute('aria-labelledby') || "
                    + "element.getAttribute('title') || element.textContent || '').trim()");
            org.assertj.core.api.Assertions.assertThat(accessibleName)
                    .as("visible settings button %s must expose text, aria-label, aria-labelledby, or title", index)
                    .isNotBlank();
        }
    }

    @Test void everyVisibleProfileFieldHasAProgrammaticLabel() {
        Locator fields = customerPage.locator("input:visible, select:visible, textarea:visible");
        org.assertj.core.api.Assertions.assertThat(fields.count()).isGreaterThan(0);
        for (int index = 0; index < fields.count(); index++) {
            boolean labelled = (Boolean) fields.nth(index).evaluate("element => {"
                    + "const id = element.id;"
                    + "const explicit = id && document.querySelector(`label[for=\"${CSS.escape(id)}\"]`);"
                    + "const wrapped = element.closest('label');"
                    + "const aria = element.getAttribute('aria-label') || element.getAttribute('aria-labelledby');"
                    + "return Boolean(explicit || wrapped || (aria && aria.trim()));"
                    + "}");
            org.assertj.core.api.Assertions.assertThat(labelled)
                    .as("visible profile field %s must be associated with a label", index).isTrue();
        }
    }

    @Test void everyVisibleNewAddressFieldHasAProgrammaticLabel() {
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Addresses").setExact(true)).click();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add / Manage Addresses").setExact(true)).click();
        Locator panel = customerPage.getByText("Delivery Location",
                new Page.GetByTextOptions().setExact(true)).locator("xpath=../../..");
        Locator fields = panel.locator("input:visible, select:visible, textarea:visible");
        org.assertj.core.api.Assertions.assertThat(fields.count()).isGreaterThan(0);
        for (int index = 0; index < fields.count(); index++) {
            boolean labelled = (Boolean) fields.nth(index).evaluate("element => {"
                    + "const id = element.id;"
                    + "const explicit = id && document.querySelector(`label[for=\"${CSS.escape(id)}\"]`);"
                    + "const wrapped = element.closest('label');"
                    + "const aria = element.getAttribute('aria-label') || element.getAttribute('aria-labelledby');"
                    + "return Boolean(explicit || wrapped || (aria && aria.trim()));"
                    + "}");
            org.assertj.core.api.Assertions.assertThat(labelled)
                    .as("visible address field %s must be associated with a label", index).isTrue();
        }
        panel.locator("button:has(svg.lucide-x)").click();
    }
    @Test void blankNewAddressFormIsBlockedAndCanBeClosed() {
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Addresses").setExact(true)).click();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add / Manage Addresses").setExact(true)).click();
        Locator panel = customerPage.getByText("Delivery Location",
                new Page.GetByTextOptions().setExact(true)).locator("xpath=../../..");
        assertThat(panel).isVisible();
        assertThat(panel.getByPlaceholder("Label (e.g. Home, Work)")).isVisible();
        assertThat(panel.getByPlaceholder("Address Line 1")).isVisible();
        assertThat(panel.getByPlaceholder("City")).isVisible();
        assertThat(panel.getByPlaceholder("State")).isVisible();
        assertThat(panel.getByPlaceholder("ZIP Code")).isVisible();
        Locator save = panel.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Save Address").setExact(true));
        assertThat(save).isDisabled();
        panel.getByPlaceholder("Label (e.g. Home, Work)").fill("Unsaved E2E address");
        panel.getByPlaceholder("Address Line 1").fill("123 Unsaved Test Street");
        assertThat(panel.getByPlaceholder("Label (e.g. Home, Work)")).hasValue("Unsaved E2E address");
        assertThat(panel.getByPlaceholder("Address Line 1")).hasValue("123 Unsaved Test Street");
        assertThat(save).isDisabled();
        panel.getByPlaceholder("City").fill("Bengaluru");
        panel.getByPlaceholder("State").fill("Karnataka");
        panel.getByPlaceholder("ZIP Code").fill("560001");
        assertThat(save).isEnabled();
        panel.locator("button:has(svg.lucide-x)").click();
        assertThat(customerPage.getByText("Delivery Location",
                new Page.GetByTextOptions().setExact(true))).isHidden();
        assertThat(customerPage.getByText("Unsaved E2E address",
                new Page.GetByTextOptions().setExact(true))).isHidden();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add / Manage Addresses").setExact(true)).click();
        Locator reopenedPanel = customerPage.getByText("Delivery Location",
                new Page.GetByTextOptions().setExact(true)).locator("xpath=../../..");
        assertThat(reopenedPanel.getByPlaceholder("Label (e.g. Home, Work)")).hasValue("");
        assertThat(reopenedPanel.getByPlaceholder("Address Line 1")).hasValue("");
        assertThat(reopenedPanel.getByPlaceholder("City")).hasValue("");
        assertThat(reopenedPanel.getByPlaceholder("State")).hasValue("");
        assertThat(reopenedPanel.getByPlaceholder("ZIP Code")).hasValue("");
        reopenedPanel.locator("button:has(svg.lucide-x)").click();
    }
    @Test void myReviewsTabShowsReviewsOrDefinedEmptyState() {
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("My Reviews").setExact(true)).click();
        Locator outcome = customerPage.locator("article").first().or(
                customerPage.getByText("You haven't reviewed anything yet",
                        new Page.GetByTextOptions().setExact(true)));
        assertThat(outcome).isVisible();
    }

    @Test
    @DisplayName("REVIEW-01/06: Eligible targets render and an unrated review cannot be submitted")
    void deliveredOrderReviewRequiresAtLeastOneRating() {
        String orderId = "d0000000-0000-4000-8000-000000000001";
        AtomicInteger submissionAttempts = new AtomicInteger();

        // Provide an isolated delivered-history row so this test is independent of mutable shared
        // order fixtures. The real deployed UI and authentication still run; only fixture reads
        // and the review-write endpoint are intercepted.
        String historyResponse = """
                {
                  "success": true,
                  "message": "ok",
                  "data": {
                    "content": [{
                      "id": "%s",
                      "customerId": "d0000000-0000-4000-8000-000000000002",
                      "restaurantId": "d0000000-0000-4000-8000-000000000003",
                      "restaurantName": "E2E Review Fixture Outlet",
                      "status": "HANDED_OVER",
                      "deliveryStatus": "DELIVERED",
                      "totalAmount": 100.0,
                      "itemTotal": 80.0,
                      "customerPlatformFee": 5.0,
                      "sgst": 1.0,
                      "cgst": 1.0,
                      "deliveryFee": 13.0,
                      "deliveryAddress": "E2E Fixture Address",
                      "items": [{
                        "id": "d0000000-0000-4000-8000-000000000004",
                        "menuItemId": "d0000000-0000-4000-8000-000000000005",
                        "name": "E2E Review Fixture Dish",
                        "quantity": 1,
                        "price": 80.0
                      }],
                      "createdAt": "2026-09-28T10:00:00Z"
                    }],
                    "totalElements": 1,
                    "totalPages": 1,
                    "last": true,
                    "size": 10,
                    "number": 0,
                    "first": true,
                    "numberOfElements": 1,
                    "empty": false
                  },
                  "timestamp": "2026-09-28T10:00:00Z"
                }
                """.formatted(orderId);
        String eligibilityResponse = """
                {
                  "success": true,
                  "message": "ok",
                  "data": {
                    "orderId": "%s",
                    "reviewable": true,
                    "windowClosesAt": "2026-10-12T10:00:00Z",
                    "targets": [
                      {"entityType":"RESTAURANT","entityId":"d0000000-0000-4000-8000-000000000003","displayName":"E2E Review Fixture Outlet","visibility":"PUBLIC","alreadyReviewed":false},
                      {"entityType":"DRIVER","entityId":"d0000000-0000-4000-8000-000000000006","displayName":"Delivery partner","visibility":"PRIVATE","alreadyReviewed":false},
                      {"entityType":"PRODUCT","entityId":"d0000000-0000-4000-8000-000000000005","displayName":"E2E Review Fixture Dish","visibility":"PUBLIC","alreadyReviewed":false}
                    ]
                  },
                  "timestamp": "2026-09-28T10:00:00Z"
                }
                """.formatted(orderId);

        customerPage.route("**/api/v1/orders/history**", route -> route.fulfill(
                new Route.FulfillOptions().setStatus(200).setContentType("application/json")
                        .setBody(historyResponse)));
        customerPage.route("**/api/v1/reviews/orders/" + orderId + "/eligibility**", route -> route.fulfill(
                new Route.FulfillOptions().setStatus(200).setContentType("application/json")
                        .setBody(eligibilityResponse)));
        customerPage.route(java.util.regex.Pattern.compile(".*/api/v1/reviews(?:\\?.*)?$"), route -> {
            if ("POST".equals(route.request().method())) {
                submissionAttempts.incrementAndGet();
                route.fulfill(new Route.FulfillOptions().setStatus(409).setContentType("application/json")
                        .setBody("{\"success\":false,\"message\":\"test write blocked\"}"));
            } else {
                route.resume();
            }
        });

        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        Locator orderCard = customerPage.getByTestId("customer-history-order")
                .filter(new Locator.FilterOptions().setHasText("E2E Review Fixture Outlet"));
        assertThat(orderCard).isVisible();
        customerPage.getByTestId("rate-order-prompt").click();

        Locator dialog = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Rate your order").setExact(true));
        assertThat(dialog.getByText("E2E Review Fixture Outlet",
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(dialog.getByText("Delivery partner",
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(dialog.getByText("E2E Review Fixture Dish",
                new Locator.GetByTextOptions().setExact(true))).isVisible();

        Locator submit = dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Pick a rating to continue").setExact(true));
        assertThat(submit).isDisabled();
        org.assertj.core.api.Assertions.assertThat(submissionAttempts.get())
                .as("opening an unrated dialog must not send a review write")
                .isZero();
    }

    /**
     * Returns the visible theme toggle for the current viewport.
     * Desktop (≥1024 px): CustomerNavRail → button text "Dark mode" or "Light mode".
     * Mobile (&lt;1024 px): DashboardHeader → button title "Toggle Light/Dark Mode".
     */
    private Locator themeToggle() {
        if (customerPage.viewportSize() != null && customerPage.viewportSize().width >= 1024) {
            return customerPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^(Dark|Light) mode$")));
        }
        return customerPage.getByTitle("Toggle Light/Dark Mode",
                new Page.GetByTitleOptions().setExact(true));
    }

    @Test void profileThemeClassTogglesAndRestoresLight() {
        Locator app = customerPage.locator(".app-background").first();
        Locator toggle = themeToggle();
        assertThat(toggle).isVisible();

        boolean startsDark = java.util.regex.Pattern.compile("(?:^|\\s)dark(?:\\s|$)")
                .matcher(app.getAttribute("class")).find();
        if (startsDark) toggle.click();
        assertThat(app).not().hasClass(java.util.regex.Pattern.compile(".*\\bdark\\b.*"));
        themeToggle().click();
        assertThat(app).hasClass(java.util.regex.Pattern.compile(".*\\bdark\\b.*"));

        themeToggle().click();
        assertThat(app).not().hasClass(java.util.regex.Pattern.compile(".*\\bdark\\b.*"));
    }

    @Test void profileDarkThemePersistsAcrossReload() {
        Locator app = customerPage.locator(".app-background").first();
        Locator toggle = themeToggle();
        if (!java.util.regex.Pattern.compile("(?:^|\\s)dark(?:\\s|$)")
                .matcher(app.getAttribute("class")).find()) toggle.click();
        assertThat(app).hasClass(java.util.regex.Pattern.compile(".*\\bdark\\b.*"));

        customerPage.reload();
        assertThat(customerPage.locator(".app-background").first())
                .hasClass(java.util.regex.Pattern.compile(".*\\bdark\\b.*"));
    }

    @Test void profileDarkThemeChangesRenderedBackground() {
        Locator app = customerPage.locator(".app-background").first();
        Locator toggle = themeToggle();
        if (java.util.regex.Pattern.compile("(?:^|\\s)dark(?:\\s|$)")
                .matcher(app.getAttribute("class")).find()) toggle.click();
        String lightBackground = (String) app.evaluate("element => getComputedStyle(element).backgroundColor");

        themeToggle().click();
        assertThat(app).hasClass(java.util.regex.Pattern.compile(".*\\bdark\\b.*"));
        String darkBackground = (String) app.evaluate("element => getComputedStyle(element).backgroundColor");
        org.assertj.core.api.Assertions.assertThat(darkBackground)
                .as("Dark mode must visibly change the application background")
                .isNotEqualTo(lightBackground);
    }
}
