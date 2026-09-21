---
name: e2e-live-test-authoring
description: Build valid, UI-only end-to-end (E2E) tests by driving the real application in a live, persistent browser session. Log in as a role or persona, walk every page and form, compare the rendered page against the source code, fill dummy data, write the test code from elements actually observed, submit, and continue until nothing is left to submit. Covers diagnosing console and network errors, overriding third-party validations (OTP, KYC, payment, maps, captcha) for the dev profile only, stopping and reporting backend issues, and shipping backend fixes with single-service publish and deploy workflows. Use this skill whenever the user asks to write, generate, record, extend, or fix end-to-end, E2E, UI, browser, Playwright, Cypress, or Selenium tests, wants to test a user journey (signup, onboarding, registration, checkout, KYC, multi-step forms) as a specific user role, or says "walk through the app and write tests", even if they never mention this skill by name.
---

# Live UI End-to-End Test Authoring

## The idea in one paragraph

Most flaky or fictional E2E tests come from being written blind: selectors guessed from memory, label text assumed, form rules imagined. This skill removes the guessing. You drive the **real UI in a browser that stays open**, and at every page you (1) look at what is actually rendered, (2) compare it with the source code that renders it, (3) fill the form with valid dummy data, (4) write the test step from what you just saw, and (5) submit and observe the result before moving on. The finished test is a recording of verified reality, not a prediction.

## Ground rules

These apply for the whole session. The reasons are given so you can apply them sensibly in situations this document doesn't cover.

| # | Rule | Why |
|---|------|-----|
| 1 | **UI only.** The tests you write interact through the browser UI exclusively: no API calls, no database seeding, no direct service access, no backend shortcuts inside the test code. | The test must prove what a real user can do. Backend shortcuts hide UI bugs and make the test lie. |
| 2 | **Keep the browser session open.** Never close, reload, or navigate away from a live page just because something failed. | Rebuilding session state (login, half-completed wizard, uploaded files) is slow, and the failing page is the best evidence you have. |
| 3 | **Never assume a selector, label, or text.** Every locator and every asserted string must be confirmed in *both* the live UI and the source code before it goes into the test. | Guessed locators are the number one cause of tests that pass on paper and fail on first run. |
| 4 | **Stop and ask when in doubt.** Backend defects, unexplained behavior, missing information, or anything risky means: freeze, gather evidence, report, wait. | The user can unblock you in one message; a wrong guess can cost hours and corrupt the session. |
| 5 | **Dev/test environments only.** Confirm you are not pointed at production before submitting anything. | You are about to create junk data through real forms. |
| 6 | **Smallest possible blast radius for backend changes.** Change, publish, and deploy only the one service that needs it, using the project's own workflows. | Full publishes and deploys are slow and risk unrelated breakage. |

## Workflow overview

```
Phase 0  Preflight ............ discover tools, conventions, environment, credentials
Phase 1  Log in ............... open a live session as the target role
Phase 2  The page loop ........ observe -> compare with code -> fill -> write test step -> submit -> verify
            ^                                                                              |
            +------------------------ repeat until nothing is left to submit --------------+
Phase 3  Error triage ......... console + network -> classify -> fix / override / stop
Phase 4  Stop-and-wait ........ freeze, evidence, report, wait for the user
Phase 5  Backend change flow .. commit -> push -> publish ONE service -> deploy ONE service
Phase 6  Finalize ............. run the test from scratch, coverage check, summary
```

Read the reference files at the point they are needed:

- `references/error-triage.md`: console and network diagnosis, classification table, third-party override playbook. Read when the first error appears.
- `references/test-writing-standards.md`: selector hierarchy, waits, dummy-data rules, tricky widgets, code skeletons. Read before writing the first test step.
- `references/templates.md`: preflight message, session log, stop report, final summary. Read when you need to report to the user.

---

## Phase 0: Preflight

Discover before you touch the browser. Most of this is findable without asking; ask only for what you cannot find, and ask everything in **one** message (see `references/templates.md`).

| Need | Where to look | If missing |
|------|---------------|------------|
| **Browser automation tool** (Playwright MCP, Chrome DevTools MCP, Claude in Chrome, computer-use, or similar) | Your available tools | Tell the user you have no way to drive a browser and stop. |
| **Test framework and conventions** (Playwright, Cypress, Selenium, WebdriverIO; folder layout, page objects, helpers, naming, config) | `package.json`, config files (`playwright.config.*`, `cypress.config.*`), existing test folders | Ask which framework to use. Default to Playwright only if the user agrees. |
| **Frontend source code location** | Repo layout, routes/router files | Ask. You cannot compare UI to code without it. |
| **Base URL and environment/profile** | README, `.env*`, test config, docs | Ask. Never guess. |
| **Target role/persona** (rider, customer, admin, vendor, patient, ...) and **the flow to cover** | The user's request | Ask. |
| **How to log in as that role** (credentials, OTP method, SSO) | README, docs, seed data, existing tests, notes | Ask. Never guess or brute-force credentials. |
| **Dummy asset folder** (images, PDFs) | The location the user named (commonly a Screenshots folder on the Desktop) | Ask where dummy images live. |
| **Single-service publish and deploy workflows** | `.agents/workflows/`, `.github/workflows/`, `docs/`, `Makefile`, `scripts/` (look for names like `publish-one-service` and `deploy-one-service`) | Ask. Never invent deploy commands and never publish or deploy everything. |

