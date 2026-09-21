# Test Writing Standards

Contents:
1. Follow the repo first
2. Locator priority and verification
3. Asserted text
4. Waiting
5. Dummy data rules
6. Dummy images and files
7. Tricky widgets
8. Structuring the test
9. Code skeletons (placeholders only)

---

## 1. Follow the repo first

Before writing anything, read two or three existing E2E tests and the test config. Match their framework, folder layout, naming, helper and page-object patterns, fixtures, and how they read environment config. A new test that looks like it belongs is easier to review and maintain than one that is technically fancier. Where this document conflicts with a clear repo convention, follow the repo, and mention the difference to the user if it matters.

---

## 2. Locator priority and verification

Prefer locators that reflect how a user perceives the page, then explicit test hooks, and only then structural selectors.

| Priority | Locator type | Notes |
|----------|--------------|-------|
| 1 | Role + accessible name (button, textbox, link, checkbox, combobox, heading) | Best signal of real usability. Confirm the accessible name from the live accessibility snapshot. |
| 2 | Label text for form fields | Confirm the label in both the UI and the code (watch for i18n). |
| 3 | Test-id attribute already present in the code (`data-testid`, `data-test`, `data-cy`) | Read the exact attribute name and value from the code. |
| 4 | Placeholder or visible text | Only when stable, not dynamic, not translated per locale. |
| 5 | CSS or XPath anchored on stable attributes | Last resort. Never rely on index position, auto-generated class names (hashed CSS-in-JS), or deep DOM paths. |

