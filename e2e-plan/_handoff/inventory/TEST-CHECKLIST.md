# Executable test checklist

## Business Platform wallet and ads gates — 2026-10-06T13:35+05:30

Per-phase gates (E2E-STRATEGY). Detailed results in the owning feature folders; unique methods only.

- [x] `BusinessWalletAccessApiTest#businessWalletAccess` (bp-w1) — deployed W1+A1, invocation 4 PASS; business-wallet/AUDIT-STATUS.md
- [x] `AdAccountPerOrganisationApiTest#adAccountPerOrganisation` (bp-a1) — deployed W1+A1, invocation 1 PASS; ads-manager/AUDIT-STATUS.md
- [x] `RestaurantCampaignsLiveTest` 3/3 (`tabMatchesTheOwnersAdvertiser`, `draftIsDiscardedOnCancel`, `launchedCampaignIsStoredInRupees`) — invocation 3 PASS on the pre-W3 UI; **re-run after the W3 UI deploy** (locators changed); ads-manager/AUDIT-STATUS.md
- [ ] `BusinessWalletTopUpFlowTest#businessWalletTopUpFlow` (bp-w2) — **RED** twice: 12:13 (PaymentServiceClient never registered; fixed, deployed 0962e78) and 13:45 (outbox aggregate_id > 50; fixed locally, WalletService + payment-gateway redeploy pending)
- [ ] `BusinessWalletUiTest#businessWalletPage` (bp-w3) — written + compiled + locator audit PASS; **not run** (W3 undeployed)


## Current scoped acceptance — 2026-10-05T11:58:13+05:30

48 distinct selected methods pass:16 final phase methods plus32 required existing regression methods. Separate retained Happy money/quote branches and focused reruns are not extra unique methods. No full-suite/all-features green claim. Original failures and explicit deferrals remain in the release.

- [x] `AdminLedgerAdvancedTest#pendingPayoutsHavePositiveBalances` — deployed UI; 73-retained-beneficiary-queue-invocation1-pass.json
- [x] `AdminOrderMoneyOutcomesTest#panelShowsTheOutcome` — deployed UI; 62-retained-four-regressions-invocation1-original.json
- [x] `AdminStepUpTest#administratorVerifiesSeparatelyAndKeepsEverydayAccess` — deployed UI; 66-final-ui-gate-invocation6.json
- [x] `AdminStepUpTest#nonStaffCannotStartAdministratorVerification` — deployed UI; 66-final-ui-gate-invocation6.json
- [x] `BusinessHubOrganisationUiTest#invitationRoleChangesAndRemovalAreVisibleToBothPeople` — deployed UI; 17-final-ui-gate-invocation4.json
- [x] `CspSmokeUiTest#securityHeadersAndConsoleHoldAcrossEveryCurrentPortal` — deployed UI; 17-final-ui-gate-invocation4.json
- [x] `CustomerSettingsUiTest#blankNewAddressFormIsBlockedAndCanBeClosed` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#currentLoggedInDeviceIsListedWithoutRemovingIt` — deployed UI; 64-final-session-release-and-three-reruns.json
- [x] `CustomerSettingsUiTest#deliveredOrderReviewRequiresAtLeastOneRating` — deployed UI; 57-delivered-read-only-invocation1.json
- [x] `CustomerSettingsUiTest#everyCustomerSettingsTabCanBeSelectedWithoutLosingSettings` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#everyVisibleNewAddressFieldHasAProgrammaticLabel` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#everyVisibleProfileFieldHasAProgrammaticLabel` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#everyVisibleSettingsButtonHasAnAccessibleName` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#keyboardTabsReachHistoryAndWallet` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#myReviewsTabShowsReviewsOrDefinedEmptyState` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#profileDarkThemeChangesRenderedBackground` — deployed UI; 64-final-session-release-and-three-reruns.json
- [x] `CustomerSettingsUiTest#profileDarkThemePersistsAcrossReload` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#profileIdentityAndStoreCreditBalanceRender` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#profilePhoneIsReadOnlyAndCloseReturnsHome` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#profileThemeClassTogglesAndRestoresLight` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#savedHomeAddressIsVisibleWithoutEditingIt` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `DeliveryOnboardingUiTest#approvedRiderEntersDeliveryOfflineWithoutAnotherPersonLogin` — deployed UI; 17-final-ui-gate-invocation4.json
- [x] `EntitlementRevocationTest#acceptedStaffApprovalAndRemovalRefreshOnNormalUiRequests` — deployed UI; 66-final-ui-gate-invocation6.json
- [x] `EntitlementRevocationTest#ownBrandSuspensionAndReinstatementChangeAccessAndDiscovery` — deployed UI; 66-final-ui-gate-invocation6.json
- [x] `HappyDeliveryFlowTest#completeOrderLifecycle` — deployed UI; 55-happy-owned-delivery-invocation9-pass.json; retained earlier-step/failure evidence;56-money-quote branch
- [x] `OneLoginEntitlementsTest#approvedRiderUsesCustomerAndDeliveryButCannotOpenRestaurantOperations` — deployed UI; 66-final-ui-gate-invocation6.json
- [x] `OneLoginEntitlementsTest#freshPersonHasCustomerAndApplicationEntryPoints` — deployed UI; 66-final-ui-gate-invocation6.json
- [x] `OneLoginEntitlementsTest#ownerUsesCustomerRestaurantAndBusinessAfterOneOtp` — deployed UI; 66-final-ui-gate-invocation6.json
- [x] `OneLoginEntitlementsTest#pendingRiderSeesApplicationWithoutOperationalAccess` — deployed UI; 66-final-ui-gate-invocation6.json
- [x] `OrderReviewsFlowTest#participantsReviewEachOther` — deployed UI; 58-participant-reviews-invocation1-pass.json
- [x] `PartnerReadOnlyUiTest#restaurantEarningsPanel` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#restaurantProfileCanCloseWithoutChanges` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#restaurantStockControlsRenderWithoutToggling` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderCompletedTripShowsRestaurantPayoutAndDate` — deployed UI; 57-delivered-read-only-invocation1.json
- [x] `PartnerReadOnlyUiTest#riderHistoryDateCanBeCleared` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderHistoryShowsEmptyStateForOldDate` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderProfileValuesArePopulatedWithoutEditing` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderSettingsCanCloseWithoutChanges` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderTodayEarningsIsNonNegativeCurrency` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderVerificationAndWalletSectionsRender` — deployed UI; 59-settings-partner-invocation1-original.json
- [x] `PortalLauncherUiTest#ownerSwitchesApprovedPortalsAndSeesDeliveryStart` — deployed UI; 17-final-ui-gate-invocation4.json
- [x] `PortalLauncherUiTest#pendingRestaurantOpensItsApplicationStatus` — deployed UI; 17-final-ui-gate-invocation4.json
- [x] `PortalLauncherUiTest#rejectedRiderSeesActionAndReason` — deployed UI; 17-final-ui-gate-invocation4.json
- [x] `PortalLauncherUiTest#staffPortalRequestsAdministratorVerification` — deployed UI; 17-final-ui-gate-invocation4.json
- [x] `RestaurantApplicationWizardUiTest#approvedApplicationOpensItsOutletWithoutAnotherPersonLogin` — deployed UI; 17-final-ui-gate-invocation4.json
- [x] `RestaurantEarningsLiveTest#earningsMatchTheLedger` — deployed UI; 62-retained-four-regressions-invocation1-original.json
- [x] `RestaurantNavigationUiTest#restaurantReviewsShowPublicFeedbackAndAggregate` — deployed UI; 62-retained-four-regressions-invocation1-original.json
- [x] `RestaurantNavigationUiTest#restaurantSectionsRender` — deployed UI; 64-final-session-release-and-three-reruns.json

Earlier inventory sections below are historical except the rows explicitly updated at checkpoint113.

Checked means current reviewed source and passing execution; previous evidence and pending/deferred checks remain separate.

## TestConfigTest

