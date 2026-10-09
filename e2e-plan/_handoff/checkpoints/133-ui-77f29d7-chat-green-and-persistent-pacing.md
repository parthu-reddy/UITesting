# Checkpoint 133: UI 77f29d7 live; support-chat isolation 4/4; runner pacing persists (2026-10-08 06:30 IST)

## Deploy verified (owner: "UI deployed")
DEPLOY_LOG 2026-10-08T00:46:06Z food-delivery-app-ui `77f29d7` == `Deployment/env_deployments/dev/food-delivery-app-ui.env`
== UI `main` HEAD == origin/main; VM `docker compose ps`: `food-delivery-app-ui:77f29d7…` Up, healthy. The commit holds
exactly `useChatSession.ts` + its test; the working tree equals it. Deployment checkout even with origin/main.
`DEPLOY-IN-PROGRESS` (created 06:12) removed after the check. **New release cutoff: 2026-10-08T00:46:06Z.**

## Live result (evidence `P02_E2EInventory/evidence/chat-77f29d7`, surefire XML read directly)
Canaries 4/4; AdminSupportChatIsolationRoutedUiTest **4/4**, incl. 01 `switchingSupportTickets…` whose
`sessionRequests hasSize(2)` was 4 before the fix. The double session POST is fixed on Dev. Impact A/B: routed
fixtures; no order, ticket, refund or wallet row created.

## Runner changes (`_handoff/tools/run_e2e_batch.py`, local)
- Admin step-up history persists in `_handoff/ADMIN-STEP-UPS.json` (written at each step-up, atomic; loaded at start;
  >10 min dropped; dry run never writes; unreadable file refuses). Live: 5 step-ups written; the 5th waited 248 s.
- Guard tests now isolate the deploy-lock and history paths (they used to fail 7/10 whenever a real deploy was on).
- `test_run_e2e_batch.py` 13/13; each of 6 guards broken in place → red → restored byte-identical.

## Inventory at 00:46:06Z
PASS_CURRENT 8, PASS_OLDER 282, LAST_FAILED 32, NEVER_RUN 31, LAST_SKIPPED 1, DISABLED 5; `validate_p02.py --phase 1` 6/6.

## Parallel split
[PARALLEL-SPLIT.md](../PARALLEL-SPLIT.md): lane 1 (live, this session), lane 2 fixture-schema guard, lane 3 impact-C prep.
Lanes 2/3 are static and must not run mvn in UITesting or edit UITesting/src.

## Next
NEXT-STEPS step 3: A/B/F re-confirmation sweep at the new cutoff. ChatWindowRoutedUiTest (impact C) still needs the
owner's go-ahead.
