package com.fooddelivery.e2e.pages.ads;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Ads Manager (A4): the organisation, the start step, the campaigns list and one campaign's page. Real responses only. */
public class AdsManagerPage {
    private final Page page;

    public AdsManagerPage(Page page) { this.page = page; }

    private Locator button(String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    /** The portal has settled on the start step or the campaigns list. */
    public void waitForPortal() {
        page.getByTestId("start-advertising").or(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Campaigns").setExact(true)))
                .first().waitFor(new Locator.WaitForOptions().setTimeout(30000));
    }

    /** The organisation shown in the header (a single organisation is a label, not a switcher). */
    public Locator organisation() {
        return page.getByTestId("ads-organisation");
    }

    /** Starts the ad account when the start step is offered; returns whether it was. */
    public boolean startAdvertisingIfOffered() {
        if (!page.getByTestId("start-advertising").isVisible()) return false;
        Locator start = page.getByTestId("start-advertising").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Start advertising").setExact(true));
        assertThat(start).isEnabled(new com.microsoft.playwright.assertions.LocatorAssertions.IsEnabledOptions().setTimeout(15000));
        start.click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Campaigns").setExact(true))).isVisible();
        return true;
    }

    public CampaignWizardPage newCampaign() {
        button("New campaign").click();
        assertThat(page.getByTestId("campaign-wizard")).isVisible();
        return new CampaignWizardPage(page);
    }

    public Locator detail() {
        return page.getByTestId("campaign-detail");
    }

    public void waitForStatus(String status) {
        assertThat(detail()).hasAttribute("data-status", status, new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
    }

    public Locator creativePanel() {
        return page.getByTestId("creative-panel");
    }

    public void reloadCampaign() {
        page.reload();
        detail().waitFor(new Locator.WaitForOptions().setTimeout(30000));
    }

    public void activate() {
        Locator activate = detail().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Activate").setExact(true));
        assertThat(activate).isEnabled();
        activate.click();
    }

    public void pause() {
        detail().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Pause").setExact(true)).click();
    }

    /** The status label the advertiser reads ("In review", "Live", "Paused" ...). */
    public void assertStatusLabel(String label) {
        assertThat(detail().getByText(label, new Locator.GetByTextOptions().setExact(true)).first()).isVisible();
    }

    public Locator performance() {
        return page.getByTestId("campaign-performance");
    }

    public String campaignIdFromUrl() {
        var matcher = Pattern.compile("/ads/campaigns/([0-9a-f-]{36})").matcher(page.url());
        if (!matcher.find()) throw new AssertionError("Not on a campaign page: " + page.url());
        return matcher.group(1);
    }
}