- [x] `TestConfigTest#propertyUrlTakesPrecedenceOverEnvironment` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, T, e, s, t, C, o, n, f, i, g, T, e, s, t, ., x, m, l
- [x] `TestConfigTest#environmentUrlIsUsedWhenPropertyIsAbsent` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, T, e, s, t, C, o, n, f, i, g, T, e, s, t, ., x, m, l
- [x] `TestConfigTest#centralDefaultIsUsedWhenNeitherOverrideExists` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, T, e, s, t, C, o, n, f, i, g, T, e, s, t, ., x, m, l
- [x] `TestConfigTest#phoneOverridesAllowDedicatedScenarioAccountsForEveryRole` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, T, e, s, t, C, o, n, f, i, g, T, e, s, t, ., x, m, l
- [x] `TestConfigTest#ordinaryDefaultsNeverChooseNegativeOrDisposableFixtures` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, T, e, s, t, C, o, n, f, i, g, T, e, s, t, ., x, m, l
## AdminAuthorizationLiveE2ETest

- [ ] `AdminAuthorizationLiveE2ETest#customerCannotOpenAdminRoutesOrReadAdminInterventions` — not-reviewed; not-run (parameterized)
## AdminDispatchSafetyRoutedUiTest

- [ ] `AdminDispatchSafetyRoutedUiTest#staleCandidatesAreDiscardedAndLiveOpsCannotDirectlyAssign` — not-reviewed; not-run
- [ ] `AdminDispatchSafetyRoutedUiTest#manualInterventionGuardsAreDeterministicAndDoNotWrite` — not-reviewed; not-run
- [ ] `AdminDispatchSafetyRoutedUiTest#manualAssignmentFailureBecomesVisibleAndReenablesTheNextAttempt` — not-reviewed; not-run
- [ ] `AdminDispatchSafetyRoutedUiTest#acceptedManualAssignmentRemovesTheResolvedIntervention` — not-reviewed; not-run
- [ ] `AdminDispatchSafetyRoutedUiTest#acceptedCancellationRemovesTheResolvedIntervention` — not-reviewed; not-run
- [ ] `AdminDispatchSafetyRoutedUiTest#rejectedCancellationKeepsTheInterventionCorrectableWithoutFalseSuccess` — not-reviewed; not-run
## AdminFleetSafetyRoutedUiTest

- [ ] `AdminFleetSafetyRoutedUiTest#fleetLayersRefreshSafelyAndRecoverFromOneUnavailableRead` — not-reviewed; not-run
- [ ] `AdminFleetSafetyRoutedUiTest#fleetCitySelectionScopesEveryLayerRequest` — not-reviewed; not-run
## AdminLedgerAdvancedTest

- [ ] `AdminLedgerAdvancedTest#ledgerViewVisible` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#filterByTransactionId` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#filterByOwnerType` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#filterByDirectionCredit` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#filterByCategory` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#ownerTypeRequiresOwnerId` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#ledgerRowsHaveCompleteFinancialValues` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#clearFiltersResetsAll` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#ledgerPagination` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#openPayoutHistoryTab` — not-reviewed; not-run
- [x] `AdminLedgerAdvancedTest#pendingPayoutsHavePositiveBalances` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 73-retained-beneficiary-queue-invocation1-pass.json
- [ ] `AdminLedgerAdvancedTest#payoutHistoryRequiresPayeeAndShowsUnknownAsEmpty` — not-reviewed; not-run
- [ ] `AdminLedgerAdvancedTest#payoutOrderMoneyRouteUsesTheSelectedOrderAndSendsNoWrite` — not-reviewed; not-run
## AdminLedgerMoneyReadOnlyRoutedUiTest

- [ ] `AdminLedgerMoneyReadOnlyRoutedUiTest#ledgerFilterAndPaginationAreReadOnly` — not-reviewed; not-run
- [ ] `AdminLedgerMoneyReadOnlyRoutedUiTest#paginatedPayoutHistoryOpensReadOnlyOrderMoney` — not-reviewed; not-run
## AdminLiveOpsFleetTest

- [ ] `AdminLiveOpsFleetTest#fleetMapVisible` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#deployedDriverMarkersUseKnownRiderTones` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#refreshFleetMap` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#liveOpsTabVisible` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#activeOrderCount` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#nearbyDriverTelemetryAndManualInterventionHandoff` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#refreshLiveOps` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#liveOpsPagination` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#operationsPanelVisible` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#openRejectionsTab` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#openReconciliationTab` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#openPaymentDlqTab` — not-reviewed; not-run
- [ ] `AdminLiveOpsFleetTest#openWalletDlqTab` — not-reviewed; not-run
## AdminMoneyOperationsSafetyUiTest

- [ ] `AdminMoneyOperationsSafetyUiTest#resolutionRequiresNoteAndCanceledConfirmationSendsNoWrite` — not-reviewed; not-run
- [ ] `AdminMoneyOperationsSafetyUiTest#paymentWebhookRetryUsesOnlyTheFulfilledSuccessResponse` — not-reviewed; not-run
- [ ] `AdminMoneyOperationsSafetyUiTest#failedPaymentWebhookRetrySurfacesTheErrorAndKeepsTheEventInTheQueue` — not-reviewed; not-run
- [ ] `AdminMoneyOperationsSafetyUiTest#failedResolutionSurfacesTheErrorWithoutPretendingTheMovementWasResolved` — not-reviewed; not-run
- [ ] `AdminMoneyOperationsSafetyUiTest#successfulResolutionUsesOnlyTheFulfilledSuccessResponse` — not-reviewed; not-run
- [ ] `AdminMoneyOperationsSafetyUiTest#walletOutboxRetryUsesOnlyTheFulfilledSuccessResponse` — not-reviewed; not-run
- [ ] `AdminMoneyOperationsSafetyUiTest#failedWalletOutboxRetrySurfacesTheErrorAndKeepsTheEventInTheQueue` — not-reviewed; not-run
## AdminPayoutSafetyUiTest

- [ ] `AdminPayoutSafetyUiTest#payoutDialogsAreClientValidatedAndCancelableWithoutWrites` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#payoutCreationUsesOnlyTheFulfilledFixtureAndOpensTheDraft` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#unverifiedNamedPayeeRequiresExplicitOverrideAndSendsForceTrue` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#ambiguousCreateFailureReusesTheIdempotencyKeyAndOpensTheExistingDraft` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#currentAdministratorCannotApproveTheirOwnDraftPayout` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#approvedDraftUsesOnlyTheFulfilledFixtureAndRendersApproved` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#cancelledDraftUsesOnlyTheFulfilledFixtureAndRendersCancelled` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#markPaidUsesOnlyTheFulfilledFixtureAndRendersPaid` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#markFailedUsesOnlyTheFulfilledFixtureAndRendersFailed` — not-reviewed; not-run
- [ ] `AdminPayoutSafetyUiTest#failedPayoutTransitionSurfacesTheErrorWithoutPretendingThePayoutChanged` — not-reviewed; not-run
## AdminPortalRouteCoverageRoutedUiTest

- [ ] `AdminPortalRouteCoverageRoutedUiTest#sidebarDestinationsAndWildcardRedirectFollowTheAdminRouteContract` — not-reviewed; not-run
## AdminRefundPolicyRoutedUiTest

- [ ] `AdminRefundPolicyRoutedUiTest#invalidNoAndZeroQuotesCannotBeApprovedButCanBeRejected` — not-reviewed; not-run
- [ ] `AdminRefundPolicyRoutedUiTest#validQuoteCapsOverrideAndUsesOnlyTheFulfilledResolverResponse` — not-reviewed; not-run
- [ ] `AdminRefundPolicyRoutedUiTest#failedResolutionKeepsTheAuditNoteAndTicketOpen` — not-reviewed; not-run
- [ ] `AdminRefundPolicyRoutedUiTest#confirmedRejectionUsesTheFixtureAndDoesNotInventAnApproval` — not-reviewed; not-run
## AdminSupportChatIsolationRoutedUiTest

