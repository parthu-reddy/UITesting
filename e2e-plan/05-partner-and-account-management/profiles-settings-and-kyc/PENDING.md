# Validation notes and pending work

The earlier customer dashboard timeout below is no longer reproducible in `CustomerSettingsUiTest`. The two original tests passed in the latest class run, and the added focused profile/wallet test passed after correcting its locator: seeded name and email are nonempty, and Store Credit is an INR-formatted nonnegative value.

The first profile/wallet attempt found an accessibility defect in the deployed customer settings form: the visible `Full Name` and `Email Address` labels are not programmatically associated with their inputs, so role-based `getByLabel` lookup fails. The functional test uses explicit input types while retaining the data assertions. This label association remains pending as a UI accessibility fix.

Remaining profile, KYC, onboarding and session-management scenarios in `scenarios.md` have not yet been validated. Tests that mutate shared profile, KYC or address data must restore state or use a specifically authorized disposable account.

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
