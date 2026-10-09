package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPayoutSafetyPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser-routed payout lifecycle coverage.
 *
 * <p>Each test first signs into the deployed application as an administrator. It then supplies a
 * disposable, stateful payout fixture directly in that browser. Reads and the single intended
 * transition receive an explicit response; every other payout write is terminated by the browser
 * fixture. No test can alter shared Dev financial data.</p>
 */
@Tag("browser-routed")
@Tag("feature-money-ledger")
public class AdminPayoutSafetyUiTest extends TestBase {

    private static final String PAYOUTS_PATH = "/api/v1/internal/admin/payouts";
    private static final String LEDGER_STATEMENT_PATH = "/api/v1/ledger/statements";
    private static final String PAYOUT_ID = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";
    private static final String PAYEE_ID = "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb";
    private static final String OTHER_ADMIN_ID = "cccccccc-cccc-4ccc-8ccc-cccccccccccc";
    private static final String PAYEE_NAME = "Fixture Kitchen";
    private static final String BANK_REFERENCE = "UTR-FIXTURE-20260929";
    private static final String FAILURE_REASON = "Fixture beneficiary account was closed";
    private static final String FAILURE_MESSAGE = "Fixture payout transition was rejected";
    private static final String CREATE_RESPONSE_LOST_FAILURE = "Fixture payout creation response was lost";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";

    private AdminPortalPage portal;

    @BeforeEach
    void loginAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("PAYOUT-SAFE-01: payout dialogs block invalid input and cancellation sends no write")
    void payoutDialogsAreClientValidatedAndCancelableWithoutWrites() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.blockAllMutations(LifecycleState.APPROVED);
        AdminPayoutSafetyPage payouts = openHistoryPayout(fixture);

        payouts.openMarkPaidDialog();
        payouts.fillBankReference("  ");
        assertThat(payouts.isConfirmPaymentDisabled())
                .as("a whitespace-only bank reference must not enable payment")
                .isTrue();
        assertThat(fixture.transitionWrites.get()).isZero();

        payouts.fillBankReference(BANK_REFERENCE);
        assertThat(payouts.isConfirmPaymentEnabled())
                .as("a meaningful bank reference should make the confirmation actionable")
                .isTrue();
        payouts.cancelMarkPaidDialog();

        payouts.openFailPayoutDialog();
        payouts.fillFailureReason(" no ");
        assertThat(payouts.isMarkFailedDisabled())
                .as("a failure reason shorter than five meaningful characters must be blocked")
                .isTrue();
        payouts.cancelFailPayoutDialog();

