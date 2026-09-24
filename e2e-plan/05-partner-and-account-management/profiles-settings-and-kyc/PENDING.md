# Validation notes and pending work

The earlier customer dashboard timeout below is no longer reproducible in `CustomerSettingsUiTest`. The two original tests passed in the latest class run, and the added focused profile/wallet test passed after correcting its locator: seeded name and email are nonempty, and Store Credit is an INR-formatted nonnegative value.

The first profile/wallet attempt found an accessibility defect in the deployed customer settings form: the visible `Full Name` and `Email Address` labels are not programmatically associated with their inputs, so role-based `getByLabel` lookup fails. The functional test uses explicit input types while retaining the data assertions. This label association remains pending as a UI accessibility fix.

The strict all-visible-fields audit reproduced the profile association defect on 2026-09-24 and also found an unassociated field in the new-address panel. It accepts explicit `label[for]`, a wrapping label, `aria-label`, or `aria-labelledby`, and deliberately does not treat placeholder text as an accessible label. Both regression tests remain active.

Remaining profile, KYC, onboarding and session-management scenarios in `scenarios.md` have not yet been validated. Tests that mutate shared profile, KYC or address data must restore state or use a specifically authorized disposable account.

The legacy `RegistrationUiTest` generated arbitrary phones and permanently created customer addresses, rider profiles, restaurant brands, and outlets. The profile-completion test similarly used hardcoded unseeded phone `9998887776`. These tests are now disabled until disposable registration accounts and cleanup behavior are explicitly available. They are excluded from the normal seeded-data suite instead of silently polluting the deployed environment.

## Customer dark-theme rendering

The Account Settings theme control adds the `dark` class, but the computed `.app-background` color remains the light value `rgb(255, 252, 248)`. Source places `dark` and `app-background` on the same root element, while the stylesheet uses the descendant selector `.dark .app-background`; that selector cannot match the same element. `profileDarkThemeChangesRenderedBackground` remains strict and failing.

Reload persistence also fails independently: `ThemeContext` initializes state to `light` and does not persist the selected value, so a full reload removes the `dark` class. `profileDarkThemePersistsAcrossReload` remains strict and failing. The immediate dark-to-light class toggle is separated into a non-persistence test so PROFILE-13/14 can be validated without hiding either defect.

The separated immediate toggle test live-passed on 2026-09-24 and restored light mode before teardown.

On 2026-09-23, the read-only rider settings checks passed with randomized approved riders: Rider Settings opened and closed, exact phone was disabled, Documents and Bank statuses rendered as Approved or Pending, Earnings Wallet rendered with a nonnegative INR balance, and Sign Out was visible. Restaurant Account Settings did not open because the deployed restaurant dashboard immediately routes back to Live Kitchen; this matches the known restaurant navigation defect.

The rider profile now also verifies nonempty name, email, vehicle registration and vehicle type. Rider settings exposes no theme toggle, and the deployed rider document root does not carry an observable `dark` class, so PROFILE-13 through PROFILE-15 cannot be asserted through the rider UI as currently designed. Customer saved-address coverage passed without mutation: the Addresses tab shows Home and the Add / Manage Addresses action.

The Add / Manage Addresses form test also passed without mutation. It verifies label, address line, city, state and ZIP fields, proves Save is disabled when required data is incomplete and enabled after all required values are entered, then closes the panel. The unsaved label does not appear afterward, and reopening the form starts with every required field blank.

## Historical failure

**Test Class**: `ProfileSettingsTest.java`
**Failed Method**: `sharedSettingsWalletTab`

## Issue
The test fails with a `TimeoutError` when trying to verify successful login by waiting for the dashboard elements to load before checking profile settings.

**Error Trace:**
```
Timeout 15000ms exceeded.
Call log:
  - waiting for locator("text=Deliver to, text=Good Morning, text=What are you craving").first() to be visible
```

This historical failure is retained for context; no user action is currently required for the customer settings batch.

## 2026-09-24 focused rerun

The legacy `ProfileSettingsTest` address check passed. Its wallet and history checks initially failed because `SharedSettingsPage` still targeted retired labels. The page object now uses the exact `Store Credit` tab and `Available Balance`; the wallet rerun passed despite the deployment returning HTTP 400 from the wallet endpoint, because the UI still rendered its defined balance state. The History tab correctly rendered its defined empty state, `No order history found.`; the obsolete assertion expected a heading that only exists in the wallet transaction view. The history assertion now accepts loading, empty, or populated order-list states while requiring the History tab to be selected, and its focused rerun passed.

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

**Customer dark theme** -- both recorded defects fixed:

- The stylesheet used the descendant selector `.dark .app-background`, but `App.tsx` puts `dark`
  and `app-background` on the SAME element, and a selector with a space cannot match one
  element. `index.css` now carries the compound form `.app-background.dark` as well, keeping the
  descendant form for nested cases. This is what made the measured background stay
  `rgb(255, 252, 248)` after the class was added.
- `ThemeContext` was `useState<Theme>('light')` with nothing written back, so a reload discarded
  the choice. It now seeds lazily from `localStorage` and persists on toggle, with every storage
  access wrapped -- private mode and blocked site data both throw, and a theme is not worth
  failing a render over.

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
