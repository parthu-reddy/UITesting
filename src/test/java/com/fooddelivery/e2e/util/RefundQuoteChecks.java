package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.common.ChatWidgetPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderChatPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.math.BigDecimal;
import java.util.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Uses the happy flow's delivered order. Quoting alone never approves or pays a refund. */
public final class RefundQuoteChecks {
    private RefundQuoteChecks() {}
    public static void verify(Page customer,String id,Map<?,?> money) {
        ChatWidgetPage refund=new ChatWidgetPage(customer);
        refund.openRefundRequest(id);
        Locator modal=refund.refundModal();
        assertThat(modal).isVisible();
        Locator submit=refund.requestQuoteButton();assertThat(submit).isDisabled();
        refund.fillRefundReason("Reason without items");assertThat(submit).isDisabled();
        refund.fillRefundReason("");
        customer.waitForCondition(()->refund.refundItemCheckboxes().count()>0,
                new Page.WaitForConditionOptions().setTimeout(15000));
        for(Locator box:refund.refundItemCheckboxes().all())box.check();
        assertThat(submit).isDisabled();refund.fillRefundReason("  \t ");assertThat(submit).isDisabled();
        refund.fillRefundReason("E2E selected-item quote "+id.substring(0,8));
        assertThat(submit).isEnabled(new com.microsoft.playwright.assertions.LocatorAssertions.IsEnabledOptions().setTimeout(15000));
        int before=customer.locator("[data-testid='chat-message'][data-message-type='REFUND_QUOTE_RESPONSE']").count();
        refund.submitRefundRequest();assertThat(modal).isHidden();
        // Retained chat history can contain older requests, and a fast response can
        // replace the pending label before this assertion. Verify the new quote below.
        Locator quotes=customer.locator("[data-testid='chat-message'][data-message-type='REFUND_QUOTE_RESPONSE']");
        assertThat(quotes).hasCount(before+1,new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions().setTimeout(20000));
        Locator quote=quotes.last();
        assertThat(quote).containsText("Type: PARTIAL");
        BigDecimal expected=OrderMoneyChecks.amount(money,"foodCost").add(OrderMoneyChecks.amount(money,"sgst"))
                .add(OrderMoneyChecks.amount(money,"cgst"));
        org.assertj.core.api.Assertions.assertThat(expected).isPositive();
        org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.parseInr(quote.innerText())).isEqualByComparingTo(expected);
        // The old deployed quote loses its selected items. Keep this assertion strict until
        // the reviewed context fix is deployed; never submit a stale/expanded financial request.
        assertThat(quote.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Submit Refund Request").setExact(true))).isEnabled();
        new CustomerOrderChatPage(customer).closeChat();
    }
}
