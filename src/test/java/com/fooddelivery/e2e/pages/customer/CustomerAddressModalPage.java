package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.Locator;

/**
 * Page Object for Customer Address Modal.
 * Maps to: {@code AddressSelectionModal.tsx, AddressDetailsForm.tsx, CustomerAddressModal.tsx}
 * <p>
 * Also covers: {@code AddressFormFields.tsx}, {@code CustomerAddressPage.tsx},
 * {@code CustomerAddressSelectorModal.tsx}
 * </p>
 */
public class CustomerAddressModalPage {

    private final Page page;

    public CustomerAddressModalPage(Page page) {
        this.page = page;
    }

    public void waitForModalOpen() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING, 
            new com.microsoft.playwright.Page.GetByRoleOptions().setName("Delivery Location").setExact(true))
            .first()
            .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
    }

    public void selectExistingAddress(String label) {
        page.locator("p:has-text('" + label + "')").first().click();
        page.waitForTimeout(500);
    }

    public void clickAddNewAddress() {
        page.locator("button:has-text('Add New'), button:has-text('Add Address')").first().click();
        page.waitForTimeout(500);
    }

    public void searchAndSelectLocation(String query) {
        page.locator("input[aria-label='Search for a place']").first().fill(query);
        // Wait for the dropdown and click the first result
        page.locator(".absolute.z-50 button").first().waitFor();
        page.locator(".absolute.z-50 button").first().click();
        page.waitForTimeout(1000);
    }

    public void fillAddressLabel(String label) {
        page.locator("input[placeholder*='label'], input[placeholder*='Label']").first().fill(label);
    }

    public void fillAddressLine(String address) {
        page.locator("input[placeholder*='address'], input[placeholder*='Address'], textarea").first().fill(address);
    }

    public void fillCity(String city) {
        page.locator("input[placeholder*='city'], input[placeholder*='City']").first().fill(city);
    }

    public void fillState(String state) {
        page.locator("input[placeholder*='state'], input[placeholder*='State']").first().fill(state);
    }

    public void fillZipCode(String zipCode) {
        page.locator("input[placeholder*='zip'], input[placeholder*='Zip'], input[placeholder*='ZIP']").first().fill(zipCode);
    }

    public void saveAddress() {
        page.locator("button:has-text('Save'), button:has-text('Add')").first().click();
        page.waitForTimeout(1000);
    }

    public boolean isModalOpen() {
        return page.locator("text=Delivery Location, text=Select Address").first().isVisible();
    }

    public int getAddressCount() {
        return page.locator("p:has-text('Home'), p:has-text('Work'), p:has-text('Other')").count();
    }
}