- [ ] `AdminSupportChatIsolationRoutedUiTest#switchingSupportTicketsUsesSeparateReadOnlyChatSessions` — not-reviewed; not-run
- [ ] `AdminSupportChatIsolationRoutedUiTest#supportModeratorCanSendAndReceiveWithinTheSelectedTicketSession` — not-reviewed; not-run
- [ ] `AdminSupportChatIsolationRoutedUiTest#moderatorImageUploadUsesTheSelectedSessionAndAuthoritativeBroadcast` — not-reviewed; not-run
- [ ] `AdminSupportChatIsolationRoutedUiTest#imageUploadWithoutUsableUrlShowsAnErrorWithoutForgingAnAttachment` — not-reviewed; not-run
## AdminSupportRefundQueueTest

- [ ] `AdminSupportRefundQueueTest#supportTicketQueue` — not-reviewed; not-run
## AdminSupportTicketResolutionRoutedUiTest

- [ ] `AdminSupportTicketResolutionRoutedUiTest#rejectionConfirmationAndFulfilledResolutionUseOnlyTheFixture` — not-reviewed; not-run
- [ ] `AdminSupportTicketResolutionRoutedUiTest#approvalUsesTheVerifiedQuoteAndTheAuthenticatedAuditActor` — not-reviewed; not-run
- [ ] `AdminSupportTicketResolutionRoutedUiTest#failedResolverKeepsTheSelectedTicketAndAuditNoteOpen` — not-reviewed; not-run
## AdminSupportUserReviewTest

- [ ] `AdminSupportUserReviewTest#supportTicketsVisibleAndCounted` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#supportPagination` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#refundQueueVisibleAndCounted` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#searchAndOpenSeededUser` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#filterByRole` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#userPagination` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#statusControlIsStateAwareAndSuspensionConfirmationCanBeCanceled` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#roleChangeConfirmationCanBeCanceled` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#reviewLookupModes` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#searchReviewsByUnknownEntity` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#searchSeededAuthorAndInspectReviews` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#categoriesEditorAndCount` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#invalidCategoryNameDoesNotCreateCategory` — not-reviewed; not-run
- [ ] `AdminSupportUserReviewTest#categoryListEmptyStateOrEditFormIsReadOnly` — not-reviewed; not-run
## AdminUiTest

- [ ] `AdminUiTest#verifyAdminDashboardUI` — not-reviewed; not-run
- [ ] `AdminUiTest#verifyAdminRoutingPersistence` — not-reviewed; not-run
## AdminUserCatalogModerationRoutedUiTest

- [ ] `AdminUserCatalogModerationRoutedUiTest#userSelectionIsolationAndCanceledRoleStatusChangesAreWriteFree` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#confirmedRoleRemovalAndStatusUpdateUseOnlyFixtureWrites` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#confirmedRoleGrantAndActivationUseOnlyFixtureWrites` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#failedRoleRemovalAndStatusUpdateLeaveTheUserUnchanged` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#categoryValidationEditCancellationAndFixtureCreateAreDeterministic` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#categoryUpdateSuccessUsesOnlyTheBrowserFixture` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#categoryUpdateErrorKeepsThePendingEditCorrectableWithoutFalseSuccess` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#categoryCreateErrorKeepsThePendingCategoryCorrectableWithoutFalseSuccess` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#reviewModeSelectionRejectsStaleResultsAndNeverExposesMutationControls` — not-reviewed; not-run
- [ ] `AdminUserCatalogModerationRoutedUiTest#reviewApiErrorIsVisibleAndDoesNotExposeMutationControls` — not-reviewed; not-run
## AdminUserOpsTest

- [ ] `AdminUserOpsTest#adminUserManagement` — not-reviewed; not-run
## CrossRoleSessionIsolationTest

- [x] `CrossRoleSessionIsolationTest#simultaneousRolesRemainIsolatedAcrossReload` — reviewed; passed; 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, C, r, o, s, s, R, o, l, e, S, e, s, s, i, o, n, I, s, o, l, a, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `CrossRoleSessionIsolationTest#customerLogoutDoesNotAffectRestaurantSession` — reviewed; passed; 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, C, r, o, s, s, R, o, l, e, S, e, s, s, i, o, n, I, s, o, l, a, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `CrossRoleSessionIsolationTest#forgedHeadersCannotChangeSessionOwnership` — reviewed; passed; 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, C, r, o, s, s, R, o, l, e, S, e, s, s, i, o, n, I, s, o, l, a, t, i, o, n, T, e, s, t, ., x, m, l
## ProfileSettingsTest

- [x] `ProfileSettingsTest#completeProfileModalPrompt` — reviewed; passed; 0, 2, -, p, r, o, f, i, l, e, -, v, a, l, i, d, a, t, i, o, n, ., x, m, l
- [ ] `ProfileSettingsTest#sharedSettingsAddressTab` — not-reviewed; not-run
- [ ] `ProfileSettingsTest#sharedSettingsWalletTab` — not-reviewed; not-run
- [ ] `ProfileSettingsTest#sharedSettingsHistoryTab` — not-reviewed; not-run
## RegistrationUiTest

- [x] `RegistrationUiTest#customerRegistrationFlow` — reviewed; passed; 0, 2, -, r, e, g, i, s, t, r, a, t, i, o, n, ., x, m, l
- [x] `RegistrationUiTest#riderRegistrationFlow` — reviewed; passed; 0, 2, -, r, e, g, i, s, t, r, a, t, i, o, n, ., x, m, l
- [x] `RegistrationUiTest#restaurantRegistrationFlow` — reviewed; passed; 0, 2, -, r, e, g, i, s, t, r, a, t, i, o, n, ., x, m, l
## SessionManagementTest

- [x] `SessionManagementTest#currentSessionIsVisible` — reviewed; passed; 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, S, e, s, s, i, o, n, M, a, n, a, g, e, m, e, n, t, T, e, s, t, ., x, m, l
- [x] `SessionManagementTest#sessionActionsMatchCurrentUiContract` — reviewed; passed; 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, S, e, s, s, i, o, n, M, a, n, a, g, e, m, e, n, t, T, e, s, t, ., x, m, l
- [x] `SessionManagementTest#removeOwnSecondDeviceRevokesOnlyThatSession` — reviewed; passed; 0, 3, -, d, e, v, i, c, e, -, r, e, t, a, i, n, e, d, ., x, m, l
- [x] `SessionManagementTest#sessionLimitCancelPreservesExistingDevices` — reviewed; passed; 0, 3, -, c, a, n, c, e, l, -, d, e, p, l, o, y, e, d, ., x, m, l
- [x] `SessionManagementTest#sessionLimitReplacementRevokesSelectedDeviceOnly` — reviewed; passed; 0, 3, -, r, e, p, l, a, c, e, m, e, n, t, -, d, e, p, l, o, y, e, d, ., x, m, l
## SettingsTest

- [ ] `SettingsTest#settingsTabs` — not-reviewed; not-run
- [ ] `SettingsTest#logout` — not-reviewed; not-run
## CheckoutRoutedUiTest

- [x] `CheckoutRoutedUiTest#orderErrorPreservesCartAndExplicitRetryUsesChosenMethod` — reviewed; passed (parameterized); 0, 6, -, r, o, u, t, e, d, -, i, n, i, t, i, a, l, -, C, h, e, c, k, o, u, t, R, o, u, t, e, d, U, i, T, e, s, t, ., x, m, l
- [x] `CheckoutRoutedUiTest#unavailableItemResponseRemovesOnlyTheRejectedItem` — reviewed; passed; 0, 6, -, r, e, c, o, v, e, r, y, -, d, e, p, l, o, y, e, d, -, C, h, e, c, k, o, u, t, R, o, u, t, e, d, U, i, T, e, s, t, ., x, m, l
- [x] `CheckoutRoutedUiTest#walletCannotBeSelectedBeforeItsBalanceArrives` — reviewed; passed; 0, 6, -, r, o, u, t, e, d, -, i, n, i, t, i, a, l, -, C, h, e, c, k, o, u, t, R, o, u, t, e, d, U, i, T, e, s, t, ., x, m, l
- [x] `CheckoutRoutedUiTest#reopeningCheckoutWaitsForCurrentWalletBalance` — reviewed; passed; 0, 6, -, r, e, c, o, v, e, r, y, -, d, e, p, l, o, y, e, d, -, C, h, e, c, k, o, u, t, R, o, u, t, e, d, U, i, T, e, s, t, ., x, m, l
- [x] `CheckoutRoutedUiTest#tipChangesChargeWalletEligibilityAndExactSubmissionPayload` — reviewed; passed; 0, 6, -, t, i, p, -, f, i, n, a, l, -, C, h, e, c, k, o, u, t, R, o, u, t, e, d, U, i, T, e, s, t, ., x, m, l
- [x] `CheckoutRoutedUiTest#abandoningCheckoutPreservesCartAndSubmitsNoOrder` — reviewed; passed (parameterized); 0, 6, -, t, i, p, -, a, b, a, n, d, o, n, m, e, n, t, -, i, n, i, t, i, a, l, -, C, h, e, c, k, o, u, t, R, o, u, t, e, d, U, i, T, e, s, t, ., x, m, l
## CheckoutUiTest

