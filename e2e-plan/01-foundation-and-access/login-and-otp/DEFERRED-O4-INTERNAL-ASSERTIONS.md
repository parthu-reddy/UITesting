# Deferred O4 internal assertions

Updated 2026-10-04T17:21:35.186761+05:30. Not executed, not passing E2E evidence. Owner requires all E2E through visible UI only; ordinary source/unit/embedded H2 tests prove internal contracts separately.

| ID | Former browser assertion | Current proof / replacement | Resume condition |
|---|---|---|---|
| O4-INT-001 | Decode JWT/storage and assert role set/ADMIN separation | AuthServiceTest, EntitlementServiceTest and UI portal/step-up journeys | Owner explicitly allows internal browser state inspection |
| O4-INT-002 | Call a product API directly with ordinary/admin tokens to demand403 | Gateway/endpoint authorization tests; visible unauthorised portal navigation | Owner explicitly allows direct API E2E |
| O4-INT-003 | Read DB/Redis projections and compare cross-service statuses after E2E | Deterministic seed validator plus real embedded H2 transactions; visible approval/revocation outcomes | Owner explicitly allows direct-state E2E |
| O4-INT-004 | Force session expiry/version-key loss or manipulate blacklist/token | Fixed-clock auth/gateway and embedded H2 proof; UI-only normal refresh/session controls | Owner explicitly authorises internal setup or a suitable visible fixture |

Natural expiry, deliberate production rate limits and SSE scenarios remain in their existing separate deferred files. No automatic fixture cleanup.

## 2026-10-04 — Current method dispositions

`ChatAndRefundIsolationTest#outsidersAreRefused` is disabled under O4-INT-002: its direct API/token/STOMP fixture cannot run under UI-only policy. `LoginValidationTest#unallowlistedAdministratorCannotRetrieveDevOtpOrRegister` was removed because ordinary O4 Dev person login allows any ten-digit number; staff restriction applies to the separate ADMIN OTP. Visible staff refusal is covered by AdminAuthorizationTest/AdminStepUpTest; direct Dev endpoint/storage assertions are deferred. `verificationLimitResponseKeepsTheBrowserLoggedOut` and `otpRequestLimitResponseLeavesResendAvailable` were removed from browser execution: intercepted synthetic backend responses and storage assertions are local UI/auth unit proof, not deployed E2E. Natural expiry remains explicitly opt-in/deferred; no intentional waits/rate-limit probes are run. Resend scenarios now finish at one person login and a rendered portal choice without inspecting browser storage.

`CrossRoleSessionIsolationTest#forgedHeadersCannotChangeSessionOwnership` is disabled under O4-INT-002. Its old direct API/storage body is historical. The two visible profile/session/logout methods are compiled and selected separately; no direct-state method is included in the live run.

2026-10-04: OrderReviewsFlowTest remainingDishReviewRefreshesTheCachedAggregate, submittedDishReviewRemainsReadOnly and remainingDriverReviewRefreshesTheCachedAggregate are disabled under O4-INT-002 because their old direct eligibility/aggregate requests are prohibited. The selected participantsReviewEachOther journey uses actual dialog requests and proves submitted reviews read-only. HappyDeliveryFlowTest excludes OrderChatChecks synthetic HTTP503 retry and forced socket closure; real UI chat round trips remain required. No disabled method or excluded synthetic assertion is passing proof.
