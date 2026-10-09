package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminUserCatalogModerationSafetyPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deterministic browser coverage for the admin user, category, and moderation surfaces.
 *
 * <p>Each test signs into the deployed application first. It then fulfills only the API calls
 * needed by the selected view with disposable in-browser fixtures. Any mutation under those API
 * prefixes is recorded and terminated in the browser, so the tests cannot alter shared Dev data.</p>
 */
@Tag("browser-routed")
public class AdminUserCatalogModerationRoutedUiTest extends TestBase {

    private static final String USER_API_PATH = "/api/v1/internal/admin/users";
    private static final String ACTIVE_ORDERS_PATH = "/api/v1/internal/admin/orders/user";
    private static final String CATEGORIES_PATH = "/api/v1/categories";
    private static final String REVIEWS_PATH = "/api/v1/internal/admin/reviews";

    private static final String USER_A_ID = "a1000000-0000-4000-8000-000000000001";
    private static final String USER_B_ID = "b1000000-0000-4000-8000-000000000001";
    private static final String USER_A_PHONE = "8111111111";
    private static final String USER_B_PHONE = "8222222222";
    private static final String USER_A_RESTAURANT = "Fixture user A kitchen";
    private static final String USER_B_RESTAURANT = "Fixture user B kitchen";

    private static final String CATEGORY_A_ID = "c1000000-0000-4000-8000-000000000001";
    private static final String CATEGORY_A_NAME = "Fixture category A";
    private static final String CREATED_CATEGORY_NAME = "Fixture category created";
    private static final String REJECTED_CATEGORY_NAME = "Fixture rejected category";
    private static final String UPDATED_CATEGORY_NAME = "Fixture category updated";

    private static final String ENTITY_ID = "fixture-restaurant-a";
    private static final String REVIEW_AUTHOR_ID = "d1000000-0000-4000-8000-000000000001";
    private static final String REVIEW_ORDER_ID = "d1000000-0000-4000-8000-000000000002";
    private static final String ENTITY_REVIEW_COMMENT = "Fixture stale entity review";
    private static final String AUTHOR_REVIEW_COMMENT = "Fixture author review";
    private static final String ROLE_REMOVE_FAILURE = "Fixture role removal rejected";
    private static final String STATUS_UPDATE_FAILURE = "Fixture status update rejected";
    private static final String REVIEW_LOOKUP_FAILURE = "Fixture review lookup failed";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";

    private AdminPortalPage portal;

    @BeforeEach
    void authenticateLiveAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER-SAFE-01: stale active orders are discarded and cancelled role/status confirmations send no write")
    void userSelectionIsolationAndCanceledRoleStatusChangesAreWriteFree() {
        UserFixture fixture = new UserFixture();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isUserFixtureUrl, fixture::handle);

        portal.openUsersTab();
        AdminUserCatalogModerationSafetyPage users = new AdminUserCatalogModerationSafetyPage(adminPage);
        users.waitForUserManagement();
        waitFor(() -> fixture.listReads.get() == 1, "the fixture user list must load once");

        users.selectUserByPhone(USER_A_PHONE);
        waitFor(() -> fixture.activeOrderRoute(USER_A_ID) != null,
                "selecting user A must request A's active orders");

        users.chooseNewRole("ADMIN");
        users.openAddRoleConfirmation();
        PlaywrightAssertions.assertThat(users.confirmationDialog("Grant ADMIN role?")).isVisible();
        users.cancelConfirmation("Grant ADMIN role?");

        users.openSuspendConfirmation();
        PlaywrightAssertions.assertThat(users.confirmationDialog("Suspend this user?")).isVisible();
        users.cancelConfirmation("Suspend this user?");
        assertThat(fixture.roleWrites.get()).as("cancelled role grants do not issue a write").isZero();
        assertThat(fixture.statusWrites.get()).as("cancelled suspensions do not issue a write").isZero();

        users.selectUserByPhone(USER_B_PHONE);
        waitFor(() -> fixture.activeOrderRoute(USER_B_ID) != null,
                "selecting user B must request B's active orders");

        Response currentResponse = adminPage.waitForResponse(
                response -> isActiveOrderResponse(response, USER_B_ID),
                () -> fixture.releaseActiveOrders(USER_B_ID));
        assertThat(currentResponse.status()).isEqualTo(200);
        users.waitForActiveOrderRestaurant(USER_B_RESTAURANT);

        Response staleResponse = adminPage.waitForResponse(
                response -> isActiveOrderResponse(response, USER_A_ID),
                () -> fixture.releaseActiveOrders(USER_A_ID));
        assertThat(staleResponse.status()).isEqualTo(200);
        waitForBrowserPaint();

