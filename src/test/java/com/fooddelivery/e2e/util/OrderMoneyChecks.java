package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminOrderMoneyPage;
import com.microsoft.playwright.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Real read-only admin response, exact-order UI amounts, arithmetic and balanced ledger trace. */
public final class OrderMoneyChecks {
    private OrderMoneyChecks() {}
    public static Map<?,?> verify(Page admin,String id,Map<?,?> created,double completedTripPayout) {
        String path="/api/v1/internal/admin/orders/"+id+"/money";
        var writes=new java.util.concurrent.atomic.AtomicInteger();
        admin.route("**"+path,route -> {
            if(!route.request().method().equals("GET")) {writes.incrementAndGet();route.abort();}
            else route.resume();
        });
        try {
            Response response=admin.waitForResponse(r -> r.request().method().equals("GET")
                    && java.net.URI.create(r.url()).getPath().equals(path),
                    ()->admin.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/admin/orders/"+id+"/money"));
            assertThat(response.status()).isEqualTo(200);
            Map<?,?> money=(Map<?,?>)admin.evaluate("text=>JSON.parse(text)",response.text());
            assertThat(money.get("orderId")).isEqualTo(id);
            assertThat(money.get("paymentMethod")).isEqualTo("CARD");
            assertThat(money.get("paymentStatus")).isEqualTo("SUCCESS");
            for(String field:List.of("totalAmount","foodCost","deliveryFee","customerPlatformFee","sgst","cgst",
                    "restaurantPayout","restaurantPlatformFee","restaurantDeliveryContribution",
                    "driverGrossPayout","driverTaxes","driverNetPayout","platformBonus")) {
                BigDecimal value=amount(money,field);
                if(!field.equals("restaurantPayout"))assertThat(value).as(field).isNotNegative();
            }
            assertThat(amount(money,"totalAmount")).isEqualByComparingTo(amount(created,"totalAmount"));
            BigDecimal tip=amount(created,"tipAmount");
            assertThat(amount(money,"totalAmount")).isEqualByComparingTo(amount(money,"foodCost")
                    .add(amount(money,"deliveryFee")).add(amount(money,"customerPlatformFee"))
                    .add(amount(money,"sgst")).add(amount(money,"cgst")).add(tip));
            assertThat(amount(money,"restaurantPayout")).isEqualByComparingTo(amount(money,"foodCost")
                    .subtract(amount(money,"restaurantPlatformFee")).subtract(amount(money,"restaurantDeliveryContribution"))
                    .subtract(amount(money,"platformBonus")));
            assertThat(amount(money,"driverNetPayout")).isEqualByComparingTo(amount(money,"driverGrossPayout")
                    .subtract(amount(money,"driverTaxes")));
            assertThat(BigDecimal.valueOf(completedTripPayout)).isEqualByComparingTo(amount(money,"driverNetPayout").add(tip));
            var page=new AdminOrderMoneyPage(admin);page.waitForOrderMoney();
            assertThat(parseInr(page.getCustomerTotal())).isEqualByComparingTo(amount(money,"totalAmount"));
            assertThat(parseInr(page.getRestaurantNetPayout())).isEqualByComparingTo(amount(money,"restaurantPayout"));
            assertThat(parseInr(page.getRiderNetPayout())).isEqualByComparingTo(amount(money,"driverNetPayout"));
            List<?> lines=(List<?>)money.get("ledgerLines");assertThat(lines).as("the exact delivered order has a durable ledger trace").isNotEmpty();
            for (String payee : List.of("RESTAURANT_PAYABLE", "DRIVER_PAYABLE")) {
                var payeeLines = lines.stream().map(x -> (Map<?,?>) x).filter(line -> payee.equals(line.get("ownerType"))).toList();
                assertThat(payeeLines)
                        .as("delivered order must post %s earnings, not only its payment capture", payee).isNotEmpty();
                BigDecimal postedPayout = BigDecimal.ZERO;
                for (Map<?,?> line : payeeLines) {
                    assertThat(line.get("direction")).isIn("CREDIT", "DEBIT");
                    BigDecimal value = amount(line, "amount");
                    postedPayout = postedPayout.add("CREDIT".equals(line.get("direction")) ? value : value.negate());
                }
                BigDecimal expectedPayout = payee.equals("RESTAURANT_PAYABLE")
                        ? amount(money, "restaurantPayout") : amount(money, "driverNetPayout").add(tip);
                assertThat(postedPayout).as("posted %s payout agrees with the exact order", payee)
                        .isEqualByComparingTo(expectedPayout);
            }
            BigDecimal credits=BigDecimal.ZERO,debits=BigDecimal.ZERO;
            for(Object value:lines) {
                Map<?,?> line=(Map<?,?>)value;
                assertThat(line.get("transactionId")).isNotNull();
                assertThat(line.get("referenceId")).isEqualTo(id);
                assertThat(line.get("accountId")).isNotNull();
                assertThat(line.get("createdAt")).isNotNull();
                assertThat(line.get("ownerType")).isNotNull();assertThat(line.get("ownerId")).isNotNull();
                assertThat(line.get("category")).isNotNull();
                String direction=line.get("direction").toString();assertThat(direction).isIn("CREDIT","DEBIT");
                if(direction.equals("CREDIT"))credits=credits.add(amount(line,"amount"));else debits=debits.add(amount(line,"amount"));
            }
            assertThat(credits).as("ledger credits are positive").isPositive();
            assertThat(credits).as("debits and credits balance for the exact order").isEqualByComparingTo(debits);
            assertThat(writes.get()).isZero();
            Path evidence=Path.of("target/lifecycle",id+"-money.json");
            Files.writeString(evidence,(String)admin.evaluate("data=>JSON.stringify(data,null,2)",money));
            return money;
        } catch(java.io.IOException failure) {throw new AssertionError("Cannot retain order money evidence",failure);}
        finally {admin.unroute("**"+path);}
    }
    public static BigDecimal amount(Map<?,?> data,String field) {
        Object value=data.get(field);assertThat(value).as("authoritative money field %s",field).isNotNull();
        return new BigDecimal(value.toString());
    }
    public static BigDecimal parseInr(String text) {
        var match=java.util.regex.Pattern.compile("(-?)₹\\s*([0-9,]+(?:\\.[0-9]{1,2})?)").matcher(text);
        assertThat(match.find()).as("INR amount is present: %s",text).isTrue();
        return new BigDecimal(match.group(1)+match.group(2).replace(",",""));
    }
}
