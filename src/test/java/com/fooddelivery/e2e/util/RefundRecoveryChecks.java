package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Money assertions on an existing owned cancelled/rejected CARD order, with no new lifecycle. */
public final class RefundRecoveryChecks {
    private RefundRecoveryChecks() {}
    public static Map<?,?> read(Page page,String path) {
        Map<?,?> result=(Map<?,?>)page.evaluate("""
            async path => {
              const response = await fetch(path, {headers:{Authorization:'Bearer '+localStorage.getItem('auth_token')}});
              return {status:response.status, body:await response.json()};
            }
            """,path);
        assertThat(result.get("status")).as("owned read %s",path).isEqualTo(200);
        return result;
    }
    public static Map<?,?> order(Page customer,String id) {
        Map<?,?> envelope=(Map<?,?>)read(customer,"/api/v1/orders/"+id).get("body");
        Map<?,?> order=(Map<?,?>)envelope.get("data");
        assertThat(order.get("id")).isEqualTo(id);assertThat(order.get("paymentMethod")).isEqualTo("CARD");
        return order;
    }
    public static void resume(Page customer,Page admin,String id,String expectedStatus,
            String customerPhone,String restaurantPhone,String riderPhone) throws java.io.IOException {
        Map<?,?> manifest=(Map<?,?>)customer.evaluate("text=>JSON.parse(text)",
                Files.readString(Path.of("target/lifecycle",id+".json")));
        assertThat(manifest.get("orderId")).isEqualTo(id);
        assertThat(manifest.get("customerPhone")).isEqualTo(customerPhone);
        assertThat(manifest.get("restaurantPhone")).isEqualTo(restaurantPhone);
        assertThat(manifest.get("riderPhone")).isEqualTo(riderPhone);
        Map<?,?> order=order(customer,id);assertThat(order.get("status")).isEqualTo(expectedStatus);
        customer.route("**/api/v1/orders",route->{
            if(route.request().method().equals("POST"))throw new AssertionError("Retained recovery must not create another order");
            route.resume();
        });
        verify(customer,admin,id,OrderMoneyChecks.amount(order,"totalAmount"));
    }
    public static void verify(Page customer,Page admin,String id,BigDecimal originalPaid) throws java.io.IOException {
        final java.util.concurrent.atomic.AtomicReference<Map<?,?>> completed=new java.util.concurrent.atomic.AtomicReference<>();
        long[] nextRead={0};
        // Normal asynchronous Dev refund processing, not an intentional expiry/quota wait.
        customer.waitForCondition(()->{
            long now=System.nanoTime();if(now<nextRead[0])return false;nextRead[0]=now+1_000_000_000L;
            List<?> refunds=(List<?>)read(customer,"/api/v1/money/customer/orders/"+id+"/refunds").get("body");
            if(refunds.isEmpty())return false;
            assertThat(refunds).as("one automatic refund for one terminal command").hasSize(1);
            Map<?,?> refund=(Map<?,?>)refunds.get(0);
            assertThat(refund.get("orderId")).isEqualTo(id);assertThat(refund.get("id")).isNotNull();
            assertThat(OrderMoneyChecks.amount(refund,"amount")).isEqualByComparingTo(originalPaid);
            assertThat(refund.get("destination")).isEqualTo("ORIGINAL_METHOD");
            assertThat(refund.get("status")).isIn("REQUESTED","PROCESSING","COMPLETED");
            if(!"COMPLETED".equals(refund.get("status")))return false;
            assertThat(refund.get("completedAt")).as("actual refund completion time").isNotNull();
            completed.set(refund);return true;
        },new Page.WaitForConditionOptions().setTimeout(30000));

        customer.reload();CustomerDashboardPage.openProfileSettings(customer);
        customer.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customer.locator("[data-testid='customer-history-order'][data-order-id='"+id+"']").click();
        Locator state=customer.locator("[data-testid='order-tracker'][data-order-id='"+id+"'] [data-testid='refund-state']");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(state).containsText("COMPLETED");
        assertThat(OrderMoneyChecks.parseInr(state.innerText())).isEqualByComparingTo(originalPaid);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(state).containsText("Returned to your original payment method");

        java.util.concurrent.atomic.AtomicReference<Map<?,?>> posted=new java.util.concurrent.atomic.AtomicReference<>();
        nextRead[0]=0;
        admin.waitForCondition(()->{
            long now=System.nanoTime();if(now<nextRead[0])return false;nextRead[0]=now+1_000_000_000L;
            Map<?,?> money=(Map<?,?>)read(admin,"/api/v1/internal/admin/orders/"+id+"/money").get("body");
            assertThat(money.get("orderId")).isEqualTo(id);
            assertThat(OrderMoneyChecks.amount(money,"totalAmount")).isEqualByComparingTo(originalPaid);
            if(!"REFUNDED".equals(money.get("paymentStatus")))return false;
            List<?> lines=(List<?>)money.get("ledgerLines");
            if(lines==null||lines.stream().noneMatch(x->"REFUND".equals(((Map<?,?>)x).get("category"))))return false;
            BigDecimal credit=BigDecimal.ZERO,debit=BigDecimal.ZERO;
            for(Object value:lines){
                Map<?,?> line=(Map<?,?>)value;assertThat(line.get("referenceId")).isEqualTo(id);
                assertThat(line.get("ownerType")).as("undelivered order books no payee earnings").isNotIn("DRIVER_PAYABLE","RESTAURANT_PAYABLE");
                if(!"REFUND".equals(line.get("category")))continue;
                assertThat(line.get("transactionId")).isNotNull();
                assertThat(line.get("direction")).isIn("CREDIT","DEBIT");
                if("CREDIT".equals(line.get("direction")))credit=credit.add(OrderMoneyChecks.amount(line,"amount"));
                else debit=debit.add(OrderMoneyChecks.amount(line,"amount"));
            }
            assertThat(credit).as("refund ledger credit").isEqualByComparingTo(originalPaid);
            assertThat(debit).as("refund ledger debit").isEqualByComparingTo(originalPaid);
            List<?> adminRefunds=(List<?>)money.get("refunds");assertThat(adminRefunds).hasSize(1);
            Map<?,?> adminRefund=(Map<?,?>)adminRefunds.get(0);
            assertThat(adminRefund.get("id")).isEqualTo(completed.get().get("id"));
            assertThat(adminRefund.get("status")).isEqualTo("COMPLETED");
            posted.set(money);return true;
        },new Page.WaitForConditionOptions().setTimeout(30000));
        Path output=Path.of("target/lifecycle",id+"-refund-recovery.json");
        Files.writeString(output,(String)admin.evaluate("value=>JSON.stringify(value,null,2)",posted.get()));
        admin.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/admin/orders/"+id+"/money");
        new com.fooddelivery.e2e.pages.admin.AdminOrderMoneyPage(admin).waitForOrderMoney();
    }
}
