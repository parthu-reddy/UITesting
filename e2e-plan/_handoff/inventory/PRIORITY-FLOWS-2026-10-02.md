> Historical checklist snapshot. For current deployment/fixture state and active next steps, read [CURRENT-STATE](../CURRENT-STATE.md) and [checkpoint 17](../checkpoints/17-deployment-confirmation-and-budget-handoff.md). The old pending deployment below is now completed; financial recovery remains open.

# Priority continuation — 2026-10-02

User requested chat, refunds, money and order-related admin validation first, consolidated coverage, no repeated successful lifecycles. SSE is parked; slow/rate tests stay excluded. No publish/deploy authorization is active. No cleanup of test records; idle riders OFFLINE only.

## Fixture and execution plan

| Flow | Fixture | Checks | State |
|---|---|---|---|
| Delivered happy lifecycle | One new quote-paid CARD order; isolated customer/restaurant/rider/admin | Incoming restaurant card/details; acceptance/preparation; dispatch and delivery; chat round trips/blank/length/persistence/typing/unread/reconnect/image/grace; customer receipt, rider earnings and admin monetary arithmetic/ledger trace; delivered refund quote | Existing HappyDeliveryFlowTest being extended; fresh execution pending |
| Restaurant rejection | Separate paid pending order | Required reason/Back, owned rejection and customer terminal reason, refund routing/amount/idempotency, admin money/refund read view | Reuse existing RestaurantRejectFlowTest; additions pending |
| Customer cancellation | Separate paid pending order | Exact cancellation/refund amount, balances/ledger/terminal views and duplicate protection | Existing coverage mapping pending |
| Support decisions | Owned delivered order/ticket; separate ticket per incompatible decision | Quote/ticket amount, approve/partial/deny, boundaries, persisted refund vs ticket state and admin visibility | Source and existing routed coverage review pending |
| Administrative money recovery | Existing controlled browser fixtures and local service tests | Payout maker/checker/idempotency, DLQ recovery, ledger balancing and order isolation | Review/run existing coverage; no shared unrelated writes |

Related assertions are helpers called by one existing test, not additional order-creating methods. Read-only follow-ups may consume an explicit owned manifest after checking identity/state. Incompatible terminal outcomes have separate fixtures. Captured chat failures are asserted after normal delivery, never swallowed as passed coverage. Existing standalone chat lifecycle methods have been moved into OrderChatChecks; redundant successful RestaurantFulfillmentTest method has been consolidated. No scenario is considered passed solely because its name or helper exists.

## Coverage boundaries

- All money must be sourced from the exact order/authoritative API, including nullable fields, signed restaurant payout, rounding, and ledger reference binding.
- Refund approval is not completion; check actual refund/payment/ledger state. Dev uses mock gateway, not an external payment provider.
- Routed fixtures demonstrate UI behavior only. Local backend proof and deployed integration proof are recorded separately.
- Parked SSE/map-duration coverage and intentional expiry/rate waits are not executed or marked passed.
- Dispatch feature review is reordered behind the priority flows; happy dispatch remains reused coverage, not a complete dispatch edge-case claim.

## Current environment checkpoint

Oracle has 162 GB free and services are healthy after user cleanup. Initial fresh databases were empty; after the user completed seeding, selected customer 8000000484, restaurant 9000000001, rider 7000000026 and admin 1000000001 were verified active with their correct roles, with 104 outlets present. Historical audit order IDs no longer exist on this baseline. No cleanup was performed by the audit. Fresh owned lifecycle execution will follow deployment of the refund-context fix.

## Checklist

- [ ] Consolidate existing successful lifecycle/acceptance/chat coverage without dropping assertions.
- [ ] Verify exact money API/UI fields and balanced reference-scoped ledger.
- [ ] Add delivered quote to the same lifecycle.
- [ ] Validate seed readiness and run the consolidated delivered order once.
- [ ] Review rejection/cancellation money branches and execute only distinct outcomes.
- [ ] Review support/refund decisions, payout safety and recovery; repair source-backed defects.
- [ ] Capture reports/manifests and update inventory/scenario mappings.

