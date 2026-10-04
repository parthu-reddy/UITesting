package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.admin.AdminPartnerApprovalsPage;
import com.fooddelivery.e2e.pages.business.RestaurantApplicationWizardPage;
import com.microsoft.playwright.Page;
import static org.assertj.core.api.Assertions.assertThat;

/** Allocated locally, created and retained through normal browser controls. */
public final class BusinessPlatformFixture {
    private BusinessPlatformFixture() {}
    public static String phone(String key, String prefix) {
        assertThat(System.getProperty("bp.o45.preflight")).as("Use the retained O4/O5 UI runner").isEqualTo("true");
        assertThat(TestConfig.APP_URL).matches("https://[a-z0-9-]+\\.trycloudflare\\.com/?");
        String value = System.getProperty("bp.o45.phone." + key);
        assertThat(value).matches(prefix + "[0-9]{6}"); return value;
    }
    public static LoginPage fresh(Page page, String phone, String name) {
        return new LoginPage(page).loginNewPerson(phone, name, "bp45_" + phone + "@test.com");
    }
    public static String approvedRestaurant(Page applicant, Page admin, String phone, String adminPhone) {
        String brand = "E2E O45 Restaurant " + phone;
        fresh(applicant, phone, brand).openOnboarding(Portal.RESTAURANT);
        RestaurantApplicationWizardPage wizard = new RestaurantApplicationWizardPage(applicant); wizard.submit(brand, phone);
        approve(admin, adminPhone, brand, false); wizard.refresh("Approved"); return brand;
    }
    public static void approve(Page admin, String adminPhone, String name, boolean delivery) {
        new LoginPage(admin).login(adminPhone).openPortal(Portal.ADMIN);
        AdminPartnerApprovalsPage approvals = new AdminPartnerApprovalsPage(admin); approvals.open();
        if (delivery) approvals.deliveryPartners(); else approvals.restaurants();
        approvals.select(name); approvals.decide("Approve", null); approvals.assertLeftQueue(name);
    }
}
