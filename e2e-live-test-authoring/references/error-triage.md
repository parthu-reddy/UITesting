# Error Triage Reference

Contents:
1. Gathering evidence
2. Classifying by symptom
3. UI input issues (test-side)
4. Third-party integrations and dev-profile overrides
5. Backend issues: stop conditions
6. "Nothing happened" checklist

Use this when a submit fails, a page misbehaves, or the console reports errors. The goal is to decide quickly and correctly between **fix it**, **override it for dev**, and **stop and report**. Guessing wrong in either direction is expensive: fixing something that is actually a backend defect hides a real bug, and stopping on a mistyped phone number wastes the user's time.

---

## 1. Gathering evidence

Do all of these before deciding anything. The session should stay open the whole time.

1. **Console.** Read errors and warnings that appeared since you last cleared it. Note the message, the source file and line if given, and whether it appeared on page load or only on submit.
2. **Network.** List failed or suspicious requests: 4xx, 5xx, cancelled, CORS-blocked, mixed-content, timeouts. For each, record method, URL, status, request payload (relevant fields only), and response body. **Redact** tokens, cookies, passwords, and personal data before putting anything in a report.
3. **The UI's own message.** Inline field errors, toasts, banners, modals. Copy the exact text, since it is often the backend's validation message passed through.
4. **Screenshot** of the current state, taken before you touch anything.
5. **Payload versus code.** Compare what the frontend sent with what the frontend code says it should send, and with what the backend validators (if you can read them) accept.

Read-only investigation of backend source is encouraged. It is how you form a good hypothesis. Editing it is governed by sections 4 and 5.

---

## 2. Classifying by symptom

| Symptom | Most likely category | First thing to check |
|---------|---------------------|----------------------|
| 400/422 with a field-specific message | UI input issue (your data) | Validator for that field in frontend and backend code; fix the value. |
| 400/422 but your data satisfies every validator you can find | Backend issue (validator stricter than documented, or a bug) | Stop and report with payload and response. |
| Form does nothing, no request sent, no console error | UI input issue | Section 6 checklist. |
| JS `TypeError`/`ReferenceError` in console, no failed request | UI code issue | Stack trace file and line; read the component. |
| 401 right after login or mid-flow | Session or token issue | Re-login through the UI; if it recurs, backend/auth issue: report. |
| 403 | Permission | Does this role legitimately have access? If yes, backend/config issue: report. If no, your flow is wrong. |
| 404 on an API call | Deployed service out of sync with the code, or wrong base URL | Report; do not guess routes. |
| 404 on a page route | Wrong URL, feature flag off, or role lacks the page | Check router code and role rules. |
| 500/502/503/504 | Backend/infra | Stop and report. |
| CORS error, blocked request, mixed content | Environment or config | Report; this is rarely fixable from the UI. |
| Error naming a third-party provider (SMS/OTP, KYC, payment, geocoding/maps, email, captcha, storage, push) | Third-party validation | Section 4. |
| Success toast but data missing or wrong on the next page | Backend or data-flow issue | Compare the request payload and the later response; report. |
| Works on second attempt, fails first | Timing/race in UI, or cold start | Add a proper wait for the outcome, not a sleep; if the backend is slow, report. |

If two rows seem to fit, or none does, treat it as **Unknown** and stop and ask.

---

## 3. UI input issues (test-side)

These are yours to fix without asking. Common causes:

- **Format mismatch**: phone number with wrong length or country prefix, email failing a stricter regex, postal code pattern, date format (day/month order), decimal separators.
- **Length or range**: below minimum, above maximum, number outside allowed range.
- **Required-but-hidden fields**: a field below the fold, inside a collapsed section, or revealed by another choice.
- **Dependent fields**: state/city options load only after country is chosen; a later field is disabled until an earlier one is valid.
- **Custom selects and autocompletes**: text typed but no option actually chosen, so the underlying value is empty.
- **Masked or controlled inputs**: programmatic fill does not fire the events the framework listens for. Type with real key presses instead.
- **Date pickers**: typed date rejected; the picker requires clicking a day, or disallows past/future dates.
- **File uploads**: wrong type, too large, wrong aspect ratio or dimensions. Check accepted types and size limits in the code.
- **Terms/consent checkboxes** and captcha widgets that gate the submit button.
- **Uniqueness conflicts**: the value already exists from a previous run. Make data unique per run.

After fixing, retry once. If the same error persists after a correct, validator-compliant input, reclassify: it may be a backend issue.

