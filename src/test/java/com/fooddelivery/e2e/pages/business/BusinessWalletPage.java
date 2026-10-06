package com.fooddelivery.e2e.pages.business;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** The organisation's Wallet tab in the Business hub (W3), driven through its real controls. */
public final class BusinessWalletPage {
    private final Page page;
    public BusinessWalletPage(Page page) { this.page = page; }

    /** From an open organisation page. */
    public void open() {
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Wallet").setExact(true)).click();
        page.waitForURL(Pattern.compile(".*/business/[a-f0-9-]{36}/wallet(?:[?#].*)?$"));
        assertThat(balance()).isVisible();
    }
    /** The organisation id, from the wallet route. */
    public String organisationId() {
        var m = Pattern.compile("/business/([a-f0-9-]{36})/wallet").matcher(page.url());
        if (!m.find()) throw new AssertionError("Not on a wallet page: " + page.url());
        return m.group(1);
    }
    public Locator balance() { return page.getByTestId("business-wallet-balance"); }
    public Locator addMoneyButton() { return button("Add money"); }
    public Locator unavailableReason() { return page.getByTestId("add-money-unavailable"); }
    public Locator statement() { return page.getByRole(AriaRole.TABLE, new Page.GetByRoleOptions().setName("Business wallet statement").setExact(true)); }
    public Locator newestLineCategory() { return statement().getByTestId("statement-category").first(); }
    public Locator newestLineAmount() { return statement().getByTestId("statement-amount").first(); }

    /** Enters the amount and continues to the payment dialog; returns its pay button. */
    public Locator startAddMoney(String rupees) {
        addMoneyButton().click();
        page.getByTestId("add-money-amount").fill(rupees);
        button("Continue to payment").click();
        Locator pay = page.getByTestId("payment-confirm");
        assertThat(pay).isVisible();
        return pay;
    }
    /**
     * Pays (the Dev payment mock captures a card payment) and waits for the confirmation. Returns the app's
     * own POST …/topups response, so the test can follow the exact top-up it started.
     */
    public com.microsoft.playwright.Response payAndWaitForConfirmation(Locator pay) {
        com.microsoft.playwright.Response started = page.waitForResponse(
                r -> "POST".equals(r.request().method()) && com.fooddelivery.e2e.util.UrlPaths.path(r.url()).matches("/api/v1/money/business/[a-f0-9-]{36}/topups"),
                pay::click);
        assertThat(page.getByText("Money added", new Page.GetByTextOptions().setExact(true)))
                .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(20_000));
        return started;
    }
    private Locator button(String name) { return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true)); }
}
