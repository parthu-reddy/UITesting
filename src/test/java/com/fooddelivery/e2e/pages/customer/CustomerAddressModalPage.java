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
        page.locator("text=Delivery Location, text=Select Address").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));
    }

    public void selectExistingAddress(String label) {
        page.locator("p:has-text('" + label + "')").first().click();
        page.waitForTimeout(500);
    }

    public void clickAddNewAddress() {
        page.locator("button:has-text('Add New'), button:has-text('Add Address')").first().click();
        page.waitForTimeout(500);
    }

    public void fillAddressLabel(String label) {
        page.locator("input[placeholder*='label'], input[placeholder*='Label']").first().fill(label);
    }

    public void fillAddressLine(String address) {
        page.locator("input[placeholder*='address'], input[placeholder*='Address'], textarea").first().fill(address);
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
