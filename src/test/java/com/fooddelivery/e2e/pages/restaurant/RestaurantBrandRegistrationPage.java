package com.fooddelivery.e2e.pages.restaurant;

import com.fooddelivery.e2e.pages.common.KycUploadPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Browser-only driver for the rendered restaurant application wizard.
 *
 * <p>Every state change here comes from a visible control. Response listeners only observe the
 * requests caused by those controls; this object never calls an application endpoint or alters
 * browser storage.</p>
 */
public class RestaurantBrandRegistrationPage {
    private final Page page;
    private Locator field(String name) {
        return page.getByLabel(java.util.regex.Pattern.compile("^" + name + "\\s*\\*?$"));
    }

    public RestaurantBrandRegistrationPage(Page page) {
        this.page = page;
    }

    public void waitForWizard() {
        assertThat(page.getByTestId("restaurant-application")).isVisible();
        assertThat(page.getByRole(AriaRole.FORM,
                new Page.GetByRoleOptions().setName("Restaurant business details"))).isVisible();
    }

    public void submitNewApplication(String brandName, String phone) {
        waitForWizard();
        fillBusinessDetails(brandName, phone);
        saveBusinessDetails();
        fillOutletDetails(brandName, phone);
        uploadRequiredDocuments();
        openReview();
        submitForReview("Submit for review");
        assertStatus("Awaiting admin review");
    }

    public void reviseBrandNameAndResubmit(String correctedName) {
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("1. Business").setExact(true)).click();
        fillBrandName(correctedName);
        saveBusinessDetails();
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("3. Documents").setExact(true)).click();
        openReview();
        submitForReview("Resubmit for review");
        assertStatus("Awaiting admin review");
    }

    public void refreshAndAssertStatus(String status) {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Refresh status").setExact(true)).click();
        assertStatus(status);
    }

    public void assertStatus(String status) {
        page.waitForCondition(
                () -> page.getByText(status, new Page.GetByTextOptions().setExact(true)).isVisible(),
                new Page.WaitForConditionOptions().setTimeout(20_000));
    }

    public void assertReviewReason(String reason) {
        assertThat(page.getByText("Review reason", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(page.getByText(reason, new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    public void fillBrandName(String name) {
        field("Brand name").fill(name);
    }

    private void fillBusinessDetails(String brandName, String phone) {
        fillBrandName(brandName);
        // Encode all phone digits injectively, without a DB lookup or reused seeded tax ID.
        int prefix = Integer.parseInt(phone.substring(0, 6));
        char[] letters = new char[5];
        for (int i = 4; i >= 0; i--) { letters[i] = (char) ('A' + prefix % 26); prefix /= 26; }
        String pan = new String(letters, 0, 3) + "C" + letters[3] + phone.substring(6) + letters[4];
        field("GSTIN").fill("29" + pan + "1Z5");
        field("PAN").fill(pan);
        field("Bank account number").fill("1234567890");
        field("IFSC code").fill("HDFC0001234");
    }

    private void saveBusinessDetails() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save details and continue").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.FORM,
                new Page.GetByRoleOptions().setName("Application outlet"))).isVisible();
    }

    private void fillOutletDetails(String brandName, String phone) {
        field("Outlet name").fill(brandName + " Outlet");
        field("FSSAI licence number").fill("9999" + phone);
        field("Latitude").fill("12.9808");
        field("Longitude").fill("77.6467");
        field("City code").fill("BLR");
        // These fresh O3 fixtures need an explicit opening window for discovery at test time.
        field("Opens").fill("00:00");
        field("Closes").fill("23:59");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save outlet and continue").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Upload GST registration").setExact(true))).isVisible();
    }

    private void uploadRequiredDocuments() {
        String fixture = Paths.get("src/test/resources/dummy.png").toAbsolutePath().toString();
        KycUploadPage uploads = new KycUploadPage(page);
        uploads.uploadDocument("GST registration", fixture);
        uploads.uploadDocument("FSSAI licence", fixture);
    }

    private void openReview() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Review application").setExact(true)).click();
        assertThat(reviewButton()).isVisible();
    }

    private void submitForReview(String action) {
        Locator submit = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(action).setExact(true));
        submit.click();
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        assertThat(dialog).isVisible();
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Submit for review").setExact(true)).click();
        assertThat(dialog).isHidden();
    }

    private Locator reviewButton() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^(Submit|Resubmit) for review$")));
    }
}
