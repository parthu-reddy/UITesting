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

import com.fooddelivery.e2e.pages.common.Portal;
@Tag("customer-settings-ui")
public class CustomerSettingsUiTest extends TestBase {
    @BeforeEach void openSettings() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
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
        Locator current=sessions.getByTestId("active-session")
                .filter(new Locator.FilterOptions().setHasText("This device"));
        assertThat(current).hasCount(1);
        assertThat(current.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true))).isVisible();
        assertThat(current).containsText("Everyday account");
        assertThat(current).containsText("Last active:");
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
        // With -Dreview.customer.comment (a review this customer submitted), require that exact review;
        // without it, any review or the defined empty state is accepted.
        String submitted = System.getProperty("review.customer.comment", "").trim();
        if (!submitted.isEmpty()) {
            assertThat(customerPage.locator("article").filter(new Locator.FilterOptions().setHasText(submitted))).hasCount(1);
            return;
        }
        Locator outcome = customerPage.locator("article").first().or(
                customerPage.getByText("You haven't reviewed anything yet",
                        new Page.GetByTextOptions().setExact(true)));
        assertThat(outcome).isVisible();
    }

    @Test
    @DisplayName("REVIEW-01/06: Eligible targets render and an unrated review cannot be submitted")
    void deliveredOrderReviewRequiresAtLeastOneRating() {
        String orderId = System.getProperty("settings.review.order.id", "").trim();
        org.assertj.core.api.Assertions.assertThat(orderId).as("Reuse the owned canonical delivered order").matches("[a-f0-9-]{36}");
        AtomicInteger submissionAttempts = new AtomicInteger();
        customerPage.onRequest(request -> {
            if (request.method().equals("POST") && com.fooddelivery.e2e.util.UrlPaths.path(request.url()).equals("/api/v1/reviews")) submissionAttempts.incrementAndGet();
        });
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        Locator orderCard = customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + orderId + "']");
        assertThat(orderCard).isVisible(); orderCard.click();
        new com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage(customerPage, orderId)
                .tracker().getByTestId("rate-order-prompt").click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Rate your order").setExact(true));
        assertThat(dialog).isVisible();
        dialog.getByRole(AriaRole.RADIOGROUP).first().waitFor();
        org.assertj.core.api.Assertions.assertThat(dialog.getByRole(AriaRole.RADIOGROUP).count())
                .as("Live delivered order has restaurant, rider and dish targets").isGreaterThanOrEqualTo(3);
        assertThat(dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Pick a rating to continue").setExact(true))).isDisabled();
        org.assertj.core.api.Assertions.assertThat(submissionAttempts.get())
                .as("Opening an unrated real review dialog sends no review write").isZero();
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
        customerPage.waitForCondition(() -> "rgb(255, 252, 248)".equals(
                app.evaluate("element => getComputedStyle(element).backgroundColor")),
                new Page.WaitForConditionOptions().setTimeout(5000));
        String lightBackground = (String) app.evaluate("element => getComputedStyle(element).backgroundColor");

        themeToggle().click();
        assertThat(app).hasClass(java.util.regex.Pattern.compile(".*\\bdark\\b.*"));
        // The real design animates background-color for380ms; read its painted endpoint.
        customerPage.waitForCondition(() -> "rgb(18, 22, 28)".equals(
                app.evaluate("element => getComputedStyle(element).backgroundColor")),
                new Page.WaitForConditionOptions().setTimeout(5000));
        String darkBackground = (String) app.evaluate("element => getComputedStyle(element).backgroundColor");
        org.assertj.core.api.Assertions.assertThat(darkBackground)
                .as("Dark mode must visibly change the application background")
                .isNotEqualTo(lightBackground);
    }
}