- [x] `CheckoutUiTest#verifyCheckoutAndPaymentOptions` — reviewed; passed; 0, 6, -, c, o, r, e, -, C, h, e, c, k, o, u, t, U, i, T, e, s, t, ., x, m, l
- [x] `CheckoutUiTest#paymentChoicesAndCheckoutReentry` — reviewed; passed; 0, 6, -, c, o, r, e, -, C, h, e, c, k, o, u, t, U, i, T, e, s, t, ., x, m, l
- [x] `CheckoutUiTest#reloadDuringCheckoutPreservesCart` — reviewed; passed; 0, 6, -, c, o, r, e, -, C, h, e, c, k, o, u, t, U, i, T, e, s, t, ., x, m, l
- [x] `CheckoutUiTest#enterKeyActivatesCheckout` — reviewed; passed; 0, 6, -, c, o, r, e, -, C, h, e, c, k, o, u, t, U, i, T, e, s, t, ., x, m, l
## CustomerAddressTest

- [x] `CustomerAddressTest#selectExistingHomeAddress` — reviewed; passed; 0, 7, -, c, o, r, e, -, C, u, s, t, o, m, e, r, A, d, d, r, e, s, s, T, e, s, t, ., x, m, l
- [x] `CustomerAddressTest#unsavedAddressDraftDoesNotCreateSharedData` — reviewed; passed; 0, 7, -, c, o, r, e, -, C, u, s, t, o, m, e, r, A, d, d, r, e, s, s, T, e, s, t, ., x, m, l
## CustomerCartTest

- [x] `CustomerCartTest#twoRestaurantsCreateIndependentCartsWithoutReplacementDialog` — reviewed; passed; 0, 5, -, i, s, o, l, a, t, i, o, n, -, c, o, r, r, e, c, t, e, d, -, C, u, s, t, o, m, e, r, C, a, r, t, T, e, s, t, ., x, m, l
## CustomerHomeAddressTest

- [ ] `CustomerHomeAddressTest#searchRestaurantFromHome` — not-reviewed; not-run
- [ ] `CustomerHomeAddressTest#restaurantCountOnHome` — not-reviewed; not-run
- [ ] `CustomerHomeAddressTest#openRestaurantFromHome` — not-reviewed; not-run
- [ ] `CustomerHomeAddressTest#addressModalOpens` — not-reviewed; not-run
- [ ] `CustomerHomeAddressTest#selectExistingHomeAddress` — not-reviewed; not-run
- [ ] `CustomerHomeAddressTest#addressCountAccurate` — not-reviewed; not-run
- [ ] `CustomerHomeAddressTest#freeDeliveryTrackerVisible` — not-reviewed; not-run
## CustomerOrderHistoryUiTest

- [x] `CustomerOrderHistoryUiTest#historyRendersDefinedState` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#populatedRowsHaveRequiredSummaryFields` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#historyRowOpensExactOrderTrackerWhenHistoryExists` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#cancelledHistoryRowOpensExactTerminalTracker` — reviewed current mounted source and exact-owned/routed scenario scope; passed exact cancelled details, Back dismissal and retained history;1test12.151s;0failure/error/skip; 09-cancelled-dismiss-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#historyPaginationPreservesRowsWithoutDuplicates` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#successfulEmptyHistoryHasExplicitState` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#failedHistoryRetriesWithoutFalseEmptyState` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#pendingHistoryShowsLoadingUntilTheResponseCompletes` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#completedOrderReordersIntoTheCartWithoutSubmitting` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#decliningReorderReplacementPreservesEditedCart` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#closedReorderOutletShowsWarningWithoutCart` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#unavailableReorderQuoteShowsWarningWithoutCart` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
- [x] `CustomerOrderHistoryUiTest#missingReorderItemsShowWarningWithoutPartialCart` — reviewed current mounted source and exact-owned/routed scenario scope; passed;13combined deployed methods;0failure/error/skip; 09-history-reorder-final-CustomerOrderHistoryUiTest.xml
## CustomerRoutingUiTest

- [ ] `CustomerRoutingUiTest#verifyCustomerRoutingPersistence` — not-reviewed; not-run
## MenuCartFeatureTest

- [x] `MenuCartFeatureTest#menuDisplaysItemsWithoutRestaurantEditingControls` — reviewed; passed; 0, 4, -, f, i, n, a, l, -, M, e, n, u, C, a, r, t, F, e, a, t, u, r, e, T, e, s, t, ., x, m, l
- [x] `MenuCartFeatureTest#addIncrementDecrementAndEmptyCart` — reviewed; passed; 0, 5, -, f, i, n, a, l, -, M, e, n, u, C, a, r, t, F, e, a, t, u, r, e, T, e, s, t, ., x, m, l
## OutletSelectionTest

- [x] `OutletSelectionTest#outletSelectorListsSelectableOutlets` — reviewed; passed; 0, 7, -, c, o, r, e, -, O, u, t, l, e, t, S, e, l, e, c, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `OutletSelectionTest#everyOutletShowsDistance` — reviewed; passed; 0, 7, -, c, o, r, e, -, O, u, t, l, e, t, S, e, l, e, c, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `OutletSelectionTest#farOutletsCannotBeSelected` — reviewed; passed; 0, 7, -, f, a, r, -, r, o, u, t, e, d, -, O, u, t, l, e, t, S, e, l, e, c, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `OutletSelectionTest#selectNearbyOutletLoadsItsMenu` — reviewed; passed; 0, 7, -, c, o, r, e, -, O, u, t, l, e, t, S, e, l, e, c, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `OutletSelectionTest#outletPersistsDuringCartNavigation` — reviewed; passed; 0, 7, -, c, o, r, e, -, O, u, t, l, e, t, S, e, l, e, c, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `OutletSelectionTest#homeAddressPersistsAfterReload` — reviewed; passed; 0, 7, -, c, o, r, e, -, O, u, t, l, e, t, S, e, l, e, c, t, i, o, n, T, e, s, t, ., x, m, l
## ChatCommunicationTest

- [ ] `ChatCommunicationTest#orderParticipantsCanChatThroughDelivery` — consolidated into `HappyDeliveryFlowTest#completeOrderLifecycle` via `OrderChatChecks.afterDispatch/afterDelivery`; fresh deployed verification running; no independent lifecycle
- [ ] `ChatCommunicationTest#chatWindowMaintainsRealtimeState` — consolidated into `HappyDeliveryFlowTest#completeOrderLifecycle` via `OrderChatChecks.afterDispatch`; fresh deployed verification running; no independent lifecycle
## ChatRefundMapTest

- [ ] `ChatRefundMapTest#mapContainerVisible` — not-reviewed; not-run
- [ ] `ChatRefundMapTest#searchPlaceInMap` — not-reviewed; not-run
- [ ] `ChatRefundMapTest#selectFirstMapSearchResult` — not-reviewed; not-run
- [ ] `ChatRefundMapTest#fillMapCoordinates` — not-reviewed; not-run
## ChatWindowRoutedUiTest