A genuine **UI code defect** (for example an unhandled exception in a click handler, a field bound to the wrong state key) is fixed in the frontend code. Keep the fix small, follow the project's frontend workflow, and tell the user what you changed. If the "defect" might actually be intended product behavior, ask first.

---

## 4. Third-party integrations and dev-profile overrides

Third-party checks are the most common reason a UI-only test cannot get past a form: an SMS OTP that goes to a real phone, an identity or document verification, a payment authorization, an address or geocoding lookup, an email verification link, a captcha, a virus scan on upload. The test cannot and should not depend on a real external provider.

### 4.1 Recognize it

- The console or response body names a provider or an integration error (timeouts to an external domain, "verification failed", "invalid OTP", "captcha token missing", "address could not be validated").
- A network request goes to a third-party domain and fails, or the backend response wraps a provider error.
- The UI is waiting on something a human would do outside the app (read an SMS, click an email link).

### 4.2 Trace it (read-only)

1. From the UI action, find the frontend call, then the backend handler, then the integration client. Search for the provider name, SDK imports, base URLs, and config keys.
2. Read configuration for the different profiles or environments: `application-dev.*`, `.env.development`, environment-specific config, feature flags, `@Profile`-style annotations, `NODE_ENV`/`APP_ENV` checks.
3. Determine **why the check exists**: compliance (KYC, age, sanctions), fraud prevention (captcha, OTP), payment integrity, data quality (address validation). This tells you what a safe dev substitute looks like.
4. Determine whether the dev profile **already** has a switch, sandbox mode, mock, or fake implementation that is simply not enabled in the deployed dev environment.

### 4.3 Choose the least invasive override

In order of preference:

1. **Existing switch or sandbox**: a config flag or provider sandbox mode already supported by the code. Enable it for the dev profile. No logic change.
2. **Provider test values**: providers often document test OTPs, test cards, and test document numbers that work in sandbox mode. Use them, and record them in the test config.
3. **Profile-gated stub or fake client**: a fake implementation of the integration client, wired in only under the dev/test profile.
4. **Profile-gated fixed test value**: for example accept a documented fixed OTP only when running under the dev profile.

**Never acceptable:**

- Removing or weakening the validation for all environments.
- Any bypass that is active in production, or whose activation depends on something a client can send (a header, query parameter, or request field).
- Committing real secrets, API keys, or real personal data.
- Turning on live payment or messaging to make a test pass.

### 4.4 Safety checks before making the change

- Is the override guarded by the dev/test profile or environment in a way that makes it **inert in production by default**? Confirm how the profile is set in each environment.
- Is the change minimal and easy to review (ideally config or a small profile-gated class)?
- Is there a short comment explaining why it exists and that it must never apply in production?
- Does the test now depend on it? If so, document it as a **precondition** in the test file header and in the final summary.

If you cannot be confident the override is inert in production, or it requires more than a small change, **stop and ask**.

### 4.5 Ship it

Tell the user in one or two lines what you found and what you are changing. Then follow Phase 5 of the main skill: commit, push, publish only that service, deploy only that service, and verify in the still-open browser by retrying the blocked step.

If the override is config-only and lives outside the repo, or the project's process for it is unclear, ask where and how it should be applied.

---

## 5. Backend issues: stop conditions

Stop (Phase 4) and do not attempt a fix when:

- Any 5xx response.
- Validation on the server rejects input that satisfies every documented and visible rule.
- Data saved on one page is missing or wrong on the next.
- Authorization errors for a role that should have access.
- CORS, gateway, or infrastructure errors.
- The failure is intermittent and you cannot show a cause.
- The fix would require changing business logic, data models, or shared services.

In the report, make the user's decision easy: give the evidence, your best hypothesis with file references, and what you need from them (for example "should I proceed with a dev-only workaround, or wait for a fix?").

---

## 6. "Nothing happened" checklist

The button click produced no visible result and no console error. Check, in order:

1. Is the submit button disabled or has it a loading spinner that never ended?
2. Is there an inline validation message off-screen? Scroll through the whole form and check for red text, `aria-invalid`, or error containers.
3. Is a required field empty because a custom control did not register the value?
4. Is a terms checkbox or captcha unchecked or unsolved?
5. Is a modal, overlay, or cookie banner intercepting the click? Is another element on top of the button?
6. Did the click land on the right element (locator resolving to a wrapper instead of the button)?
7. Is a request pending (slow response)? Wait for the outcome, not a fixed delay.
8. Does the handler in the code have a guard condition that silently returns early? Read it.

If none of these explains it, treat it as **Unknown** and stop and report.
