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
longer match a `FormField`; the descendant form does. `RestaurantMenuEditorPage` and
`AdminCampaignsPage` were updated accordingly and the suite recompiles.

These changes are applied to `FoodDeliveryAppUI` source and pass typecheck, lint and the 339
unit tests. **They are not verified**: nothing here is proven until the UI is deployed and the
owning test is re-run live. Do not mark anything validated on the strength of this note.
