package com.fooddelivery.e2e.pages.ads;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** The five-step new-campaign wizard (A4): business, outlet, budget and schedule, creative, review. */
public class CampaignWizardPage {
    private final Page page;
    private final Locator wizard;

    CampaignWizardPage(Page page) {
        this.page = page;
        this.wizard = page.getByTestId("campaign-wizard");
    }

    private void next() {
        wizard.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Next").setExact(true)).click();
    }

    public CampaignWizardPage business(String label) {
        wizard.getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions().setName(label).setExact(true)).click();
        next();
        return this;
    }

    /**
     * Only promotable outlets are offered; the one named is chosen. Playwright runs the name pattern as a JavaScript
     * RegExp, which has no {@code \Q…\E}, so Pattern.quote never matches: escape each metacharacter instead.
     */
    public CampaignWizardPage outlet(String outletName) {
        String literal = outletName.replaceAll("[\\\\^$.|?*+()\\[\\]{}]", "\\\\$0");
        Locator choice = wizard.getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions().setName(Pattern.compile("^" + literal + " · ")));
        choice.waitFor(new Locator.WaitForOptions().setTimeout(20000));
        choice.click();
        next();
        return this;
    }

    public CampaignWizardPage budget(String name, String daily, String total, String maxBid) {
        wizard.getByLabel("Campaign name").fill(name);
        wizard.getByLabel("Daily budget (₹)").fill(daily);
        wizard.getByLabel("Total budget (₹)").fill(total);
        wizard.getByLabel("Max bid per impression (₹)").fill(maxBid);
        next();
        return this;
    }

    /** Uploads an image file as the creative (D-A4-UPLOAD); it is previewed locally until submit. */
    public CampaignWizardPage uploadImage(Path image) {
        wizard.getByTestId("creative-file").setInputFiles(image);
        assertThat(wizard.getByTestId("creative-preview")).isVisible();
        next();
        return this;
    }

    /** Submits from the review step and returns the new campaign's id, once its page is open. */
    public String submit(String expectedName) {
        assertThat(wizard.getByTestId("campaign-review")).containsText(expectedName);
        wizard.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Submit for review").setExact(true)).click();
        page.waitForURL(Pattern.compile(".*/ads/campaigns/[0-9a-f-]{36}$"), new Page.WaitForURLOptions().setTimeout(60000));
        var matcher = Pattern.compile("/ads/campaigns/([0-9a-f-]{36})").matcher(page.url());
        if (!matcher.find()) throw new AssertionError("No campaign id in " + page.url());
        return matcher.group(1);
    }
}
