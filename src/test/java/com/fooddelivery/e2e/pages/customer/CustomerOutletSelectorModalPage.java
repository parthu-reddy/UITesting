package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Actual named dialog and outlet buttons; selection may leave a cart confirmation open. */
public class CustomerOutletSelectorModalPage {
    private final Page page;
    public CustomerOutletSelectorModalPage(Page page) { this.page = page; }
    public Locator dialog() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Select Outlet Location").setExact(true));
    }
    public Locator choices() {
        return dialog().getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("km away"));
    }
    public void open() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet").setExact(true)).click();
        assertThat(dialog()).isVisible();assertThat(choices().first()).isVisible();
    }
    public boolean isModalOpen() { return dialog().isVisible(); }
    public int getOutletCount() { return choices().count(); }
    public void selectOutlet(String outletName) {
        choices().filter(new Locator.FilterOptions().setHas(page.getByText(outletName,
                new Page.GetByTextOptions().setExact(true)))).click();
    }
    public void selectOutletByIndex(int index) { choices().nth(index).click(); }
}
