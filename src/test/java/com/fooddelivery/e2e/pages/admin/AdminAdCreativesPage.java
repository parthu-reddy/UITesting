package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** The admin Ad creatives screen (A3): the pending queue, approve, reject with a reason. Real responses only. */
public class AdminAdCreativesPage {
    final Page page;
    public AdminAdCreativesPage(Page page) {this.page = page;}

    public void open() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ad creatives").setExact(true)).click();
        assertThat(page.getByTestId("ad-creative-moderation")).isVisible();
    }

    public void refresh() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Refresh creatives").setExact(true)).click();
    }

    /** The pending card for one creative, by id (several campaigns may share a name across runs). */
    public Locator card(String creativeId) {
        return page.getByTestId("ad-creative-" + creativeId);
    }

    public void assertPending(String creativeId, String campaignName, String outletName) {
        Locator card = card(creativeId);
        card.waitFor(new Locator.WaitForOptions().setTimeout(20000));
        assertThat(card).containsText(campaignName);
        assertThat(card).containsText(outletName);
        assertThat(card.getByRole(AriaRole.IMG)).isVisible();
    }

    public void approve(String creativeId) {
        card(creativeId).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Approve").setExact(true)).click();
        assertThat(card(creativeId)).hasCount(0);
    }

    public void reject(String creativeId, String reason) {
        Locator card = card(creativeId);
        card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reject").setExact(true)).click();
        card.getByLabel("Reason for the advertiser", new Locator.GetByLabelOptions().setExact(true)).fill(reason);
        card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reject creative").setExact(true)).click();
        // Final, so a danger confirm repeats the reason before anything is sent.
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Reject this creative?").setExact(true));
        assertThat(dialog).containsText(reason.trim());
        Locator confirm = dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reject creative").setExact(true));
        assertThat(confirm).hasAttribute("data-variant", "danger");
        confirm.click();
        assertThat(card(creativeId)).hasCount(0);
    }
}