**Verification is mandatory.** For every locator, confirm on the live page that it resolves to **exactly one** intended element (use the automation tool's locator-count, evaluate, or snapshot reference). Then confirm it against the code. Zero matches means it is wrong; multiple matches means it is ambiguous, so narrow it (scope to a section or dialog) rather than picking `.first()`.

**If no stable hook exists**, add a test-id to the UI code following the repo's convention, keep the change minimal, and tell the user. Do not settle for a brittle selector to avoid touching the code.

**Never assume tags, roles, or names.** A "Submit" button might be a `<div role="button">`, a link styled as a button, or labelled "Continue". Look.

---

## 3. Asserted text

- Copy exact strings from the live UI. Check the code or translation files to see whether the string is static, dynamic, or locale-dependent.
- If the text is dynamic (includes a name, count, or date), assert the stable part or build the expected string from your test data.
- If the text is likely to change with copy edits, prefer asserting on role, state, route, or presence of your unique data over long marketing-style sentences.
- Do not assert on text you have not seen rendered.

---

## 4. Waiting

- **No fixed sleeps** (`waitForTimeout`, `cy.wait(2000)`, `Thread.sleep`). They make tests both slow and flaky.
- Wait for an observable outcome: element visible or enabled, URL changed, spinner gone, row appears, toast shown.
- Use the framework's auto-waiting assertions rather than manual polling loops.
- "Network idle" is unreliable on modern apps with polling or analytics. Prefer waiting for a specific visible result.
- For slow but legitimate operations (uploads, processing), raise the timeout on that specific assertion, not globally, and add a comment explaining why.
- Never wait on backend internals. The test sees only the UI.

---

## 5. Dummy data rules

1. **Satisfy the validators you read** in the frontend and backend code: patterns, min/max lengths, ranges, allowed values, date constraints (future or past only, minimum age), numeric precision.
2. **Unique per run.** Add a timestamp or random suffix to anything that must be unique (email, username, phone where allowed, reference numbers, names used to find a row later). Keep the base readable, for example a prefix like `e2e-` so test data is recognizable and can be cleaned up by the team.
3. **Safe by construction.** Use reserved or test-only domains and values so nothing reaches real people:
   - Email: reserved domains such as `example.com`/`example.org`, or the project's designated test domain. Never a real person's address.
   - Phone: numbers the project or SMS provider designates for testing. Never a real person's number.
   - Names and addresses: obviously fictional, but formatted plausibly for the app's region (check locale rules in validators).
   - Payment: only the provider's documented sandbox test values, and only when the dev profile uses sandbox mode. If unsure whether a payment is live, stop and ask.
4. **Match the app's region and locale**: phone prefix, postal code format, currency, date format.
5. **Store test data in one place** (a constants/fixtures block or the repo's data helper) so it is easy to review and change. Credentials come from environment variables or test config, never hardcoded.
6. **Fill optional fields sparingly.** Fill required fields and a representative subset of optional ones. Fill every optional field only if the flow logic depends on them.

---

## 6. Dummy images and files

- Use the folder the user designated (commonly a Screenshots folder on the Desktop). If you do not know it, ask.
- Read the code for accepted file types, size limits, and dimension or aspect-ratio rules. Choose files that satisfy them. If none do, tell the user which rule blocks and which kind of file is needed rather than working around it.
- **Prefer neutral files.** Screenshots can contain personal or confidential content. Pick ones that show nothing sensitive, and tell the user which you chose.
- **Make the test portable**: copy the chosen files into the repo's test fixtures folder (for example `e2e/fixtures/`) and reference them by relative path, so the test does not depend on someone's Desktop. Keep them small. Tell the user which files you added, since they will be committed.
- Use the framework's upload facility on the real file input, or the drag-and-drop zone if that is all the UI offers. Confirm on the live page that the preview or filename appears before submitting.

---

## 7. Tricky widgets

| Widget | Approach |
|--------|----------|
| **Custom dropdown / select** | Open it, wait for options, click the option by its accessible name. Verify the displayed value changed. |
| **Autocomplete / typeahead** | Type with real key presses, wait for the suggestion list, click a suggestion. Typing alone often leaves the underlying value empty. |
| **Date picker** | If typing works and the value sticks, type it. Otherwise drive the picker (navigate month, click day). Respect allowed ranges. |
| **Masked inputs (phone, card, currency)** | Type sequentially with key presses so the mask logic runs; then verify the formatted displayed value. |
| **File upload** | See section 6. |
| **OTP inputs (multiple boxes)** | Fill each box or paste, per how the UI behaves. Obtain the code from the dev override or provider test value, never by reading the backend. |
| **Captcha** | Do not try to solve it. Look for a dev-profile bypass (see error-triage section 4) or ask the user. |
| **Iframes (payment, embedded widgets)** | Scope locators to the frame. Confirm the frame is present and loaded before interacting. |
| **Maps and location pickers** | Prefer the UI's search box or address input over clicking map coordinates. If only click-on-map exists, verify the coordinates result on the live page. |
| **Toasts and transient banners** | Assert immediately after the action, using auto-waiting; they disappear on a timer. |
| **Modals and overlays** | Scope locators inside the dialog. Wait for it to open and to close. |
| **Multi-step wizards** | Assert the step indicator or heading at the start of each step, so failures point to the right step. |
| **Tables and lists** | Find your row by unique test data, not by position. |
| **New tabs or popups** | Handle via the framework's popup/page event; confirm the new page loaded before interacting. |

---

## 8. Structuring the test

- **One test per user journey**, with named steps for each page or form (for example the framework's `test.step`, or clearly commented sections). Steps in a wizard depend on each other, so splitting them into isolated tests would force each to redo all previous steps.
- **Independent journeys are separate tests** and must not depend on each other's leftovers.
- **Login** is the first step or a shared setup that itself uses the UI (a saved authenticated storage state produced by a UI login is fine; a token minted by calling the backend is not).
- **Name tests by behavior**: "rider completes onboarding and reaches the pending-review screen", not "test1".
- **Add short comments where they help a reviewer**: why a test-id was added, which component a locator was verified against, what a dev-profile precondition is. Do not comment the obvious.
- **List preconditions in the file header**: environment/profile, required dev overrides, test accounts, fixture files, one-time-only flows.
- **Failure diagnostics**: enable screenshot/trace/video on failure if the framework config supports it and the repo does not already.
- **No leftover debug code**: no `pause()`, `console.log` dumps, `.only`, or commented-out experiments.

---

## 9. Code skeletons (placeholders only)

Every locator and string below is a **placeholder**. Replace each with a value you verified in the live UI and the source code. The skeleton shows structure, not real selectors.

### Playwright (TypeScript)

```ts
import { test, expect } from '@playwright/test';

// Preconditions:
//  - Environment/profile: <verified dev profile name>
//  - Dev overrides required: <e.g. OTP bypass under dev profile, or "none">
//  - Fixtures: <e2e/fixtures/...>
// Credentials come from environment variables.

const run = Date.now();
const data = {
  // Values satisfy the validators verified in <component/validator file>.
  fullName: `<verified-format> ${run}`,
  email: `e2e-${run}@example.com`,
};

test.describe('<Role>: <journey name>', () => {
  test('<behavior in plain words>', async ({ page }) => {
    await test.step('Log in', async () => {
      await page.goto('<verified login route>');
      await page.<verified-locator-1>.fill(process.env.E2E_USERNAME!);
      await page.<verified-locator-2>.fill(process.env.E2E_PASSWORD!);
      await page.<verified-locator-3>.click();
      await expect(page).toHaveURL(<verified post-login route>);
    });

    await test.step('<Page or form name>', async () => {
      // Verified against: <UI page> and <component file>
      await expect(page.<verified heading locator>).toBeVisible();
      await page.<verified field locator>.fill(data.fullName);
      await page.<verified submit locator>.click();
      await expect(page.<verified success indicator>).toBeVisible();
    });

    // ...one step per page or form, added while that page was open...

    await test.step('Reach terminal state', async () => {
      await expect(page.<verified terminal indicator>).toBeVisible();
    });
  });
});
```

### Cypress (JavaScript)

```js
// Preconditions: same header as above.
const run = Date.now();
const data = { fullName: `<verified-format> ${run}` };

describe('<Role>: <journey name>', () => {
  it('<behavior in plain words>', () => {
    // Log in via the UI
    cy.visit('<verified login route>');
    cy.<verified-locator>.type(Cypress.env('E2E_USERNAME'));
    cy.<verified-locator>.type(Cypress.env('E2E_PASSWORD'), { log: false });
    cy.<verified-locator>.click();
    cy.url().should('include', '<verified post-login route>');

    // <Page or form name> (verified against <UI page> and <component file>)
    cy.<verified heading locator>.should('be.visible');
    cy.<verified field locator>.type(data.fullName);
    cy.<verified submit locator>.click();
    cy.<verified success indicator>.should('be.visible');
  });
});
```

### Other frameworks

For Selenium, WebdriverIO, or others, keep the same structure: preconditions header, unique data block, verified locators, named steps, outcome assertions after each submit, explicit waits on conditions rather than sleeps.