- [ ] `ChatWindowRoutedUiTest#sessionInitializationFailureShowsRetryAndSecondAttemptConnects` — not-reviewed; not-run
- [ ] `ChatWindowRoutedUiTest#initialSocketFailureShowsReconnectStatusThenRecovers` — not-reviewed; not-run
- [ ] `ChatWindowRoutedUiTest#shiftEnterAddsLineBreakAndEnterPublishesOnce` — not-reviewed; not-run
## ExceptionsSupportUiTest

- [ ] `ExceptionsSupportUiTest#restaurantRejectsOrder` — consolidated into `RestaurantRejectFlowTest#restaurantCancelsOrder`; current deployed verification pending; no duplicate order creation
- [ ] `ExceptionsSupportUiTest#customerCancelsOrder` — consolidated into `OrderCancellationFlowTest#customerCancelsBeforeAcceptance`; current deployed verification pending; no duplicate order creation
- [ ] `ExceptionsSupportUiTest#submitReview` — consolidated into `ReviewFlowTest#submitReview`; current deployed verification pending; no duplicate order creation
## OrderCancellationTest

- [ ] `OrderCancellationTest#restaurantRejectsOrder` — consolidated into `RestaurantRejectFlowTest#restaurantCancelsOrder`; current deployed verification pending; no duplicate order creation
- [ ] `OrderCancellationTest#customerCancelsOrderBeforeAccept` — consolidated into `OrderCancellationFlowTest#customerCancelsBeforeAcceptance`; current deployed verification pending; no duplicate order creation
## RefundRequestTest

- [ ] `RefundRequestTest#requestRefundQuoteForDeliveredTestOrder` — consolidated into `HappyDeliveryFlowTest#completeOrderLifecycle` via `RefundQuoteChecks.verify`; fresh deployed verification running; no independent lifecycle
## CustomerOrderPlacementTest

- [x] `CustomerOrderPlacementTest#customerPlacesOrderSuccessfully` — reviewed; passed; 0, 6, -, o, r, d, e, r, -, d, e, p, l, o, y, e, d, -, C, u, s, t, o, m, e, r, O, r, d, e, r, P, l, a, c, e, m, e, n, t, T, e, s, t, ., x, m, l
- [x] `CustomerOrderPlacementTest#tippedOrderMatchesMockPaidTotal` — reviewed; passed; 0, 6, -, t, i, p, -, f, a, l, l, b, a, c, k, -, C, u, s, t, o, m, e, r, O, r, d, e, r, P, l, a, c, e, m, e, n, t, T, e, s, t, ., x, m, l
- [x] `CustomerOrderPlacementTest#trackingReadFailureDoesNotCreateDuplicateOrder` — reviewed; passed; 0, 6, -, t, i, p, -, f, a, l, l, b, a, c, k, -, C, u, s, t, o, m, e, r, O, r, d, e, r, P, l, a, c, e, m, e, n, t, T, e, s, t, ., x, m, l
## PickupDeliveryOtpTest

- [x] `PickupDeliveryOtpTest#pickupOtpInputAppears` — reviewed/repaired exact lifecycle and negative assertions; passed deployed fast scenario; retained order delivered/released; 10-remaining-final-PickupDeliveryOtpTest.xml, 10-all-retained-postconditions.json
- [x] `PickupDeliveryOtpTest#enterValidPickupOtpAndSwipeToConfirm` — package scope corrected; retained-order resume passed40.204s; exact delivery/release/OFFLINE confirmed with28orders/2addresses retained; 10-package-resume-final-PickupDeliveryOtpTest.xml, 10-package-resume-postconditions.json
- [x] `PickupDeliveryOtpTest#wrongPickupOtpBlocked` — reviewed/repaired exact lifecycle and negative assertions; passed deployed fast scenario; retained order delivered/released; 10-wrong-pickup-final-PickupDeliveryOtpTest.xml, 10-all-retained-postconditions.json
- [x] `PickupDeliveryOtpTest#deliveryPhaseWithOtp` — reviewed/repaired exact lifecycle and negative assertions; passed deployed fast scenario; retained order delivered/released; 10-remaining-final-PickupDeliveryOtpTest.xml, 10-all-retained-postconditions.json
- [x] `PickupDeliveryOtpTest#completeDeliveryWithOtp` — exact offline-after completion passed56.967s; authoritative OFFLINE and delivered/released confirmed;10-offline-after-final-PickupDeliveryOtpTest.xml,10-offline-after-final-postcondition.json
- [x] `PickupDeliveryOtpTest#wrongDeliveryOtpBlocked` — reviewed/repaired exact lifecycle and negative assertions; passed deployed fast scenario; retained order delivered/released; 10-remaining-final-PickupDeliveryOtpTest.xml, 10-all-retained-postconditions.json
- [x] `PickupDeliveryOtpTest#openNavigationMapDuringJob` — reviewed/repaired exact lifecycle and negative assertions; passed deployed fast scenario; retained order delivered/released; 10-remaining-final-PickupDeliveryOtpTest.xml, 10-all-retained-postconditions.json
## RestaurantFulfillmentTest

- [ ] `RestaurantFulfillmentTest#restaurantCanAcceptAndPrepareOrder` — consolidated into `HappyDeliveryFlowTest#completeOrderLifecycle` via `RestaurantAcceptanceChecks`; fresh deployed verification running; no independent lifecycle
- [ ] `RestaurantFulfillmentTest#restaurantRejectsIncomingOrder` — consolidated into `RestaurantRejectFlowTest#restaurantCancelsOrder`; current deployed verification pending; no duplicate order creation
- [ ] `RestaurantFulfillmentTest#restaurantQueueTabStates` — not-reviewed; not-run
## RestaurantNavigationUiTest

- [x] `RestaurantNavigationUiTest#restaurantSectionsRender` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 64-final-session-release-and-three-reruns.json
- [x] `RestaurantNavigationUiTest#restaurantReviewsShowPublicFeedbackAndAggregate` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 62-retained-four-regressions-invocation1-original.json
## RiderAvailabilityUiTest

- [ ] `RiderAvailabilityUiTest#riderDutyAndDashboardState` — not-reviewed; not-run
## RiderFulfillmentTest

- [ ] `RiderFulfillmentTest#riderCanCompleteFulfillment` — not-reviewed; not-run
## ResilienceRegressionUiTest

- [ ] `ResilienceRegressionUiTest#freshContextStartsAtRoleSelector` — not-reviewed; not-run
- [ ] `ResilienceRegressionUiTest#allOperationalRolesRemainIsolated` — not-reviewed; not-run
## ResponsiveAccessibilityTest

- [ ] `ResponsiveAccessibilityTest#desktopRoleSelectorFitsViewport` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#mobileCustomerDashboardFitsViewport` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#tabletCustomerDashboardFitsViewport` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#mobileRestaurantDashboardFitsViewport` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#mobileRiderDashboardFitsViewport` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#escapeClosesAddressDialog` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#cartQuantityIsLiveAndEscapeClosesDrawer` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#visibleRoleButtonsHaveAccessibleNames` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#tabKeyAdvancesThroughCustomerDashboardControls` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#customerRestaurantImagesHaveAltAttributes` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#phoneInputHasAssociatedLabel` — not-reviewed; not-run
- [ ] `ResponsiveAccessibilityTest#unknownRouteRendersAuthenticatedCustomerSafely` — not-reviewed; not-run
## PartnerOperationsUiTest

- [ ] `PartnerOperationsUiTest#verifyCampaignManagement` — reviewed checkpoint34 (accepts either Campaigns screen); not-run since
- [ ] `PartnerOperationsUiTest#verifyRiderEarnings` — not-reviewed; not-run
## RestaurantMenuManagementTest

- [ ] `RestaurantMenuManagementTest#toggleMenuItemAvailability` — not-reviewed; not-run
## RestaurantUiTest

- [ ] `RestaurantUiTest#verifyRestaurantDashboardUI` — not-reviewed; not-run
- [ ] `RestaurantUiTest#verifyRestaurantRoutingPersistence` — not-reviewed; not-run
## RiderOnboardingFullTest