**Environment safety check.** Look at the base URL, the profile name, and any visible environment banner. If anything suggests production (real customer data, live payment mode, production domain), stop and ask.

**Credentials in test code.** Read credentials from environment variables or the project's test config, following existing convention. Never hardcode them in test files.

---

## Phase 1: Log in and open a live session

1. Open the base URL in the browser and log in through the UI as the target role, using the method found in preflight.
2. If login involves a third-party step (OTP, captcha, SSO) and it blocks you, go to `references/error-triage.md` section "Third-party integrations". Do not fight it repeatedly; lockout policies exist.
3. Write the login as the first step of the test, using locators you verified on the login page (same rules as Phase 2).
4. Confirm the landing page after login (URL, heading, nav) and record it in the session log.

From this point the browser stays open until the user says otherwise.

---

## Phase 2: The page loop

Repeat this loop for every page, step, or form until there is nothing left to submit. **Do not batch**: write each test step while its page is open. Reconstructing steps afterward from memory or screenshots is exactly how assumptions creep in.

### A. Observe the live page

- Capture the **URL, title, main heading, and a DOM or accessibility snapshot** of the page. A screenshot alone is not inspection; it shows pixels, not the roles, labels, and attributes a locator needs. Take a screenshot too, as evidence.
- Clear the console (or note its current state) so any errors that appear after this point can be attributed to your next action.
- Note everything interactive: fields, dropdowns, checkboxes, radios, toggles, upload zones, date pickers, buttons, links, tabs, stepper position.
- Scroll the whole page. Required fields, terms checkboxes, and submit buttons often sit below the fold.

### B. Compare with the code

- Find the component that renders this page: search the source by route/URL, heading text, or a distinctive label.
- Extract from code: field names, labels, input types, required flags, validation rules (regex, min/max, length, allowed values, date rules), conditional or dependent fields, test-id attributes, what the submit handler does, where it navigates on success, and which API it calls (for diagnosis only; your test never calls it).
- **Reconcile UI and code.** Mismatches are information, not noise. Possible causes: translations (code has a key, UI shows resolved text), feature flags, role-based rendering, a stale local checkout versus the deployed build, or a different branch. UI wins for what to type into the locator; but flag any unexplained mismatch to the user, because a stale checkout means your code reading may be wrong for other pages too.
- Check any i18n file so asserted text matches what actually renders.

### C. Choose dummy data

