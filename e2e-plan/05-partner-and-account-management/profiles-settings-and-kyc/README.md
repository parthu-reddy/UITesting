# profiles-settings-and-kyc

Status: first UI-only batch implemented; see scenarios and validation notes.

Scope: Profiles, settings, onboarding and KYC; reuse existing records by default.

When starting this folder, follow the workflow in the [main plan](../../README.md). Record source/page-object references, prerequisites, scenario IDs, implementation links and validation results here. Add `scenarios.md` only at that point.

See [validation and pending work](PENDING.md).

`CustomerSettingsUiTest` passes with randomized seeded customers. It verifies the settings panel, nonempty name/email, exact logged-in phone, read-only phone control, saved Home address, close-to-Home behavior, keyboard tab navigation, INR-formatted nonnegative Store Credit balance, Transaction History heading, and return to Profile. Rider read-only profile checks now verify populated identity and vehicle values. Missing programmatic associations for the visible name/email labels are documented in `PENDING.md`.

The address-management form is covered without saving: all required address fields render, label and address-line inputs accept temporary values, Save remains disabled while required fields are missing, and closing discards the unsaved draft.
