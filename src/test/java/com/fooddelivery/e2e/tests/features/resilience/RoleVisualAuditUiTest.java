package com.fooddelivery.e2e.tests.features.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminRefundQueuePage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Read-only deployed screenshots for redesign D1/E2 and strict responses for historical F12. */
@Tag("ui-only")
@Tag("feature-shell")
public class RoleVisualAuditUiTest extends TestBase {
    private final Path evidence = Path.of(System.getProperty("visual.audit.dir", "target/role-visual-audit"));

    @Tag("feature-partner-onboarding")
    @Tag("feature-rider-delivery")
    @Test
    void riderMobileDashboardAndVerificationRead() throws IOException {
        riderPage.setViewportSize(390, 844);
        List<Integer> verificationStatuses = new ArrayList<>();
        List<String> scriptPaths = new ArrayList<>();
        riderPage.onResponse(response -> {
            if (path(response).equals("/api/delivery/verification/status")) verificationStatuses.add(response.status());
            if (response.request().resourceType().equals("script")) scriptPaths.add(path(response));
        });
        new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        riderPage.waitForCondition(() -> !verificationStatuses.isEmpty());
        org.assertj.core.api.Assertions.assertThat(verificationStatuses).containsOnly(200);
        Locator duty = riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^(Offline|Online Duty)$")));
        assertThat(duty).isVisible();
        org.assertj.core.api.Assertions.assertThat(duty.boundingBox().height).isGreaterThanOrEqualTo(48);
        assertThat(riderPage.getByText(Pattern.compile("^(Today’s earnings|Paid today)$"))).isVisible();
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Trips Completed")))).isVisible();
        String expectedAsset = System.getProperty("visual.expected.delivery.asset", "").trim();
        if (!expectedAsset.isEmpty()) {
            org.assertj.core.api.Assertions.assertThat(scriptPaths)
                    .as("the served rider chunk matches the owner-published build").contains("/assets/" + expectedAsset);
        }
        capture(riderPage, "rider-mobile", "verificationStatuses=" + verificationStatuses + "\nscriptPaths=" + scriptPaths);
        noHorizontalOverflow(riderPage);
        // A dark class can change the text tokens while leaving the light App background visible.
        // Assert the browser's painted ground, rather than class presence alone.
        Locator shell = riderPage.getByRole(AriaRole.MAIN,
                new Page.GetByRoleOptions().setName("Delivery").setExact(true)).locator("xpath=../..");
        org.assertj.core.api.Assertions.assertThat(shell.evaluate(
                "element => getComputedStyle(element).backgroundColor"))
                .as("the forced-dark rider shell must paint its own opaque dark ground")
                .isEqualTo("rgb(18, 22, 28)");
        org.assertj.core.api.Assertions.assertThat(shell.evaluate(
                "element => getComputedStyle(element).color"))
                .as("inherited rider text must use the same forced-dark theme")
                .isEqualTo("rgb(255, 255, 255)");
    }