        assertThat(users.isActiveOrderRestaurantVisible(USER_B_RESTAURANT)).isTrue();
        assertThat(users.isActiveOrderRestaurantVisible(USER_A_RESTAURANT))
                .as("a delayed response for A must never overwrite B's detail panel")
                .isFalse();
        assertThat(fixture.activeOrderReads.get()).isEqualTo(2);
        assertThat(fixture.unexpectedRequests.get()).isZero();
        assertThat(fixture.blockedWrites.get()).isZero();
    }

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER-SAFE-02: confirmed role removal and suspension use only fulfilled fixture writes and update the visible record")
    void confirmedRoleRemovalAndStatusUpdateUseOnlyFixtureWrites() {
        UserFixture fixture = UserFixture.fulfillMutations().withUserAHoldingAdmin();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isUserFixtureUrl, fixture::handle);

        portal.openUsersTab();
        AdminUserCatalogModerationSafetyPage users = new AdminUserCatalogModerationSafetyPage(adminPage);
        users.waitForUserManagement();
        users.selectUserByPhone(USER_A_PHONE);
        users.waitForActiveOrderRestaurant(USER_A_RESTAURANT);
        assertThat(users.hasRemoveRoleButton("CUSTOMER")).as("partner/customer roles are not removable here").isFalse();

        users.openRemoveRoleConfirmation("ADMIN");
        PlaywrightAssertions.assertThat(users.confirmationDialog("Remove ADMIN role?")).isVisible();
        users.confirmConfirmation("Remove ADMIN role?", "Remove role");
        waitFor(() -> fixture.roleWrites.get() == 1, "the browser fixture receives the confirmed role removal");
        users.waitForToast("Role removed");
        assertThat(users.hasRemoveRoleButton("ADMIN")).isFalse();

        users.openSuspendConfirmation();
        PlaywrightAssertions.assertThat(users.confirmationDialog("Suspend this user?")).isVisible();
        users.confirmConfirmation("Suspend this user?", "Suspend user");
        waitFor(() -> fixture.statusWrites.get() == 1, "the browser fixture receives the confirmed suspension");
        users.waitForToast("User suspended");
        users.waitForUserStatus("Suspended");
        assertThat(users.isUserActionEnabled("Activate User")).isTrue();

        assertThat(fixture.statusRequestBody.get()).contains("\"isActive\":false");
        assertThat(fixture.activeOrderReads.get()).isEqualTo(1);
        assertThat(fixture.blockedWrites.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER-SAFE-04: confirmed role grant and activation use fulfilled fixture writes and update the visible user")
    void confirmedRoleGrantAndActivationUseOnlyFixtureWrites() {
        UserFixture fixture = UserFixture.fulfillMutations();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isUserFixtureUrl, fixture::handle);

        portal.openUsersTab();
        AdminUserCatalogModerationSafetyPage users = new AdminUserCatalogModerationSafetyPage(adminPage);
        users.waitForUserManagement();
        users.selectUserByPhone(USER_A_PHONE);
        users.waitForActiveOrderRestaurant(USER_A_RESTAURANT);

        users.chooseNewRole("ADMIN");
        users.openAddRoleConfirmation();
        PlaywrightAssertions.assertThat(users.confirmationDialog("Grant ADMIN role?")).isVisible();
        users.confirmConfirmation("Grant ADMIN role?", "Grant role");
        waitFor(() -> fixture.roleWrites.get() == 1, "the confirmed role grant reaches the browser fixture");
        users.waitForToast("Role added");
        users.waitForRemoveRoleButton("ADMIN");

        assertThat(fixture.roleRequestMethod.get()).isEqualTo("POST");
        assertThat(fixture.roleRequestPath.get()).isEqualTo(USER_API_PATH + "/" + USER_A_ID + "/roles");
        // AdminUserManagement posts { roleName } only; partner roles come from applications and membership.
        assertThat(fixture.roleRequestBody.get()).contains("\"roleName\":\"ADMIN\"").doesNotContain("serviceName");

        users.selectUserByPhone(USER_B_PHONE);
        users.waitForActiveOrderRestaurant(USER_B_RESTAURANT);
        users.activateUser();
        waitFor(() -> fixture.statusWrites.get() == 1, "the activation reaches the browser fixture");
        users.waitForToast("User activated");
        users.waitForUserStatus("Active");
        assertThat(users.isUserActionEnabled("Suspend User")).isTrue();

        assertThat(fixture.statusRequestMethod.get()).isEqualTo("PUT");
        assertThat(fixture.statusRequestPath.get()).isEqualTo(USER_API_PATH + "/" + USER_B_ID + "/status");
        assertThat(fixture.statusRequestBody.get()).contains("\"isActive\":true");
        assertThat(fixture.activeOrderReads.get()).isEqualTo(2);
        assertThat(fixture.blockedWrites.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Tag("feature-admin-ops")
    @Test
    @DisplayName("ADMIN-USER-SAFE-03: failed role removal and suspension preserve the displayed user and recover the controls")
    void failedRoleRemovalAndStatusUpdateLeaveTheUserUnchanged() {
        UserFixture fixture = UserFixture.rejectMutations().withUserAHoldingAdmin();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isUserFixtureUrl, fixture::handle);

        portal.openUsersTab();
        AdminUserCatalogModerationSafetyPage users = new AdminUserCatalogModerationSafetyPage(adminPage);
        users.waitForUserManagement();
        users.selectUserByPhone(USER_A_PHONE);
        users.waitForActiveOrderRestaurant(USER_A_RESTAURANT);

        users.openRemoveRoleConfirmation("ADMIN");
        users.confirmConfirmation("Remove ADMIN role?", "Remove role");
        waitFor(() -> fixture.roleWrites.get() == 1, "the rejected role removal reaches only the fixture");
        users.waitForToast(ROLE_REMOVE_FAILURE);
        assertThat(users.hasRemoveRoleButton("ADMIN")).isTrue();

        users.openSuspendConfirmation();
        users.confirmConfirmation("Suspend this user?", "Suspend user");
        waitFor(() -> fixture.statusWrites.get() == 1, "the rejected suspension reaches only the fixture");
        users.waitForToast(STATUS_UPDATE_FAILURE);
        users.waitForUserStatus("Active");
        assertThat(users.isUserActionEnabled("Suspend User")).isTrue();

        assertThat(fixture.blockedWrites.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Tag("feature-catalog")
    @Test
    @DisplayName("ADMIN-CATEGORY-SAFE-01: client validation and cancelling edit are inert; a fixture-only create preserves its request")
    void categoryValidationEditCancellationAndFixtureCreateAreDeterministic() {
        CategoryFixture fixture = new CategoryFixture();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isCategoryFixtureUrl, fixture::handle);

        portal.openCategoriesTab();
        AdminUserCatalogModerationSafetyPage categories = new AdminUserCatalogModerationSafetyPage(adminPage);
        categories.waitForCategories();
        categories.waitForCategory(CATEGORY_A_NAME);

        categories.fillCategoryName("Valid fixture category");
        categories.fillCategoryDescription("x".repeat(256));
        categories.submitCategory();
        adminPage.getByText("Description cannot exceed 255 characters",
                        new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(5_000));
        assertThat(fixture.createWrites.get())
                .as("an over-long description must be blocked in the browser")
                .isZero();
        assertThat(fixture.updateWrites.get()).isZero();

        categories.openCategoryEdit(CATEGORY_A_NAME);
        adminPage.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Edit Category").setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(5_000));
        categories.cancelCategoryEdit();
        adminPage.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Global Categories").setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(5_000));
        assertThat(fixture.createWrites.get()).as("opening and cancelling edit cannot create").isZero();
        assertThat(fixture.updateWrites.get()).as("opening and cancelling edit cannot update").isZero();

        categories.fillCategoryName(CREATED_CATEGORY_NAME);
        categories.fillCategoryDescription("Created only in the browser fixture");
        categories.submitCategory();
        waitFor(() -> fixture.createWrites.get() == 1, "the valid create is received by the browser fixture");
        categories.waitForCategory(CREATED_CATEGORY_NAME);

        assertThat(fixture.createRequestBody.get())
                .contains("\"name\":\"" + CREATED_CATEGORY_NAME + "\"")
                .contains("\"description\":\"Created only in the browser fixture\"");
        assertThat(fixture.updateWrites.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Tag("feature-catalog")
    @Test
    @DisplayName("ADMIN-CATEGORY-SAFE-02: a confirmed fixture update refreshes the saved category and preserves its exact request")
    void categoryUpdateSuccessUsesOnlyTheBrowserFixture() {
        CategoryFixture fixture = CategoryFixture.updateSucceeds();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isCategoryFixtureUrl, fixture::handle);

        portal.openCategoriesTab();
        AdminUserCatalogModerationSafetyPage categories = new AdminUserCatalogModerationSafetyPage(adminPage);
        categories.waitForCategories();
        categories.waitForCategory(CATEGORY_A_NAME);
        categories.openCategoryEdit(CATEGORY_A_NAME);
        categories.waitForCategoryEditor("Edit Category");
        categories.fillCategoryName(UPDATED_CATEGORY_NAME);
        categories.fillCategoryDescription("Updated only in the browser fixture");
        categories.submitCategoryUpdate();

        waitFor(() -> fixture.updateWrites.get() == 1, "the fixture receives one category update");
        categories.waitForToast("Category updated successfully");
        categories.waitForCategoryEditor("Global Categories");
        categories.waitForCategory(UPDATED_CATEGORY_NAME);
        assertThat(fixture.updateRequestBody.get())
                .contains("\"name\":\"" + UPDATED_CATEGORY_NAME + "\"")
                .contains("\"description\":\"Updated only in the browser fixture\"");
        assertThat(fixture.createWrites.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Tag("feature-catalog")
    @Test
    @DisplayName("ADMIN-CATEGORY-SAFE-03: a rejected fixture update keeps the edit correctable without a false success or list change")
    void categoryUpdateErrorKeepsThePendingEditCorrectableWithoutFalseSuccess() {
        CategoryFixture fixture = CategoryFixture.updateFails();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isCategoryFixtureUrl, fixture::handle);

        portal.openCategoriesTab();
        AdminUserCatalogModerationSafetyPage categories = new AdminUserCatalogModerationSafetyPage(adminPage);
        categories.waitForCategories();
        categories.waitForCategory(CATEGORY_A_NAME);
        categories.openCategoryEdit(CATEGORY_A_NAME);
        categories.waitForCategoryEditor("Edit Category");
        categories.fillCategoryName(UPDATED_CATEGORY_NAME);
        categories.fillCategoryDescription("Rejected only in the browser fixture");
        categories.submitCategoryUpdate();

        waitFor(() -> fixture.updateWrites.get() == 1, "the rejected update reaches only the fixture");
        categories.waitForToast("Failed to update category");
        categories.waitForCategoryEditor("Edit Category");
        assertThat(categories.categoryNameValue()).isEqualTo(UPDATED_CATEGORY_NAME);
        assertThat(categories.categoryDescriptionValue()).isEqualTo("Rejected only in the browser fixture");
        categories.fillCategoryDescription("Corrected after the fixture rejection");
        assertThat(categories.categoryDescriptionValue())
                .as("the retained form remains editable after a failed save")
                .isEqualTo("Corrected after the fixture rejection");
        assertThat(categories.isToastVisible("Category updated successfully"))
                .as("a rejected write must not be reported as a successful update")
                .isFalse();
        assertThat(categories.isCategoryVisible(CATEGORY_A_NAME))
                .as("the saved category remains unchanged after the rejected write")
                .isTrue();
        assertThat(categories.isCategoryVisible(UPDATED_CATEGORY_NAME))
                .as("the rejected name must not appear in the category list")
                .isFalse();
        assertThat(fixture.createWrites.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Tag("feature-catalog")
    @Test
    @DisplayName("ADMIN-CATEGORY-SAFE-04: a rejected fixture create keeps the new category editable without a false success or list update")
    void categoryCreateErrorKeepsThePendingCategoryCorrectableWithoutFalseSuccess() {
        CategoryFixture fixture = CategoryFixture.createFails();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isCategoryFixtureUrl, fixture::handle);

        portal.openCategoriesTab();
        AdminUserCatalogModerationSafetyPage categories = new AdminUserCatalogModerationSafetyPage(adminPage);
        categories.waitForCategories();
        categories.waitForCategory(CATEGORY_A_NAME);
        categories.fillCategoryName(REJECTED_CATEGORY_NAME);
        categories.fillCategoryDescription("Rejected only in the browser fixture");
        categories.submitCategory();

        waitFor(() -> fixture.createWrites.get() == 1, "the rejected create reaches only the fixture");
        categories.waitForToast("Failed to create category");
        categories.waitForCategoryEditor("Global Categories");
        assertThat(categories.categoryNameValue()).isEqualTo(REJECTED_CATEGORY_NAME);
        assertThat(categories.categoryDescriptionValue()).isEqualTo("Rejected only in the browser fixture");
        categories.fillCategoryDescription("Corrected after the fixture rejection");
        assertThat(categories.categoryDescriptionValue())
                .as("the form remains editable after the rejected create")
                .isEqualTo("Corrected after the fixture rejection");
        assertThat(categories.isToastVisible("Category created successfully"))
                .as("a rejected create must not be reported as successful")
                .isFalse();
        assertThat(categories.isCategoryVisible(CATEGORY_A_NAME))
                .as("the existing category list must remain intact")
                .isTrue();
        assertThat(categories.isCategoryVisible(REJECTED_CATEGORY_NAME))
                .as("the rejected category must not appear in the saved list")
                .isFalse();
        assertThat(fixture.updateWrites.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Tag("feature-reviews")
    @Test
    @DisplayName("ADMIN-REVIEW-SAFE-01: lookup-mode change releases Loading and rejects a stale read; review records remain immutable")
    void reviewModeSelectionRejectsStaleResultsAndNeverExposesMutationControls() {
        ReviewFixture fixture = new ReviewFixture();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isReviewFixtureUrl, fixture::handle);

        portal.openReviewsTab();
        AdminUserCatalogModerationSafetyPage reviews = new AdminUserCatalogModerationSafetyPage(adminPage);
        reviews.waitForReviewModeration();

        reviews.fillEntityId("  " + ENTITY_ID + "  ");
        reviews.searchReviews();
        waitFor(() -> fixture.staleEntityRoute.get() != null,
                "the entity lookup must be held until a different lookup is selected");
        assertThat(queryValue(fixture.entityRequestUrl.get(), "entityId"))
                .as("entity ids are trimmed before the API request")
                .isEqualTo(ENTITY_ID);

        reviews.switchToAuthorLookup();
        reviews.fillAuthorUserId(REVIEW_AUTHOR_ID);
        reviews.searchReviews();
        reviews.waitForReviewComment(AUTHOR_REVIEW_COMMENT);

        Locator reviewCard = reviews.reviewArticleForComment(AUTHOR_REVIEW_COMMENT);
        assertThat(reviewCard.innerText()).contains(REVIEW_AUTHOR_ID, REVIEW_ORDER_ID, "author", "order", "target", "review");
        assertThat(reviewCard.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON).count())
                .as("review moderation is read-only")
                .isZero();

        Response staleResponse = adminPage.waitForResponse(
                AdminUserCatalogModerationRoutedUiTest::isEntityReviewResponse,
                fixture::releaseStaleEntityReview);
        assertThat(staleResponse.status()).isEqualTo(200);
        waitForBrowserPaint();

        assertThat(reviews.isReviewCommentVisible(AUTHOR_REVIEW_COMMENT)).isTrue();
        assertThat(reviews.isReviewCommentVisible(ENTITY_REVIEW_COMMENT))
                .as("the prior entity response cannot replace the author lookup")
                .isFalse();
        assertThat(fixture.authorReads.get()).isEqualTo(1);
        assertThat(fixture.writeAttempts.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Tag("feature-reviews")
    @Test
    @DisplayName("ADMIN-REVIEW-SAFE-02: a routed review-read failure is visible, leaves no stale cards, and restores search")
    void reviewApiErrorIsVisibleAndDoesNotExposeMutationControls() {
        ReviewFixture fixture = ReviewFixture.failingAuthorLookup();
        adminPage.route(AdminUserCatalogModerationRoutedUiTest::isReviewFixtureUrl, fixture::handle);

        portal.openReviewsTab();
        AdminUserCatalogModerationSafetyPage reviews = new AdminUserCatalogModerationSafetyPage(adminPage);
        reviews.waitForReviewModeration();
        reviews.switchToAuthorLookup();
        reviews.fillAuthorUserId(REVIEW_AUTHOR_ID);
        reviews.searchReviews();

        waitFor(() -> fixture.authorReads.get() == 1, "the fixture receives the author lookup");
        reviews.waitForReviewError(REVIEW_LOOKUP_FAILURE);
        assertThat(reviews.reviewArticleCount()).isZero();
        assertThat(reviews.isReviewSearchEnabled()).isTrue();
        assertThat(fixture.writeAttempts.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    private void waitFor(java.util.function.BooleanSupplier condition, String expectation) {
        adminPage.waitForCondition(condition, new Page.WaitForConditionOptions().setTimeout(10_000));
        assertThat(condition.getAsBoolean()).as(expectation).isTrue();
    }

    private void waitForBrowserPaint() {
        adminPage.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
    }

    private static boolean isUserFixtureUrl(String url) {
        String path = pathOf(url);
        return path.startsWith(USER_API_PATH) || path.startsWith(ACTIVE_ORDERS_PATH + "/");
    }

    private static boolean isCategoryFixtureUrl(String url) {
        String path = pathOf(url);
        return CATEGORIES_PATH.equals(path) || path.startsWith(CATEGORIES_PATH + "/");
    }

    private static boolean isReviewFixtureUrl(String url) {
        return pathOf(url).startsWith(REVIEWS_PATH);
    }

    private static boolean isActiveOrderResponse(Response response, String userId) {
        return "GET".equals(response.request().method())
                && (ACTIVE_ORDERS_PATH + "/" + userId + "/active").equals(pathOf(response.url()));
    }

    private static boolean isEntityReviewResponse(Response response) {
        return "GET".equals(response.request().method()) && REVIEWS_PATH.equals(pathOf(response.url()));
    }

    private static String pathOf(String url) {
        return URI.create(url).getPath();
    }

    private static String queryValue(String url, String name) {
        if (url == null) return null;
        String rawQuery = URI.create(url).getRawQuery();
        if (rawQuery == null) return null;
        for (String parameter : rawQuery.split("&")) {
            int separator = parameter.indexOf('=');
            String rawName = separator < 0 ? parameter : parameter.substring(0, separator);
            if (!name.equals(URLDecoder.decode(rawName, StandardCharsets.UTF_8))) continue;
            String rawValue = separator < 0 ? "" : parameter.substring(separator + 1);
            return URLDecoder.decode(rawValue, StandardCharsets.UTF_8);
        }
        return null;
    }

    private static void fulfillJson(Route route, int status, String body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setBody(body));
    }

    private static final class UserFixture {
        private enum MutationOutcome { BLOCK, SUCCESS, FAILURE }

        private final MutationOutcome mutationOutcome;
        private final boolean holdActiveOrders;
        /** Only ADMIN is manageable (MANAGEABLE_ROLES, staffRoles.isStaffRole); removal needs a holder. */
        private boolean userAHoldsAdmin;
        private final AtomicInteger listReads = new AtomicInteger();
        private final AtomicInteger activeOrderReads = new AtomicInteger();
        private final AtomicInteger roleWrites = new AtomicInteger();
        private final AtomicInteger statusWrites = new AtomicInteger();
        private final AtomicInteger blockedWrites = new AtomicInteger();
        private final AtomicInteger unexpectedRequests = new AtomicInteger();
        private final AtomicReference<String> roleRequestMethod = new AtomicReference<>();
        private final AtomicReference<String> roleRequestPath = new AtomicReference<>();
        private final AtomicReference<String> roleRequestBody = new AtomicReference<>();
        private final AtomicReference<String> statusRequestMethod = new AtomicReference<>();
        private final AtomicReference<String> statusRequestPath = new AtomicReference<>();
        private final AtomicReference<String> statusRequestBody = new AtomicReference<>();
        private final AtomicReference<Route> userAActiveOrdersRoute = new AtomicReference<>();
        private final AtomicReference<Route> userBActiveOrdersRoute = new AtomicReference<>();

        private UserFixture() {
            this(MutationOutcome.BLOCK, true);
        }

        private UserFixture(MutationOutcome mutationOutcome, boolean holdActiveOrders) {
            this.mutationOutcome = mutationOutcome;
            this.holdActiveOrders = holdActiveOrders;
        }

        private static UserFixture fulfillMutations() {
            return new UserFixture(MutationOutcome.SUCCESS, false);
        }

        private static UserFixture rejectMutations() {
            return new UserFixture(MutationOutcome.FAILURE, false);
        }

        private UserFixture withUserAHoldingAdmin() {
            userAHoldsAdmin = true;
            return this;
        }

        private void handle(Route route) {
            Request request = route.request();
            String method = request.method();
            String path = pathOf(request.url());

            if ("GET".equals(method) && (USER_API_PATH + "/all").equals(path)) {
                listReads.incrementAndGet();
                fulfillJson(route, 200, userPageJson());
                return;
            }

            if ("GET".equals(method) && (ACTIVE_ORDERS_PATH + "/" + USER_A_ID + "/active").equals(path)) {
                activeOrderReads.incrementAndGet();
                fulfillOrCaptureActiveOrders(route, USER_A_ID, userAActiveOrdersRoute);
                return;
            }

            if ("GET".equals(method) && (ACTIVE_ORDERS_PATH + "/" + USER_B_ID + "/active").equals(path)) {
                activeOrderReads.incrementAndGet();
                fulfillOrCaptureActiveOrders(route, USER_B_ID, userBActiveOrdersRoute);
                return;
            }

            if ("DELETE".equals(method) && (USER_API_PATH + "/" + USER_A_ID + "/roles/ADMIN").equals(path)) {
                roleWrites.incrementAndGet();
                recordRoleRequest(request, path);
                handleRoleMutation(route);
                return;
            }

            if ("POST".equals(method) && (USER_API_PATH + "/" + USER_A_ID + "/roles").equals(path)) {
                roleWrites.incrementAndGet();
                recordRoleRequest(request, path);
                handleRoleMutation(route);
                return;
            }

            if ("PUT".equals(method) && isFixtureUserStatusPath(path)) {
                statusWrites.incrementAndGet();
                statusRequestMethod.set(method);
                statusRequestPath.set(path);
                statusRequestBody.set(request.postData());
                handleStatusUpdate(route);
                return;
            }

            if (isWrite(method)) {
                blockedWrites.incrementAndGet();
                if (path.endsWith("/roles")) roleWrites.incrementAndGet();
                if (path.endsWith("/status")) statusWrites.incrementAndGet();
                fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked an admin user mutation\"}");
                return;
            }

            unexpectedRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected admin user fixture request\"}");
        }

        private Route activeOrderRoute(String userId) {
            return USER_A_ID.equals(userId) ? userAActiveOrdersRoute.get() : userBActiveOrdersRoute.get();
        }

        private void fulfillOrCaptureActiveOrders(Route route, String userId, AtomicReference<Route> target) {
            if (!holdActiveOrders) {
                fulfillJson(route, 200, activeOrdersJson(userId));
                return;
            }
            if (target.compareAndSet(null, route)) return;
            unexpectedRequests.incrementAndGet();
            fulfillJson(route, 409, "{\"message\":\"Duplicate active-order fixture request\"}");
        }

        private static boolean isFixtureUserStatusPath(String path) {
            return (USER_API_PATH + "/" + USER_A_ID + "/status").equals(path)
                    || (USER_API_PATH + "/" + USER_B_ID + "/status").equals(path);
        }

        private void recordRoleRequest(Request request, String path) {
            roleRequestMethod.set(request.method());
            roleRequestPath.set(path);
            roleRequestBody.set(request.postData());
        }

        private void handleRoleMutation(Route route) {
            if (mutationOutcome == MutationOutcome.SUCCESS) {
                fulfillJson(route, 200, mutationSuccessJson());
                return;
            }
            if (mutationOutcome == MutationOutcome.FAILURE) {
                fulfillJson(route, 409, "{\"message\":\"" + ROLE_REMOVE_FAILURE + "\"}");
                return;
            }
            blockedWrites.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked an admin role mutation\"}");
        }

        private void handleStatusUpdate(Route route) {
            if (mutationOutcome == MutationOutcome.SUCCESS) {
                fulfillJson(route, 200, mutationSuccessJson());
                return;
            }
            if (mutationOutcome == MutationOutcome.FAILURE) {
                fulfillJson(route, 409, "{\"message\":\"" + STATUS_UPDATE_FAILURE + "\"}");
                return;
            }
            blockedWrites.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked an admin status mutation\"}");
        }

        private static String mutationSuccessJson() {
            return """
                    {
                      "success": true,
                      "message": "fixture mutation accepted",
                      "timestamp": "%s"
                    }
                    """.formatted(FIXTURE_TIME);
        }

        private void releaseActiveOrders(String userId) {
            AtomicReference<Route> target = USER_A_ID.equals(userId)
                    ? userAActiveOrdersRoute : userBActiveOrdersRoute;
            Route route = target.getAndSet(null);
            if (route == null) throw new IllegalStateException("No pending active-order route for " + userId);
            fulfillJson(route, 200, activeOrdersJson(userId));
        }

        private String userPageJson() {
            return """
                    {
                      "success": true,
                      "message": "fixture",
                      "data": {
                        "content": [
                          {"id":"%s","phoneNumber":"%s","roles":%s,"active":true},
                          {"id":"%s","phoneNumber":"%s","roles":["CUSTOMER"],"active":false}
                        ],
                        "totalElements": 2,
                        "totalPages": 1,
                        "last": true,
                        "size": 20,
                        "number": 0,
                        "first": true,
                        "numberOfElements": 2,
                        "empty": false
                      },
                      "timestamp": "%s"
                    }
                    """.formatted(USER_A_ID, USER_A_PHONE, userAHoldsAdmin ? "[\"CUSTOMER\",\"ADMIN\"]" : "[\"CUSTOMER\"]",
                            USER_B_ID, USER_B_PHONE, FIXTURE_TIME);
        }

        private static String activeOrdersJson(String userId) {
            boolean firstUser = USER_A_ID.equals(userId);
            String restaurant = firstUser ? USER_A_RESTAURANT : USER_B_RESTAURANT;
            String orderId = firstUser
                    ? "a1000000-0000-4000-8000-000000000003"
                    : "b1000000-0000-4000-8000-000000000003";
            String restaurantId = firstUser
                    ? "a1000000-0000-4000-8000-000000000004"
                    : "b1000000-0000-4000-8000-000000000004";
            return """
                    {
                      "success": true,
                      "message": "fixture",
                      "data": {
                        "content": [{
                          "id":"%s",
                          "customerId":"%s",
                          "restaurantId":"%s",
                          "restaurantName":"%s",
                          "status":"ACCEPTED",
                          "deliveryStatus":"PENDING",
                          "totalAmount":125.0,
                          "itemTotal":100.0,
                          "customerPlatformFee":5.0,
                          "sgst":5.0,
                          "cgst":5.0,
                          "deliveryFee":10.0,
                          "deliveryAddress":"Fixture address",
                          "items":[],
                          "createdAt":"%s"
                        }],
                        "totalElements": 1,
                        "totalPages": 1,
                        "last": true,
                        "size": 20,
                        "number": 0,
                        "first": true,
                        "numberOfElements": 1,
                        "empty": false
                      },
                      "timestamp": "%s"
                    }
                    """.formatted(orderId, userId, restaurantId, restaurant, FIXTURE_TIME, FIXTURE_TIME);
        }
    }

    private static final class CategoryFixture {
        private enum CreateOutcome { SUCCESS, FAILURE }
        private enum UpdateOutcome { BLOCK, SUCCESS, FAILURE }

        private final CreateOutcome createOutcome;
        private final UpdateOutcome updateOutcome;
        private final AtomicBoolean created = new AtomicBoolean(false);
        private final AtomicBoolean updated = new AtomicBoolean(false);
        private final AtomicInteger categoryReads = new AtomicInteger();
        private final AtomicInteger createWrites = new AtomicInteger();
        private final AtomicInteger updateWrites = new AtomicInteger();
        private final AtomicInteger unexpectedRequests = new AtomicInteger();
        private final AtomicReference<String> createRequestBody = new AtomicReference<>();
        private final AtomicReference<String> updateRequestBody = new AtomicReference<>();

        private CategoryFixture() {
            this(CreateOutcome.SUCCESS, UpdateOutcome.BLOCK);
        }

        private CategoryFixture(UpdateOutcome updateOutcome) {
            this(CreateOutcome.SUCCESS, updateOutcome);
        }

        private CategoryFixture(CreateOutcome createOutcome, UpdateOutcome updateOutcome) {
            this.createOutcome = createOutcome;
            this.updateOutcome = updateOutcome;
        }

        private static CategoryFixture updateSucceeds() {
            return new CategoryFixture(UpdateOutcome.SUCCESS);
        }

        private static CategoryFixture updateFails() {
            return new CategoryFixture(UpdateOutcome.FAILURE);
        }

        private static CategoryFixture createFails() {
            return new CategoryFixture(CreateOutcome.FAILURE, UpdateOutcome.BLOCK);
        }

        private void handle(Route route) {
            Request request = route.request();
            String method = request.method();
            String path = pathOf(request.url());

            if ("GET".equals(method) && CATEGORIES_PATH.equals(path)) {
                categoryReads.incrementAndGet();
                fulfillJson(route, 200, categoriesJson(created.get(), updated.get()));
                return;
            }

            if ("POST".equals(method) && CATEGORIES_PATH.equals(path)) {
                createWrites.incrementAndGet();
                createRequestBody.set(request.postData());
                if (createOutcome == CreateOutcome.SUCCESS) {
                    created.set(true);
                    fulfillJson(route, 201, categoryCreatedJson());
                    return;
                }
                fulfillJson(route, 409, "{\"message\":\"Fixture category create rejected\"}");
                return;
            }

            if ("PUT".equals(method) && path.startsWith(CATEGORIES_PATH + "/")) {
                updateWrites.incrementAndGet();
                updateRequestBody.set(request.postData());
                if (updateOutcome == UpdateOutcome.SUCCESS) {
                    updated.set(true);
                    fulfillJson(route, 200, categoryUpdatedJson());
                    return;
                }
                if (updateOutcome == UpdateOutcome.FAILURE) {
                    fulfillJson(route, 409, "{\"message\":\"Fixture category update rejected\"}");
                    return;
                }
                fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked a category update\"}");
                return;
            }

            unexpectedRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected category fixture request\"}");
        }

        private static String categoriesJson(boolean includeCreated, boolean includeUpdated) {
            String createdCategory = includeCreated
                    ? ",{\"id\":\"c1000000-0000-4000-8000-000000000002\",\"name\":\""
                    + CREATED_CATEGORY_NAME + "\",\"description\":\"Created only in the browser fixture\"}"
                    : "";
            String primaryName = includeUpdated ? UPDATED_CATEGORY_NAME : CATEGORY_A_NAME;
            String primaryDescription = includeUpdated
                    ? "Updated only in the browser fixture"
                    : "Existing fixture category";
            return """
                    {
                      "success": true,
                      "message": "fixture",
                      "data": [{
                        "id":"%s",
                        "name":"%s",
                        "description":"%s"
                      }%s],
                      "timestamp":"%s"
                    }
                    """.formatted(CATEGORY_A_ID, primaryName, primaryDescription, createdCategory, FIXTURE_TIME);
        }

        private static String categoryCreatedJson() {
            return """
                    {
                      "success": true,
                      "message": "fixture created",
                      "data": {
                        "id":"c1000000-0000-4000-8000-000000000002",
                        "name":"%s",
                        "description":"Created only in the browser fixture"
                      },
                      "timestamp":"%s"
                    }
                    """.formatted(CREATED_CATEGORY_NAME, FIXTURE_TIME);
        }

        private static String categoryUpdatedJson() {
            return """
                    {
                      "success": true,
                      "message": "fixture updated",
                      "data": {
                        "id":"%s",
                        "name":"%s",
                        "description":"Updated only in the browser fixture"
                      },
                      "timestamp":"%s"
                    }
                    """.formatted(CATEGORY_A_ID, UPDATED_CATEGORY_NAME, FIXTURE_TIME);
        }
    }

    private static final class ReviewFixture {
        private final boolean failAuthorLookup;
        private final AtomicInteger authorReads = new AtomicInteger();
        private final AtomicInteger writeAttempts = new AtomicInteger();
        private final AtomicInteger unexpectedRequests = new AtomicInteger();
        private final AtomicReference<Route> staleEntityRoute = new AtomicReference<>();
        private final AtomicReference<String> entityRequestUrl = new AtomicReference<>();

        private ReviewFixture() {
            this(false);
        }

        private ReviewFixture(boolean failAuthorLookup) {
            this.failAuthorLookup = failAuthorLookup;
        }

        private static ReviewFixture failingAuthorLookup() {
            return new ReviewFixture(true);
        }

        private void handle(Route route) {
            Request request = route.request();
            String method = request.method();
            String path = pathOf(request.url());

            if ("GET".equals(method) && REVIEWS_PATH.equals(path)) {
                entityRequestUrl.set(request.url());
                if (staleEntityRoute.compareAndSet(null, route)) return;
                unexpectedRequests.incrementAndGet();
                fulfillJson(route, 409, "{\"message\":\"Duplicate entity review lookup\"}");
                return;
            }

            if ("GET".equals(method) && (REVIEWS_PATH + "/by-user/" + REVIEW_AUTHOR_ID).equals(path)) {
                authorReads.incrementAndGet();
                if (failAuthorLookup) {
                    fulfillJson(route, 503, "{\"message\":\"" + REVIEW_LOOKUP_FAILURE + "\"}");
                    return;
                }
                fulfillJson(route, 200, authorReviewsJson());
                return;
            }

            if (isWrite(method)) {
                writeAttempts.incrementAndGet();
                fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked review mutation\"}");
                return;
            }

            unexpectedRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected review fixture request\"}");
        }

        private void releaseStaleEntityReview() {
            Route route = staleEntityRoute.getAndSet(null);
            if (route == null) throw new IllegalStateException("No stale entity review route was captured");
            fulfillJson(route, 200, entityReviewsJson());
        }

        private static String entityReviewsJson() {
            return reviewEnvelope("e1000000-0000-4000-8000-000000000001", ENTITY_REVIEW_COMMENT);
        }

        private static String authorReviewsJson() {
            return reviewEnvelope("e1000000-0000-4000-8000-000000000002", AUTHOR_REVIEW_COMMENT);
        }

        private static String reviewEnvelope(String reviewId, String comment) {
            return """
                    {
                      "success": true,
                      "message": "fixture",
                      "data": {
                        "content": [{
                          "id":"%s",
                          "entityType":"RESTAURANT",
                          "entityId":"%s",
                          "orderId":"%s",
                          "userId":"%s",
                          "authorRole":"CUSTOMER",
                          "visibility":"PUBLIC",
                          "authorDisplayName":"Fixture reviewer",
                          "rating":4,
                          "comment":"%s",
                          "createdAt":"%s"
                        }]
                      },
                      "timestamp":"%s"
                    }
                    """.formatted(reviewId, ENTITY_ID, REVIEW_ORDER_ID, REVIEW_AUTHOR_ID,
                    comment, FIXTURE_TIME, FIXTURE_TIME);
        }
    }

    private static boolean isWrite(String method) {
        return "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);
    }
}