- [ ] `RiderOnboardingFullTest#onboardingWizardVisible` — not-reviewed; not-run
- [ ] `RiderOnboardingFullTest#fillVehicleNumberInWizard` — not-reviewed; not-run
- [ ] `RiderOnboardingFullTest#selectVehicleTypeInWizard` — not-reviewed; not-run
- [ ] `RiderOnboardingFullTest#navigateWizardSteps` — not-reviewed; not-run
## RiderOnboardingTest

- [ ] `RiderOnboardingTest#checkOnboardingWizard` — not-reviewed; not-run
## RiderUiTest

- [ ] `RiderUiTest#verifyRiderDashboardAndOnlineStatus` — not-reviewed; not-run
- [ ] `RiderUiTest#verifyRiderRoutingPersistence` — not-reviewed; not-run
## DelayApprovalFlowTest

- [ ] `DelayApprovalFlowTest#customerApprovesRestaurantDelay` — not-reviewed; not-run
- [ ] `DelayApprovalFlowTest#customerRejectsRestaurantDelay` — not-reviewed; not-run
## HappyDeliveryFlowTest

- [x] `HappyDeliveryFlowTest#completeOrderLifecycle` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 55-happy-owned-delivery-invocation9-pass.json; retained earlier-step/failure evidence;56-money-quote branch
- [x] `HappyDeliveryFlowTest#overlappingOrdersRemainIndependent` — reviewed current source and owning scenarios; passed strengthened carousel and two deliveries135.077s; 08-concurrent-corrected-HappyDeliveryFlowTest.xml, 09-carousel-final-HappyDeliveryFlowTest.xml, 09-carousel-first-postcondition.json, 09-carousel-second-postcondition.json
## OrderCancellationFlowTest

- [ ] `OrderCancellationFlowTest#customerCancelsBeforeAcceptance` — not-reviewed; not-run
## RestaurantRejectFlowTest

- [ ] `RestaurantRejectFlowTest#restaurantCancelsOrder` — not-reviewed; not-run
## ReviewFlowTest

- [ ] `ReviewFlowTest#submitReview` — not-reviewed; not-run
## RiderReviewHistoryApiTest

- [ ] `RiderReviewHistoryApiTest#riderCanReadCompletedHistoryWhileOffline` — not-reviewed; not-run
## NetworkRecoveryUiTest

- [ ] `NetworkRecoveryUiTest#cartAndPageRecoverAfterOfflinePeriod` — not-reviewed; not-run
## PageReloadRecoveryTest

- [ ] `PageReloadRecoveryTest#sessionPersistsAcrossReload` — not-reviewed; not-run
- [ ] `PageReloadRecoveryTest#selectedAddressPersistsAcrossReload` — not-reviewed; not-run
- [x] `PageReloadRecoveryTest#cartPersistsAcrossReload` — reviewed; passed; 0, 5, -, i, s, o, l, a, t, i, o, n, -, i, n, i, t, i, a, l, -, P, a, g, e, R, e, l, o, a, d, R, e, c, o, v, e, r, y, T, e, s, t, ., x, m, l
- [ ] `PageReloadRecoveryTest#settingsRoutePersistsAcrossReload` — not-reviewed; not-run
- [ ] `PageReloadRecoveryTest#secondTabSharesCustomerSession` — not-reviewed; not-run
- [x] `PageReloadRecoveryTest#browserBackFromPaymentPreservesCart` — reviewed; passed; 0, 6, -, c, o, r, e, -, P, a, g, e, R, e, l, o, a, d, R, e, c, o, v, e, r, y, T, e, s, t, ., x, m, l
- [ ] `PageReloadRecoveryTest#browserBackFromRestaurantMenuRestoresHome` — not-reviewed; not-run
- [ ] `PageReloadRecoveryTest#cartSynchronizesAcrossCustomerTabs` — not-reviewed; not-run
## SSEReconnectTest

- [ ] `SSEReconnectTest#sseReconnection` — not-reviewed; not-run
## AdminReadOnlyUiTest

- [ ] `AdminReadOnlyUiTest#ledgerFiltersCanBeCleared` — not-reviewed; not-run
- [ ] `AdminReadOnlyUiTest#categoriesEditorOpensWithoutChangingData` — not-reviewed; not-run
- [ ] `AdminReadOnlyUiTest#supportStatusNavigation` — not-reviewed; not-run
- [ ] `AdminReadOnlyUiTest#findExistingCustomerByPhone` — not-reviewed; not-run
- [ ] `AdminReadOnlyUiTest#reviewModerationIsReadOnly` — not-reviewed; not-run
- [ ] `AdminReadOnlyUiTest#moneyOperationsTabsRender` — not-reviewed; not-run
- [ ] `AdminReadOnlyUiTest#payoutHistorySearchForm` — not-reviewed; not-run
## CustomerSettingsUiTest

- [x] `CustomerSettingsUiTest#profilePhoneIsReadOnlyAndCloseReturnsHome` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#keyboardTabsReachHistoryAndWallet` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#everyCustomerSettingsTabCanBeSelectedWithoutLosingSettings` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#profileIdentityAndStoreCreditBalanceRender` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#currentLoggedInDeviceIsListedWithoutRemovingIt` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 64-final-session-release-and-three-reruns.json
- [x] `CustomerSettingsUiTest#savedHomeAddressIsVisibleWithoutEditingIt` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#everyVisibleSettingsButtonHasAnAccessibleName` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#everyVisibleProfileFieldHasAProgrammaticLabel` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#everyVisibleNewAddressFieldHasAProgrammaticLabel` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#blankNewAddressFormIsBlockedAndCanBeClosed` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#myReviewsTabShowsReviewsOrDefinedEmptyState` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#deliveredOrderReviewRequiresAtLeastOneRating` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 57-delivered-read-only-invocation1.json
- [x] `CustomerSettingsUiTest#profileThemeClassTogglesAndRestoresLight` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#profileDarkThemePersistsAcrossReload` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `CustomerSettingsUiTest#profileDarkThemeChangesRenderedBackground` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 64-final-session-release-and-three-reruns.json
## LoginSmokeTest

- [x] `LoginSmokeTest#isolatedContextsStartWithoutAuthentication` — reviewed; passed; 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, L, o, g, i, n, S, m, o, k, e, T, e, s, t, ., x, m, l
- [x] `LoginSmokeTest#successfulLogin` — reviewed; passed (parameterized); 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, L, o, g, i, n, S, m, o, k, e, T, e, s, t, ., x, m, l
- [x] `LoginSmokeTest#failedLogin` — reviewed; passed (parameterized); 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, L, o, g, i, n, S, m, o, k, e, T, e, s, t, ., x, m, l
## LoginThemeUiTest

- [ ] `LoginThemeUiTest#themeSwitchAndRoleNavigation` — not-reviewed; not-run (parameterized)
## LoginValidationTest

- [x] `LoginValidationTest#phoneValidation` — reviewed; passed (parameterized); 0, 2, -, l, o, g, i, n, -, d, e, p, l, o, y, e, d, -, b, o, u, n, d, a, r, i, e, s, ., x, m, l
- [x] `LoginValidationTest#otpValidationAndBack` — reviewed; passed (parameterized); 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, L, o, g, i, n, V, a, l, i, d, a, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `LoginValidationTest#unallowlistedAdministratorCannotRetrieveDevOtpOrRegister` — reviewed; passed; 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, L, o, g, i, n, V, a, l, i, d, a, t, i, o, n, T, e, s, t, ., x, m, l
- [ ] `LoginValidationTest#verificationLimitResponseKeepsTheBrowserLoggedOut` — reviewed; deferred-excluded; D, E, F, E, R, R, E, D, -, W, A, I, T, -, T, E, S, T, S, ., m, d,  , /,  , D, E, F, E, R, R, E, D, -, R, A, T, E, -, L, I, M, I, T, -, T, E, S, T, S, ., m, d
- [ ] `LoginValidationTest#otpRequestLimitResponseLeavesResendAvailable` — reviewed; deferred-excluded; D, E, F, E, R, R, E, D, -, W, A, I, T, -, T, E, S, T, S, ., m, d,  , /,  , D, E, F, E, R, R, E, D, -, R, A, T, E, -, L, I, M, I, T, -, T, E, S, T, S, ., m, d
- [ ] `LoginValidationTest#expiredOtpIsRejectedWithoutCreatingASession` — reviewed; deferred-excluded; D, E, F, E, R, R, E, D, -, W, A, I, T, -, T, E, S, T, S, ., m, d,  , /,  , D, E, F, E, R, R, E, D, -, R, A, T, E, -, L, I, M, I, T, -, T, E, S, T, S, ., m, d
- [x] `LoginValidationTest#resendRejectsThePreviousCode` — reviewed; passed (parameterized); 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, L, o, g, i, n, V, a, l, i, d, a, t, i, o, n, T, e, s, t, ., x, m, l
- [x] `LoginValidationTest#resendOtp` — reviewed; passed (parameterized); 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, L, o, g, i, n, V, a, l, i, d, a, t, i, o, n, T, e, s, t, ., x, m, l
## MenuCartUiTest

