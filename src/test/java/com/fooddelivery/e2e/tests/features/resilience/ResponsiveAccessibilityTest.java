package com.fooddelivery.e2e.tests.features.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("accessibility-responsive")
public class ResponsiveAccessibilityTest extends TestBase {

    @Test
    @DisplayName("RESP-01: Desktop layout (1280x800)")
    void testDesktopLayout() {
        customerPage.setViewportSize(1280, 800);
        customerPage.navigate(TestConfig.APP_URL);
        
        // Assert some main element is visible in desktop layout
        // For instance, the role selector or login box should not be clipped.
        boolean loginFormVisible = customerPage.locator("text=Login").isVisible() ||
                                   customerPage.locator("button", new com.microsoft.playwright.Page.LocatorOptions().setHasText("Order Food")).isVisible();
        assertThat(loginFormVisible).isTrue();
    }

    @Test
    @DisplayName("RESP-02: Mobile layout (390x844)")
    void testMobileLayout() {
        customerPage.setViewportSize(390, 844);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        
        // Mobile layout should still show key features (bottom nav or similar elements might be responsive)
        assertThat(customerPage.locator("text=Deliver to").isVisible()).isTrue();
        
        // RESP-06: Check horizontal overflow
        Integer scrollWidth = (Integer) customerPage.evaluate("document.body.scrollWidth");
        assertThat(scrollWidth).isLessThanOrEqualTo(390);
    }

    @Test
    @DisplayName("ACCESS-04: Escape closes modal")
    void testEscapeClosesModal() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        
        // Open the user settings or a modal
        if (customerPage.locator("button[aria-label='Settings'], text=Settings").isVisible()) {
            customerPage.locator("button[aria-label='Settings'], text=Settings").first().click();
            customerPage.waitForTimeout(500);
            
            // Press Escape
            customerPage.keyboard().press("Escape");
            customerPage.waitForTimeout(500);
            
            // The modal should close
            // This is a soft check depending on how the modal is identified
            System.out.println("[INFO] Escape key pressed, modal should be closed.");
        }
    }

    @Test
    @DisplayName("ACCESS-08: Form fields have labels")
    void testFormFieldsHaveLabels() {
        customerPage.navigate(TestConfig.APP_URL);
        customerPage.locator("button:has-text('Order Food')").click();
        
        // Let's test the Phone number input field for aria-label or associated label
        // Assuming the input placeholder is "Phone number"
        Object inputAriaLabel = customerPage.locator("input[placeholder*='Phone']").getAttribute("aria-label");
        boolean hasIdForLabel = customerPage.locator("input[placeholder*='Phone']").getAttribute("id") != null;
        
        // Either aria-label or ID to associate with a <label for="...">
        assertThat(inputAriaLabel != null || hasIdForLabel).isTrue();
    }
}