## Local verification checkpoint

48 focused refund/quote/money backend checks, 25 chat authorization backend checks and 17 UI checks passed. Common-core installation, UI typecheck/focused lint and Java E2E compilation passed. Existing browser-routed chat/admin policy and read-only refund queue checks are running separately; they cannot substitute for the deployed combined lifecycle.

Additional fixes ready in the same deployment: chat initialization/history lifetime (16 focused UI checks, typecheck/lint), and audited reduced itemized support approval (54 focused backend checks; overlaps earlier 48). Ten existing payout browser-fixture tests passed. The three corrected admin refund-policy cases passed; three chat fixture cases are product-blocked until UI deployment. Ledger envelope corrected; money recovery batch still running.

## User-requested pause

2026-10-02: pause until commit/push/deploy confirmation. Final money batch: 10/10 payout, 4/7 money recovery, 1/2 ledger/order-money fixture checks passed. Three money cases failed an exact combined-row event-ID locator; the ledger fixture lacked its API envelope (corrected, rerun pending). Three corrected refund-policy checks and two read-only queue checks passed. Three chat browser cases await the loading fix deployment. No fresh order created. Typecheck, focused lint, Java compilation and diff checks passed. Do not resume automatically before user deployment confirmation. Handoff: outputs/refund-context-and-admin-money-deployment.md in the Codex task directory.

## Resumed after deployment and fresh seed — 2026-10-02

User confirmed deployment and database reset/reseed. All four selected roles are active, rider initially OFFLINE, selected customer has zero orders and 104 outlets exist. Seven affected browser-fixture methods passed (3 chat, 3 admin recovery, 1 ledger pagination), zero skipped. One new shared lifecycle order `b83c71bd-022c-439d-8586-aa4fe5b1c0d1` was created and dispatched; execution continues. Historical manifests are evidence only and cannot be reused after this reset. No cleanup performed.

## Current deployment blockers

One shared order delivered; rider authoritatively OFFLINE. Complete invocation failed only after reaching admin money, which throws LazyInitializationException on detached order items. Dedicated eager item query and detached repository/mapper regression are ready (11 focused CustomerApplication checks passed). Retained quote continuation exposed missing chat outbox publisher; the existing startup regression reproduced it, and Boot scan exclusions fixed startup locally (14 existing chat/startup checks passed). Fourteen refund-display UI checks, typecheck and focused lint passed. CustomerMoneyController now uses actual completedAt, and cancelled/delivered customer screens show refund status independently of order status. Deployment handoff: outputs/admin-money-chat-outbox-refund-state-deployment.md. Continue the same owned delivered order after deployment; do not rerun successful delivery.

Cancellation/rejection consolidation while deployment is pending: removed six duplicate/vacuous methods across two feature classes and the redundant fulfillment rejection; kept one distinct canonical flow per terminal outcome. Moved the rejected-history label and hidden-cancel-after-accept assertions into shared owners. New RefundRecoveryChecks consumes the same owned terminal order and can resume it without new order creation. Source/compilation proof is separate from fresh integration proof.

## Current deployment gate

See 14-postdeployment-financial-contracts.md: prior admin HTTP 500 and outbox startup defects are fixed in the deployed environment; raw Kafka chat contracts, mock refund completion and ledger funding/rejection JSON defects now have focused fixes. 129 local backend invocations pass. Whole quote/cancellation/rejection invocations remain failed, not green. Deploy ledger-service, customer-service, chat-service and payment-gateway, then recover the three owned manifests. Actual support decision integration remains pending. No cleanup; rider OFFLINE.

## Latest continuation — checkpoint 15

[15-delivered-terminal-and-capture.md](../checkpoints/15-delivered-terminal-and-capture.md) supersedes the preceding deployment gate. Old delivered fixture was autonomously failed/refunded by the abandonment job; no replacement lifecycle was created. 81 selected local checks passed, including the previously missed ledger test and directly affected contracts/integration. CustomerApplication and PaymentGatewayIntegration fixes require deployment. Real chat/quote/support decisions and owned money recovery remain pending.
