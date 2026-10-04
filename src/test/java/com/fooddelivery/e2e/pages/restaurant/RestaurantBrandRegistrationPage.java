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
        fillBusinessDetails(brandName);
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
        page.getByLabel("Brand name", new Page.GetByLabelOptions().setExact(true)).fill(name);
    }

    private void fillBusinessDetails(String brandName) {
        fillBrandName(brandName);
        page.getByLabel("GSTIN", new Page.GetByLabelOptions().setExact(true)).fill("29ABCDE1234F1Z5");
        page.getByLabel("PAN", new Page.GetByLabelOptions().setExact(true)).fill("ABCDE1234F");
        page.getByLabel("Bank account number", new Page.GetByLabelOptions().setExact(true)).fill("1234567890");
        page.getByLabel("IFSC code", new Page.GetByLabelOptions().setExact(true)).fill("HDFC0001234");
    }

    private void saveBusinessDetails() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save details and continue").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.FORM,
                new Page.GetByRoleOptions().setName("Application outlet"))).isVisible();
    }

    private void fillOutletDetails(String brandName, String phone) {
        page.getByLabel("Outlet name", new Page.GetByLabelOptions().setExact(true)).fill(brandName + " Outlet");
        page.getByLabel("FSSAI licence number", new Page.GetByLabelOptions().setExact(true)).fill("9999" + phone);
        page.getByLabel("Latitude", new Page.GetByLabelOptions().setExact(true)).fill("12.9808");
        page.getByLabel("Longitude", new Page.GetByLabelOptions().setExact(true)).fill("77.6467");
        page.getByLabel("City code", new Page.GetByLabelOptions().setExact(true)).fill("BLR");
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save outlet and continue").setExact(true)).click();
        assertThat(page.getByLabel("GST registration file", new Page.GetByLabelOptions().setExact(true))).isVisible();
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
