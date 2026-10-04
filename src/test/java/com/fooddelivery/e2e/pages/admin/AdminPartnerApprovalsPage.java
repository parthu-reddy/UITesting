package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Actual Partner approvals screen and confirm dialog; no routed or replaced responses. */
public class AdminPartnerApprovalsPage {
    final Page page;
    public AdminPartnerApprovalsPage(Page page) {this.page=page;}
    public void open() {page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Partner Approvals").setExact(true)).click();
        assertThat(page.getByTestId("partner-approvals")).isVisible();}
    /** Uses the visible queue refresh control after an applicant submits or is resubmitted. */
    public void refreshApplications() {page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Refresh applications").setExact(true)).click();}
    public void restaurants() {page.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Restaurants").setExact(true)).click();}
    public void deliveryPartners() {page.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Delivery partners").setExact(true)).click();}
    public Locator row(String name) {return page.getByTestId("partner-approvals").getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText(name));}
    public void select(String name) {Locator candidate=row(name);candidate.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(20000));assertThat(candidate).hasCount(1);candidate.click();assertThat(page.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName(name).setExact(true))).isVisible();}
    public void assertChecks(String label,String result) {assertThat(page.locator("dt").filter(new Locator.FilterOptions().setHasText(label)).locator("..").locator("dd")).hasText(result);}
    public void requestPrivateView(String type) {page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("View "+type).setExact(true)).click();
        Locator link=page.getByRole(AriaRole.LINK,new Page.GetByRoleOptions().setName("Open "+type+" · link expires in 5 minutes").setExact(true));
        assertThat(link).isVisible();
        Page document=page.waitForPopup(link::click);
        try {document.waitForCondition(() -> Boolean.TRUE.equals(document.evaluate("() => Array.from(document.images).some(image => image.complete && image.naturalWidth > 0)")),
                new Page.WaitForConditionOptions().setTimeout(10000));}
        finally {document.close();}}
    public void rejectWithoutReason() {page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Reject").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.ALERT)).containsText("Enter a clear reason between 10 and 500 characters.");
        assertThat(page.getByRole(AriaRole.DIALOG)).isHidden();}
    public void decide(String action,String reason) {if(reason!=null)page.getByLabel("Decision reason",new Page.GetByLabelOptions().setExact(true)).fill(reason);
        page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(action).setExact(true)).click();
        Locator dialog=page.getByRole(AriaRole.DIALOG);assertThat(dialog).isVisible();
        dialog.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName(action).setExact(true)).click();assertThat(dialog).isHidden();}
    public void assertLeftQueue(String name) {assertThat(row(name)).hasCount(0);}
}
