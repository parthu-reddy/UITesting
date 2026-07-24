package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class OrderHistoryPage {
    private final Page page;

    public OrderHistoryPage(Page page) {
        this.page = page;
    }

    public void filterByDate(String date) {
        page.locator("input[type='date']").fill(date);
    }

    public void clearDateFilter() {
        page.locator("button:has-text('Clear')").click();
    }

    public void clickRefundOrder(String orderIdPrefix) {
        Locator row = page.locator("tr").filter(new Locator.FilterOptions().setHasText(orderIdPrefix));
        row.locator("button:has-text('Refund')").click();
    }

    public void clickPreviousPage() {
        page.locator("button:has(svg.lucide-chevron-left)").click();
    }

    public void clickNextPage() {
        page.locator("button:has(svg.lucide-chevron-right)").click();
    }

    public void clickPageNumber(String pageNum) {
        page.locator("button", new Page.LocatorOptions().setHasText(pageNum)).click();
    }
}
