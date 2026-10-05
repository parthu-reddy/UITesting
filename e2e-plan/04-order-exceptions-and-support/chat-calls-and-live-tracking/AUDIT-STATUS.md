# Durable audit status: chat-calls-and-live-tracking


## 2026-10-05T11:57:12+05:30 — O4/O5 scoped regression acceptance

The required current methods for this feature pass on the retained Dev fixtures. Canonical60631296 is delivered with exact receipt/posted earnings/18balanced ledger lines, selected-item quote without refund submission and four immutable reviews; actual participant chat round trips are retained from the same lifecycle. Settings15/15 distinct methods,partner10/10,restaurant earnings1/1,navigation2/2,admin-money1/1 and exact beneficiary queue1/1 pass. Original failed invocations remain separately recorded; no duplicate lifecycle or server cleanup. See the checkpoint113 release evidence and feature-specific artifacts. Unselected/outcome/provider/internal/routed/duration/rate/SSE cases remain outside this acceptance.
## 2026-10-05T06:59:57+05:30 — checkpoint106: canonical delivered; existing money/quote branch running

Happy assigned resume invocation9 passes1/1,0failures/errors/skips in53.834s on UI509d084/harnessafe12f1. Pickup and delivery return actual200; the exact rider Delivered history has positive payout and today earnings increase by it; the exact customer Order delivered receipt and real post-delivery chat assertion pass. Evidence55 and the separate retained state manifest record DELIVERED. Original creation manifest and both older cancelled orders remain preserved. The canonical checkout/accept/dispatch/both actual chat round trips were observed in invocation6 and Ready/saved arrival in invocation8; their original later failures remain failures, not a fabricated single clean full run.

The existing retained-delivered branch now runs money/quote invocation1 on the same60631296. It reads the normal rendered History/receipt, verifies the exact admin money page and balanced posted payouts, then asks for a selected-item quote without submitting a refund. Staff1000000001 is used to spread ordinary admin verification quota before the final O4/O5 gates. No second checkout, direct business API, DB/Redis or cleanup is used.

Next: successful money/quote, unrated-dialog guard before review writes, required settings/partner/restaurant earnings/navigation/admin-money and immutable participant reviews using this one order, final O4/O5 image gates and measured histograms/checklists. Existing static O4 22/22,O5 7/7 and GitHub941/941 remain valid. Stop after O4/O5; explicit internal/performance-baseline/CSP-mutation/SSE/rate/duration/provider deferrals stay unverified and are now linked from the shared DEFERRED index.

Updated 2026-10-02T17:13:01+05:30. Review: Priority source/local/routed work; real aggregate unfinished. Implementation/next scope: Publisher/raw-message/loading fixes deployed. Evidence: Historical native text receipt plus 3 routed passes; current quote blocked; no complete chat signoff.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint18 (2026-10-02T18:13:07+05:30): local exact-refund queueing, UI refund read/retry/refresh and newest chat window fixes complete; 120customer/22chat/30UI final checks plus typecheck/lint passed. Customer/chat/UI deployment pending. No new live record/action/E2E lifecycle; historical refund/capture/ledger recovery is still open. See [checkpoint18](../../_handoff/checkpoints/18-refund-retry-ui-chat-history.md) and [deployment gate](../../_handoff/DEPLOYMENT-GATE.md).

Checkpoint24 (2026-10-02T23:58:00+05:30): live chat refund path on order 0554f250. Quote, Submit Refund Request, ticket creation and decision messages all worked through chat (session 3e585519). A quote for an already-refunded item produced **no reply**: CustomerApplication's `ChatRefundProcessorService` rolled back its own CHAT_REFUND_ERROR (refusal crossing a transactional proxy) and dead-lettered the request. Fixed locally (refusals decided before the write transaction); deploy customer-service, then rerun SUPPORT-REFUND-03. The deliberate 503 on the first `POST /chat/sessions` in lifecycle runs is OrderChatChecks' retry fixture, not a defect.

Checkpoint25 (2026-10-03T04:40:00+05:30): the refusal reply is proven live on deployed customer-service cc04ed7 (REFUND_ERROR shown, CHAT_REFUND_ERROR committed, no DLT). Found: after the two-hour post-delivery window `CustomerOrderChat` unmounts, but "Something wrong with this order?" stayed and silently did nothing. Fixed locally (`isOrderChatOffered` shared by both; UI deploy pending). The window is UI-only; the backend accepts refund chat commands at any age (decision pending with the user). CHAT-12 (grace period) remains a deferred wait.

Checkpoint26 (2026-10-03T07:40:00+05:30): UI 41578ee deployed. CHAT-REFUND-05 (`ChatSupportWindowClosedTest`) PASS on 0554f250 and d3acfc93: no support button, no chat launcher past the window (pre-fix UI showed the button on the same order). CHAT-REFUND-01 inside the window PASS via the delivered follow-up on bb43e2a4 (button → quote form → real quote). Proof: real deployed UI + DB.

Checkpoint27 (2026-10-03T08:00:00+05:30): isolation (CHAT-ISO-01..03) proven live on d3acfc93 over HTTP and STOMP; the only failure, the outsider's order read answering 500, is fixed locally in customer-service. History beyond 50 (CHAT-22): UI paging + server bound fixed locally; the live test is red on the deployed UI as expected. Deploy customer-service, chat-service and UI, then rerun both (NEXT-STEPS).

Checkpoint29 (2026-10-03T08:50:00+05:30): passed live on deployed code — see [checkpoint29](../../_handoff/checkpoints/29-deployed-priority-runs.md) (isolation on d3acfc93; CHAT-22 and REFUND-RETRY-01 on cf608115).