- [x] `MenuCartUiTest#menuDisplaysItemsWithoutRestaurantEditingControls` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#allMenuItemsHaveNamesPositivePricesAndCategories` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#menuDescriptionsUseSecondaryTextStyling` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#everyMenuItemHasLoadedImageOrFallback` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#dietaryMarkersAndPrepTimesNeverInventValues` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#outOfStockItemsCannotBeAdded` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#customerOutletSelectorExposesNoManagementActions` — reviewed; passed; 0, 4, -, f, i, n, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#catalogFailureOffersRetryAndRecoversTheSelectedOutlet` — reviewed; passed; 0, 4, -, r, e, c, o, v, e, r, y, -, d, e, p, l, o, y, e, d, ., x, m, l
- [x] `MenuCartUiTest#successfulEmptyCatalogShowsAnExplicitEmptyState` — reviewed; passed; 0, 4, -, r, e, c, o, v, e, r, y, -, d, e, p, l, o, y, e, d, ., x, m, l
- [x] `MenuCartUiTest#addIncrementDecrementAndEmptyCart` — reviewed; passed; 0, 5, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#removeOnlyItemFromCart` — reviewed; passed; 0, 5, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#fiveSequentialIncrementsReachQuantitySix` — reviewed; passed; 0, 5, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#menuQuantityControlsAndRemoval` — reviewed; passed; 0, 5, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#cartSubtotalAndCloseReopen` — reviewed; passed; 0, 5, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#twoDistinctItemsProduceExactSubtotal` — reviewed; passed; 0, 5, -, i, s, o, l, a, t, i, o, n, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#cartPersistsAfterSettingsNavigation` — reviewed; passed; 0, 5, -, i, n, i, t, i, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#freeDeliveryTrackerShowsProgressAfterAddingItem` — reviewed; passed; 0, 5, -, f, i, n, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#freeDeliveryCanBeUnlockedThroughCartAdditions` — reviewed; passed; 0, 5, -, f, i, n, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
- [x] `MenuCartUiTest#checkoutTotalEqualsItemTotalFeesAndTaxes` — reviewed; passed; 0, 5, -, f, i, n, a, l, -, M, e, n, u, C, a, r, t, U, i, T, e, s, t, ., x, m, l
## NavigationSmokeTest

- [ ] `NavigationSmokeTest#customerTabs` — not-reviewed; not-run
- [ ] `NavigationSmokeTest#restaurantTabs` — not-reviewed; not-run
- [ ] `NavigationSmokeTest#riderTabs` — not-reviewed; not-run
- [ ] `NavigationSmokeTest#adminTabs` — not-reviewed; not-run
## PartnerReadOnlyUiTest

- [x] `PartnerReadOnlyUiTest#riderHistoryDateCanBeCleared` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderHistoryShowsEmptyStateForOldDate` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderCompletedTripShowsRestaurantPayoutAndDate` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 57-delivered-read-only-invocation1.json
- [x] `PartnerReadOnlyUiTest#riderTodayEarningsIsNonNegativeCurrency` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderSettingsCanCloseWithoutChanges` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#restaurantEarningsPanel` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#restaurantProfileCanCloseWithoutChanges` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#restaurantStockControlsRenderWithoutToggling` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- ~~`PartnerReadOnlyUiTest#campaignDraftCanBeCancelled`~~ — moved to `RestaurantCampaignsLiveTest#draftIsDiscardedOnCancel` (checkpoint34)
- [x] `PartnerReadOnlyUiTest#riderVerificationAndWalletSectionsRender` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `PartnerReadOnlyUiTest#riderProfileValuesArePopulatedWithoutEditing` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
## RestaurantDiscoveryUiTest

- [x] `RestaurantDiscoveryUiTest#multipleRestaurantBrandsAndDisplayedDistancesAreValid` — reviewed; passed; 0, 4, -, f, i, n, a, l, -, R, e, s, t, a, u, r, a, n, t, D, i, s, c, o, v, e, r, y, U, i, T, e, s, t, ., x, m, l
- [x] `RestaurantDiscoveryUiTest#everyRestaurantCardHasALoadedNamedCoverImage` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, R, e, s, t, a, u, r, a, n, t, D, i, s, c, o, v, e, r, y, U, i, T, e, s, t, ., x, m, l
- [x] `RestaurantDiscoveryUiTest#searchNoResultsAndClearRestoreRestaurantCards` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, R, e, s, t, a, u, r, a, n, t, D, i, s, c, o, v, e, r, y, U, i, T, e, s, t, ., x, m, l
- [x] `RestaurantDiscoveryUiTest#categoryFilterCanBeSelectedAndCleared` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, R, e, s, t, a, u, r, a, n, t, D, i, s, c, o, v, e, r, y, U, i, T, e, s, t, ., x, m, l
- [x] `RestaurantDiscoveryUiTest#cuisineSearchMatchesTheRenderedCuisine` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, R, e, s, t, a, u, r, a, n, t, D, i, s, c, o, v, e, r, y, U, i, T, e, s, t, ., x, m, l
- [x] `RestaurantDiscoveryUiTest#failedCoverImagesRenderNamedFallbacks` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, R, e, s, t, a, u, r, a, n, t, D, i, s, c, o, v, e, r, y, U, i, T, e, s, t, ., x, m, l
- [x] `RestaurantDiscoveryUiTest#nearbyFailureShowsRetryAndRecoversWithoutClaimingOutOfRange` — reviewed; passed; 0, 4, -, i, n, i, t, i, a, l, -, R, e, s, t, a, u, r, a, n, t, D, i, s, c, o, v, e, r, y, U, i, T, e, s, t, ., x, m, l
- [x] `RestaurantDiscoveryUiTest#emptyNearbyAreaOffersAnAddressChange` — reviewed; passed; 0, 7, -, c, a, r, t, -, i, n, i, t, i, a, l, -, R, e, s, t, a, u, r, a, n, t, D, i, s, c, o, v, e, r, y, U, i, T, e, s, t, ., x, m, l
## RoleNavigationUiTest

- [x] `RoleNavigationUiTest#roleSelectionAndBack` — reviewed; passed (parameterized); 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, R, o, l, e, N, a, v, i, g, a, t, i, o, n, U, i, T, e, s, t, ., x, m, l
- [x] `RoleNavigationUiTest#repeatedMobileRoleChangesDoNotTriggerRequestsOrPageErrors` — reviewed; passed; 0, 2, -, i, n, d, e, p, e, n, d, e, n, t, -, R, o, l, e, N, a, v, i, g, a, t, i, o, n, U, i, T, e, s, t, ., x, m, l
## SavedAddressOutletUiTest

