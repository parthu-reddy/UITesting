# Feature tags: run only the E2E tests a change needs

Owner request, 2026-10-08:

> "add tags to tests based on the features they cover so that when we run tests we can only run tests for the features
> we made changes for"

Plan and evidence are in `RandomDocuments/PendingWork_2026-10-07/P02_E2EInventory/Phase7_FeatureTags/`.

## Use
```bash
python3 feature-tags/select_tests.py --git
python3 _handoff/tools/run_e2e_batch.py --changed --evidence DIR
python3 _handoff/tools/run_e2e_batch.py --changed --since origin/main --evidence DIR
python3 _handoff/tools/run_e2e_batch.py --features chat,refunds-support --evidence DIR
```

What each one does:
1. Previews which features and tests your uncommitted changes select, without running anything.
2. Runs that selection.
3. Also includes commits since a ref.
4. Runs explicit features.

Add `--include-slow` to keep `slow`/`auto-cancel` tests. Tests tagged `parked`, `measurement` or
`auth-rate-limit` never run.

## Files
| File | Role |
|---|---|
| `features.json` | Defines the 19 features and maps each repo's source paths to them. Shared code maps to ALL; notifications and test infrastructure map to `no-e2e`. |
| `feature_map.py` | Resolves a path to its features. `--coverage` checks that every one of ~1977 in-scope files resolves and that no rule is dead. |
| `tag_scan.py` | Lists test methods with their effective tags. Strings and comments are blanked before matching. |
| `select_tests.py` | Turns changed paths and features into a runner selection. A changed E2E test or page object selects the tests that use it. |
| `validate_feature_tags.py` | The gate, F1–F6. Every enabled test has a vocabulary feature tag and no one-off tags remain. |
| `test_feature_tags.py` | 17 break tests. Each guard has been seen red. |

## Rules
- A new E2E test needs at least one `@Tag("feature-<name>")`: the features whose behaviour it asserts.
  - A class-level tag covers what every method shares.
  - A method-level tag adds what only that method covers.
- A new product source file must resolve to a feature, or `--coverage` fails. In strict repos (UI, Customer,
  Restaurant, Identity, CommonLibrary) that means adding a rule to `features.json`.
- A selection is only as good as the map. If a change obviously affects a feature the map does not name, fix the
  rule rather than widening the run by hand.
