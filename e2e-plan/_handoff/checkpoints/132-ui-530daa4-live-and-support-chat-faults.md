# Checkpoint 132: UI 530daa4 live-green; support-chat ×4 diagnosed (2026-10-08 06:05 IST)

Plan, tools, evidence: `RandomDocuments/PendingWork_2026-10-07/P02_E2EInventory/README.md`. Lessons:
`P02_E2EInventory/Phase3_SafeLiveRuns/mistakes_and_improvements.md` and
`CommonMistakesDocumentation/UI/routed-fixture-socket-and-schema-drift-2026-10-08.md`.

## Deploy verified (owner said "deployed")
DEPLOY_LOG 2026-10-07T23:50:20Z food-delivery-app-ui `530daa4` == `Deployment/env_deployments/dev` pin == UI `main`
HEAD == origin; VM container `food-delivery-app-ui:530daa4…` healthy. New release cutoff for the inventory:
**2026-10-07T23:50:20Z** (UI).

## The 3 deployed UI fixes pass live (evidence/ui-530daa4, canaries 4/4)
AdminFleetSafety fleetLayersRefresh… 1/1, AdminSupportTicketResolution rejectionConfirmation… 1/1,
AdminPortalRouteCoverage sidebarDestinationsAndWildcardRedirect… 1/1.

## Support-chat isolation ×4: four stacked faults, three in the test, one in the UI
1. **Fixture drift:** session response lacked `participants[].entityId` (server always sends it; generated Zod
   schema requires it) → Zodios threw → no session. Proved with the real schema via tsx. Same drift fixed in
   `ChatWindowRoutedUiTest` (restaurant entry now `entityId`, no `userId`, as the server sends). Scan: 3/3 objects OK.
2. **Dead socket stub:** `routeWebSocket` was registered after login on a loaded page, so `/ws/chat` reached real
   Dev ("Reconnecting to chat server..."). Both support classes now `navigate(APP_URL + "/admin/support_tickets")`
   after registering routes. Probe proved CONNECT/SUBSCRIBE/SEND reach the stub.
3. **Short-id collision:** both fixture orders displayed as "Order #f1000000"; ticket B is now `f2000000-…` and
   `AdminSupportChatPage.selectTicketForOrder` dropped `.first()` (ambiguity now fails as strict mode).
4. **UI defect (fixed locally, NOT deployed):** `useChatSession` keyed its init effect on `getUserProfile()`, a new
   object every render; `setIsLoading(true)` re-rendered mid-request, so **every chat open sent POST
   /chat/sessions twice** (server is safe: advisory lock + unique index; cost is a doubled roster lookup).
   Effect now keys on `userId`. Vitest: mock made faithful (fresh object) → new test + 5 existing went red
   ("called 2 times"); after the fix 20/20. Full UI gates: lint 0, typecheck 0, vitest 180 files / 1078 tests.
   Whole-set scan for profile objects in hook deps: 0 (scan seen to HIT the pre-fix file at line 198).

Live after fixes 1–3 (evidence/chat-ws-navigate): chat isolation **3/4** (02, 03, 04 pass); 01
`switchingSupportTickets…` fails only on `sessionRequests hasSize(2)` = 4 → needs fix 4 deployed.

Resolution class: my first fix (reload) regressed it (fleet map `fleet-cities` read hit the catch-all, 0/3);
navigate gave 2/3; `fleet-cities` now answered `[]` explicitly (map then requests no layers,
AdminFleetMap.tsx:69-82). Result: **3/3 pass** (evidence/resolution-fleet-cities).

## Owner actions
- Deploy **food-delivery-app-ui** (useChatSession.ts + its test). Then rerun
  `AdminSupportChatIsolationRoutedUiTest#switchingSupportTicketsUsesSeparateReadOnlyChatSessions`.
- `ChatWindowRoutedUiTest` (impact C) needs a go-ahead to run live after its fixture fix.

## Inventory
Rebuilt at cutoff 2026-10-07T23:50:20Z: PASS_CURRENT 12, PASS_OLDER 277, LAST_FAILED 33, NEVER_RUN 31,
DISABLED 5, LAST_SKIPPED 1. `validate_p02.py --phase 1` 6/6 after making P1.5 cutoff-independent (break-tested).

## Runner gap found
`run_e2e_batch.py` keeps step-up pacing only within one invocation: back-to-back batches (and direct `mvn`
probes) hit `ADMIN_STEP_UP_RATE_LIMITED` once each; the built-in retry absorbed it. Not fixed.

## Still open (unchanged)
Gated: reorder ×2 (07:00–20:00 IST), history pagination, cancelled-history, earnings, review history, owned refund
visual, sponsored listing. Impact C batches await go-ahead (Phase4).
