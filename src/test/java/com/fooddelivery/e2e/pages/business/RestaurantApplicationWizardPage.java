package com.fooddelivery.e2e.pages.business;
import com.fooddelivery.e2e.pages.restaurant.RestaurantBrandRegistrationPage;
import com.microsoft.playwright.Page;
/** New route owns the established four-step application controls. */
public final class RestaurantApplicationWizardPage {
    private final RestaurantBrandRegistrationPage wizard;
    public RestaurantApplicationWizardPage(Page page) { wizard = new RestaurantBrandRegistrationPage(page); }
    public void submit(String brandName, String phone) { wizard.submitNewApplication(brandName, phone); }
    public void refresh(String status) { wizard.refreshAndAssertStatus(status); }
    public void reason(String reason) { wizard.assertReviewReason(reason); }
}
