# P0-2 parallel split (2026-10-08 06:30 IST)

Three lanes. Only lane 1 touches Dev. Lanes 2 and 3 are static and can run in other sessions at the same time.

## Why live runs cannot be split
- Every run signs in the same seeded people, and Identity keeps at most 3 sessions per user
  (`IdentityService/.../SessionRegistry.java:25`, `Deployment/identity-service.yml:44` `max-per-user: 3`).
  Two runs evict each other's sessions. The runner enforces this with `_handoff/RUN-IN-PROGRESS`.
- Admin step-up is 5 verifies / 5 min per admin phone. Two sessions share the budget.
- `run_e2e_batch.py` runs `mvn test` (whole test tree compiles) once per class and deletes
  `UITesting/target/surefire-reports` first. Another session's `mvn` in UITesting, or a half-saved edit under
  `UITesting/src`, breaks or corrupts lane 1's evidence.

## Lane ownership

| Lane | Session | Owns (only this lane writes here) | Touches Dev |
|---|---|---|---|
| 1 Live runs | the checkpoint132 session | `_handoff/*.md`, `_handoff/tools/`, `_handoff/checkpoints/`, `UITesting/src/**`, `P02_E2EInventory/evidence/`, `Phase3_SafeLiveRuns/`, memory | yes, through the runner only |
| 2 Fixture-schema guard | parallel session | `P02_E2EInventory/Phase5_FixtureSchemaGuard/` | no |
| 3 Impact-C and gated prep | parallel session | `P02_E2EInventory/Phase4_OwnerApprovedRuns/` | no |

**Lane 1** follows NEXT-STEPS: deploy check, chat isolation 4/4, the A/B/F re-confirmation sweep (226 enabled methods
not PASS_CURRENT at the 530daa4 cutoff), runner pacing persisted across invocations, then impact-C batches only after the
owner's go-ahead. It folds lanes 2 and 3 into the handoff when their HANDOFF.md is ready.

## Rules for lanes 2 and 3
- Never commit or push. No local servers. No Dev access of any kind: no `run_e2e_batch.py`, no `mvn` in UITesting,
  no browser on the tunnel, no ssh.
- Do not edit anything outside your own folder, except NEW files in `CommonMistakesDocumentation/` or
  `CodingPracticesAcrossAllServices/`. In particular, do not edit `UITesting/src/**`, `UITesting/e2e-plan/_handoff/**`,
  `FoodDeliveryAppUI/**` (its working tree feeds the owner's next deploy), memory files, or the other lane's folder.
- Proposed changes to tests or UI go in your folder as unified diffs (`proposed/*.diff`) with the reason for each.
  Lane 1 applies them between live batches.
- Your folder follows the plan layout: `plan.md`, `checklist.md`, `validation.md` (programmatic checks, written before
  any mass change), `mistakes_and_improvements.md`. Finish with `HANDOFF.md`: done, verified how, unverified,
  proposed diffs, what lane 1 must do.
- Never assume, always check the code. A guard counts only once you have seen it fail on a deliberately broken input.
- Read first: `UITesting/e2e-plan/AGENTS.md`, `_handoff/USER-INSTRUCTIONS.md`, checkpoint132, and
  `P02_E2EInventory/README.md`.

## Lane 2: fixture-schema guard (NEXT-STEPS 132 step 6a)
Routed fixtures drifted from the server's real shape (checkpoint132 fault 1: `participants[].entityId` missing, so
Zodios threw in the UI and the test failed far from the cause). Build a static guard that:
1. finds every routed fixture body in `UITesting/src/test/java` (26 files use `route(`/`routeWebSocket`/`fulfill(`),
   with the URL pattern and method it answers;
2. maps each one to its endpoint's generated schema in `FoodDeliveryAppUI/src/api/generated/` (13 schema dirs) and
   parses it with the real Zod schema (the previous agent proved this works via `tsx`);
3. reports PASS / DRIFT (with the Zod path) / UNMAPPED, and FAILS on any DRIFT. UNMAPPED is listed, never skipped
   silently.
Break-test it: a copy of the pre-fix chat fixture without `entityId` must go red. Report the drifts it finds as
proposed diffs.

## Lane 3: impact-C and gated prep (NEXT-STEPS 132 steps 4-5)
Impact C = 40 classes, 103 enabled methods (Phase4 plan.md still says 39/102, from the earlier triage) (28 NEVER_RUN, 24 LAST_FAILED, 51 PASS_OLDER at the 530daa4 cutoff, from
`evidence/inventory.json` + `evidence/triage.json`). Using source and saved evidence only, produce:
1. per class: records it creates/changes, accounts it uses, preconditions (rider online near the outlet, 07:00-20:00
   IST category hours, fixtures), last failure signature, and whether it is stale against the current UI
   (`e2e_locator_audit.py --ui-ref HEAD` is static and allowed);
2. a batch order that keeps the blast radius small and consolidates order-placing tests into the fewest lifecycles
   (HappyDeliveryFlowTest is the canonical lifecycle), with the expected Dev records per batch, for the owner to approve;
3. for the gated items (history pagination, cancelled history, earnings, review history, owned refund visual,
   sponsored listing), the exact seed data each needs, as ONE consolidated seed request for the owner. Agents never
   reset or seed.
