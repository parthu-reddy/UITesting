# Phased E2E coverage plan

Status: phase 01 started; environment/account configuration and its checklist are implemented, and the first eight login cases now validated, including admin. Detailed scenarios will be written only when work begins on a functionality folder. Existing test files are starting points, not proof that a feature is covered or passing.

## Phase order

| Phase | Purpose | Status |
| --- | --- | --- |
| [01-foundation-and-access](01-foundation-and-access/README.md) | Establish reliable configuration, seeded accounts and authentication before order flows. | In progress |
| [02-customer-order-entry](02-customer-order-entry/README.md) | Prepare a customer order using existing data and a serviceable outlet. | Planned |
| [03-order-fulfillment](03-order-fulfillment/README.md) | Complete the core journey while monitoring customer, restaurant and rider together. | Planned |
| [04-order-exceptions-and-support](04-order-exceptions-and-support/README.md) | Extend the working order lifecycle to exception handling and support. | Planned |
| [05-partner-and-account-management](05-partner-and-account-management/README.md) | Cover supporting workflows using seeded accounts and reversible changes. | Planned |
| [06-admin-operations](06-admin-operations/README.md) | Validate administration after confirming a usable existing admin account. | Planned |
| [07-resilience-and-regression](07-resilience-and-regression/README.md) | Make validated functional coverage repeatable across UI and connectivity conditions. | Planned |

## Work one folder at a time

1. Inspect the current UI source, backend rules relevant to the feature, existing page objects and any existing tests. Confirm behavior in the deployed environment.
2. Write that folder’s `scenarios.md`: IDs, role, seeded data, prerequisites, actions, expected UI/business result and cleanup. Consider success, rejection, validation, boundaries, permissions and recovery where applicable; do not duplicate another folder’s cases.
3. Implement a small batch in the existing Java/Playwright/JUnit structure under `src/test/java/com/fooddelivery/e2e`. Reuse or repair page objects; these planning folders do not replace executable test packages.
4. Run the focused batch against the configured deployment, inspect screenshots/reports, distinguish application failures from environment or test-data blockers, and record evidence.
5. Mark the folder validated only after implemented cases pass and cleanup/repeatability are checked. Record remaining gaps explicitly, then move to the next folder. Each phase depends on the relevant validated prerequisites in earlier phases.

## Starting point

Start with phase 01: reconcile configuration with the user-confirmed seeded numbers, validate all four roles, and resolve admin account availability. The previous login run recorded six passes and two profile-form blockers using older rider/admin defaults; do not treat that as current verification of the newly supplied rider account.

## Shared rules

- Use [seeded data and prerequisites](TEST-DATA.md). Prefer existing records over creating users, addresses, brands or outlets.
- Keep the URL configurable through `E2E_APP_URL` or `-Dapp.url`; do not embed a tunnel URL into individual tests.
- Keep customer, restaurant and rider sessions open together for order workflows. Do not log out the user’s unrelated sessions.
- Shared seeded accounts/state require serial execution of conflicting flows. Track only test-created orders and restore changed settings; define cleanup before implementing state-changing tests.
- Reuse the current smoke/flows/features/resilience packages and their helpers. Review each existing test before claiming coverage.
- Broader negative cases, device/browser combinations and precise acceptance rules will be selected per folder after source inspection. This is a coverage roadmap, not a claim of exhaustive scenarios.
- No new executable tests or live account changes are part of this planning step.

## UI-only execution update

Current constraint: all test actions must be possible through the deployed UI. No direct backend access or state manipulation. Keep failures, deferred tests, questions and validation notes in `PENDING.md` inside the owning functionality folder. Do not create a central failure/deferred-work log. New validated coverage includes eight role-navigation cases, four session cases and one Home/nearby-outlet case. Earlier phase checklists are not declarations of complete coverage.

Additional UI-only batches now cover independent cart operations, customer settings, partner history/settings/earnings, stock-control presentation, campaign cancellation, admin forms/navigation, and responsive themes. Read the owning folders for exact validated behavior and open failures; none of the broader phases is declared complete.
