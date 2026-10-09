# Checkpoint 137: feature tags and change-based selection (2026-10-08 18:05 IST)

The owner asked (17:50) to tag tests by feature so a change runs only its features' tests. Done, all static: no Dev run.
Plan: `RandomDocuments/PendingWork_2026-10-07/P02_E2EInventory/Phase7_FeatureTags/`. Tools:
`UITesting/e2e-plan/feature-tags/` (README there).

## Results
- **Vocabulary**: 19 `feature-<name>` tags. **Map**: every one of 1977 in-scope source files resolves.
  - `feature_map.py --coverage` PASS: no dead rule, no unknown feature.
  - Notifications, test infrastructure and parked ONDC resolve to `no-e2e`. The selector reports these instead of
    running anything.
- **Tags**: all 97 test classes edited by `Phase2_TagTests/tools/apply_feature_tags.py`.
  - The tool refuses to run if its table is incomplete.
  - Only `@Tag`/import lines changed (386 lines, diffed against a backup).
  - 50+ one-off tags were deleted, kept: slow, auto-cancel, slow-auth, auth-rate-limit, measurement, ui-only,
    browser-routed, routed-ui, smoke, plus new `parked` (SSEReconnectTest).
- **Selector**: `select_tests.py --git|--files|--features`; runner `--changed [--since REF]`, `--features`,
  `--include-slow`. The runner always excludes parked, measurement, slow-auth and auth-rate-limit.
  - Real check: UI commit 77f29d7 (useChatSession) selects only 4 chat classes (9 methods).
  - The slow lifecycle is named as left out.
- **Gates**: validate_feature_tags 6/6; test_feature_tags 17/17 (a break test found that F2 accepted misspelled
  tags; fixed); runner tests 19/19; locator audit PASS; validate_p02 6/6 (inventory rebuilt); Phase 4 10/10 (dossier
  rebuilt); Phase 6 green; fixture guard PASS; redaction guard PASS; test-compile rc 0.

## Rule (AGENTS.md)
A change runs `run_e2e_batch.py --changed`. A new test needs a feature tag. A new source file must resolve, or
`--coverage` fails. No full sweep unless the owner asks for one.

All local and uncommitted: UITesting (97 test classes, runner + tests, AGENTS.md, feature-tags/), RandomDocuments Phase7.