    @Tag("feature-refunds-support")
    @Test
    void adminDesktopRefundQueueAndSupport() throws IOException {
        adminPage.setViewportSize(1280, 800);
        loginAsAdmin();
        AdminPortalPage portal = new AdminPortalPage(adminPage);
        portal.openRefundsTab();
        for (String name : List.of("Manual Interventions", "Money Operations")) {
            Locator label = adminPage.getByRole(AriaRole.NAVIGATION)
                    .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(name)).locator("span").nth(1);
            org.assertj.core.api.Assertions.assertThat((Boolean) label.evaluate(
                    "element => element.scrollWidth <= element.clientWidth"))
                    .as("full sidebar label must be readable: %s", name).isTrue();
        }
        assertThat(adminPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Refund Exception Queue").setExact(true))).isVisible();
        assertThat(adminPage.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Status Filter").setExact(true))).isVisible();
        assertThat(adminPage.getByRole(AriaRole.TABLE).or(adminPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Queue Empty").setExact(true)))).isVisible();
        capture(adminPage, "admin-refunds-desktop", "viewport=1280x800");
        noHorizontalOverflow(adminPage);
        portal.openSupportTab();
        assertThat(adminPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Support Tickets").setExact(true))).isVisible();
        assertThat(adminPage.getByText("Loading tickets…", new Page.GetByTextOptions().setExact(true))).isHidden();
        assertThat(adminPage.getByText("Tickets could not be refreshed. Existing results are still shown.",
                new Page.GetByTextOptions().setExact(true))).isHidden();
        assertThat(adminPage.getByText("No tickets found", new Page.GetByTextOptions().setExact(true))
                .or(adminPage.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText("View Details"))).first()).isVisible();
        // Sidebar colours animate after selection; capture the finished selected state.
        Locator selectedSupport = adminPage.getByRole(AriaRole.NAVIGATION).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Support Tickets").setExact(true));
        adminPage.waitForCondition(() -> "rgb(255, 255, 255)".equals(selectedSupport.evaluate(
                "element => getComputedStyle(element).color")));
        capture(adminPage, "admin-support-desktop", "viewport=1280x800");
        noHorizontalOverflow(adminPage);
    }

    @Tag("feature-reviews")
    @Test
    void customerMyReviewsReadReturns200() throws IOException {
        customerPage.setViewportSize(1280, 800);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
        dashboard.openSettingsTab();
        Response reviews = customerPage.waitForResponse(response -> path(response).equals("/api/v1/reviews/me")
                        && response.request().method().equals("GET"),
                () -> customerPage.getByRole(AriaRole.TAB,
                        new Page.GetByRoleOptions().setName("My Reviews").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(reviews.status()).isEqualTo(200);
        assertThat(customerPage.locator("article").first().or(customerPage.getByText(
                "You haven't reviewed anything yet", new Page.GetByTextOptions().setExact(true)))).isVisible();
        capture(customerPage, "customer-my-reviews", "reviewsStatus=" + reviews.status());
    }

    @Tag("feature-refunds-support")
    @Test
    void adminAllRefundHistoryRead() throws IOException {
        adminPage.setViewportSize(1280, 800);
        var resolutionWrites = new java.util.concurrent.atomic.AtomicInteger();
        adminPage.onRequest(request -> {
            if (request.method().equals("POST") && java.net.URI.create(request.url()).getPath()
                    .startsWith("/api/v1/internal/admin/refunds/")) resolutionWrites.incrementAndGet();
        });
        loginAsAdmin();
        new AdminPortalPage(adminPage).openRefundsTab();
        adminPage.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Status Filter").setExact(true)).click();
        Response all = adminPage.waitForResponse(response -> path(response).equals("/api/v1/internal/admin/refunds")
                        && response.request().method().equals("GET")
                        && !java.util.Objects.toString(java.net.URI.create(response.url()).getRawQuery(), "").contains("status="),
                () -> adminPage.getByRole(AriaRole.OPTION,
                        new Page.GetByRoleOptions().setName("All Tickets").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(all.status()).isEqualTo(200);
        java.util.Map<?, ?> result = (java.util.Map<?, ?>) adminPage.evaluate("text => JSON.parse(text)", all.text());
        List<?> tickets = (List<?>) result.get("content");
        org.assertj.core.api.Assertions.assertThat(tickets).isNotNull();
        AdminRefundQueuePage queue = new AdminRefundQueuePage(adminPage);
        Locator rows = adminPage.getByRole(AriaRole.TABLE).locator("tbody tr");
        if (tickets.isEmpty()) {
            assertThat(adminPage.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Queue Empty").setExact(true))).isVisible();
        } else {
            assertThat(rows).hasCount(tickets.size());
        }
        capture(adminPage, "admin-refunds-all", "allStatus=" + all.status() + "\nrows=" + tickets.size());
        boolean actionRead = false;
        boolean auditRead = false;
        for (int i = 0; i < tickets.size(); i++) {
            String status = String.valueOf(((java.util.Map<?, ?>) tickets.get(i)).get("status"));
            boolean actionable = status.equals("OPEN") || status.equals("IN_REVIEW");
            if ((actionable && actionRead) || (!actionable && auditRead)) continue;
            queue.openRefundTicket(i);
            assertThat(adminPage.getByText("Customer Reason", new Page.GetByTextOptions().setExact(true))).isVisible();
            assertThat(adminPage.getByText("Requested Amount", new Page.GetByTextOptions().setExact(true))).isVisible();
            if (actionable) {
                queue.openResolutionConfirmation(false);
                assertThat(queue.confirmationDialog()).containsText("Reject this refund?");
                capture(adminPage, "admin-refund-confirmation", "status=" + status + "\nresolutionConfirmed=false");
                queue.cancelConfirmation();
                actionRead = true;
            } else {
                assertThat(adminPage.getByText("Resolution Audit", new Page.GetByTextOptions().setExact(true))).isVisible();
                auditRead = true;
            }
            capture(adminPage, "admin-refund-details-" + status.toLowerCase(), "status=" + status);
            adminPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Close ticket details").setExact(true)).click();
            if (actionRead && auditRead) break;
        }
        noHorizontalOverflow(adminPage);
        org.assertj.core.api.Assertions.assertThat(resolutionWrites.get()).as("read-only queue audit resolves no ticket").isZero();
        Files.createDirectories(evidence);
        Files.writeString(evidence.resolve("admin-refunds-all-disposition.json"),
                "{\"rows\":" + tickets.size() + ",\"actionDetailRead\":" + actionRead
                        + ",\"resolutionAuditRead\":" + auditRead + ",\"resolutionWrites\":0}");
    }

    private String path(Response response) { return java.net.URI.create(response.url()).getPath(); }

    private void noHorizontalOverflow(Page page) {
        org.assertj.core.api.Assertions.assertThat((Boolean) page.evaluate(
                "() => document.documentElement.scrollWidth <= innerWidth")).isTrue();
    }

    private void capture(Page page, String name, String observations) throws IOException {
        Files.createDirectories(evidence);
        page.screenshot(new Page.ScreenshotOptions().setPath(evidence.resolve(name + ".png")).setFullPage(true));
        // Rendered text only: never save browser storage, authentication headers or response bodies.
        Files.writeString(evidence.resolve(name + ".txt"), observations + "\n" + page.locator("body").innerText());
    }
}