- [x] `SavedAddressOutletUiTest#existingHomeAndNearbyBrand1Outlet` — reviewed; passed; 0, 7, -, c, o, r, e, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#homeAddressModalCanBeReopenedAndDismissedTwice` — reviewed; passed; 0, 7, -, c, o, r, e, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#homeAddressPersistsAcrossReload` — reviewed; passed; 0, 7, -, c, o, r, e, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#visibleBrand1OutletDistancesAreNumeric` — reviewed; passed; 0, 7, -, c, o, r, e, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#switchBetweenTwoNearbyBrand1Outlets` — reviewed; passed; 0, 7, -, c, o, r, e, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#outletSelectorArrowDownMovesFocus` — reviewed; passed; 0, 7, -, c, o, r, e, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#reloadKeepsAddressIdentityAndCoordinatesConsistent` — reviewed; passed (parameterized); 0, 7, -, b, i, n, d, i, n, g, -, d, e, p, l, o, y, e, d, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#outletSwitchWithCartRequiresDecisionAndPreservesOtherCart` — reviewed; passed (parameterized); 0, 7, -, c, a, r, t, -, c, o, r, r, e, c, t, e, d, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#selectingSameOutletKeepsCartWithoutConfirmation` — reviewed; passed; 0, 7, -, c, a, r, t, -, c, o, r, r, e, c, t, e, d, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#keyboardWrapHomeEndSkipDisabledOutlets` — reviewed; passed; 0, 7, -, k, e, y, b, o, a, r, d, -, r, o, u, t, e, d, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
- [x] `SavedAddressOutletUiTest#changingSavedAddressConfirmsCartClearAndQuotesNewAddress` — reviewed; passed; 0, 7, -, b, i, n, d, i, n, g, -, d, e, p, l, o, y, e, d, -, S, a, v, e, d, A, d, d, r, e, s, s, O, u, t, l, e, t, U, i, T, e, s, t, ., x, m, l
## SessionUiTest

- [x] `SessionUiTest#reloadAndLogout` — reviewed; passed (parameterized); 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, S, e, s, s, i, o, n, U, i, T, e, s, t, ., x, m, l
- [x] `SessionUiTest#unauthenticatedDeepLinksExposeOnlyLogin` — reviewed; passed (parameterized); 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, S, e, s, s, i, o, n, U, i, T, e, s, t, ., x, m, l
- [x] `SessionUiTest#authenticatedRoleCannotOpenAnotherDashboard` — reviewed; passed (parameterized); 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, S, e, s, s, i, o, n, U, i, T, e, s, t, ., x, m, l
- [x] `SessionUiTest#unsignedAdministratorHeadersCannotReadSessions` — reviewed; passed; 0, 3, -, r, e, t, a, i, n, e, d, -, c, o, r, e, -, S, e, s, s, i, o, n, U, i, T, e, s, t, ., x, m, l
## E2eOtpClientTest

- [ ] `E2eOtpClientTest#usesTheGatewayOriginRatherThanTheBrowserRoute` — not-reviewed; not-run
- [ ] `E2eOtpClientTest#refusesToSendTheRunnerCredentialOverRemotePlaintextHttp` — not-reviewed; not-run
- [ ] `E2eOtpClientTest#permitsPlainHttpOnlyForLocalDevelopment` — not-reviewed; not-run
## SeededRiderDutyTest

- [x] `SeededRiderDutyTest#offlineRiderNeedsNoStatusWrite` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, S, e, e, d, e, d, R, i, d, e, r, D, u, t, y, T, e, s, t, ., x, m, l
- [x] `SeededRiderDutyTest#onlineIdleRiderGoesOfflineAndServerStateIsChecked` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, S, e, e, d, e, d, R, i, d, e, r, D, u, t, y, T, e, s, t, ., x, m, l
- [x] `SeededRiderDutyTest#activeDeliveryIsNotForcedOffline` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, S, e, e, d, e, d, R, i, d, e, r, D, u, t, y, T, e, s, t, ., x, m, l
- [x] `SeededRiderDutyTest#rejectedOfflineRequestIsReported` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, S, e, e, d, e, d, R, i, d, e, r, D, u, t, y, T, e, s, t, ., x, m, l
- [x] `SeededRiderDutyTest#optimisticButtonCannotReplaceServerOfflineConfirmation` — reviewed; passed; r, e, t, e, n, t, i, o, n, -, S, e, e, d, e, d, R, i, d, e, r, D, u, t, y, T, e, s, t, ., x, m, l

## SupportRefundResolutionFlowTest (added checkpoint23; this entry 2026-10-02T23:58+05:30, checkpoint24)

Rescanned from source: 3 methods, run in `@Order` on one delivered order (`-Dsupport.order.id`).
- [x] `SupportRefundResolutionFlowTest#deniedRequestMovesNoMoney` — SUPPORT-REFUND-02; reviewed; passed live on 0554f250 (evidence/24-support-refund-run.json)
- [x] `SupportRefundResolutionFlowTest#reducedAwardWithRestaurantFault` — SUPPORT-REFUND-01; reviewed; passed live on 0554f250 with DB money proof
- [x] `SupportRefundResolutionFlowTest#refundedItemCannotBeRefundedAgain` — SUPPORT-REFUND-03; failed live at checkpoint24 on a product defect; **passed on deployed cc04ed7 at checkpoint25** (d3acfc93)

Checkpoint25: all three passed together on d3acfc93 (3/3, 73.4s). Precondition: order updated within two hours.

## ChatSupportWindowClosedTest (added checkpoint26, 2026-10-03T07:40+05:30)

- [x] `ChatSupportWindowClosedTest#closedWindowOffersNoSupportEntry` (parameterized by `-Dsupport.closed.order.ids`) — CHAT-REFUND-05; reviewed; passed live 2/2 on 0554f250, d3acfc93 (UI 41578ee)

## Checkpoint27 additions (2026-10-03T08:00+05:30)

- [x] `ChatAndRefundIsolationTest#outsidersAreRefused` — CHAT-ISO-01..03; passed live at checkpoint29 on d3acfc93 (order read now 404)
- [x] `ChatHistoryPagingTest#earlierMessagesCanBeLoaded` — CHAT-22; red on UI 41578ee, passed live on UI 766b214 (cf608115)
- [x] `AdminRefundRetryFlowTest#declinedRefundIsRetriedAndCompletes` — REFUND-RETRY-01; passed live at checkpoint29 (cf608115, refund 67562478)
- [x] `AdminOrderMoneyOutcomesTest#panelShowsTheOutcome` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 62-retained-four-regressions-invocation1-original.json
- [x] `OrderCancellationFlowTest#customerCancelsBeforeAcceptance` — passed live at checkpoint30 (19359711)
- [x] `RestaurantRejectFlowTest#restaurantCancelsOrder` — passed live at checkpoint30 (bf109947)
- [x] `OrderReviewsFlowTest#participantsReviewEachOther` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 58-participant-reviews-invocation1-pass.json
- [x] `RestaurantNavigationUiTest#restaurantReviewsShowPublicFeedbackAndAggregate` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 62-retained-four-regressions-invocation1-original.json
- [x] `CustomerSettingsUiTest#myReviewsTabShowsReviewsOrDefinedEmptyState` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 59-settings-partner-invocation1-original.json
- [x] `RiderReviewHistoryApiTest` — REVIEW-05 history read; passed checkpoint32
- [x] `RestaurantEarningsLiveTest#earningsMatchTheLedger` — current O4/O5 deployed UI pass; original failures retained; checkpoint113; 62-retained-four-regressions-invocation1-original.json
- [ ] `RestaurantCampaignsLiveTest#tabMatchesTheOwnersAdvertiser` — CAMPAIGN-01/02/03, ONBOARD-01/02, WALLET-01; written checkpoint34, awaits deploy
- [ ] `RestaurantCampaignsLiveTest#draftIsDiscardedOnCancel` — CAMPAIGN-05..07; needs an advertiser; awaits deploy
- [ ] `RestaurantCampaignsLiveTest#launchedCampaignIsStoredInRupees` — CAMPAIGN-14; `-Dcampaign.create=true`; awaits deploy

## OrganisationLifecycleApiTest — 2026-10-03T15:50:36+05:30

- [ ] `OrganisationLifecycleApiTest#organisationLifecycle` — reviewed; compile passed; live not run; ORG-01..06; checkpoint35 owner rollout gate.
