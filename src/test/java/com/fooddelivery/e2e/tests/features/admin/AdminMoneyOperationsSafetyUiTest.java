package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminMoneyOperationsSafetyPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser-routed safety coverage for the Admin Money Operations screen.
 *
 * <p>The administrator authenticates normally against the deployed application. After that,
 * every Money Operations read is supplied by a disposable browser fixture and every mutation is
 * intercepted. The fixture either rejects the request, or fulfills the one response the test is
 * expressly checking; no mutation can reach shared Dev data.</p>
 */
@Tag("admin")
@Tag("admin-money-operations")
@Tag("browser-routed")
public class AdminMoneyOperationsSafetyUiTest extends TestBase {

    private static final String LEDGER_REJECTIONS_PATH = "/api/v1/internal/admin/ledger/rejections";
    private static final String RECONCILIATION_PATH = "/api/v1/internal/admin/ledger/reconciliation";
    private static final String PAYMENT_WEBHOOKS_PATH = "/api/v1/internal/admin/payments/dlq/webhooks";
    private static final String WALLET_OUTBOX_PATH = "/api/v1/internal/admin/wallet/dlq/outbox";

    private static final String REJECTION_ID = "c1000000-0000-4000-8000-000000000001";
    private static final String REJECTION_EVENT_ID = "fixture-ledger-rejection-001";
    private static final String WEBHOOK_ID = "c1000000-0000-4000-8000-000000000002";
    private static final String WEBHOOK_EVENT_ID = "fixture-payment-webhook-001";
    private static final String WALLET_OUTBOX_EVENT_ID = "c1000000-0000-4000-8000-000000000003";
    private static final String WALLET_OUTBOX_AGGREGATE_ID = "fixture-wallet-aggregate-001";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";
    private static final String RESOLUTION_NOTE = "Fixture movement was reconciled outside the ledger.";
    private static final String RESOLUTION_FAILURE = "Fixture ledger refused this resolution";
    private static final String PAYMENT_RETRY_FAILURE = "Fixture payment processor still rejects this webhook";
    private static final String WALLET_RETRY_FAILURE = "Fixture wallet publisher is still unavailable";

    private AdminPortalPage portal;

