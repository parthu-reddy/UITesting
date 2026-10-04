package com.fooddelivery.e2e.pages.delivery;

import com.fooddelivery.e2e.pages.common.KycUploadPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Paths;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Current four-step DeliveryApplicationWizard; provider checks prepare admin review. */
public class RiderOnboardingWizardPage {
    private final Page page;
    public RiderOnboardingWizardPage(Page page) {this.page=page;}
    public boolean isWizardVisible() {try {page.getByTestId("delivery-application").waitFor(new Locator.WaitForOptions().setTimeout(15000));return true;}catch(TimeoutError absent){return false;}}
    public void completeOnboarding() {completeDevModeOnboarding("E2E Test Rider", "KA"+String.format("%010d",System.currentTimeMillis()%10000000000L));}
    public void completeDevModeOnboarding() {completeOnboarding();}
    public void completeDevModeOnboarding(String vehicle) {
        completeDevModeOnboarding("E2E Test Rider", vehicle);
    }
    public void completeDevModeOnboarding(String fullName, String vehicle) {
        isWizardVisible();page.getByLabel("Full name",new Page.GetByLabelOptions().setExact(true)).fill(fullName);
        fillVehicleNumber(vehicle);selectVehicleType("Motorcycle");page.getByLabel("City code",new Page.GetByLabelOptions().setExact(true)).fill("BLR");
        clickNext();
        page.getByLabel("Driving licence number",new Page.GetByLabelOptions().setExact(true)).fill("KA"+vehicle.substring(2));
        page.getByLabel("Date of birth",new Page.GetByLabelOptions().setExact(true)).fill("1990-01-01");
        upload("Driving licence");verify("Verify driving licence","/api/v1/verification/driving-license");
        page.getByLabel("Registration number",new Page.GetByLabelOptions().setExact(true)).fill(vehicle);
        upload("Vehicle registration");verify("Verify vehicle registration","/api/v1/verification/vehicle-rc");
        button("Continue to bank and selfie").click();
        page.getByLabel("Bank account number",new Page.GetByLabelOptions().setExact(true)).fill("1234567890");
        page.getByLabel("IFSC code",new Page.GetByLabelOptions().setExact(true)).fill("HDFC0001234");
        verify("Verify bank account","/api/v1/verification/bank-account");
        upload("Selfie");verify("Verify selfie","/api/v1/verification/biometric");button("Review application").click();clickSubmit();
        page.waitForCondition(() -> page.getByText("Awaiting admin review",new Page.GetByTextOptions().setExact(true)).isVisible(),new Page.WaitForConditionOptions().setTimeout(20000));
        assertThat(page.getByText("Your checks are complete. An admin will review the application before you can start.",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(button("Offline")).isHidden();
    }
    private Locator button(String name) {return page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(name).setExact(true));}
    private void upload(String label) {new KycUploadPage(page).uploadDocument(label,Paths.get("src/test/resources/dummy.png").toAbsolutePath().toString());}
    private void verify(String label,String path) {var response=page.waitForResponse(r -> java.net.URI.create(r.url()).getPath().equals(path) && r.request().method().equals("POST"),() -> button(label).click());
        org.assertj.core.api.Assertions.assertThat(response.status()).as("Provider check response").isEqualTo(200);}
    public int getCurrentStep() {String text=page.locator("[role=tab][aria-selected=true]").innerText();return Integer.parseInt(text.substring(0,1));}
    public void fillVehicleNumber(String number) {page.getByLabel("Vehicle registration",new Page.GetByLabelOptions().setExact(true)).fill(number);}
    public void selectVehicleType(String type) {String label=switch(type){case "Motorcycle / Scooter" -> "Motorcycle";case "EV Two-Wheeler" -> "Electric two-wheeler";case "Car / LMV" -> "Car / light vehicle";default -> type;};
        page.getByRole(AriaRole.COMBOBOX,new Page.GetByRoleOptions().setName("Vehicle type").setExact(true)).click();page.getByRole(AriaRole.OPTION,new Page.GetByRoleOptions().setName(label).setExact(true)).click();}
    public void clickNext() {button("Save details and continue").click();assertThat(page.getByLabel("Driving licence number",new Page.GetByLabelOptions().setExact(true))).isVisible();}
    public void clickSubmit() {button("Submit for review").click();Locator dialog=page.getByRole(AriaRole.DIALOG);assertThat(dialog).isVisible();
        var response=page.waitForResponse(r -> java.net.URI.create(r.url()).getPath().equals("/api/v1/delivery-onboarding/application/submit") && r.request().method().equals("POST"),
                () -> dialog.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Submit for review").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200);assertThat(dialog).isHidden();}
}