Follow the dummy-data rules in `references/test-writing-standards.md`. In short: satisfy the validators you just read, make values unique per run, use reserved or test-safe values (never real people's emails or phone numbers), and use the user's designated folder for images.

### D. Fill the form while the page is open

- Fill field by field. After each field, confirm it took the value (some masked or controlled inputs ignore programmatic fills and need real keystrokes).
- Handle selects, autocompletes, date pickers, and uploads as described in `references/test-writing-standards.md`.
- Before submit, re-check for inline validation messages and for disabled state on the submit button.

### E. Write the test step now

- Write the step's test code immediately, using locators you verified on this page.
- Verify every locator against the live page: it must resolve to **exactly one** intended element. Use your automation tool's locator-count or evaluate capability, or its snapshot references. Zero or multiple matches means the locator is wrong.
- Locator priority, waits, and code skeletons are in `references/test-writing-standards.md`.
- If the code offers no stable hook (no accessible name, no label, no test id) and the only option is a brittle selector, add a `data-testid` following the repo's convention (a small UI change), and tell the user you did.

### F. Submit and observe

- Submit. Then observe: URL change, success toast or banner, next form, error messages, spinner behavior.
- **Check the console and failed network requests every time**, even if the UI looks fine. Silent errors today become mysterious failures tomorrow.
- If anything is wrong, leave Phase 2 and go to Phase 3. Do not paper over it.

### G. Assert the outcome

Add assertions to the test for what you just observed: the success message text, the new route, the new row containing your unique data, the status badge. An E2E test that only clicks and never asserts proves nothing.

### H. Advance

Update the session log, then move to the next page and repeat. **The loop ends when** the flow has reached its terminal state (dashboard, confirmation, "pending review" page, and so on) and no unexplored form or pending action remains in the role's scope. Phase 6 defines the final coverage check.

---

## Phase 3: Error triage

When a submit fails, the page behaves oddly, or the console shows errors, diagnose before acting. The full method is in `references/error-triage.md`. The decision summary:

| What you find | Category | Action |
|---------------|----------|--------|
| Dummy data violates a validator, wrong format, hidden required field, masked input, dropdown not truly selected | **UI input issue (test-side)** | Fix your data or interaction and continue. |
| A genuine, small frontend defect (JS runtime error, broken handler, wrong binding) | **UI code issue** | Fix it in the frontend code following the project's frontend workflow; tell the user. |
| A third-party integration validation blocks the flow (OTP, KYC, payment, address or map lookup, captcha, email or phone verification) | **Third-party validation** | Find out why it is needed, check whether it can be overridden **for the dev profile only**, and if so do it via Phase 5. If unclear or risky, ask. |
| 5xx, unexplained 4xx, wrong or missing data, permission errors, CORS or infra errors, server-side validation contradicting the UI | **Backend issue** | **Stop.** Do not attempt a fix. Go to Phase 4. |
| Anything you cannot classify | **Unknown** | Stop and ask (Phase 4). |

---

## Phase 4: Stop-and-wait protocol

Triggers: a backend issue, an unclassifiable error, a UI/code mismatch you cannot explain, missing information, a captcha or OTP with no dev bypass, anything that looks production-like, anything irreversible (real payment, real message sent to a real person), or risk of account lockout.

1. **Freeze.** Do not navigate, reload, close, or retry the submission. Repeated retries can create duplicate records, trip rate limits, or lock the account.
2. **Capture evidence.** Screenshot of the current state, the URL, exactly what you entered, console errors, and for any failed request: method, URL, status, and response body (redact tokens and personal data).
3. **Investigate read-only.** Reading source to form hypotheses is fine and useful. Changing backend code (outside the dev-override case in Phase 3) is not.
4. **Report** using the stop report template in `references/templates.md`: what you did, what happened, the evidence, likely cause with file references, and the specific decision you need from the user.
5. **Wait.** Leave the browser exactly as it is. Do not continue on your own.
6. **On resume:** first check the live page is still in the expected state. If the session expired, log in again through the UI and return to the same step (via URL if the app supports deep links); tell the user before restarting anything from scratch.

---

## Phase 5: Backend change flow

Only enter this phase for a change that is allowed: a **dev-profile override of a third-party validation**, or a fix the user explicitly approved after a Phase 4 report.

1. **Make the smallest change** that solves the problem. Overrides must be inert in production by construction. See the override rules in `references/error-triage.md`.
2. **Commit** with a clear message and **push** to the appropriate branch (follow the repo's branch convention; ask if unsure). Changes must be committed and pushed **before** publishing.
3. **Publish only the changed service** using the project's single-service publish workflow (for example a `publish-one-service` workflow). Wait for it to finish and confirm it succeeded.
4. **Deploy only that service**, after publishing completes, using the project's single-service deploy workflow (for example `deploy-one-service`). Wait for the service to become healthy.
5. **Verify in the still-open browser**: retry the blocked step and confirm the change took effect.
6. If more than one service changed, handle them one at a time. If you are unsure which service owns the code, ask.

If you cannot find these workflows, ask the user where they are. Do not improvise your own publish or deploy commands, and do not publish or deploy everything.

For **frontend** changes, use the project's frontend dev workflow (hot reload or rebuild). If a frontend deployment is needed and no workflow is known, ask.

---

## Phase 6: Finalize

1. **Run the finished test from scratch.** The incremental session proves each step worked once, not that the assembled test passes from a clean start. Run the full test in a **separate** browser context or process (headed first is helpful), leaving the exploration session open. If it fails, diagnose with Phase 3; the usual culprits are timing, state carried over from the live session, and data that was only unique by accident.
2. **Re-run it a second time** to catch collisions from non-unique data. If the flow can only be done once per account (one-time onboarding, for example), say so and ask how the user resets state. Never reach into the backend to reset it.
3. **Coverage check.** Revisit the navigation, menus, and tabs for the role. List any forms or actions not covered, and say why (out of scope, needs another role, blocked, deferred).
4. **Cross-role dependencies.** If the flow needs another role to advance (an admin approving a rider, a vendor accepting an order), note it. Only proceed through that role via the UI, in a separate browser context, with the user's go-ahead and credentials.
5. **Deliver the summary** using the final summary template in `references/templates.md`: test files added, pages and forms covered, dummy data and assets used, dev overrides and code changes, open backend issues, how to run the tests, and candidate follow-up tests (for example validation messages you observed, which could become negative tests).

Do not close the browser until the user confirms they are done with the session.

---

## Quick self-check before every submit

- [ ] I looked at the live DOM/snapshot, not just a screenshot.
- [ ] I compared this page to the code that renders it.
- [ ] Every locator and asserted string was verified in both UI and code.
- [ ] Each locator resolves to exactly one element on the live page.
- [ ] My dummy data satisfies the validators I read.
- [ ] The test step for this page is already written.
- [ ] I will check the console and network after submitting.
- [ ] If something breaks, I will freeze and diagnose, not close or retry blindly.