    @BeforeEach
    void authenticateLiveAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("MONEY-OPS-SAFE-01: a resolution requires a meaningful note and cancellation sends no write")
    void resolutionRequiresNoteAndCanceledConfirmationSendsNoWrite() {
        MoneyOperationsFixture fixture = MoneyOperationsFixture.blockAllMutations();
        AdminMoneyOperationsSafetyPage operations = openMoneyOperations(fixture);

        operations.waitForRejectedMovement(REJECTION_EVENT_ID);
        operations.openResolution();
        operations.fillResolutionNote(" \t ");
        assertThat(operations.isResolutionConfirmationDisabled())
                .as("whitespace must not be accepted as an audit note")
                .isTrue();
        assertThat(fixture.mutationAttempts.get()).isZero();

        operations.fillResolutionNote(RESOLUTION_NOTE);
        assertThat(operations.isResolutionConfirmationEnabled())
                .as("a meaningful note should be ready for the second confirmation")
                .isTrue();
        operations.openResolutionConfirmation();
        PlaywrightAssertions.assertThat(operations.resolutionDialog()).containsText(
                "This removes the movement from the unresolved queue.");
        operations.cancelResolutionConfirmation();

        assertThat(fixture.ledgerReads.get()).isGreaterThanOrEqualTo(1);
        assertThat(fixture.mutationAttempts.get())
                .as("cancelling a resolution must not call its endpoint")
                .isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("MONEY-OPS-SAFE-02: confirmed webhook retry uses an intercepted success and refreshes the local queue")
    void paymentWebhookRetryUsesOnlyTheFulfilledSuccessResponse() {
        MoneyOperationsFixture fixture = MoneyOperationsFixture.fulfillPaymentRetrySuccess();
        AdminMoneyOperationsSafetyPage operations = openMoneyOperations(fixture);

        operations.openPaymentDlq();
        operations.waitForPaymentWebhook(WEBHOOK_EVENT_ID);
        operations.openPaymentWebhookRetryConfirmation();
        assertThat(fixture.mutationAttempts.get()).isZero();

        operations.confirmPaymentWebhookRetry();
        adminPage.waitForCondition(() -> fixture.paymentRetryWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        operations.waitForToast("Payment webhook retry submitted.");
        operations.waitForNoFailedPaymentWebhooks();

        assertThat(fixture.paymentWebhookReads.get())
                .as("a successful retry reloads the active queue")
                .isGreaterThanOrEqualTo(2);
        assertThat(fixture.mutationAttempts.get()).isEqualTo(1);
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("MONEY-OPS-SAFE-03: an intercepted payment webhook retry failure keeps the event retryable")
    void failedPaymentWebhookRetrySurfacesTheErrorAndKeepsTheEventInTheQueue() {
        MoneyOperationsFixture fixture = MoneyOperationsFixture.fulfillPaymentRetryFailure();
        AdminMoneyOperationsSafetyPage operations = openMoneyOperations(fixture);

        operations.openPaymentDlq();
        operations.waitForPaymentWebhook(WEBHOOK_EVENT_ID);
        operations.openPaymentWebhookRetryConfirmation();
        operations.confirmPaymentWebhookRetry();

        adminPage.waitForCondition(() -> fixture.paymentRetryWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        operations.waitForToast(PAYMENT_RETRY_FAILURE);
        operations.waitForPaymentWebhook(WEBHOOK_EVENT_ID);
        adminPage.waitForCondition(operations::isPaymentWebhookRetryEnabled,
                new Page.WaitForConditionOptions().setTimeout(5_000));

        assertThat(operations.isPaymentWebhookRetryEnabled())
                .as("a failed retry must leave the operator able to retry after investigating the failure")
                .isTrue();
        assertThat(fixture.paymentWebhookReads.get())
                .as("a failed retry must not replace the known failed queue item with a fabricated refresh")
                .isEqualTo(1);
        assertThat(fixture.mutationAttempts.get()).isEqualTo(1);
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.paymentRetryRequestPath.get())
                .isEqualTo(PAYMENT_WEBHOOKS_PATH + "/" + WEBHOOK_EVENT_ID + "/retry");
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("MONEY-OPS-SAFE-04: an intercepted resolution error keeps the note and unresolved item visible")
    void failedResolutionSurfacesTheErrorWithoutPretendingTheMovementWasResolved() {
        MoneyOperationsFixture fixture = MoneyOperationsFixture.fulfillResolutionFailure();
        AdminMoneyOperationsSafetyPage operations = openMoneyOperations(fixture);

        operations.waitForRejectedMovement(REJECTION_EVENT_ID);
        operations.openResolution();
        operations.fillResolutionNote(RESOLUTION_NOTE);
        operations.openResolutionConfirmation();
        operations.confirmResolution();

        adminPage.waitForCondition(() -> fixture.resolutionWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        operations.waitForToast(RESOLUTION_FAILURE);
        assertThat(operations.resolutionNoteValue())
                .as("the operator must be able to correct or retain the audit note after a failure")
                .isEqualTo(RESOLUTION_NOTE);
        operations.waitForRejectedMovement(REJECTION_EVENT_ID);

        assertThat(fixture.mutationAttempts.get()).isEqualTo(1);
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.resolutionRequestBody.get()).contains("\"note\":\"" + RESOLUTION_NOTE + "\"");
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("MONEY-OPS-SAFE-05: a confirmed ledger resolution uses an intercepted success and removes only the local fixture item")
    void successfulResolutionUsesOnlyTheFulfilledSuccessResponse() {
        MoneyOperationsFixture fixture = MoneyOperationsFixture.fulfillResolutionSuccess();
        AdminMoneyOperationsSafetyPage operations = openMoneyOperations(fixture);

        operations.waitForRejectedMovement(REJECTION_EVENT_ID);
        operations.openResolution();
        operations.fillResolutionNote(RESOLUTION_NOTE);
        operations.openResolutionConfirmation();
        operations.confirmResolution();

        adminPage.waitForCondition(() -> fixture.resolutionWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        operations.waitForToast("Rejected ledger movement resolved.");
        operations.waitForNoUnresolvedRejections();

        assertThat(fixture.ledgerReads.get())
                .as("a successful resolution reloads the unresolved-rejection queue")
                .isGreaterThanOrEqualTo(2);
        assertThat(fixture.mutationAttempts.get()).isEqualTo(1);
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.resolutionRequestPath.get())
                .isEqualTo(LEDGER_REJECTIONS_PATH + "/" + REJECTION_ID + "/resolve");
        assertThat(fixture.resolutionRequestBody.get()).contains("\"note\":\"" + RESOLUTION_NOTE + "\"");
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("MONEY-OPS-SAFE-06: confirmed wallet outbox retry uses an intercepted success and refreshes the local queue")
    void walletOutboxRetryUsesOnlyTheFulfilledSuccessResponse() {
        MoneyOperationsFixture fixture = MoneyOperationsFixture.fulfillWalletOutboxRetrySuccess();
        AdminMoneyOperationsSafetyPage operations = openMoneyOperations(fixture);

        operations.openWalletDlq();
        operations.waitForWalletOutboxAggregate(WALLET_OUTBOX_AGGREGATE_ID);
        operations.openWalletOutboxRetryConfirmation();
        assertThat(fixture.mutationAttempts.get()).isZero();

        operations.confirmWalletOutboxRetry();
        adminPage.waitForCondition(() -> fixture.walletRetryWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        operations.waitForToast("Wallet outbox retry submitted.");
        operations.waitForNoWalletOutboxEvents();

        assertThat(fixture.walletOutboxReads.get())
                .as("a successful wallet retry reloads the active outbox queue")
                .isGreaterThanOrEqualTo(2);
        assertThat(fixture.mutationAttempts.get()).isEqualTo(1);
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.walletRetryRequestPath.get())
                .isEqualTo(WALLET_OUTBOX_PATH + "/" + WALLET_OUTBOX_EVENT_ID + "/retry");
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    @Test
    @DisplayName("MONEY-OPS-SAFE-07: an intercepted wallet outbox retry failure keeps the event retryable")
    void failedWalletOutboxRetrySurfacesTheErrorAndKeepsTheEventInTheQueue() {
        MoneyOperationsFixture fixture = MoneyOperationsFixture.fulfillWalletOutboxRetryFailure();
        AdminMoneyOperationsSafetyPage operations = openMoneyOperations(fixture);

        operations.openWalletDlq();
        operations.waitForWalletOutboxAggregate(WALLET_OUTBOX_AGGREGATE_ID);
        operations.openWalletOutboxRetryConfirmation();
        operations.confirmWalletOutboxRetry();

        adminPage.waitForCondition(() -> fixture.walletRetryWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        operations.waitForToast(WALLET_RETRY_FAILURE);
        operations.waitForWalletOutboxAggregate(WALLET_OUTBOX_AGGREGATE_ID);
        adminPage.waitForCondition(operations::isWalletOutboxRetryEnabled,
                new Page.WaitForConditionOptions().setTimeout(5_000));

        assertThat(operations.isWalletOutboxRetryEnabled())
                .as("a failed outbox retry must leave the operator able to retry after investigating the failure")
                .isTrue();
        assertThat(fixture.walletOutboxReads.get())
                .as("a failed retry must keep the known DLQ event rather than pretending it left the queue")
                .isEqualTo(1);
        assertThat(fixture.mutationAttempts.get()).isEqualTo(1);
        assertThat(fixture.blockedMutationAttempts.get()).isZero();
        assertThat(fixture.walletRetryRequestPath.get())
                .isEqualTo(WALLET_OUTBOX_PATH + "/" + WALLET_OUTBOX_EVENT_ID + "/retry");
        assertThat(fixture.unexpectedRequests.get()).isZero();
    }

    private AdminMoneyOperationsSafetyPage openMoneyOperations(MoneyOperationsFixture fixture) {
        adminPage.route(AdminMoneyOperationsSafetyUiTest::isMoneyOperationsApiUrl, fixture::handle);
        portal.openMoneyOperationsTab();

        AdminMoneyOperationsSafetyPage operations = new AdminMoneyOperationsSafetyPage(adminPage);
        operations.waitForOperations();
        adminPage.waitForCondition(() -> fixture.ledgerReads.get() > 0,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        return operations;
    }

    private static boolean isMoneyOperationsApiUrl(String url) {
        String path = pathOf(url);
        return isAtOrBelow(path, LEDGER_REJECTIONS_PATH)
                || isAtOrBelow(path, RECONCILIATION_PATH)
                || isAtOrBelow(path, PAYMENT_WEBHOOKS_PATH)
                || isAtOrBelow(path, WALLET_OUTBOX_PATH);
    }

    private static boolean isAtOrBelow(String path, String prefix) {
        return prefix.equals(path) || path.startsWith(prefix + "/");
    }

    private static String pathOf(String url) {
        return URI.create(url).getPath();
    }

    private static void fulfillJson(Route route, int status, String body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setBody(body));
    }

    private enum ResolutionOutcome { BLOCK, FAILURE, SUCCESS }

    private enum PaymentRetryOutcome { BLOCK, FAILURE, SUCCESS }

    private enum WalletRetryOutcome { BLOCK, FAILURE, SUCCESS }

    private static final class MoneyOperationsFixture {
        private final ResolutionOutcome resolutionOutcome;
        private final PaymentRetryOutcome paymentRetryOutcome;
        private final WalletRetryOutcome walletRetryOutcome;
        private final AtomicBoolean rejectionResolved = new AtomicBoolean(false);
        private final AtomicBoolean webhookRetried = new AtomicBoolean(false);
        private final AtomicBoolean walletOutboxRetried = new AtomicBoolean(false);
        private final AtomicInteger ledgerReads = new AtomicInteger();
        private final AtomicInteger paymentWebhookReads = new AtomicInteger();
        private final AtomicInteger walletOutboxReads = new AtomicInteger();
        private final AtomicInteger mutationAttempts = new AtomicInteger();
        private final AtomicInteger resolutionWrites = new AtomicInteger();
        private final AtomicInteger paymentRetryWrites = new AtomicInteger();
        private final AtomicInteger walletRetryWrites = new AtomicInteger();
        private final AtomicInteger blockedMutationAttempts = new AtomicInteger();
        private final AtomicInteger unexpectedRequests = new AtomicInteger();
        private final AtomicReference<String> resolutionRequestBody = new AtomicReference<>();
        private final AtomicReference<String> resolutionRequestPath = new AtomicReference<>();
        private final AtomicReference<String> paymentRetryRequestPath = new AtomicReference<>();
        private final AtomicReference<String> walletRetryRequestPath = new AtomicReference<>();

        private MoneyOperationsFixture(
                ResolutionOutcome resolutionOutcome,
                PaymentRetryOutcome paymentRetryOutcome,
                WalletRetryOutcome walletRetryOutcome) {
            this.resolutionOutcome = resolutionOutcome;
            this.paymentRetryOutcome = paymentRetryOutcome;
            this.walletRetryOutcome = walletRetryOutcome;
        }

        private static MoneyOperationsFixture blockAllMutations() {
            return new MoneyOperationsFixture(
                    ResolutionOutcome.BLOCK, PaymentRetryOutcome.BLOCK, WalletRetryOutcome.BLOCK);
        }

        private static MoneyOperationsFixture fulfillPaymentRetrySuccess() {
            return new MoneyOperationsFixture(
                    ResolutionOutcome.BLOCK, PaymentRetryOutcome.SUCCESS, WalletRetryOutcome.BLOCK);
        }

        private static MoneyOperationsFixture fulfillPaymentRetryFailure() {
            return new MoneyOperationsFixture(
                    ResolutionOutcome.BLOCK, PaymentRetryOutcome.FAILURE, WalletRetryOutcome.BLOCK);
        }

        private static MoneyOperationsFixture fulfillResolutionFailure() {
            return new MoneyOperationsFixture(
                    ResolutionOutcome.FAILURE, PaymentRetryOutcome.BLOCK, WalletRetryOutcome.BLOCK);
        }

        private static MoneyOperationsFixture fulfillResolutionSuccess() {
            return new MoneyOperationsFixture(
                    ResolutionOutcome.SUCCESS, PaymentRetryOutcome.BLOCK, WalletRetryOutcome.BLOCK);
        }

        private static MoneyOperationsFixture fulfillWalletOutboxRetrySuccess() {
            return new MoneyOperationsFixture(
                    ResolutionOutcome.BLOCK, PaymentRetryOutcome.BLOCK, WalletRetryOutcome.SUCCESS);
        }

        private static MoneyOperationsFixture fulfillWalletOutboxRetryFailure() {
            return new MoneyOperationsFixture(
                    ResolutionOutcome.BLOCK, PaymentRetryOutcome.BLOCK, WalletRetryOutcome.FAILURE);
        }

        private void handle(Route route) {
            Request request = route.request();
            String method = request.method();
            String path = pathOf(request.url());

            if ("GET".equals(method)) {
                handleRead(route, path);
                return;
            }

            if ("POST".equals(method) || "PUT".equals(method)
                    || "PATCH".equals(method) || "DELETE".equals(method)) {
                mutationAttempts.incrementAndGet();
                handleMutation(route, request, path);
                return;
            }

            unexpectedRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked an unexpected money-operations request\"}");
        }

        private void handleRead(Route route, String path) {
            if (LEDGER_REJECTIONS_PATH.equals(path)) {
                ledgerReads.incrementAndGet();
                fulfillJson(route, 200, ledgerRejectionsJson());
                return;
            }
            if (RECONCILIATION_PATH.concat("/runs").equals(path)) {
                fulfillJson(route, 200, emptyPageJson());
                return;
            }
            if (PAYMENT_WEBHOOKS_PATH.equals(path)) {
                paymentWebhookReads.incrementAndGet();
                fulfillJson(route, 200, paymentWebhooksJson());
                return;
            }
            if (WALLET_OUTBOX_PATH.equals(path)) {
                walletOutboxReads.incrementAndGet();
                fulfillJson(route, 200, walletOutboxJson());
                return;
            }

            unexpectedRequests.incrementAndGet();
            fulfillJson(route, 404, "{\"message\":\"Browser fixture has no response for this money-operations read\"}");
        }

        private void handleMutation(Route route, Request request, String path) {
            if (path.equals(LEDGER_REJECTIONS_PATH + "/" + REJECTION_ID + "/resolve")) {
                resolutionWrites.incrementAndGet();
                resolutionRequestPath.set(path);
                resolutionRequestBody.set(request.postData());
                if (resolutionOutcome == ResolutionOutcome.SUCCESS) {
                    rejectionResolved.set(true);
                    fulfillJson(route, 200, resolvedLedgerRejectionJson());
                    return;
                }
                if (resolutionOutcome == ResolutionOutcome.FAILURE) {
                    fulfillJson(route, 409, "{\"message\":\"" + RESOLUTION_FAILURE + "\"}");
                    return;
                }
                blockMutation(route);
                return;
            }

            if (path.equals(WALLET_OUTBOX_PATH + "/" + WALLET_OUTBOX_EVENT_ID + "/retry")) {
                walletRetryWrites.incrementAndGet();
                walletRetryRequestPath.set(path);
                if (walletRetryOutcome == WalletRetryOutcome.SUCCESS) {
                    walletOutboxRetried.set(true);
                    fulfillJson(route, 200, successfulWalletOutboxRetryJson());
                    return;
                }
                if (walletRetryOutcome == WalletRetryOutcome.FAILURE) {
                    fulfillJson(route, 409, "{\"message\":\"" + WALLET_RETRY_FAILURE + "\"}");
                    return;
                }
                blockMutation(route);
                return;
            }

            if (path.equals(PAYMENT_WEBHOOKS_PATH + "/" + WEBHOOK_EVENT_ID + "/retry")) {
                paymentRetryWrites.incrementAndGet();
                paymentRetryRequestPath.set(path);
                if (paymentRetryOutcome == PaymentRetryOutcome.SUCCESS) {
                    webhookRetried.set(true);
                    fulfillJson(route, 200, successfulRetryJson());
                    return;
                }
                if (paymentRetryOutcome == PaymentRetryOutcome.FAILURE) {
                    fulfillJson(route, 409, "{\"message\":\"" + PAYMENT_RETRY_FAILURE + "\"}");
                    return;
                }
                blockMutation(route);
                return;
            }

            blockMutation(route);
        }

        private void blockMutation(Route route) {
            blockedMutationAttempts.incrementAndGet();
            // fulfill(), unlike continue(), terminates the request in the browser. This is a
            // visible test failure response, never an outbound financial mutation.
            fulfillJson(route, 405, "{\"message\":\"Browser fixture blocked a money-operations mutation\"}");
        }

        private String ledgerRejectionsJson() {
            if (rejectionResolved.get()) {
                return emptyPageJson();
            }
            return """
                    {
                      "content": [{
                        "id": "%s",
                        "eventId": "%s",
                        "producer": "fixture-ledger-producer",
                        "reason": "Fixture booking needs operator review",
                        "payload": "{\\"fixture\\":true}",
                        "createdAt": "%s",
                        "ageMinutes": 12
                      }],
                      "totalElements": 1,
                      "totalPages": 1,
                      "last": true,
                      "size": 50,
                      "number": 0,
                      "first": true,
                      "numberOfElements": 1,
                      "empty": false
                    }
                    """.formatted(REJECTION_ID, REJECTION_EVENT_ID, FIXTURE_TIME);
        }

        private static String resolvedLedgerRejectionJson() {
            return """
                    {
                      "id": "%s",
                      "eventId": "%s",
                      "producer": "fixture-ledger-producer",
                      "reason": "Fixture booking needs operator review",
                      "payload": "{\\"fixture\\":true}",
                      "createdAt": "%s",
                      "resolvedAt": "%s",
                      "resolvedBy": "fixture-admin",
                      "resolutionNote": "%s",
                      "ageMinutes": 12
                    }
                    """.formatted(REJECTION_ID, REJECTION_EVENT_ID, FIXTURE_TIME, FIXTURE_TIME, RESOLUTION_NOTE);
        }

        private String paymentWebhooksJson() {
            if (webhookRetried.get()) {
                return emptyPageJson();
            }
            return """
                    {
                      "content": [{
                        "id": "%s",
                        "eventId": "%s",
                        "processingStatus": "DEAD_LETTER",
                        "gatewayName": "RAZORPAY",
                        "errorLog": "Fixture payment gateway timeout",
                        "createdAt": "%s",
                        "updatedAt": "%s",
                        "version": 1,
                        "eventType": "PAYMENT_WEBHOOK",
                        "payload": "{}"
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
                    """.formatted(WEBHOOK_ID, WEBHOOK_EVENT_ID, FIXTURE_TIME, FIXTURE_TIME);
        }

        private String walletOutboxJson() {
            if (walletOutboxRetried.get()) {
                return emptyPageJson();
            }
            return """
                    {
                      "content": [{
                        "id": "%s",
                        "aggregateType": "WALLET",
                        "aggregateId": "%s",
                        "eventType": "WALLET_CREDIT_REQUESTED",
                        "payload": "{\\"fixture\\":true}",
                        "createdAt": "%s",
                        "status": "DLQ",
                        "errorMessage": "Fixture wallet publisher timeout",
                        "retryCount": 3
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
                    """.formatted(WALLET_OUTBOX_EVENT_ID, WALLET_OUTBOX_AGGREGATE_ID, FIXTURE_TIME);
        }

        private static String emptyPageJson() {
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

        private static String successfulRetryJson() {
            return """
                    {
                      "success": true,
                      "message": "Fixture retry accepted",
                      "timestamp": "2026-09-29T10:00:00Z"
                    }
                    """;
        }

        private static String successfulWalletOutboxRetryJson() {
            return """
                    {
                      "success": true,
                      "message": "Successfully reset to UNPROCESSED",
                      "data": "Outbox event queued for retry",
                      "timestamp": "2026-09-29T10:00:00Z"
                    }
                    """;
        }
    }
}
