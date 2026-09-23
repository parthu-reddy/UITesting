package com.fooddelivery.e2e.tests.features.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.customer.NearbyOutletPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("accessibility-responsive")
public class ResponsiveAccessibilityTest extends TestBase {

    private int horizontalOverflow(Page page) {
        return ((Number) page.evaluate(
                "Math.max(document.documentElement.scrollWidth, document.body.scrollWidth) - window.innerWidth"))
                .intValue();
    }

    @Test
    @DisplayName("RESP-01/06/07: Desktop role selector has no overflow")
    void desktopRoleSelectorFitsViewport() {
        customerPage.setViewportSize(1280, 800);
        customerPage.navigate(TestConfig.APP_URL);

        assertThat(customerPage.locator("button:has-text('Order Food'):visible").first()).isVisible();
        assertThat(horizontalOverflow(customerPage)).isLessThanOrEqualTo(0);
        int overflowingImages = ((Number) customerPage.evaluate(
                "Array.from(document.images).filter(i => i.getBoundingClientRect().width > innerWidth + 1).length"))
                .intValue();
        assertThat(overflowingImages).isZero();
    }

    @Test
    @DisplayName("RESP-02/06: Mobile customer dashboard has no horizontal overflow")
    void mobileCustomerDashboardFitsViewport() {
        customerPage.setViewportSize(390, 844);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))))
                .containsText("Home:");
        assertThat(horizontalOverflow(customerPage)).isLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("RESP-05/06: Tablet customer dashboard has no horizontal overflow")
    void tabletCustomerDashboardFitsViewport() {
        customerPage.setViewportSize(768, 1024);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))))
                .containsText("Home:");
        assertThat(horizontalOverflow(customerPage)).isLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("RESP-03: Mobile restaurant dashboard fits viewport")
    void mobileRestaurantDashboardFitsViewport() {
        restaurantPage.setViewportSize(390, 844);
        restaurantPage.navigate(TestConfig.APP_URL);
        restaurantPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Restaurant Partner").setExact(true)).click();
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        assertThat(restaurantPage.getByText("Kitchen Kanban Board",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(horizontalOverflow(restaurantPage)).isLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("RESP-04: Mobile rider dashboard keeps duty control accessible")
    void mobileRiderDashboardFitsViewport() {
        riderPage.setViewportSize(390, 844);
        riderPage.navigate(TestConfig.APP_URL);
        riderPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Delivery Executive").setExact(true)).click();
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        new DeliveryDashboardPage(riderPage).waitForDashboard();

        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^(Offline|Online Duty)$"))).first())
                .isVisible();
        assertThat(horizontalOverflow(riderPage)).isLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("ACCESS-04: Escape closes the delivery-location dialog")
    void escapeClosesAddressDialog() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG);
        assertThat(dialog).containsText("Select Delivery Location");

        customerPage.keyboard().press("Escape");

        assertThat(dialog).isHidden();
    }

    @Test
    @DisplayName("ACCESS-04/09: Cart quantity is announced and Escape closes the drawer")
    void cartQuantityIsLiveAndEscapeClosesDrawer() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        Locator item = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                .setHas(customerPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("ADD").setExact(true)))).first();
        assertThat(item).isVisible();
        item.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();

        Locator cart = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        assertThat(cart).isVisible();
        assertThat(cart.locator("output").first()).hasAttribute("aria-live", "polite");

        customerPage.keyboard().press("Escape");

        assertThat(cart).isHidden();
    }

    @Test
    @DisplayName("ACCESS-07/10: Visible role-selector buttons have accessible names")
    void visibleRoleButtonsHaveAccessibleNames() {
        customerPage.navigate(TestConfig.APP_URL);
        int unnamedVisibleButtons = ((Number) customerPage.evaluate("""
                Array.from(document.querySelectorAll('button')).filter(button => {
                  const style = getComputedStyle(button);
                  const visible = button.getClientRects().length > 0 && style.visibility !== 'hidden';
                  const name = button.getAttribute('aria-label') || button.getAttribute('title') || button.innerText;
                  return visible && !name.trim();
                }).length
                """)).intValue();

        assertThat(unnamedVisibleButtons).isZero();
        assertThat(customerPage.locator("button:has-text('Order Food'):visible").first()).isVisible();
        assertThat(customerPage.locator("button:has-text('Restaurant Partner'):visible").first()).isVisible();
        assertThat(customerPage.locator("button:has-text('Delivery Executive'):visible").first()).isVisible();
    }

    @Test
    @DisplayName("ACCESS-02: Tab key advances through customer dashboard controls")
    void tabKeyAdvancesThroughCustomerDashboardControls() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        customerPage.locator("body").click(new Locator.ClickOptions().setPosition(1, 1));

        Set<String> focusedControls = new HashSet<>();
        for (int i = 0; i < 12; i++) {
            customerPage.keyboard().press("Tab");
            String control = (String) customerPage.evaluate("""
                    () => {
                      const el = document.activeElement;
                      return `${el?.tagName || ''}:${el?.getAttribute('aria-label') || el?.getAttribute('title') || el?.textContent?.trim() || ''}`;
                    }
                    """);
            if (!control.startsWith("BODY:") && !control.startsWith("HTML:")) focusedControls.add(control);
        }

        org.assertj.core.api.Assertions.assertThat(focusedControls.size())
                .as("Tab should move through multiple dashboard controls")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("ACCESS-06: Customer restaurant images expose alt attributes")
    void customerRestaurantImagesHaveAltAttributes() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        Locator images = customerPage.locator("img");
        org.assertj.core.api.Assertions.assertThat(images.count()).isGreaterThan(0);
        int imagesWithoutAlt = ((Number) customerPage.evaluate(
                "Array.from(document.images).filter(img => !img.hasAttribute('alt')).length")).intValue();
        org.assertj.core.api.Assertions.assertThat(imagesWithoutAlt)
                .as("Every customer-home image must explicitly declare alt text or an empty decorative alt")
                .isZero();
    }

    @Test
    @DisplayName("ACCESS-08: Phone input has a programmatically associated label")
    void phoneInputHasAssociatedLabel() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).selectRole("Order Food");
        Locator phone = customerPage.getByPlaceholder("9876543210");
        assertThat(phone).isVisible();

        boolean hasAccessibleLabel = (Boolean) phone.evaluate("""
                input => Boolean(
                  input.getAttribute('aria-label') ||
                  input.getAttribute('aria-labelledby') ||
                  (input.id && document.querySelector(`label[for="${CSS.escape(input.id)}"]`)) ||
                  input.closest('label')
                )
                """);
        assertThat(hasAccessibleLabel)
                .as("The visible PHONE NUMBER text must be associated with the telephone input")
                .isTrue();
    }

    @Test
    @DisplayName("NAV-03: Unknown route renders authenticated customer safely")
    void unknownRouteRendersAuthenticatedCustomerSafely() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        customerPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/i-do-not-exist");

        org.assertj.core.api.Assertions.assertThat(customerPage.url()).endsWith("/i-do-not-exist");
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))))
                .containsText("Home:");
        org.assertj.core.api.Assertions.assertThat(customerPage.locator("body").innerText().trim()).isNotEmpty();
    }
}
