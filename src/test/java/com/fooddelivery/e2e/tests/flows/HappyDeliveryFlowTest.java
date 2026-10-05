package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import com.fooddelivery.e2e.pages.delivery.*;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.fooddelivery.e2e.util.SeededRiderDuty;
import com.fooddelivery.e2e.util.CheckoutAvailability;
import com.fooddelivery.e2e.util.AutomaticCustomerProgress;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Full happy-path E2E flow: Customer places order → Restaurant accepts/cooks/prepares →
 * Rider accepts dispatch, picks up with OTP, delivers with customer OTP → Order marked delivered.
 * <p>
 * This is the most critical test — it exercises the complete 3-actor order lifecycle
 * across 3 separate browser contexts using the deployed polling and live-update paths.
 * </p>
 */
@Tag("flow")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HappyDeliveryFlowTest extends TestBase {

    private SeededRiderDuty duty;
    private com.fooddelivery.e2e.util.OrderChatChecks chatChecks;
    // User explicitly parked SSE. Enable only for a later dedicated tracking run.
    private static final boolean SSE_ENABLED=Boolean.getBoolean("e2e.sse.enabled");
    private String expectedOrderId;
    private AutomaticCustomerProgress customerProgress;
    private String streamStage = "login";
    private final java.util.List<Map<String, Object>> customerMapStreams = new java.util.ArrayList<>();
    private final java.util.List<Map<String, Object>> riderTelemetry = new java.util.ArrayList<>();
    private java.util.List<Map<String, Object>> customerMapPoints = java.util.List.of();
    private final java.util.List<Map<String, Object>> streamResponses = new java.util.ArrayList<>();
    private final Map<com.microsoft.playwright.Request, String> streamRequestStages = new java.util.IdentityHashMap<>();

    @BeforeEach
    void preventAcceptanceOfUnrelatedRetainedOrders() {
        // Inspect only sanitized metadata from the UI's existing stream; no extra request or raw body retention.
        if (Boolean.getBoolean("resume.verify.customer.map")) customerPage.addInitScript("""
            (() => {
              const original = window.fetch;
              window.__mapWire = [];
              window.fetch = async (...args) => {
                const response = await original(...args);
                const url = typeof args[0] === 'string' ? args[0] : args[0]?.url;
                if (url && new URL(url, location.href).pathname.endsWith('/live-tracking') && response.body) {
                  const row = { chunks: 0, bytes: 0, events: [] };
                  window.__mapWire.push(row);
                  const reader = response.clone().body.getReader();
                  void (async () => {
                    let pending = '';
                    const decoder = new TextDecoder();
                    try {
                      while (row.events.length < 8) {
                        const chunk = await reader.read();
                        if (chunk.done) { row.ended = true; break; }
                        row.chunks++; row.bytes += chunk.value.length;
                        pending += decoder.decode(chunk.value, {stream: true});
                        pending = pending.replaceAll('\\r\\n', '\\n');
                        let boundary;
                        while ((boundary = pending.indexOf('\\n\\n')) >= 0) {
                          const frame = pending.slice(0, boundary); pending = pending.slice(boundary + 2);
                          const lines = frame.split('\\n');
                          const payload = lines.filter(line => line.startsWith('data:')).map(line => line.slice(5).trimStart()).join('\\n');
                          const item = { event: lines.find(line => line.startsWith('event:'))?.slice(6).trim() || 'comment' };
                          if (payload) {
                            try {
                              const data = JSON.parse(payload);
                              item.kind = typeof data;
                              if (data && typeof data === 'object') {
                                item.keys = Object.keys(data);
                                if (Number.isFinite(data.lat)) item.lat = data.lat;
                                if (Number.isFinite(data.lng)) item.lng = data.lng;
                              }
                            } catch { item.kind = 'invalid-json'; }
                          }
                          row.events.push(item);
                        }
                      }
                    } catch { row.readError = true; }
                    finally { void reader.cancel().catch(() => {}); }
                  })();
                }
                return response;
              };
            })();
            """);
        // Observe responses only. Never persist request headers, tokens, OTPs or response bodies.
        customerPage.onResponse(response -> {
            String path = java.net.URI.create(response.url()).getPath();
            if (path != null && path.endsWith("/live-tracking")) {
                customerMapStreams.add(Map.of("path", path,
                        "status", response.status(), "contentType", response.headerValue("content-type") == null ? "" : response.headerValue("content-type")));
            }
        });
        riderPage.onWebSocket(socket -> socket.onFrameSent(frame -> {
            if (frame.text() == null) return;
            String text = frame.text();
            Matcher lat = Pattern.compile("\"lat\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(text);
            Matcher lng = Pattern.compile("\"lng\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(text);
            if (!lat.find() || !lng.find()) return;
            java.util.Map<String, Object> fix = new java.util.HashMap<>();
            fix.put("lat", Double.parseDouble(lat.group(1))); fix.put("lng", Double.parseDouble(lng.group(1)));
            for (String name : java.util.List.of("driverId", "orderId", "cityId")) {
                Matcher value = Pattern.compile(Pattern.quote("\"" + name + "\"") + "\\s*:\\s*\"([^\"]*)\"").matcher(text);
                fix.put(name, value.find() ? value.group(1) : "");
            }
            riderTelemetry.add(fix);
        }));
        riderPage.onRequest(request -> {
            if (request.url().contains("/restaurant-status-stream")) streamRequestStages.put(request, streamStage);
        });
        riderPage.onResponse(response -> {
            if (!response.url().contains("/restaurant-status-stream")) return;
            String classification = "not-read";
            if (response.status() == 403) {
                try {
                    String body = response.text();
                    if (body.contains("Access Denied: You do not have permission to access this resource.")) {
                        classification = "application-access-denied";
                    } else if (body.contains("This order is not assigned to you.")) {
                        classification = "application-order-assignment-denied";
                    } else if (body.contains("Just a moment...") || body.contains("cf-chl-")) {
                        classification = "cloudflare-challenge";
                    } else if (body.contains("Forbidden") && body.contains("timestamp")) {
                        classification = "spring-forbidden";
                    } else { classification = "unclassified-forbidden"; }
                } catch (com.microsoft.playwright.PlaywrightException ignored) { classification = "body-unavailable"; }
            }
            streamResponses.add(Map.of("path", java.net.URI.create(response.url()).getPath(),
                    "status", response.status(), "requestStage", streamRequestStages.getOrDefault(response.request(), "unknown"),
                    "responseStage", streamStage, "classification", classification));
        });
        // Before accepting, the visible order contract and scoped Accept control must match the
        // retained order ID. No synthetic response or request interception is used for setup.
    }

    @AfterEach
    void finishIdleRiderDuty() {
        try {
            if (expectedOrderId != null) {
                Path evidence = Path.of("target/lifecycle", expectedOrderId + "-stream.json");
                Files.createDirectories(evidence.getParent());
                Files.writeString(Path.of("target/lifecycle", expectedOrderId + "-customer-map.json"),
                        (String) customerPage.evaluate("data => JSON.stringify(data,null,2)", Map.of(
                                "streams", customerMapStreams, "points", customerMapPoints, "telemetry", riderTelemetry,
                                "wire", customerPage.evaluate("() => window.__mapWire || []"))));
                Files.writeString(evidence, (String) riderPage.evaluate("data => JSON.stringify(data,null,2)", streamResponses));
                if (customerProgress != null) Files.writeString(Path.of("target/lifecycle", expectedOrderId + "-progress.json"),
                        (String) customerPage.evaluate("data => JSON.stringify(data,null,2)", customerProgress.observations()));
            }
        } catch (java.io.IOException failure) { throw new AssertionError("Cannot retain sanitized stream evidence", failure); }
        finally {
            if(chatChecks!=null) {
                try {
                    if(expectedOrderId!=null) Files.writeString(Path.of("target/lifecycle",expectedOrderId+"-chat.json"),
                            (String)customerPage.evaluate("data=>JSON.stringify(data,null,2)",chatChecks.evidence()));
                } catch(java.io.IOException failure) { throw new AssertionError("Cannot retain chat evidence",failure); }
                finally {chatChecks.close();}
            }
            if(duty!=null) duty.close();
        }
    }

    private static double parseInr(String text) {
        Matcher amount = Pattern.compile("₹\\s*([0-9,]+(?:\\.[0-9]{1,2})?)").matcher(text);
        org.assertj.core.api.Assertions.assertThat(amount.find())
                .as("INR amount in: %s", text).isTrue();
        return Double.parseDouble(amount.group(1).replace(",", ""));
    }

    private static String valueAfterLabel(String text, String label) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length - 1; i++) {
            if (!lines[i].trim().equalsIgnoreCase(label)) continue;
            for (int next = i + 1; next < lines.length; next++) {
                String value = lines[next].trim();
                if (!value.isEmpty()) return value;
            }
        }
        throw new AssertionError("No value after " + label + " in dispatch: " + text);
    }

    private void resumeAssignedOrder(String orderId, String outletName) {
        String shortOrderId = orderId.substring(0, Math.min(8, orderId.length()));
        Locator activeHeading = riderPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Active Contract").setExact(true));
        Locator dispatchOffer = riderPage.getByRole(AriaRole.ALERT)
                .filter(new Locator.FilterOptions().setHasText("New Dispatch"))
                .filter(new Locator.FilterOptions().setHasText(outletName));
        riderPage.waitForCondition(() -> activeHeading.isVisible() || dispatchOffer.isVisible(),
                new Page.WaitForConditionOptions().setTimeout(60000));
        if (!riderPage.getByText("Active Contract",
                new Page.GetByTextOptions().setExact(true)).isVisible()) {
            // A failed dispatch attempt can leave this test's prepared order unassigned.
            // Recover it through the same visible popup, without creating another order.
            DispatchPingPage ping = new DispatchPingPage(riderPage);
            ping.waitForPing(outletName);
            assertThat(riderPage.getByText("ORDER CONTRACT #" + shortOrderId,
                    new Page.GetByTextOptions().setExact(true))).isVisible();
            riderPage.bringToFront();
            ping.acceptDispatch(orderId);
            waitForActiveOrder(orderId);
        }
        waitForActiveOrder(orderId);
        Locator todayEarnings = riderPage.getByText(Pattern.compile("^(?:Today’s earnings|Paid today)$",Pattern.CASE_INSENSITIVE),
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("span").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^₹")));
        double earningsBefore = parseInr(todayEarnings.innerText());

        assertThat(riderPage.getByText("#" + orderId,
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByText("RESTAURANT ADDRESS",
                new Page.GetByTextOptions().setExact(true)).locator("..")).containsText(outletName);

        if(SSE_ENABLED) riderPage.waitForCondition(() -> streamResponses.stream().anyMatch(event ->
                        ((String) event.get("path")).endsWith("/orders/" + orderId + "/restaurant-status-stream")
                                && Integer.valueOf(200).equals(event.get("status"))),
                new Page.WaitForConditionOptions().setTimeout(15000));
        DeliveryActiveJobPage activeJob = new DeliveryActiveJobPage(riderPage);
        if (!riderPage.getByPlaceholder("Ask customer for 6-digit OTP").isVisible()) {
            RestaurantDashboardPage restaurantDashboard = new RestaurantDashboardPage(restaurantPage);
            restaurantDashboard.selectOutlet(outletName);
            restaurantDashboard.openOrdersTab();
            RestaurantOrderActionsPage orderActions = new RestaurantOrderActionsPage(restaurantPage);
            Locator card = orderActions.orderCard(orderId);
            assertThat(card).hasCount(1);
            String preparationStatus = card.getAttribute("data-status");
            org.assertj.core.api.Assertions.assertThat(preparationStatus)
                    .as("Only this assigned retained order may advance preparation")
                    .isIn("ACCEPTED", "PREPARING", "READY_FOR_PICKUP");
            boolean preparedHere = !"READY_FOR_PICKUP".equals(preparationStatus);
            if (preparedHere) riderPage.bringToFront();
            if ("ACCEPTED".equals(preparationStatus)) {
                card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Start cooking").setExact(true)).click();
                assertThat(card).hasAttribute("data-status", "PREPARING");
                preparationStatus = "PREPARING";
            }
            if ("PREPARING".equals(preparationStatus)) {
                card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Mark ready").setExact(true)).click();
                assertThat(card).hasAttribute("data-status", "READY_FOR_PICKUP");
            }
            // The ready toast arrives only over the restaurant-status SSE stream (parked through the
            // quick tunnel). Without it, prove the server-backed state the way the main path does.
            if (preparedHere && SSE_ENABLED) {
                assertThat(riderPage.getByText("Order " + shortOrderId + " is now ready for pickup!",
                        new Page.GetByTextOptions().setExact(true))).isVisible();
            } else if (preparedHere) {
                riderPage.reload();
                waitForActiveOrder(orderId);
            }
            assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isVisible();
            String pickupOtp = orderActions.getPickupOtp(shortOrderId);
            org.assertj.core.api.Assertions.assertThat(pickupOtp).matches("[0-9]{6}");

            Locator arrived = riderPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Mark Arrived at Restaurant").setExact(true));
            if (arrived.isVisible()) confirmStatus(orderId, "AT_RESTAURANT", activeJob::markArrivedAtRestaurant);
            activeJob.enterPickupOtp(pickupOtp);
            confirmStatus(orderId, "OUT_FOR_DELIVERY", activeJob::swipeToConfirmPickup);
        }
        assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();

        customerPage.reload();
        // Login/reload can restore a different active order. Choose the owned one through the UI
        // before reading its code; never take the first OTP from another order.
        customerPage.getByTestId("order-tracker").first().waitFor();
        Locator ownedCustomerTracker = customerPage.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "']");
        if (!ownedCustomerTracker.isVisible()) {
            customerPage.setViewportSize(1100, 900);
            customerPage.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Which order to track").setExact(true)).click();
            customerPage.getByRole(AriaRole.OPTION).filter(new Locator.FilterOptions().setHasText(outletName)).click();
        }
        assertThat(ownedCustomerTracker).isVisible();
        if (Boolean.getBoolean("resume.verify.customer.map")) {
            CustomerOrderTrackerPage details = new CustomerOrderTrackerPage(customerPage, orderId);
            org.assertj.core.api.Assertions.assertThat(parseInr(details.getTotalPaid())).isPositive();
            org.assertj.core.api.Assertions.assertThat(details.getPaymentMethod()).isEqualTo("Paid via CARD");
            customerMapPoints = com.fooddelivery.e2e.util.LiveCustomerMap.assertMovement(customerPage, riderPage,
                    orderId, testRiderPhone, TestConfig.GEO_LAT, TestConfig.GEO_LNG);
            org.assertj.core.api.Assertions.assertThat(customerMapPoints).hasSize(2);
            java.util.List<Map<String, Object>> mapStreams = customerMapStreams.stream()
                    .filter(event -> ((String) event.get("path")).endsWith("/orders/" + orderId + "/live-tracking")).toList();
            org.assertj.core.api.Assertions.assertThat(mapStreams).isNotEmpty();
            for (Map<String, Object> stream : mapStreams) {
                org.assertj.core.api.Assertions.assertThat(stream.get("status")).isEqualTo(200);
                org.assertj.core.api.Assertions.assertThat((String) stream.get("contentType")).startsWith("text/event-stream");
            }
        }
        String deliveryOtp = ownedCustomerTracker.getByTestId("delivery-code").innerText().trim();
        org.assertj.core.api.Assertions.assertThat(deliveryOtp).matches("[0-9]{6}");
        activeJob.enterDeliveryOtp(deliveryOtp);
        confirmDelivery(activeJob, orderId);

        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Online Duty$")))).isVisible();
        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator completedTrip = riderPage.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("ORDER #" + shortOrderId))
                .filter(new Locator.FilterOptions().setHasText("Delivered"));
        completedTrip.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        assertThat(completedTrip).containsText(outletName);
        double recordedPayout = parseInr(completedTrip.innerText());
        org.assertj.core.api.Assertions.assertThat(recordedPayout).isPositive();
        riderPage.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Completed Deliveries").setExact(true))
                .locator("..").getByRole(AriaRole.BUTTON).click();
        riderPage.waitForCondition(() -> parseInr(todayEarnings.innerText()) >= earningsBefore + recordedPayout,
                new Page.WaitForConditionOptions().setTimeout(30000));
        Locator receipt = customerPage.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "']")
                .filter(new Locator.FilterOptions().setHas(customerPage.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Order delivered").setExact(true))));
        receipt.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(90000));
        assertThat(receipt).containsText("Delivered from " + outletName);
        chatChecks.afterDelivery(orderId,outletName);
        chatChecks.assertDeliveredPassed();
        if (SSE_ENABLED) {
        java.util.List<Map<String, Object>> ownedStreams = streamResponses.stream()
                .filter(event -> ((String) event.get("path")).endsWith("/orders/" + orderId + "/restaurant-status-stream"))
                .toList();
        org.assertj.core.api.Assertions.assertThat(ownedStreams).isNotEmpty();
        for (Map<String, Object> event : ownedStreams) org.assertj.core.api.Assertions.assertThat(event.get("status"))
                .as("resumed order's authorised stream %s", event).isEqualTo(200);
        }
    }

    /** Continue financial/quote checks on this audit's delivered manifest without an order POST. */
    private void verifyRetainedDeliveredOrder(String id) {
        var orderWrites=new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<com.microsoft.playwright.Request> observeWrites=request->{
            if(request.method().equals("POST") && java.net.URI.create(request.url()).getPath().equals("/api/v1/orders"))
                orderWrites.incrementAndGet();
        };
        customerPage.onRequest(observeWrites);
        try {
            Map<?,?> manifest=(Map<?,?>)customerPage.evaluate("text=>JSON.parse(text)",
                    Files.readString(Path.of("target/lifecycle",id+".json")));
            org.assertj.core.api.Assertions.assertThat(manifest.get("orderId")).isEqualTo(id);
            org.assertj.core.api.Assertions.assertThat(manifest.get("customerPhone")).isEqualTo(testCustomerPhone);
            org.assertj.core.api.Assertions.assertThat(manifest.get("riderPhone")).isEqualTo(testRiderPhone);
            org.assertj.core.api.Assertions.assertThat(manifest.get("restaurantPhone")).isEqualTo(testRestaurantPhone);
            CustomerDashboardPage.openProfileSettings(customerPage);
            customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("History").setExact(true)).click();
            Locator historyCard=customerPage.locator("[data-testid='customer-history-order'][data-order-id='"+id+"']");
            assertThat(historyCard).isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(30000));
            assertThat(historyCard).containsText(Pattern.compile("Delivered",Pattern.CASE_INSENSITIVE));
            historyCard.click();
            var tracker=new CustomerOrderTrackerPage(customerPage,id);
            assertThat(tracker.tracker()).isVisible();
            assertThat(tracker.tracker().getByRole(AriaRole.HEADING,new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))).isVisible();
            org.assertj.core.api.Assertions.assertThat(tracker.getPaymentMethod()).isEqualTo("Paid via CARD");
            var total=com.fooddelivery.e2e.util.OrderMoneyChecks.parseInr(tracker.getTotalPaid());
            var items=receiptAmount(tracker.tracker(),"Items",false);
            var gst=receiptAmount(tracker.tracker(),"GST",true);
            var tip=receiptAmount(tracker.tracker(),"Rider tip",true);
            Map<?,?> order=Map.of("totalAmount",total.toPlainString(),"tipAmount",tip.toPlainString());
            if(!Boolean.getBoolean("resume.delivered.quote.only")) {
                riderPage.navigate(TestConfig.APP_URL);
                new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);
                new DeliveryDashboardPage(riderPage).waitForDashboard();
                riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
                Locator trip=riderPage.getByRole(AriaRole.BUTTON)
                        .filter(new Locator.FilterOptions().setHasText("ORDER #"+id.substring(0,8)))
                        .filter(new Locator.FilterOptions().setHasText("Delivered"));
                assertThat(trip).isVisible();
                double payout=parseInr(trip.innerText());
                loginAsAdmin();
                com.fooddelivery.e2e.util.OrderMoneyChecks.verify(adminPage,id,order,payout);
            }
            com.fooddelivery.e2e.util.RefundQuoteChecks.verify(customerPage,id,items.add(gst));
            org.assertj.core.api.Assertions.assertThat(orderWrites.get()).as("retained checks create no order").isZero();
            Files.writeString(Path.of("target/lifecycle",id+"-retained-checks.json"),
                    "{\"orderId\":\""+id+"\",\"newOrderCreated\":false,\"quotePassed\":true,\"moneyChecked\":"
                    +!Boolean.getBoolean("resume.delivered.quote.only")+"}");
        } catch(java.io.IOException failure){throw new AssertionError("Cannot validate owned retained manifest",failure);}
        finally {customerPage.offRequest(observeWrites);}
    }

    private static java.math.BigDecimal receiptAmount(Locator receipt,String label,boolean optional) {
        Locator value=receipt.getByText(label,new Locator.GetByTextOptions().setExact(true));
        if(optional && value.count()==0)return java.math.BigDecimal.ZERO;
        assertThat(value).hasCount(1);
        return com.fooddelivery.e2e.util.OrderMoneyChecks.parseInr(value.locator("..").locator("..").innerText());
    }

    @Test
    @Order(1)
    @DisplayName("Complete order lifecycle: Customer → Restaurant → Rider → Delivered")
    void completeOrderLifecycle() {
        // ── Step 1: Login all 3 actors ────────────────────────────────────
        System.out.println("═══ STEP 1: Logging in all actors ═══");
        System.out.printf("Customer=%s Restaurant=%s Rider=%s%n",
                testCustomerPhone, testRestaurantPhone, testRiderPhone);

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        String retainedDelivered = System.getProperty("resume.delivered.order.id", "").trim();
        if (!retainedDelivered.isEmpty()) {
            verifyRetainedDeliveredOrder(retainedDelivered);
            return;
        }

        chatChecks=new com.fooddelivery.e2e.util.OrderChatChecks(customerPage,restaurantPage,riderPage);
        restaurantPage.navigate(TestConfig.APP_URL);
        String restaurantPhone = testRestaurantPhone;
        new LoginPage(restaurantPage).login(restaurantPhone).openPortal(Portal.RESTAURANT);
        RestaurantDashboardPage restaurantDashboard = new RestaurantDashboardPage(restaurantPage);
        restaurantDashboard.waitForDashboard();

        String resumeOrderId = System.getProperty("resume.order.id", "").trim();
        if (resumeOrderId.isEmpty()) {
            // Install status/socket observers before login; already ONLINE is not cycled.
            duty = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone);
        } else {
            // An explicit resume keeps an assigned rider ON_DELIVERY; ordinary setup must
            // not abort that intentional recovery or attempt to toggle the duty state.
            riderPage.navigate(TestConfig.APP_URL);
            new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);
            new DeliveryDashboardPage(riderPage).waitForDashboard();
        }

        if (!resumeOrderId.isEmpty()) {
            String resumeOutlet = System.getProperty("resume.outlet", "").trim();
            org.assertj.core.api.Assertions.assertThat(resumeOutlet)
                    .as("-Dresume.outlet is required with -Dresume.order.id").isNotBlank();
            expectedOrderId = resumeOrderId;
            resumeAssignedOrder(resumeOrderId, resumeOutlet);
            return;
        }

        // ── Step 2: Rider goes online ─────────────────────────────────────
        System.out.println("═══ STEP 2: Rider going online ═══");
        DeliveryOnlineTogglePage toggle = new DeliveryOnlineTogglePage(riderPage);
        assertThat(toggle.isOnline()).isTrue();
        Locator todayEarnings = riderPage.getByText(Pattern.compile("^(?:Today’s earnings|Paid today)$",Pattern.CASE_INSENSITIVE),
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("span").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^₹")));
        assertThat(todayEarnings).hasText(Pattern.compile("^₹[0-9,]+(?:\\.[0-9]{2})?$"));
        double earningsBefore = parseInr(todayEarnings.innerText());

        String selectedOutlet;
        Map<?, ?> order;
        String orderId;
        String pendingOrder = System.getProperty("resume.pending.order.id", "").trim();
        if (!pendingOrder.isEmpty()) {
            // Resume this run's retained unaccepted order after an observer/assertion failure.
            // This branch sends no order POST and must never resurrect a terminal order.
            Map<?, ?> manifest;
            try {
                manifest = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)",
                        Files.readString(Path.of("target/lifecycle", pendingOrder + ".json")));
            } catch (java.io.IOException failure) { throw new AssertionError("Retained manifest missing", failure); }
            org.assertj.core.api.Assertions.assertThat(manifest.get("customerPhone")).isEqualTo(testCustomerPhone);
            org.assertj.core.api.Assertions.assertThat(manifest.get("restaurantPhone")).isEqualTo(testRestaurantPhone);
            selectedOutlet = (String) manifest.get("outlet"); orderId = pendingOrder; expectedOrderId = orderId;
            Response active = customerPage.waitForResponse(response -> response.request().method().equals("GET")
                    && "/api/v1/orders/active".equals(java.net.URI.create(response.url()).getPath()), customerPage::reload);
            org.assertj.core.api.Assertions.assertThat(active.status()).isEqualTo(200);
            Map<?, ?> envelope = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", active.text());
            java.util.List<?> rows = (java.util.List<?>) ((Map<?, ?>) envelope.get("data")).get("content");
            order = rows.stream().map(row -> (Map<?, ?>) row).filter(row -> pendingOrder.equals(row.get("id")))
                    .findFirst().orElseThrow(() -> new AssertionError("Retained pending order is no longer active"));
            org.assertj.core.api.Assertions.assertThat(order.get("status")).isEqualTo("PENDING_ACCEPTANCE");
            org.assertj.core.api.Assertions.assertThat(order.get("paymentMethod")).isEqualTo("CARD");
            System.out.println("Resuming retained pending order " + orderId);
        } else {
        // ── Step 3: Customer selects address and opens restaurant ─────────
        System.out.println("═══ STEP 3: Customer selecting address and restaurant ═══");

        // Use the seeded restaurant's brand and an outlet that Home can reach.
        int brandNum = Integer.parseInt(restaurantPhone.substring(7));
        selectedOutlet = new NearbyOutletPage(customerPage).openBrandAndSelectNearby("Brand " + brandNum);

        // ── Step 4: Customer adds item and places order ───────────────────
        System.out.println("═══ STEP 4: Customer adding item and placing order ═══");
        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();

        // The menu helper can choose another orderable sibling; bind all actors to the
        // outlet actually on screen rather than the originally opened one.
        selectedOutlet = menu.getSelectedOutletName();
        restaurantDashboard.selectOutlet(selectedOutlet);
        assertThat(restaurantPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Active").setExact(true))).hasAttribute("aria-pressed", "true");
        menu.clickViewCart();
        new CustomerCartDrawerPage(customerPage).waitForCartOpen();
        duty.assertReadyForCheckout();
        CheckoutAvailability.requireDeliveryAvailable(CheckoutAvailability.clickCheckoutAndWaitForAvailability(customerPage));
        PaymentModalPage payment = new PaymentModalPage(customerPage);payment.waitForOpen();payment.waitForFinalQuote();
        Response created = customerPage.waitForResponse(r -> r.request().method().equals("POST")
                && java.net.URI.create(r.url()).getPath().equals("/api/v1/orders"),
                () -> payment.placeOrder("Credit or debit card"));
        org.assertj.core.api.Assertions.assertThat(created.status()).isBetween(200,299);
        Map<?,?> envelope = (Map<?,?>) customerPage.evaluate("text => JSON.parse(text)", created.text());
        org.assertj.core.api.Assertions.assertThat(envelope.get("success")).isEqualTo(true);
        order = (Map<?,?>) envelope.get("data");
        orderId = (String) order.get("id");
        expectedOrderId = orderId;
        org.assertj.core.api.Assertions.assertThat(orderId).matches("[0-9a-fA-F-]{36}");
        try {
            Path manifest = Path.of("target/lifecycle",orderId + ".json");Files.createDirectories(manifest.getParent());
            Files.writeString(manifest,(String) customerPage.evaluate("data => JSON.stringify(data,null,2)", Map.of(
                    "orderId",orderId,"customerPhone",testCustomerPhone,"restaurantPhone",testRestaurantPhone,
                    "riderPhone",testRiderPhone,"outlet",selectedOutlet,"dataPolicy","retain","cleanupPerformed",false)));
        } catch (java.io.IOException failure) { throw new AssertionError("Cannot retain lifecycle order manifest",failure); }
        org.assertj.core.api.Assertions.assertThat(order.get("paymentMethod")).isEqualTo("CARD");
        }
        Locator exactTracker = customerPage.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "']");
        assertThat(exactTracker).isVisible();
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage, orderId);
        org.assertj.core.api.Assertions.assertThat(tracker.getOrderId()).isEqualTo(orderId);
        String shortOrderId = orderId.substring(0, Math.min(8, orderId.length()));
        System.out.println("Tracking Order ID: " + orderId);
        System.out.println("Short Order ID: " + shortOrderId);

        // ── Step 5: Restaurant accepts the order ──────────────────────────
        System.out.println("═══ STEP 5: Restaurant accepting order ═══");
        restaurantDashboard.selectOutlet(selectedOutlet);
        restaurantDashboard.openOrdersTab();
        RestaurantOrderActionsPage orderActions = new RestaurantOrderActionsPage(restaurantPage);
        com.fooddelivery.e2e.util.RestaurantAcceptanceChecks.beforeAccept(restaurantPage,orderId,order);
        Response accepted=restaurantPage.waitForResponse(r -> r.request().method().equals("POST")
                && r.url().endsWith("/orders/"+orderId+"/accept"),()->orderActions.acceptOrder(orderId));
        org.assertj.core.api.Assertions.assertThat(accepted.status()).isEqualTo(200);

        // Accept the short-lived dispatch before waiting for customer polling or showing OTPs.
        // Short-prep dispatch starts at acceptance, independently of the kitchen's Ready command.
        System.out.println("═══ STEP 8: Rider accepting dispatch ═══");
        DispatchPingPage ping = new DispatchPingPage(riderPage);
        ping.waitForPing(selectedOutlet);
        Locator dispatch = riderPage.getByRole(AriaRole.ALERT)
                .filter(new Locator.FilterOptions().setHasText("New Dispatch"))
                .filter(new Locator.FilterOptions().setHasText(selectedOutlet));
        // Take one text snapshot, then accept immediately. Chaining exact child locators here once
        // consumed the complete server-side 60-second window before the click reached the API.
        String dispatchText = dispatch.innerText();
        String countdownLabel = dispatch.getByRole(AriaRole.TIMER).getAttribute("aria-label");
        String pickupText = valueAfterLabel(dispatchText, "Pickup");
        String deliveryAddress = valueAfterLabel(dispatchText, "Dropoff");
        int declineActions = dispatch.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Decline").setExact(true)).count();
        int acceptActions = dispatch.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Accept Order").setExact(true)).count();
        // Validate identity before mutating anything: another unfinished order can be offered
        // first in a shared seeded environment. The popup exposes its outlet, the board its ID.
        org.assertj.core.api.Assertions.assertThat(pickupText).contains(selectedOutlet);
        assertThat(riderPage.getByText("ORDER CONTRACT #" + shortOrderId,
                new Page.GetByTextOptions().setExact(true))).isVisible();
        riderPage.bringToFront();
        streamStage = "accepting-dispatch";
        ping.acceptDispatch(orderId);
        streamStage = "dispatch-accepted";

        org.assertj.core.api.Assertions.assertThat(dispatchText).containsIgnoringCase("New Dispatch");
        org.assertj.core.api.Assertions.assertThat(countdownLabel)
                .matches("^[0-9]+ seconds left to accept$");
        org.assertj.core.api.Assertions.assertThat(pickupText).contains(selectedOutlet);
        org.assertj.core.api.Assertions.assertThat(deliveryAddress).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(declineActions).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(acceptActions).isEqualTo(1);
        double dispatchFee = parseInr(dispatchText);
        org.assertj.core.api.Assertions.assertThat(dispatchFee)
                .as("gross delivery fee shown in the dispatch offer").isPositive();

        waitForActiveOrder(orderId);
        Locator activeContract = riderPage.getByText("Active Contract",
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("..");
        assertThat(activeContract).containsText(orderId);
        Locator restaurantAddress = riderPage.getByText("RESTAURANT ADDRESS",
                new Page.GetByTextOptions().setExact(true)).locator("..");
        Locator customerAddress = riderPage.getByText("DELIVERY ADDRESS",
                new Page.GetByTextOptions().setExact(true)).locator("..");
        assertThat(restaurantAddress).containsText(selectedOutlet);
        assertThat(customerAddress).containsText(deliveryAddress);
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Mark Arrived at Restaurant").setExact(true))).isVisible();

        // Prove the first authorised subscription succeeds before navigation can abort it.
        // A later successful reload subscription alone does not cover the acceptance race.
        if(SSE_ENABLED) riderPage.waitForCondition(() -> streamResponses.stream().anyMatch(event ->
                        ((String) event.get("path")).endsWith("/orders/" + orderId + "/restaurant-status-stream")
                                && Integer.valueOf(200).equals(event.get("status"))),
                new Page.WaitForConditionOptions().setTimeout(15000));

        customerProgress = new AutomaticCustomerProgress(customerPage, orderId);
        customerProgress.orderStatus("ACCEPTED");
        customerProgress.deliveryLabel("Courier assigned");
        assertThat(orderActions.orderCard(orderId)).hasAttribute("data-status", "ACCEPTED");
        chatChecks.afterDispatch(orderId,selectedOutlet);
        com.fooddelivery.e2e.util.RestaurantAcceptanceChecks.afterAcceptReload(restaurantPage,orderId,selectedOutlet);
        assertThat(new CustomerOrderTrackerPage(customerPage,orderId).tracker().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel order").setExact(true))).isHidden();
        System.out.println("═══ STEP 6: Restaurant starting cook ═══");
        orderActions.startCooking(shortOrderId);
        assertThat(orderActions.orderCard(orderId)).hasAttribute("data-status", "PREPARING");
        customerProgress.orderStatus("PREPARING");
        System.out.println("═══ STEP 7: Restaurant marking prepared ═══");
        orderActions.markPrepared(shortOrderId);
        assertThat(orderActions.orderCard(orderId)).hasAttribute("data-status", "READY_FOR_PICKUP");
        customerProgress.orderStatus("READY_FOR_PICKUP");

        // An accepted assignment must survive a page reload without resetting or duplicating it.
        streamStage = "pickup-reload";
        riderPage.reload();
        streamStage = "pickup-restored";
        waitForActiveOrder(orderId);
        assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isVisible();

        System.out.println("═══ STEP 9: Getting pickup OTP ═══");
        String pickupOtp = orderActions.getPickupOtp(shortOrderId);
        assertThat(pickupOtp).matches("[0-9]{6}");

        // ── Step 10: Rider marks arrived at restaurant ────────────────────
        System.out.println("═══ STEP 10: Rider marking arrived ═══");
        DeliveryActiveJobPage activeJob = new DeliveryActiveJobPage(riderPage);
        // Waits for the server to accept the step, not for a guessed number of seconds.
        confirmStatus(orderId, "AT_RESTAURANT", activeJob::markArrivedAtRestaurant);
        customerProgress.deliveryLabel("Courier at restaurant");

        // ── Step 11: Rider enters pickup OTP and swipes to confirm ────────
        System.out.println("═══ STEP 11: Rider picking up order ═══");
        activeJob.enterPickupOtp(pickupOtp);
        streamStage = "confirming-pickup";
        confirmStatus(orderId, "OUT_FOR_DELIVERY", activeJob::swipeToConfirmPickup);
        streamStage = "out-for-delivery";
        customerProgress.deliveryLabel("Courier on the way");
        CustomerOrderTrackerPage ownedTracker = new CustomerOrderTrackerPage(customerPage, orderId);
        org.assertj.core.api.Assertions.assertThat(parseInr(ownedTracker.getTotalPaid()))
                .isEqualTo(((Number) order.get("totalAmount")).doubleValue());
        org.assertj.core.api.Assertions.assertThat(ownedTracker.getPaymentMethod()).isEqualTo("Paid via CARD");
        if(SSE_ENABLED) {
        customerMapPoints = com.fooddelivery.e2e.util.LiveCustomerMap.assertMovement(customerPage, riderPage,
                orderId, testRiderPhone, TestConfig.GEO_LAT, TestConfig.GEO_LNG);
        java.util.List<Map<String, Object>> ownedMapStreams = customerMapStreams.stream()
                .filter(event -> ((String) event.get("path")).endsWith("/orders/" + orderId + "/live-tracking")).toList();
        org.assertj.core.api.Assertions.assertThat(ownedMapStreams).isNotEmpty();
        for (Map<String, Object> event : ownedMapStreams) {
            org.assertj.core.api.Assertions.assertThat(event.get("status")).isEqualTo(200);
            org.assertj.core.api.Assertions.assertThat((String) event.get("contentType")).startsWith("text/event-stream");
        }
        }

        assertThat(riderPage.getByText("Step 2: Deliver to door",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByText("Package Picked Up",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();
        assertThat(customerAddress).containsText(deliveryAddress);

        // Delivery phase is server-backed and must be restored after reload.
        streamStage = "delivery-reload";
        riderPage.reload();
        streamStage = "delivery-restored";
        waitForActiveOrder(orderId);
        assertThat(riderPage.getByText("Step 2: Deliver to door",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();

        // ── Step 12: Get customer delivery OTP ────────────────────────────
        System.out.println("═══ STEP 12: Getting customer delivery OTP ═══");
        String deliveryOtp = tracker.getDeliveryOtp();
        assertThat(deliveryOtp).isNotEmpty().hasSize(6);

        // ── Step 13: Rider enters delivery OTP and swipes to deliver ──────
        System.out.println("═══ STEP 13: Rider delivering order ═══");
        activeJob.enterDeliveryOtp(deliveryOtp);
        streamStage = "confirming-delivery";
        confirmDelivery(activeJob, orderId);
        streamStage = "delivered";

        // ── Step 14: Verify delivery complete ─────────────────────────────
        System.out.println("═══ STEP 14: Verifying delivery complete ═══");
        // The rider returns to the scanning screen after delivery. Its completed
        // history records the exact order, unlike the still-open customer tracker.
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Online Duty$")))).isVisible();
        assertThat(riderPage.getByText("Active Contract",
                new Page.GetByTextOptions().setExact(true))).isHidden();
        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator completedTrip = riderPage.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("ORDER #" + shortOrderId))
                .filter(new Locator.FilterOptions().setHasText("Delivered"));
        completedTrip.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        assertThat(completedTrip).containsText(selectedOutlet);
        assertThat(completedTrip).containsText(Pattern.compile("\\+₹[0-9,]+(?:\\.[0-9]{2})?"));
        double recordedPayout = parseInr(completedTrip.innerText());
        org.assertj.core.api.Assertions.assertThat(recordedPayout)
                .as("completed-trip net payout should be positive")
                .isPositive();

        riderPage.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Completed Deliveries").setExact(true))
                .locator("..").getByRole(AriaRole.BUTTON).click();
        riderPage.waitForCondition(() -> parseInr(todayEarnings.innerText()) >= earningsBefore + recordedPayout,
                new Page.WaitForConditionOptions().setTimeout(30000));
        double earningsAfter = parseInr(todayEarnings.innerText());
        org.assertj.core.api.Assertions.assertThat(earningsAfter)
                .as("today's earnings after exact completed delivery")
                .isGreaterThanOrEqualTo(earningsBefore + recordedPayout);

        Locator deliveredSummary = customerPage.locator(
                "[data-testid='order-tracker'][data-order-id='"+orderId+"']")
                .filter(new Locator.FilterOptions().setHas(customerPage.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Order delivered").setExact(true))));

        System.out.println("═══ STEP 15: Verifying exact customer and restaurant history ═══");
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        Locator customerHistory = customerPage.getByTestId("customer-history-order")
                .filter(new Locator.FilterOptions().setHas(customerPage.getByText(shortOrderId,
                        new Page.GetByTextOptions().setExact(true))));
        assertThat(customerHistory).isVisible();
        assertThat(customerHistory).containsText(selectedOutlet);
        assertThat(customerHistory).containsText("Order Delivered");
        assertThat(customerHistory).containsText("1 items");
        org.assertj.core.api.Assertions.assertThat(parseInr(customerHistory.innerText()))
                .isEqualTo(((Number) order.get("totalAmount")).doubleValue());
        customerHistory.click();
        assertThat(deliveredSummary).isVisible();
        assertThat(deliveredSummary.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))).isVisible();
        assertThat(deliveredSummary).containsText("Delivered from "+selectedOutlet);
        assertThat(deliveredSummary.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Tax invoice").setExact(true))).isVisible();
        chatChecks.afterDelivery(orderId,selectedOutlet);

        // Delivered orders leave the active kitchen queue and appear in the outlet's history.
        assertThat(orderActions.orderCard(orderId)).isHidden();
        restaurantDashboard.openSettingsTab();
        restaurantPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Order History").setExact(true)).click();
        Locator restaurantHistory = restaurantPage.getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(shortOrderId + "..."));
        assertThat(restaurantHistory).isVisible();
        assertThat(restaurantHistory).containsText("Delivered");
        org.assertj.core.api.Assertions.assertThat(parseInr(restaurantHistory.innerText()))
                .isEqualTo(((Number) order.get("totalAmount")).doubleValue());

        if(SSE_ENABLED) {
        java.util.List<Map<String, Object>> ownedStreams = streamResponses.stream()
                .filter(event -> ((String) event.get("path")).endsWith("/orders/" + orderId + "/restaurant-status-stream"))
                .toList();
        org.assertj.core.api.Assertions.assertThat(ownedStreams).as("exact order's restaurant-status subscription").isNotEmpty();
        for (Map<String, Object> event : ownedStreams) {
            org.assertj.core.api.Assertions.assertThat(event.get("status"))
                    .as("authorised stream response for %s", event).isEqualTo(200);
        }

        }
        loginAsAdmin();
        Map<?,?> money=com.fooddelivery.e2e.util.OrderMoneyChecks.verify(adminPage,orderId,order,recordedPayout);
        org.junit.jupiter.api.Assertions.assertAll("Chat and refund quote",
                () -> chatChecks.assertPassed(),
                () -> com.fooddelivery.e2e.util.RefundQuoteChecks.verify(customerPage,orderId,money));

        System.out.println("════════════════════════════════════════");
        System.out.println("  ✅ HAPPY PATH E2E TEST PASSED");
        System.out.println("════════════════════════════════════════");
    }

    @Test
    @Order(2)
    @DisplayName("CROSS-15: overlapping orders from two brands remain independently tracked")
    void overlappingOrdersRemainIndependent() throws java.io.IOException {
        duty = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
        Page originalCustomer = customerPage;
        Page originalRestaurant = restaurantPage;
        String secondRestaurantPhone = System.getProperty("concurrent.restaurant.phone", "9000000002");
        org.assertj.core.api.Assertions.assertThat(secondRestaurantPhone).isNotEqualTo(testRestaurantPhone);
        try (var secondCustomerContext = browser.newContext(new com.microsoft.playwright.Browser.NewContextOptions()
                     .setPermissions(java.util.List.of("geolocation", "notifications")).setGeolocation(TestConfig.GEO_LAT, TestConfig.GEO_LNG));
             var secondRestaurantContext = browser.newContext()) {
            Page secondCustomer = secondCustomerContext.newPage();
            Page secondRestaurant = secondRestaurantContext.newPage();
            // The same seeded customer has two separate cart/browser sessions: both server records
            // must remain visible independently, including when the other one completes.
            secondCustomer.navigate(TestConfig.APP_URL);
            new LoginPage(secondCustomer).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
            new SavedDeliveryAddressPage(secondCustomer).selectHomeFromOpenDialog();
            secondRestaurant.navigate(TestConfig.APP_URL);
            new LoginPage(secondRestaurant).login(secondRestaurantPhone).openPortal(Portal.RESTAURANT);
            new RestaurantDashboardPage(secondRestaurant).waitForDashboard();
            String resumePair = System.getProperty("concurrent.resume.pair", "").trim();
            com.fooddelivery.e2e.util.LiveOrderFixture.Created first;
            com.fooddelivery.e2e.util.LiveOrderFixture.Created second;
            if (resumePair.isEmpty()) {
                first = com.fooddelivery.e2e.util.LiveOrderFixture.place(customerPage, restaurantPage,
                        testCustomerPhone, testRestaurantPhone, testRiderPhone, duty);
                expectedOrderId = first.id();
                second = com.fooddelivery.e2e.util.LiveOrderFixture.place(secondCustomer, secondRestaurant,
                        testCustomerPhone, secondRestaurantPhone, testRiderPhone, duty);
            } else {
                Map<?, ?> retainedPair = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", Files.readString(Path.of(resumePair)));
                org.assertj.core.api.Assertions.assertThat(retainedPair.get("customerPhone")).isEqualTo(testCustomerPhone);
                first = com.fooddelivery.e2e.util.LiveOrderFixture.readRetained(customerPage, (String) retainedPair.get("firstOrderId"));
                second = com.fooddelivery.e2e.util.LiveOrderFixture.readRetained(customerPage, (String) retainedPair.get("secondOrderId"));
                expectedOrderId = first.id();
                new RestaurantDashboardPage(restaurantPage).selectOutlet(first.outlet());
                new RestaurantDashboardPage(secondRestaurant).selectOutlet(second.outlet());
            }
            org.assertj.core.api.Assertions.assertThat(first.id()).isNotEqualTo(second.id());
            org.assertj.core.api.Assertions.assertThat(first.outlet()).isNotEqualTo(second.outlet());
            Path pair = Path.of("target/lifecycle", "concurrent-" + first.id() + (resumePair.isEmpty() ? ".json" : "-resume.json"));
            Files.writeString(pair, (String) customerPage.evaluate("data => JSON.stringify(data,null,2)", Map.of(
                    "firstOrderId", first.id(), "secondOrderId", second.id(), "customerPhone", testCustomerPhone,
                    "riderPhone", testRiderPhone, "dataPolicy", "retain", "cleanupPerformed", false)));
            Locator firstTracker = customerPage.locator("[data-testid='order-tracker'][data-order-id='" + first.id() + "']");
            Locator secondTracker = secondCustomer.locator("[data-testid='order-tracker'][data-order-id='" + second.id() + "']");
            assertThat(secondTracker).isVisible();
            assertThat(secondTracker).hasAttribute("data-status", "PENDING_ACCEPTANCE",
                    new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
            if (resumePair.isEmpty()) {
                // Reload once to load both newly-created owned orders, then exercise the
                // carousel itself. Subsequent status progression remains automatic.
                customerPage.setViewportSize(1100, 900);
                customerPage.reload();
                customerPage.getByTestId("order-tracker").first().waitFor();
                customerPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Back").setExact(true)).click();
                CustomerActiveOrdersCarouselPage carousel = new CustomerActiveOrdersCarouselPage(customerPage);
                assertThat(customerPage.getByRole(AriaRole.LIST,
                        new Page.GetByRoleOptions().setName("Active orders").setExact(true))).isVisible();
                org.assertj.core.api.Assertions.assertThat(carousel.getActiveOrderCount()).isEqualTo(2);
                assertThat(customerPage.locator("[data-testid='active-order-card'][data-order-id='" + first.id() + "']")).containsText("Waiting for Restaurant");
                assertThat(customerPage.locator("[data-testid='active-order-card'][data-order-id='" + second.id() + "']")).containsText("Waiting for Restaurant");
                carousel.openOrder(second.id());
                org.assertj.core.api.Assertions.assertThat(carousel.isCarouselVisible()).isFalse();
                assertThat(customerPage.locator("[data-testid='order-tracker'][data-order-id='" + second.id() + "']")).hasAttribute("data-status", "PENDING_ACCEPTANCE");
                customerPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Back").setExact(true)).click();
                carousel.openOrder(first.id());
                assertThat(firstTracker).isVisible();
                assertThat(firstTracker).hasAttribute("data-status", "PENDING_ACCEPTANCE",
                        new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
                RestaurantOrderActionsPage firstActions = new RestaurantOrderActionsPage(restaurantPage);
                firstActions.acceptOrder(first.id());firstActions.startCooking(first.id());firstActions.markPrepared(first.id());
                // One real assigned rider completes the pair sequentially; never assign two jobs to it.
                resumeAssignedOrder(first.id(), first.outlet());
            } else {
                // A selector failure after the first completed delivery must not create replacements.
                CustomerDashboardPage.openProfileSettings(customerPage);
                customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
                customerPage.getByTestId("customer-history-order").filter(new Locator.FilterOptions().setHas(
                        customerPage.getByText(first.id().substring(0, 8), new Page.GetByTextOptions().setExact(true)))).click();
            }
            assertThat(firstTracker.getByRole(AriaRole.HEADING,
                    new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))).isVisible();
            assertThat(secondTracker).hasAttribute("data-status", "PENDING_ACCEPTANCE");
            assertThat(secondTracker.getByRole(AriaRole.HEADING,
                    new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))).isHidden();
            assertThat(new RestaurantOrderActionsPage(secondRestaurant).orderCard(second.id()))
                    .hasAttribute("data-status", "CREATED");
            // Preserve the exact first receipt in its browser while progressing the second order.
            customerPage = secondCustomer;restaurantPage = secondRestaurant;expectedOrderId = second.id();
            RestaurantOrderActionsPage secondActions = new RestaurantOrderActionsPage(restaurantPage);
            secondActions.acceptOrder(second.id());secondActions.startCooking(second.id());secondActions.markPrepared(second.id());
            resumeAssignedOrder(second.id(), second.outlet());
            assertThat(secondTracker.getByRole(AriaRole.HEADING,
                    new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))).isVisible();
            assertThat(firstTracker.filter(new Locator.FilterOptions().setHas(originalCustomer.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Order delivered").setExact(true)))))
                    .containsText("Delivered from " + first.outlet());
            assertThat(secondTracker.filter(new Locator.FilterOptions().setHas(secondCustomer.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Order delivered").setExact(true)))))
                    .containsText("Delivered from " + second.outlet());
            Files.writeString(Path.of("target/lifecycle", "concurrent-" + first.id() + "-streams.json"),
                    (String) riderPage.evaluate("data => JSON.stringify(data,null,2)", streamResponses));
        } finally {
            customerPage = originalCustomer;restaurantPage = originalRestaurant;
        }
    }

    private void confirmDelivery(DeliveryActiveJobPage activeJob, String orderId) {
        confirmStatus(orderId, "DELIVERED", activeJob::swipeToConfirmDelivery);
    }

    private void confirmStatus(String orderId, String status, Runnable swipe) {
        com.microsoft.playwright.Response response = riderPage.waitForResponse(
                r -> r.request().method().equals("POST")
                        && r.url().endsWith("/orders/" + orderId + "/status")
                        && r.request().postData() != null
                        && r.request().postData().contains("\"" + status + "\""),
                new Page.WaitForResponseOptions().setTimeout(60000),
                swipe);
        System.out.printf("[OTP SWIPE] %s submission returned HTTP %d for order %s%n",
                status, response.status(), orderId);
        org.assertj.core.api.Assertions.assertThat(response.ok())
                .as("%s confirmation after pointer swipe: HTTP %s", status, response.status()).isTrue();
    }

    private void waitForActiveOrder(String orderId) {
        Locator activeContract = riderPage.getByText("Active Contract",
                new Page.GetByTextOptions().setExact(true));
        activeContract.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(60000));
        riderPage.getByText("#" + orderId,
                        new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                        .setTimeout(10000));
    }
}
