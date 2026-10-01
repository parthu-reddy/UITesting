# Pending work and observations

## OTP expiry — deferred slow test

Not implemented. With current UI-only access, real expiry requires requesting an OTP, waiting the backend's five-minute lifetime plus a small margin, then submitting the original code and checking visible rejection and continued logged-out state. Redis/database manipulation is unavailable and outside scope; no accelerated backend expiry test will be created. Browser clock changes cannot expire a server-side OTP.

Confirmation for later: include this as a separately tagged slow test? It does not block other UI tests. Old-code rejection after resend would test replacement, not natural expiry.

## Other pending cases

- Rate-limit exhaustion: defer to avoid blocking shared seeded accounts.
- Short phone/OTP input: agree expected validation; generated schemas currently lack length constraints. Do not invent acceptance criteria or create accounts from malformed test data.
- Profile-form validation remains unimplemented beyond authorized admin completion.
- Restaurant logout remains blocked in the deployed UI. `SessionUiTest` logs in and survives reload, but opening Profile settings returns to Live Kitchen and no exact `Log Out` button appears; the UI-only test times out at that control. Customer and rider logout plus post-logout reload passed in the same three-role run.

## Validation evidence

RoleNavigationUiTest passed all eight desktop/mobile cases. Earlier login assertions passed despite background HTTP 403 resource responses; affected endpoints and impact remain undiagnosed. Reports are under UITesting/target/surefire-reports and are overwritten by later runs.

## UI fixes applied 2026-09-24 (not yet deployed or re-run)

The **restaurant navigation route override** is fixed in
`src/pages/restaurant/RestaurantDashboard.tsx`.

Cause, confirmed in source: `view`, `showSettings` and `activeTab` are all derived from
`location.pathname`, and `setActiveTab` / `setView` / `setShowSettings` are all `navigate()`
calls. Three handlers called two of them in sequence, so the second navigation cancelled the
first:

```js
onChange={(key) => { setActiveTab(key); setShowSettings(false); }}
//         -> /restaurant/menu        -> /restaurant   (wins)
```

`setShowSettings(false)` was never needed -- navigating to `/restaurant/<tab>` already leaves the
settings view, because `showSettings` is derived from the path. The redundant second navigation
was removed from the tab handler, from `onToggleProfile` (the reason restaurant **Log Out** was
unreachable) and from `onToggleSettings`.

Re-run the tests in this folder after deployment.

**Label associations** are fixed:

- `shared/ui/FormField.tsx` rendered its `<label>` as a SIBLING of the control with no `htmlFor`,
  across **17 call sites** -- including customer Account Settings' `Full Name` and `Email
  Address`. The control is now nested inside the label, which associates the two without id
  plumbing. `error` and `hint` stay outside the label: a `<label>`'s content model is phrasing
  content, so a `<p>` inside it is invalid HTML.
- `features/identity/components/AuthForm.tsx`: the login `PHONE NUMBER` label now carries
  `htmlFor="login-phone"` and the input carries the matching `id`.
- The address fields had no labels at all, only placeholders. `AddressFormFields.tsx` and
  `AddressDetailsForm.tsx` now give all 7 inputs an `aria-label`. (Placeholder-as-label is
  correctly rejected by the audit: it disappears once the field has a value.)

**This changes the DOM under existing tests.** Sibling combinators `~ input` / `+ input` no
longer match a `FormField`; the descendant form does. `RestaurantMenuEditorPage` was updated
accordingly. The obsolete admin campaigns page object was removed because campaigns are a
restaurant-only surface.

These changes are applied to `FoodDeliveryAppUI` source and pass typecheck, lint and the 339
unit tests. **They are not verified**: nothing here is proven until the UI is deployed and the
owning test is re-run live. Do not mark anything validated on the strength of this note.

## Explicit signup — 2026-10-01 deployed validation

Existing REG-01/02/03 coverage now selects Create account and verifies through `/auth/register`.
The customer test completes profile and persists an address without assuming a seeded Home.
The rider test completes Dev onboarding and stops before duty/dispatch. The restaurant test
completes profile, brand and outlet onboarding. Run via `scripts/run_registration_e2e.py`;
ordinary seeded-account login must continue to use `/auth/verify`.

Deployed REG-01/02/03 passed: 3 tests, 0 failures, 0 errors, 0 skips, using fresh unused
phone numbers. Run manifest: `target/registration/3a361a1da0ff00a4.json`; the runner retired
all three accounts and revoked remaining sessions. LoginSmokeTest also passed all 8 four-role
valid/invalid-OTP checks, and live seed primary-key/admin-role verification passed after signup.
The runner initially omitted Maven's `test` goal and stopped before creating accounts; that
harness defect is fixed and covered by a new runner unit check (5 runner checks passed).
Rider duty, dispatch and financial operations remain outside this registration run.

## Seeded account-state validation — 2026-10-01

Live existing E2E checks passed without failures/errors/skips: incomplete customer profile (1),
no-address customer (2), suspended customer with valid OTP (1), restaurant no-brand/pending/
rejected/inactive-outlet states (4), customer and restaurant admin deep-link/API denial (2).
Database snapshots proved profiles, active flags, addresses, brand/outlet states and rider states
were unchanged; all tested scenario active-session lists were empty afterward.

The first no-address attempt stopped at an assertion expecting a bare array; the real API returns
its standard response envelope. The assertion now checks success=true and data=[]; both cases
passed on rerun. This was a harness correction.

Rider pending/rejected/inactive live checks are not passes: Dev verification summary overwrote
stored results, and onboarding refresh could reactivate an approved inactive rider. Fixes in
GovernmentIDValidationService and DeliveryExecutiveApplication passed 7 and 4 unit tests
respectively. Deploy both services, then run existing RiderOnboardingTest with the documented
scenario opt-in and take another before/after snapshot. Repeat fresh rider registration afterward
to verify the retained Dev shortcut and actual selfie persistence. No duty/dispatch action has
been exercised. Rider admin-route denial also remains pending that deployment.