        assertThat(fixture.transitionWrites.get())
                .as("cancelling a ready-to-submit terminal confirmation must not write")
                .isZero();
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.historyReads.get()).isPositive();
        assertThat(fixture.detailReads.get()).isPositive();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("PAYOUT-SAFE-02: a fixture-backed draft creation sends the exact request and renders DRAFT")
    void payoutCreationUsesOnlyTheFulfilledFixtureAndOpensTheDraft() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.allow(LifecycleState.NONE, Transition.CREATE);
        AdminPayoutSafetyPage payouts = openPendingPayout(fixture);

        payouts.openCreatePayoutDialog();
        payouts.confirmCreateDraftPayout();

        waitFor(() -> fixture.transitionWrites.get() == 1,
                "the browser fixture receives the requested draft creation");
        payouts.waitForPayoutStatus(LifecycleState.DRAFT.name());

        RecordedRequest request = requireRequest(fixture, Transition.CREATE);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PAYOUTS_PATH);
        assertThat(request.body())
                .contains("\"payeeId\":\"" + PAYEE_ID + "\"")
                .contains("\"payeeType\":\"RESTAURANT\"")
                .contains("\"force\":false");
        assertIdempotencyKey(request.idempotencyKey());
        assertThat(fixture.statementReads.get())
                .as("the drawer's unsettled-line read is supplied by the same local fixture")
                .isPositive();
        assertThat(fixture.historyReads.get()).isPositive();
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("PAYOUT-SAFE-08: a named unverified payee requires an explicit override and sends force true")
    void unverifiedNamedPayeeRequiresExplicitOverrideAndSendsForceTrue() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.allowUnverifiedCreate();
        AdminPayoutSafetyPage payouts = openPendingPayout(fixture);

        payouts.openCreatePayoutDialog();
        payouts.waitForCreatePayoutPayee(PAYEE_NAME);
        payouts.waitForUnverifiedBankOverride();
        assertThat(payouts.isUnverifiedBankOverrideChecked())
                .as("the named but unverified payee must start without a force override")
                .isFalse();
        assertThat(payouts.isCreateDraftPayoutVisible())
                .as("the financial action must remain unavailable until the explicit override is checked")
                .isFalse();
        assertThat(fixture.transitionWrites.get())
                .as("opening an unverified-payee dialog must not create a payout")
                .isZero();

        payouts.enableUnverifiedBankOverride();
        assertThat(payouts.isUnverifiedBankOverrideChecked()).isTrue();
        assertThat(payouts.isCreateDraftPayoutVisible())
                .as("checking the explicit override enables the intentional create action")
                .isTrue();
        payouts.confirmCreateDraftPayout();

        waitFor(() -> fixture.transitionWrites.get() == 1,
                "the browser fixture receives the explicit unverified-beneficiary override");
        payouts.waitForPayoutStatus(LifecycleState.DRAFT.name());

        RecordedRequest request = requireRequest(fixture, Transition.CREATE);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PAYOUTS_PATH);
        assertThat(request.body())
                .contains("\"payeeId\":\"" + PAYEE_ID + "\"")
                .contains("\"payeeType\":\"RESTAURANT\"")
                .contains("\"force\":true")
                .doesNotContain("\"force\":false");
        assertIdempotencyKey(request.idempotencyKey());
        assertThat(fixture.statementReads.get()).isPositive();
        assertThat(fixture.historyReads.get()).isPositive();
        assertNoUnexpectedPayoutRequests(fixture);
    }

    @Test
    @DisplayName("PAYOUT-SAFE-09: an ambiguous create failure retries the committed draft with its original idempotency key")
    void ambiguousCreateFailureReusesTheIdempotencyKeyAndOpensTheExistingDraft() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.loseCreateResponse();
        AdminPayoutSafetyPage payouts = openPendingPayout(fixture);

        payouts.openCreatePayoutDialog();
        payouts.confirmCreateDraftPayout();
        waitFor(() -> fixture.transitionWrites.get() == 1,
                "the fixture simulates a committed create whose response never reached the browser");
        payouts.waitForToast(CREATE_RESPONSE_LOST_FAILURE);

        payouts.confirmCreateDraftPayout();
        waitFor(() -> fixture.transitionWrites.get() == 2,
                "the browser retries the create after the ambiguous response");
        payouts.waitForPayoutStatus(LifecycleState.DRAFT.name());

        assertThat(fixture.createRequests).hasSize(2);
        assertThat(fixture.createRequests.get(1).idempotencyKey())
                .as("an ambiguous payout-create retry must ask LedgerService for the first durable result")
                .isEqualTo(fixture.createRequests.get(0).idempotencyKey());
        assertIdempotencyKey(fixture.createRequests.get(0).idempotencyKey());
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("PAYOUT-SAFE-10: an administrator cannot approve a fixture draft they created")
    void currentAdministratorCannotApproveTheirOwnDraftPayout() {
        String currentAdminId = authenticatedAdminIdFromSessionProfile();
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.selfApprovalBlocked(currentAdminId);
        AdminPayoutSafetyPage payouts = openHistoryPayout(fixture);

        assertThat(payouts.isPayoutActionVisible("Approve"))
                .as("a self-created draft still identifies the unavailable approval action")
                .isTrue();
        assertThat(payouts.isPayoutActionDisabled("Approve"))
                .as("the active administrator must not be able to approve a payout they created")
                .isTrue();
        assertThat(fixture.transitionWrites.get())
                .as("the disabled four-eyes control must not send an approval request")
                .isZero();
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.detailReads.get()).isPositive();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("PAYOUT-SAFE-03: approving a draft uses a fulfilled fixture write and renders APPROVED")
    void approvedDraftUsesOnlyTheFulfilledFixtureAndRendersApproved() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.allow(LifecycleState.DRAFT, Transition.APPROVE);
        AdminPayoutSafetyPage payouts = openHistoryPayout(fixture);

        payouts.approveDraftPayout();
        waitFor(() -> fixture.transitionWrites.get() == 1,
                "the browser fixture receives the approval request");
        payouts.waitForToast("Payout approved successfully");
        payouts.waitForPayoutStatus(LifecycleState.APPROVED.name());

        RecordedRequest request = requireRequest(fixture, Transition.APPROVE);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PAYOUTS_PATH + "/" + PAYOUT_ID + "/approve");
        assertThat(request.body()).isBlank();
        assertIdempotencyKey(request.idempotencyKey());
        assertThat(payouts.isPayoutActionVisible("Mark Paid"))
                .as("the approved detail exposes the next permitted lifecycle action")
                .isTrue();
        assertThat(payouts.isPayoutActionVisible("Fail Payout")).isTrue();
        assertThat(fixture.detailReads.get()).isGreaterThanOrEqualTo(2);
        assertNoUnexpectedPayoutRequests(fixture);
    }

    @Test
    @DisplayName("PAYOUT-SAFE-04: cancelling a draft uses a fulfilled fixture write and renders CANCELLED")
    void cancelledDraftUsesOnlyTheFulfilledFixtureAndRendersCancelled() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.allow(LifecycleState.DRAFT, Transition.CANCEL);
        AdminPayoutSafetyPage payouts = openHistoryPayout(fixture);

        payouts.cancelDraftPayout();
        waitFor(() -> fixture.transitionWrites.get() == 1,
                "the browser fixture receives the cancellation request");
        payouts.waitForToast("Payout cancelled");
        payouts.waitForPayoutStatus(LifecycleState.CANCELLED.name());

        RecordedRequest request = requireRequest(fixture, Transition.CANCEL);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PAYOUTS_PATH + "/" + PAYOUT_ID + "/cancel");
        assertThat(request.body()).isBlank();
        assertIdempotencyKey(request.idempotencyKey());
        assertThat(payouts.isPayoutActionVisible("Approve")).isFalse();
        assertThat(payouts.isPayoutActionVisible("Cancel")).isFalse();
        assertThat(fixture.detailReads.get()).isGreaterThanOrEqualTo(2);
        assertNoUnexpectedPayoutRequests(fixture);
    }

    @Test
    @DisplayName("PAYOUT-SAFE-05: marking an approved payout paid uses an exact fixture request and renders PAID")
    void markPaidUsesOnlyTheFulfilledFixtureAndRendersPaid() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.allow(LifecycleState.APPROVED, Transition.MARK_PAID);
        AdminPayoutSafetyPage payouts = openHistoryPayout(fixture);

        payouts.openMarkPaidDialog();
        payouts.fillBankReference(BANK_REFERENCE);
        payouts.confirmMarkPaid();

        waitFor(() -> fixture.transitionWrites.get() == 1,
                "the browser fixture receives the mark-paid request");
        payouts.waitForToast("Payout marked as paid");
        payouts.waitForPayoutStatus(LifecycleState.PAID.name());

        RecordedRequest request = requireRequest(fixture, Transition.MARK_PAID);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PAYOUTS_PATH + "/" + PAYOUT_ID + "/mark-paid");
        assertThat(queryValue(request.url(), "bankReference")).isEqualTo(BANK_REFERENCE);
        assertIdempotencyKey(request.idempotencyKey());
        assertThat(fixture.bankReference.get())
                .as("the next detail read carries the persisted fixture bank reference")
                .isEqualTo(BANK_REFERENCE);
        payouts.waitForBankReference(BANK_REFERENCE);
        assertThat(payouts.isPayoutActionVisible("Mark Paid")).isFalse();
        assertThat(payouts.isPayoutActionVisible("Fail Payout")).isFalse();
        assertThat(fixture.detailReads.get()).isGreaterThanOrEqualTo(2);
        assertNoUnexpectedPayoutRequests(fixture);
    }

    @Test
    @DisplayName("PAYOUT-SAFE-06: failing an approved payout uses an exact fixture request and renders FAILED")
    void markFailedUsesOnlyTheFulfilledFixtureAndRendersFailed() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.allow(LifecycleState.APPROVED, Transition.FAIL);
        AdminPayoutSafetyPage payouts = openHistoryPayout(fixture);

        payouts.openFailPayoutDialog();
        payouts.fillFailureReason(FAILURE_REASON);
        payouts.confirmFailPayout();

        waitFor(() -> fixture.transitionWrites.get() == 1,
                "the browser fixture receives the mark-failed request");
        payouts.waitForToast("Payout marked as failed");
        payouts.waitForPayoutStatus(LifecycleState.FAILED.name());

        RecordedRequest request = requireRequest(fixture, Transition.FAIL);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PAYOUTS_PATH + "/" + PAYOUT_ID + "/fail");
        assertThat(queryValue(request.url(), "reason")).isEqualTo(FAILURE_REASON);
        assertIdempotencyKey(request.idempotencyKey());
        assertThat(fixture.failureReason.get())
                .as("the terminal detail must show the reason returned by the successful transition")
                .isEqualTo(FAILURE_REASON);
        payouts.waitForFailureReason(FAILURE_REASON);
        assertThat(payouts.isPayoutActionVisible("Mark Paid")).isFalse();
        assertThat(payouts.isPayoutActionVisible("Fail Payout")).isFalse();
        assertThat(fixture.detailReads.get()).isGreaterThanOrEqualTo(2);
        assertNoUnexpectedPayoutRequests(fixture);
    }

    @Test
    @DisplayName("PAYOUT-SAFE-07: a rejected fail request keeps the approved payout and the operator reason visible")
    void failedPayoutTransitionSurfacesTheErrorWithoutPretendingThePayoutChanged() {
        PayoutLifecycleFixture fixture = PayoutLifecycleFixture.reject(LifecycleState.APPROVED, Transition.FAIL);
        AdminPayoutSafetyPage payouts = openHistoryPayout(fixture);

        payouts.openFailPayoutDialog();
        payouts.fillFailureReason(FAILURE_REASON);
        payouts.confirmFailPayout();

        waitFor(() -> fixture.transitionWrites.get() == 1,
                "the rejected request reaches only the browser fixture");
        payouts.waitForToast(FAILURE_MESSAGE);
        waitFor(() -> fixture.detailReads.get() >= 2,
                "the client refreshes details after a rejected terminal transition");
        payouts.waitForPayoutStatus(LifecycleState.APPROVED.name());
        payouts.waitForFailPayoutDialog();

        RecordedRequest request = requireRequest(fixture, Transition.FAIL);
        assertThat(request.path()).isEqualTo(PAYOUTS_PATH + "/" + PAYOUT_ID + "/fail");
        assertThat(queryValue(request.url(), "reason")).isEqualTo(FAILURE_REASON);
        assertIdempotencyKey(request.idempotencyKey());
        assertThat(payouts.isFailPayoutDialogVisible())
                .as("the operator can correct or retry an unsuccessful terminal action")
                .isTrue();
        assertThat(payouts.failureReasonValue()).isEqualTo(FAILURE_REASON);
        assertThat(payouts.isPayoutActionVisible("Mark Paid")).isTrue();
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    private AdminPayoutSafetyPage openPendingPayout(PayoutLifecycleFixture fixture) {
        registerFixtureRoutes(fixture);
        portal.openPayoutsTab();

        AdminPayoutSafetyPage payouts = new AdminPayoutSafetyPage(adminPage);
        payouts.waitForPayouts();
        payouts.waitForPendingPayee(PAYEE_NAME);
        payouts.openPendingPayee(PAYEE_NAME);
        waitFor(() -> fixture.statementReads.get() > 0 && fixture.historyReads.get() > 0,
                "the pending-payout drawer reads only browser-local statement and history fixtures");
        return payouts;
    }

    private AdminPayoutSafetyPage openHistoryPayout(PayoutLifecycleFixture fixture) {
        registerFixtureRoutes(fixture);
        portal.openPayoutsTab();

        AdminPayoutSafetyPage payouts = new AdminPayoutSafetyPage(adminPage);
        payouts.waitForPayouts();
        payouts.openHistory();
        payouts.searchHistory(PAYEE_ID);
        payouts.openFirstHistoryPayout();
        payouts.waitForDetails();
        payouts.waitForPayoutStatus(fixture.state.get().name());
        return payouts;
    }

    private void registerFixtureRoutes(PayoutLifecycleFixture fixture) {
        adminPage.route(AdminPayoutSafetyUiTest::isPayoutFixtureUrl, fixture::handle);
    }

    /**
     * Reads only the user id the application saved after the successful browser login. The payout
     * UI itself reads this {@code user_profile.id} through {@code getUserProfile()}, so using it
     * for the fixture's {@code createdBy} exercises the real session identity comparison without
     * reading an authentication token, invoking a live identity endpoint, or exposing a secret.
     */
    private String authenticatedAdminIdFromSessionProfile() {
        Object sessionProfileId = adminPage.evaluate("""
                () => {
                  const storedProfile = window.localStorage.getItem('user_profile');
                  if (!storedProfile) return null;
                  try {
                    const profile = JSON.parse(storedProfile);
                    return typeof profile?.id === 'string' ? profile.id : null;
                  } catch {
                    return null;
                  }
                }
                """);
        String adminId = sessionProfileId instanceof String id ? id : null;
        assertThat(adminId)
                .as("the authenticated admin session must provide the user_profile.id used by PayoutDetail")
                .matches("^[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}$");
        return adminId;
    }

    private void waitFor(java.util.function.BooleanSupplier condition, String expectation) {
        adminPage.waitForCondition(condition, new Page.WaitForConditionOptions().setTimeout(10_000));
        assertThat(condition.getAsBoolean()).as(expectation).isTrue();
    }

    private static boolean isPayoutFixtureUrl(String url) {
        String path = path(url);
        return PAYOUTS_PATH.equals(path)
                || path.startsWith(PAYOUTS_PATH + "/")
                || path.startsWith(LEDGER_STATEMENT_PATH + "/");
    }

    private static void assertNoUnexpectedPayoutRequests(PayoutLifecycleFixture fixture) {
        assertThat(fixture.transitionWrites.get()).isEqualTo(1);
        assertThat(fixture.blockedMutationAttempts.get())
                .as("the test must not attempt a payout mutation outside its single permitted transition")
                .isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    private static RecordedRequest requireRequest(PayoutLifecycleFixture fixture, Transition transition) {
        RecordedRequest request = fixture.requestFor(transition);
        assertThat(request).as("recorded %s request", transition).isNotNull();
        return request;
    }

    private static void assertIdempotencyKey(String idempotencyKey) {
        assertThat(idempotencyKey)
                .as("every financial transition must include an idempotency key")
                .matches("^[A-Za-z0-9][A-Za-z0-9._:-]{7,79}$");
    }

    private static String path(String url) {
        return URI.create(url).getPath();
    }

    private static String queryValue(String url, String name) {
        String rawQuery = URI.create(url).getRawQuery();
        if (rawQuery == null) return null;
        for (String parameter : rawQuery.split("&")) {
            int separator = parameter.indexOf('=');
            String encodedName = separator < 0 ? parameter : parameter.substring(0, separator);
            if (!name.equals(URLDecoder.decode(encodedName, StandardCharsets.UTF_8))) continue;
            String encodedValue = separator < 0 ? "" : parameter.substring(separator + 1);
            return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8);
        }
        return null;
    }

    private enum LifecycleState { NONE, DRAFT, APPROVED, PAID, FAILED, CANCELLED }

    private enum Transition { CREATE, APPROVE, CANCEL, MARK_PAID, FAIL }

    private record RecordedRequest(String method, String path, String url, String body, String idempotencyKey) { }

    private static final class PayoutLifecycleFixture {
        private final Transition permittedTransition;
        private final boolean rejectPermittedTransition;
        private final boolean loseCreateResponse;
        private final boolean beneficiaryVerified;
        private final String payoutCreatorId;
        private final AtomicReference<LifecycleState> state;
        private final AtomicInteger pendingReads = new AtomicInteger();
        private final AtomicInteger historyReads = new AtomicInteger();
        private final AtomicInteger detailReads = new AtomicInteger();
        private final AtomicInteger statementReads = new AtomicInteger();
        private final AtomicInteger transitionWrites = new AtomicInteger();
        private final AtomicInteger blockedMutationAttempts = new AtomicInteger();
        private final AtomicInteger unexpectedRequests = new AtomicInteger();
        private final AtomicReference<String> bankReference = new AtomicReference<>();
        private final AtomicReference<String> failureReason = new AtomicReference<>();
        private final AtomicReference<String> committedCreateKey = new AtomicReference<>();
        private final List<RecordedRequest> createRequests = new CopyOnWriteArrayList<>();
        private final Map<Transition, AtomicReference<RecordedRequest>> requests = new EnumMap<>(Transition.class);

        private PayoutLifecycleFixture(LifecycleState initialState, Transition permittedTransition,
                                       boolean rejectPermittedTransition) {
            this(initialState, permittedTransition, rejectPermittedTransition, false, true);
        }

        private PayoutLifecycleFixture(LifecycleState initialState, Transition permittedTransition,
                                       boolean rejectPermittedTransition, boolean loseCreateResponse,
                                       boolean beneficiaryVerified) {
            this(initialState, permittedTransition, rejectPermittedTransition, loseCreateResponse,
                    beneficiaryVerified, OTHER_ADMIN_ID);
        }

        private PayoutLifecycleFixture(LifecycleState initialState, Transition permittedTransition,
                                       boolean rejectPermittedTransition, boolean loseCreateResponse,
                                       boolean beneficiaryVerified, String payoutCreatorId) {
            this.permittedTransition = permittedTransition;
            this.rejectPermittedTransition = rejectPermittedTransition;
            this.loseCreateResponse = loseCreateResponse;
            this.beneficiaryVerified = beneficiaryVerified;
            this.payoutCreatorId = Objects.requireNonNull(payoutCreatorId, "payoutCreatorId");
            this.state = new AtomicReference<>(initialState);
            for (Transition transition : Transition.values()) {
                requests.put(transition, new AtomicReference<>());
            }
        }

        private static PayoutLifecycleFixture blockAllMutations(LifecycleState initialState) {
            return new PayoutLifecycleFixture(initialState, null, false);
        }

        private static PayoutLifecycleFixture allow(LifecycleState initialState, Transition permittedTransition) {
            return new PayoutLifecycleFixture(initialState, permittedTransition, false);
        }

        private static PayoutLifecycleFixture allowUnverifiedCreate() {
            return new PayoutLifecycleFixture(LifecycleState.NONE, Transition.CREATE, false, false, false);
        }

        private static PayoutLifecycleFixture loseCreateResponse() {
            return new PayoutLifecycleFixture(LifecycleState.NONE, Transition.CREATE, false, true, true);
        }

        private static PayoutLifecycleFixture selfApprovalBlocked(String currentAdminId) {
            return new PayoutLifecycleFixture(LifecycleState.DRAFT, null, false, false, true, currentAdminId);
        }

        private static PayoutLifecycleFixture reject(LifecycleState initialState, Transition permittedTransition) {
            return new PayoutLifecycleFixture(initialState, permittedTransition, true);
        }

        private RecordedRequest requestFor(Transition transition) {
            return requests.get(transition).get();
        }

        private void handle(Route route) {
            Request request = route.request();
            String method = request.method();
            String requestPath = path(request.url());

            if ("GET".equals(method)) {
                handleRead(route, requestPath);
                return;
            }
            if ("POST".equals(method)) {
                handlePost(route, request, requestPath);
                return;
            }

            unexpectedRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked an unexpected payout request\"}");
        }

        private void handleRead(Route route, String requestPath) {
            if ((PAYOUTS_PATH + "/pending").equals(requestPath)) {
                pendingReads.incrementAndGet();
                fulfillJson(route, 200, pendingPayoutsJson());
                return;
            }
            if (PAYOUTS_PATH.equals(requestPath)) {
                historyReads.incrementAndGet();
                fulfillJson(route, 200, historyJson());
                return;
            }
            if ((PAYOUTS_PATH + "/" + PAYOUT_ID).equals(requestPath)) {
                detailReads.incrementAndGet();
                if (state.get() == LifecycleState.NONE) {
                    fulfillJson(route, 404, "{\"message\":\"Fixture payout does not exist\"}");
                } else {
                    fulfillJson(route, 200, detailJson());
                }
                return;
            }
            if (requestPath.startsWith(LEDGER_STATEMENT_PATH + "/")) {
                statementReads.incrementAndGet();
                fulfillJson(route, 200, emptyStatementJson());
                return;
            }

            unexpectedRequests.incrementAndGet();
            fulfillJson(route, 404, "{\"message\":\"Browser fixture has no response for this payout read\"}");
        }

        private void handlePost(Route route, Request request, String requestPath) {
            Transition transition = transitionFor(requestPath);
            if (transition == null) {
                blockedMutationAttempts.incrementAndGet();
                fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked an unknown payout mutation\"}");
                return;
            }

            transitionWrites.incrementAndGet();
            RecordedRequest recorded = new RecordedRequest(
                    request.method(), requestPath, request.url(),
                    request.postData() == null ? "" : request.postData(),
                    request.headerValue("Idempotency-Key"));
            requests.get(transition).set(recorded);
            if (transition == Transition.CREATE) {
                createRequests.add(recorded);
            }

            if (transition == Transition.CREATE && loseCreateResponse
                    && state.get() == LifecycleState.DRAFT) {
                if (!Objects.equals(committedCreateKey.get(), recorded.idempotencyKey())) {
                    blockedMutationAttempts.incrementAndGet();
                    fulfillJson(route, 409,
                            "{\"message\":\"Fixture rejected a new key for an already committed payout\"}");
                    return;
                }
                fulfillJson(route, 200, createdPayoutJson());
                return;
            }

            if (transition != permittedTransition || !canTransition(transition)) {
                blockedMutationAttempts.incrementAndGet();
                fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked an unpermitted payout mutation\"}");
                return;
            }
            if (rejectPermittedTransition) {
                fulfillJson(route, 409, "{\"message\":\"" + FAILURE_MESSAGE + "\"}");
                return;
            }

            applyTransition(transition, request.url());
            if (transition == Transition.CREATE) {
                if (loseCreateResponse) {
                    committedCreateKey.set(recorded.idempotencyKey());
                    fulfillJson(route, 503, "{\"message\":\"" + CREATE_RESPONSE_LOST_FAILURE + "\"}");
                    return;
                }
                fulfillJson(route, 200, createdPayoutJson());
            } else {
                // Ledger's transition endpoints return ResponseEntity<Void>. Omitting a JSON
                // content type also matches the real empty response and keeps Zodios from trying
                // to parse an object as its generated z.void() contract.
                route.fulfill(new Route.FulfillOptions().setStatus(200));
            }
        }

        private Transition transitionFor(String requestPath) {
            if (PAYOUTS_PATH.equals(requestPath)) return Transition.CREATE;
            if ((PAYOUTS_PATH + "/" + PAYOUT_ID + "/approve").equals(requestPath)) return Transition.APPROVE;
            if ((PAYOUTS_PATH + "/" + PAYOUT_ID + "/cancel").equals(requestPath)) return Transition.CANCEL;
            if ((PAYOUTS_PATH + "/" + PAYOUT_ID + "/mark-paid").equals(requestPath)) return Transition.MARK_PAID;
            if ((PAYOUTS_PATH + "/" + PAYOUT_ID + "/fail").equals(requestPath)) return Transition.FAIL;
            return null;
        }

        private boolean canTransition(Transition transition) {
            return switch (transition) {
                case CREATE -> state.get() == LifecycleState.NONE;
                case APPROVE, CANCEL -> state.get() == LifecycleState.DRAFT;
                case MARK_PAID, FAIL -> state.get() == LifecycleState.APPROVED;
            };
        }

        private void applyTransition(Transition transition, String url) {
            switch (transition) {
                case CREATE -> state.set(LifecycleState.DRAFT);
                case APPROVE -> state.set(LifecycleState.APPROVED);
                case CANCEL -> state.set(LifecycleState.CANCELLED);
                case MARK_PAID -> {
                    bankReference.set(queryValue(url, "bankReference"));
                    state.set(LifecycleState.PAID);
                }
                case FAIL -> {
                    failureReason.set(queryValue(url, "reason"));
                    state.set(LifecycleState.FAILED);
                }
            }
        }

        private String pendingPayoutsJson() {
            if (state.get() != LifecycleState.NONE) return "[]";
            return """
                    [{
                      "payeeType": "RESTAURANT",
                      "payeeId": "%s",
                      "displayName": "%s",
                      "nameResolved": true,
                      "unsettledAmount": 750.0,
                      "unsettledSince": "%s",
                      "lineCount": 2,
                      "beneficiaryStatus": {"verified": %s}
                    }]
                    """.formatted(PAYEE_ID, PAYEE_NAME, FIXTURE_TIME, beneficiaryVerified);
        }

        private String historyJson() {
            if (state.get() == LifecycleState.NONE) return emptyPayoutPageJson();
            return """
                    {
                      "content": [{
                        "id": "%s",
                        "payeeType": "RESTAURANT",
                        "payeeId": "%s",
                        "payeeDisplayName": "%s",
                        "amount": 750.0,
                        "currency": "INR",
                        "status": "%s",
                        "createdAt": "%s"
                      }],
                      "totalElements": 1,
                      "totalPages": 1,
                      "last": true,
                      "size": 20,
                      "number": 0,
                      "first": true,
                      "numberOfElements": 1,
                      "empty": false
                    }
                    """.formatted(PAYOUT_ID, PAYEE_ID, PAYEE_NAME, state.get(), FIXTURE_TIME);
        }

        private String detailJson() {
            String approvedAt = state.get() == LifecycleState.APPROVED
                    || state.get() == LifecycleState.PAID || state.get() == LifecycleState.FAILED
                    ? ",\n  \"approvedAt\": \"" + FIXTURE_TIME + "\"" : "";
            String paidAt = state.get() == LifecycleState.PAID
                    ? ",\n  \"paidAt\": \"" + FIXTURE_TIME + "\"" : "";
            String bankReferenceField = bankReference.get() == null
                    ? "" : ",\n  \"bankReference\": \"" + bankReference.get() + "\"";
            String failureReasonField = failureReason.get() == null
                    ? "" : ",\n  \"failureReason\": \"" + failureReason.get() + "\"";
            return """
                    {
                      "id": "%s",
                      "payeeType": "RESTAURANT",
                      "payeeId": "%s",
                      "payeeDisplayName": "%s",
                      "amount": 750.0,
                      "currency": "INR",
                      "status": "%s",
                      "createdAt": "%s",
                      "createdBy": "%s",
                      "beneficiary": {
                        "accountNumberMasked": "XXXX4321",
                        "ifsc": "HDFC0001",
                        "beneficiaryName": "%s",
                        "verified": %s
                      },
                      "lines": []%s%s%s%s
                    }
                    """.formatted(PAYOUT_ID, PAYEE_ID, PAYEE_NAME, state.get(), FIXTURE_TIME,
                    payoutCreatorId, PAYEE_NAME, beneficiaryVerified, approvedAt, paidAt,
                    bankReferenceField, failureReasonField);
        }

        private static String createdPayoutJson() {
            return """
                    {
                      "id": "%s",
                      "amount": 750.0,
                      "status": "DRAFT"
                    }
                    """.formatted(PAYOUT_ID);
        }

        private static String emptyPayoutPageJson() {
            return """
                    {
                      "content": [],
                      "totalElements": 0,
                      "totalPages": 0,
                      "last": true,
                      "size": 20,
                      "number": 0,
                      "first": true,
                      "numberOfElements": 0,
                      "empty": true
                    }
                    """;
        }

        private static String emptyStatementJson() {
            return """
                    {
                      "content": [],
                      "totalElements": 0,
                      "totalPages": 0,
                      "last": true,
                      "size": 50,
                      "number": 0,
                      "first": true,
                      "numberOfElements": 0,
                      "empty": true
                    }
                    """;
        }

        private static void fulfillJson(Route route, int status, String body) {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(status)
                    .setContentType("application/json")
                    .setBody(body));
        }
    }
}
