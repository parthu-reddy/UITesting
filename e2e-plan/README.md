# Phased E2E coverage plan

## Resume with another agent — current entry point

Every agent using this plan must follow [AGENTS.md](AGENTS.md) and record its results, failures, decisions and next steps in this folder. This is a standing user instruction, including for future agents.

Start with [_handoff/START-HERE.md](_handoff/START-HERE.md). It contains the user's standing instructions, current deployed state, completed/remaining/deferred work, owned fixtures, commands, evidence boundaries, issue register and all 28 feature status entries. Each feature has AUDIT-STATUS.md. Update this handoff after every meaningful checkpoint so resumption does not depend on the previous chat.

The full audit remains unfinished. Historical passes below may precede database resets. Current priorities are chat, refunds, money and order-related admin flows; prior fixes are deployed and owned recovery is active. The newer payment callback guard is deployed and healthy; see _handoff/CURRENT-STATE.md and checkpoint 17. No database reset/reseed is needed. The current audit includes scoped backend verification and source fixes, so older UI-only text is historical. No server cleanup, duplicate happy lifecycle, intentional slow/rate waits or SSE execution. See the handoff for exact rules.


Historical status before the current audit (see _handoff for current state): active UI-only implementation across phases 01 through 03. Authentication, reversible checkout presentation, rider availability, and the complete happy delivery lifecycle have passing focused tests. Detailed scenarios are written when work begins on a functionality folder. Existing test files are starting points, not proof that a feature is covered or passing.

## Current audit instructions — 2026-10-01

The user authorized repairing missing or incorrect executable coverage and creating the central audit checklist in RandomDocuments/E2ECoverageAudit_2026-10-01. Work one feature at a time and use that checklist for current evidence; older status statements below are historical. Real user flows use the deployed UI. Read-only backend assertions and retained signup run manifests provide additional isolation evidence, explicitly distinguished from UI actions and routed contracts. Dev onboarding uses mock government approval.

Intentional duration tests and rate-limit tests must stay excluded from current runs. Record them separately in DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md in each applicable feature, with opt-in flags/tags. Do not exhaust relaxed Dev limits, count deferred checks as passes or substitute simulated rate responses for live enforcement proof.

## Current retained-data policy — 2026-10-01

The user explicitly requested no automatic test cleanup, with rider duty as the exception. Preserve created users, profiles, addresses, brands, outlets, orders and server sessions. Do not deactivate, delete, revoke or restore them as teardown. Retain run manifests to identify test-created data. Explicit logout/device removal/cancellation actions stay when they are the behavior being tested; they are not automatic teardown. Browser/tab/resource disposal remains normal. An authenticated idle rider is made OFFLINE at teardown, with authoritative server confirmation; an active delivery is preserved until completion. Slow and rate-limit scenarios remain opt-in and excluded.

Earlier cleanup/retirement evidence below describes past runs before this instruction. It does not govern future execution. Retained state must be checked as each feature is reviewed; do not rely on an automatically empty session list or restored stock.

## Phase order

| Phase | Purpose | Status |
| --- | --- | --- |
| [01-foundation-and-access](01-foundation-and-access/README.md) | Establish reliable configuration, seeded accounts and authentication before order flows. | In progress |
| [02-customer-order-entry](02-customer-order-entry/README.md) | Prepare a customer order using existing data and a serviceable outlet. | In progress |
| [03-order-fulfillment](03-order-fulfillment/README.md) | Complete the core journey while monitoring customer, restaurant and rider together. | In progress |
| [04-order-exceptions-and-support](04-order-exceptions-and-support/README.md) | Extend the working order lifecycle to exception handling and support. | In progress |
| [05-partner-and-account-management](05-partner-and-account-management/README.md) | Cover supporting workflows using seeded accounts and reversible changes. | In progress |
| [06-admin-operations](06-admin-operations/README.md) | Validate administration after confirming a usable existing admin account. | In progress |
| [07-resilience-and-regression](07-resilience-and-regression/README.md) | Make validated functional coverage repeatable across UI and connectivity conditions. | In progress |

## Work one folder at a time

1. Inspect the current UI source, backend rules relevant to the feature, existing page objects and any existing tests. Confirm behavior in the deployed environment.
2. Write that folder’s `scenarios.md`: IDs, role, seeded data, prerequisites, actions, expected UI/business result and retained-data identity. Consider success, rejection, validation, boundaries, permissions and recovery where applicable; do not duplicate another folder’s cases.
3. Implement a small batch in the existing Java/Playwright/JUnit structure under `src/test/java/com/fooddelivery/e2e`. Reuse or repair page objects; these planning folders do not replace executable test packages.
4. Run the focused batch against the configured deployment, inspect screenshots/reports, distinguish application failures from environment or test-data blockers, and record evidence.
5. Mark the folder validated only after implemented cases pass and retained-state/repeatability are checked. Record remaining gaps explicitly, then move to the next folder. Each phase depends on the relevant validated prerequisites in earlier phases.

## Historical starting point (do not restart the audit here)

Start with phase 01: reconcile configuration with the user-confirmed seeded numbers, validate all four roles, and resolve admin account availability. The previous login run recorded six passes and two profile-form blockers using older rider/admin defaults; do not treat that as current verification of the newly supplied rider account.

## Shared rules

- Use [seeded data and prerequisites](TEST-DATA.md). Prefer existing records over creating users, addresses, brands or outlets.
- Keep the URL configurable through `E2E_APP_URL` or `-Dapp.url`; do not embed a tunnel URL into individual tests.
- Keep customer, restaurant and rider sessions open together for order workflows. Do not log out the user’s unrelated sessions.
- Shared seeded accounts/state require serial execution of conflicting flows. Track test-created orders and retained changes; define ownership and repeatability before implementing state-changing tests.
- Reuse the current smoke/flows/features/resilience packages and their helpers. Review each existing test before claiming coverage.
- Broader negative cases, device/browser combinations and precise acceptance rules will be selected per folder after source inspection. This is a coverage roadmap, not a claim of exhaustive scenarios.
- The original planning step did not add executable tests. The current audit explicitly authorizes missing coverage; state-changing cases require scoped test ownership and retained-state tracking.

## UI-only execution update

Historical execution constraint for the earlier batches: UI-only actions and per-feature PENDING.md. Current audit instructions above authorize a central progress checklist and explicitly scoped verification; per-feature failure/deferred notes still belong to their owning folders. New validated coverage includes eight role-navigation cases, four session cases and one Home/nearby-outlet case. Earlier phase checklists are not declarations of complete coverage.

Additional UI-only batches now cover independent cart operations, customer settings, partner history/settings/earnings, stock-control presentation, campaign cancellation, admin forms/navigation, and responsive themes. Read the owning folders for exact validated behavior and open failures; none of the broader phases is declared complete.
