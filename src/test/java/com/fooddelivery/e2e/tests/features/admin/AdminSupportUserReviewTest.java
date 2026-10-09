package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.*;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.fooddelivery.e2e.util.UiSettle;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Strict, read-only checks for admin support, user, review and category screens. */
public class AdminSupportUserReviewTest extends TestBase {

    private static final String UNKNOWN_UUID = "00000000-0000-0000-0000-000000000000";
    private AdminPortalPage portal;

    @BeforeEach
    void loginAdmin() {
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    // ── SUPPORT TICKETS ──────────────────────────────────────────────────

    @Tag("feature-refunds-support")
    @Test
    @DisplayName("SUPPORT-ADV-01..03: Support queue loads and exposes a count or explicit empty state")
    void supportTicketsVisibleAndCounted() {
        Response response = openSupportQueue();
        assertThat(response.status()).isEqualTo(200);

        AdminSupportTicketsPage support = new AdminSupportTicketsPage(adminPage);
        assertThat(support.isSupportVisible()).isTrue();
        assertSupportCountMatchesHeader(support, "OPEN");
    }

    @Tag("feature-refunds-support")
    @Test
    @DisplayName("SUPPORT-ADV-07: Support pagination advances and returns, or is absent for one page")
    void supportPagination() {
        Response response = openSupportQueue();
        assertThat(response.status()).isEqualTo(200);
        AdminSupportTicketsPage support = new AdminSupportTicketsPage(adminPage);

        if (!support.hasPagination()) {
            assertThat(support.canGoNextPage()).isFalse();
            assertThat(support.canGoPreviousPage()).isFalse();
            assertSupportCountMatchesHeader(support, "OPEN");
            return;
        }

        String firstPage = support.getPageInfo();
        assertThat(firstPage).matches("Page 1 of [2-9][0-9]*");
        Response next = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/intervention/support-tickets")
                                && r.url().contains("status=OPEN")
                                && r.url().matches(".*[?&]page=1(?:&|$).*")
                                && "GET".equals(r.request().method()),
                support::nextPage);
        assertThat(next.status()).isEqualTo(200);
        UiSettle.after(adminPage, next);
        assertThat(support.getPageInfo()).startsWith("Page 2 of ");

        Response previous = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/intervention/support-tickets")
                                && r.url().contains("status=OPEN")
                                && r.url().matches(".*[?&]page=0(?:&|$).*")
                                && "GET".equals(r.request().method()),
                support::prevPage);
        assertThat(previous.status()).isEqualTo(200);
        UiSettle.after(adminPage, previous);
        assertThat(support.getPageInfo()).startsWith("Page 1 of ");
    }

    // ── REFUND QUEUE ─────────────────────────────────────────────────────

    @Tag("feature-refunds-support")
    @Test
    @DisplayName("REFUND-ADV-01/02: Refund queue shows pending rows or its explicit empty state")
    void refundQueueVisibleAndCounted() {
        Response response = openRefundQueue();
        assertThat(response.status()).isEqualTo(200);
        AdminRefundQueuePage refunds = new AdminRefundQueuePage(adminPage);
        assertThat(refunds.isRefundQueueVisible()).isTrue();
        int count = refunds.getRefundCount();
        if (count == 0) {
            assertThat(refunds.isQueueEmpty()).isTrue();
        } else {
            assertThat(count).isPositive();
        }
    }

    // ── USER MANAGEMENT ──────────────────────────────────────────────────

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER-11/13: Search a seeded phone and verify its exact detail record")
    void searchAndOpenSeededUser() {
        AdminUserManagementPage users = openUsersAndWaitForInitialList();
        Response response = searchSeededCustomer(users);
        assertThat(response.status()).isEqualTo(200);
        assertThat(users.getUserCount()).isEqualTo(1);
        assertThat(users.getUserCardText(0)).contains(testCustomerPhone);

        Response activeOrders = selectUserAndWaitForOrderLookup(users);
        assertThat(activeOrders.status()).isEqualTo(200);
        assertThat(users.isDetailPanelOpen()).isTrue();
        assertThat(users.getUserPhone()).isEqualTo(testCustomerPhone);
        assertThat(users.getUserId()).matches("^[0-9a-fA-F-]{36}$");
        assertThat(users.getUserRole()).contains("CUSTOMER");
        assertThat(users.getUserStatus()).matches("Active|Suspended");
        if (users.getUserActiveOrderCount() == 0) {
            assertThat(users.hasNoActiveOrders()).isTrue();
        } else {
            assertThat(users.getUserActiveOrderCount()).isPositive();
        }
    }

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER-15: Role filter returns only users with the selected role")
    void filterByRole() {
        AdminUserManagementPage users = openUsersAndWaitForInitialList();
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/users/by-role")
                                && r.url().contains("role=CUSTOMER")
                                && "GET".equals(r.request().method()),
                () -> users.filterByRole("CUSTOMER"));
        assertThat(response.status()).isEqualTo(200);
        UiSettle.after(adminPage, response);

        if (users.getUserCount() == 0) {
            assertThat(users.isNoUsersStateVisible()).isTrue();
            return;
        }
        for (String card : users.getUserCards().stream().map(Locator::innerText).toList()) {
            assertThat(card).contains("CUSTOMER");
        }
    }

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER-18: User pagination changes page and returns, or disables both controls")
    void userPagination() {
        AdminUserManagementPage users = openUsersAndWaitForInitialList();
        String initial = users.getPageInfo();
        assertThat(initial).matches("Page 1 of [1-9][0-9]*");
        int totalPages = Integer.parseInt(initial.substring("Page 1 of ".length()));
        if (totalPages == 1) {
            assertThat(users.canGoPreviousPage()).isFalse();
            assertThat(users.canGoNextPage()).isFalse();
            return;
        }

        Response next = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/users/all")
                                && r.url().matches(".*[?&]page=1(?:&|$).*")
                                && "GET".equals(r.request().method()),
                users::nextPage);
        assertThat(next.status()).isEqualTo(200);
        UiSettle.after(adminPage, next);
        assertThat(users.getPageInfo()).startsWith("Page 2 of ");

        Response previous = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/users/all")
                                && r.url().matches(".*[?&]page=0(?:&|$).*")
                                && "GET".equals(r.request().method()),
                users::prevPage);
        assertThat(previous.status()).isEqualTo(200);
        UiSettle.after(adminPage, previous);
        assertThat(users.getPageInfo()).startsWith("Page 1 of ");
    }

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER: Status controls are state-aware, and an available suspension confirmation can be canceled")
    void statusControlIsStateAwareAndSuspensionConfirmationCanBeCanceled() {
        AdminUserManagementPage users = openUsersAndWaitForInitialList();
        Response response = searchSeededCustomer(users);
        assertThat(response.status()).isEqualTo(200);
        assertThat(users.getUserCount()).isEqualTo(1);
        Response activeOrders = selectUserAndWaitForOrderLookup(users);
        assertThat(activeOrders.status()).isEqualTo(200);
        assertThat(users.isDetailPanelOpen()).isTrue();

        Locator suspend = adminPage.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Suspend User").setExact(true));
        Locator activate = adminPage.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Activate User").setExact(true));
        assertThat(suspend.count() + activate.count()).isEqualTo(1);

        AtomicInteger statusUpdates = new AtomicInteger();
        adminPage.onRequest(request -> {
            if ("PUT".equals(request.method())
                    && request.url().contains("/api/v1/internal/admin/users/")
                    && request.url().endsWith("/status")) {
                statusUpdates.incrementAndGet();
            }
        });
        if (suspend.count() == 0) {
            assertThat(users.getUserStatus()).isEqualTo("Suspended");
            assertThat(activate.isEnabled()).isTrue();
            assertThat(statusUpdates.get()).isZero();
            return;
        }

        suspend.click();
        Locator dialog = adminPage.getByRole(AriaRole.DIALOG);
        assertThat(dialog.isVisible()).isTrue();
        assertThat(dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Suspend user").setExact(true)).isVisible()).isTrue();
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        dialog.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
        assertThat(statusUpdates.get()).isZero();
    }

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER: Available role changes require confirmation and cancellation sends no write")
    void roleChangeConfirmationCanBeCanceled() {
        AdminUserManagementPage users = openUsersAndWaitForInitialList();
        Response response = searchSeededCustomer(users);
        assertThat(response.status()).isEqualTo(200);
        assertThat(users.getUserCount()).isEqualTo(1);
        Response activeOrders = selectUserAndWaitForOrderLookup(users);
        assertThat(activeOrders.status()).isEqualTo(200);
        assertThat(users.isDetailPanelOpen()).isTrue();

        AtomicInteger roleWrites = new AtomicInteger();
        adminPage.onRequest(request -> {
            if ("POST".equals(request.method())
                    && request.url().contains("/api/v1/internal/admin/users/")
                    && request.url().endsWith("/roles")) {
                roleWrites.incrementAndGet();
            }
        });
        String role = users.selectFirstAvailableNewRole();
        String dialogTitle;
        String confirmationButton;
        if (role != null) {
            users.assignRole();
            dialogTitle = "Grant " + role + " role?";
            confirmationButton = "Grant role";
        } else {
            Locator removeRole = adminPage.locator("button[aria-label^='Remove '][aria-label$=' role']").first();
            assertThat(removeRole.count()).isPositive();
            String removeLabel = removeRole.getAttribute("aria-label");
            assertThat(removeLabel).matches("^Remove .+ role$");
            role = removeLabel.substring("Remove ".length(), removeLabel.length() - " role".length());
            removeRole.click();
            dialogTitle = "Remove " + role + " role?";
            confirmationButton = "Remove role";
        }

        Locator dialog = adminPage.getByRole(AriaRole.DIALOG,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName(dialogTitle).setExact(true));
        assertThat(dialog.isVisible()).isTrue();
        assertThat(dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(confirmationButton).setExact(true)).isVisible()).isTrue();
        assertThat(roleWrites.get()).isZero();
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        dialog.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
        assertThat(roleWrites.get()).isZero();
    }

    // ── REVIEW MODERATION ───────────────────────────────────────────────

    @Tag("feature-reviews")
    @Test
    @DisplayName("ADMIN-REVIEW-07..09: Entity and author lookup modes show the right fields")
    void reviewLookupModes() {
        portal.openReviewsTab();
        AdminReviewsPage reviews = new AdminReviewsPage(adminPage);
        assertThat(reviews.isReviewsVisible()).isTrue();
        reviews.switchToEntityMode();
        assertThat(adminPage.getByRole(AriaRole.TEXTBOX,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Entity ID").setExact(true)).isVisible())
                .isTrue();
        assertThat(adminPage.getByRole(AriaRole.TEXTBOX,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Author user ID").setExact(true)).count())
                .isZero();
        assertThat(reviews.isSearchDisabled()).isTrue();

        reviews.switchToUserMode();
        assertThat(adminPage.getByRole(AriaRole.TEXTBOX,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Author user ID").setExact(true)).isVisible())
                .isTrue();
        assertThat(adminPage.getByRole(AriaRole.TEXTBOX,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Entity ID").setExact(true)).count())
                .isZero();
        assertThat(reviews.isSearchDisabled()).isTrue();
    }

    @Tag("feature-reviews")
    @Test
    @DisplayName("ADMIN-REVIEW-10: An unknown entity returns the explicit no reviews state")
    void searchReviewsByUnknownEntity() {
        portal.openReviewsTab();
        AdminReviewsPage reviews = new AdminReviewsPage(adminPage);
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/reviews")
                                && r.url().contains("entityId=" + UNKNOWN_UUID)
                                && "GET".equals(r.request().method()),
                () -> reviews.searchByEntity("RESTAURANT", UNKNOWN_UUID));
        assertThat(response.status()).isEqualTo(200);
        UiSettle.after(adminPage, response);
        assertThat(reviews.isEmptyState()).isTrue();
        assertThat(reviews.getReviewCount()).isZero();
    }

    @Tag("feature-reviews")
    @Test
    @DisplayName("ADMIN-REVIEW-08/09/11/12: Seeded author search shows readable stars and immutable records")
    void searchSeededAuthorAndInspectReviews() {
        String customerId = getSeededCustomerId();
        portal.openReviewsTab();
        AdminReviewsPage reviews = new AdminReviewsPage(adminPage);
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/reviews/by-user/" + customerId)
                                && "GET".equals(r.request().method()),
                () -> reviews.searchByUser(customerId));
        assertThat(response.status()).isEqualTo(200);
        UiSettle.after(adminPage, response);

        int count = reviews.getReviewCount();
        if (count == 0) {
            assertThat(reviews.isEmptyState()).isTrue();
            return;
        }
        assertThat(reviews.getReviewCards()).hasSize(count);
        for (Locator card : reviews.getReviewCards()) {
            assertThat(card.locator("[role='img']").count()).isEqualTo(1);
            assertThat(card.locator("[role='img']").isVisible()).isTrue();
            assertThat(card.innerText()).contains("author", "order", "target", "review");
            assertThat(card.getByRole(AriaRole.BUTTON).count()).isZero();
        }
    }

    // ── CATEGORIES ───────────────────────────────────────────────────────

    @Tag("feature-catalog")
    @Test
    @DisplayName("ADMIN-CAT-09/10: Categories renders saved cards or an explicit empty state")
    void categoriesEditorAndCount() {
        Response response = openCategoriesAndWait();
        assertThat(response.status()).isEqualTo(200);
        AdminCategoriesPage categories = new AdminCategoriesPage(adminPage);
        assertThat(categories.isCategoriesVisible()).isTrue();
        int count = categories.getCategoryCount();
        if (count == 0) {
            assertThat(categories.isEmptyStateVisible()).isTrue();
        } else {
            assertThat(categories.getCategoryCards()).hasSize(count);
        }
    }

    @Tag("feature-catalog")
    @Test
    @DisplayName("ADMIN-CAT: Invalid category name is rejected without a write")
    void invalidCategoryNameDoesNotCreateCategory() {
        Response response = openCategoriesAndWait();
        assertThat(response.status()).isEqualTo(200);

        AtomicInteger categoryWrites = new AtomicInteger();
        adminPage.onRequest(request -> {
            if ("POST".equals(request.method()) && request.url().endsWith("/api/v1/categories")) {
                categoryWrites.incrementAndGet();
            }
        });
        adminPage.getByPlaceholder("e.g. Italian, Vegan, Burgers").fill("A");
        adminPage.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Create Category").setExact(true)).click();
        assertThat(adminPage.getByText("Category name is required",
                new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).isVisible()).isTrue();
        assertThat(categoryWrites.get()).isZero();
    }

    @Tag("feature-catalog")
    @Test
    @DisplayName("ADMIN-CAT: The live category list exposes its empty state or an edit form without saving")
    void categoryListEmptyStateOrEditFormIsReadOnly() {
        Response response = openCategoriesAndWait();
        assertThat(response.status()).isEqualTo(200);
        AdminCategoriesPage categories = new AdminCategoriesPage(adminPage);
        List<Locator> cards = categories.getCategoryCards();
        if (cards.isEmpty()) {
            assertThat(categories.isEmptyStateVisible()).isTrue();
            return;
        }
        String categoryName = cards.get(0).locator("p.font-bold").innerText().trim();

        AtomicInteger writes = new AtomicInteger();
        adminPage.onRequest(request -> {
            if (("POST".equals(request.method()) || "PUT".equals(request.method()))
                    && request.url().contains("/api/v1/categories")) {
                writes.incrementAndGet();
            }
        });
        cards.get(0).getByRole(AriaRole.BUTTON).click();
        assertThat(adminPage.getByRole(AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Edit Category").setExact(true)).isVisible())
                .isTrue();
        assertThat(adminPage.getByPlaceholder("e.g. Italian, Vegan, Burgers").inputValue()).isEqualTo(categoryName);
        assertThat(writes.get()).isZero();
    }

    // ── Shared fixture helpers ───────────────────────────────────────────

    private Response openSupportQueue() {
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/intervention/support-tickets")
                                && r.url().contains("status=OPEN")
                                && "GET".equals(r.request().method()),
                portal::openSupportTab);
        UiSettle.after(adminPage, response);
        return response;
    }

    private Response openRefundQueue() {
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/refunds")
                                && r.url().contains("status=OPEN")
                                && "GET".equals(r.request().method()),
                portal::openRefundsTab);
        UiSettle.after(adminPage, response);
        return response;
    }

    private AdminUserManagementPage openUsersAndWaitForInitialList() {
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/users/all")
                                && r.url().contains("page=0")
                                && "GET".equals(r.request().method()),
                portal::openUsersTab);
        assertThat(response.status()).isEqualTo(200);
        UiSettle.after(adminPage, response);
        return new AdminUserManagementPage(adminPage);
    }

    private Response searchSeededCustomer(AdminUserManagementPage users) {
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/users/by-phone")
                                && r.url().contains("phone=" + testCustomerPhone)
                                && "GET".equals(r.request().method()),
                () -> users.searchUser(testCustomerPhone));
        UiSettle.after(adminPage, response);
        return response;
    }

    private String getSeededCustomerId() {
        AdminUserManagementPage users = openUsersAndWaitForInitialList();
        Response response = searchSeededCustomer(users);
        assertThat(response.status()).isEqualTo(200);
        assertThat(users.getUserCount()).isEqualTo(1);
        Response activeOrders = selectUserAndWaitForOrderLookup(users);
        assertThat(activeOrders.status()).isEqualTo(200);
        assertThat(users.isDetailPanelOpen()).isTrue();
        assertThat(users.getUserPhone()).isEqualTo(testCustomerPhone);
        return users.getUserId();
    }

    private Response selectUserAndWaitForOrderLookup(AdminUserManagementPage users) {
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/user/")
                                && r.url().contains("/active")
                                && "GET".equals(r.request().method()),
                () -> users.selectUser(0));
        UiSettle.after(adminPage, response);
        return response;
    }

    private Response openCategoriesAndWait() {
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/categories")
                                && "GET".equals(r.request().method()),
                portal::openCategoriesTab);
        UiSettle.after(adminPage, response);
        return response;
    }

    private void assertSupportCountMatchesHeader(AdminSupportTicketsPage support, String status) {
        String header = support.getStatusHeader();
        assertThat(header).startsWith(status.replace('_', ' ') + " Tickets (");
        int declaredCount = Integer.parseInt(header.replaceAll("^.*Tickets \\(|\\)$", ""));
        assertThat(support.getTicketCount()).isEqualTo(declaredCount);
        if (declaredCount == 0) {
            assertThat(support.isEmptyStateVisible()).isTrue();
        } else {
            assertThat(support.isEmptyStateVisible()).isFalse();
        }
    }
}
