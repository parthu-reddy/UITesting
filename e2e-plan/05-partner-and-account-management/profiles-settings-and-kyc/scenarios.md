# 05 — Profiles, Settings, and KYC — All Scenarios

Uses: `SharedSettingsPage`, `RiderSettingsPage`, `RestaurantSettingsPage`, `KycUploadPage`, `SessionManagementPage`.

## Batch 1 — Customer profile

| ID | Description | Action | Expected result |
|---|---|---|---|
| PROFILE-01 | Profile settings open | Customer logs in → selects Home → opens Settings. | `SharedSettingsPage` opens with profile info (name, phone). |
| PROFILE-02 | Phone field is read-only | On customer profile settings. | Phone number field is non-editable (read-only); existing number visible. |
| PROFILE-03 | Customer name visible | On profile settings. | Name field shows a non-empty string (seeded customer's name). |
| PROFILE-04 | Close settings returns to dashboard | Tap Close/Back on settings. | Returns to Customer Dashboard; no crash. |
| PROFILE-05 | Edit name (if allowed) | If name is editable, change it and save. | Updated name visible on settings; dashboard header updates. If editing is not supported, field is read-only — document. |

## Batch 2 — Restaurant profile and settings

| ID | Description | Action | Expected result |
|---|---|---|---|
| PROFILE-06 | Restaurant profile open | Restaurant logs in → opens Settings. | `RestaurantSettingsPage` shows restaurant/brand name and contact info. |
| PROFILE-07 | Contact email visible | On restaurant settings. | Email field visible; not `null`. |
| PROFILE-08 | Close settings returns to restaurant dashboard | Tap Close. | Returns to `RestaurantDashboardPage`. |

## Batch 3 — Rider profile and settings

| ID | Description | Action | Expected result |
|---|---|---|---|
| PROFILE-09 | Rider profile open | Rider logs in → opens Settings. | `RiderSettingsPage` shows name, phone, vehicle type. |
| PROFILE-10 | Documents section visible | On rider settings. | Documents section visible (e.g. "Upload Aadhaar", "Upload License"); not a blank section. |
| PROFILE-11 | Wallet section visible | On rider settings. | Wallet section shows bank account info or UPI ID input; not blank. |
| PROFILE-12 | No fields edited | Verify settings UI without changing any field. | UI renders correctly; no auto-save on view. |

## Batch 4 — KYC upload

| ID | Description | Action | Expected result |
|---|---|---|---|
| KYC-01 | KYC upload page renders | Rider navigates to KYC/document upload section. | `KycUploadPage` renders with document type options. |
| KYC-02 | Aadhaar upload field | On KYC page, Aadhaar upload field visible. | File input or camera capture option visible. |
| KYC-03 | License upload field | License upload field visible. | File input visible. |
| KYC-04 | Upload without file blocked | Tap "Submit" on KYC page with no files selected. | Validation error; form not submitted. |
| KYC-05 | KYC status shown | On rider settings, document status shown (Pending, Verified, Rejected). | Status badge visible and non-empty. |

## Batch 5 — Dark mode / theme settings

| ID | Description | Action | Expected result |
|---|---|---|---|
| PROFILE-13 | Dark mode toggle | Open Settings → toggle Dark Mode. | App-level dark class applied; background color changes. |
| PROFILE-14 | Light mode toggle | Toggle back to Light Mode. | Light theme restored. |
| PROFILE-15 | Theme persists on reload | Toggle Dark Mode → reload page. | Dark Mode still active after reload. |

## Batch 6 — Rider onboarding wizard (`RiderOnboardingWizardPage`)

Multi-step wizard for new riders: vehicle info, ID upload, bank details.

| ID | Description | Action | Expected result |
|---|---|---|---|
| ONBOARD-01 | Onboarding wizard visible for new rider | Login as a new/incomplete rider. | `RiderOnboardingWizardPage.isWizardVisible()` returns true; "Complete Your Profile" / "Step 1" visible. |
| ONBOARD-02 | Wizard shows current step | On wizard. | `getCurrentStep()` returns 1; step indicator visible. |
| ONBOARD-03 | Fill vehicle number | Enter vehicle number "KA01AB1234" via `fillVehicleNumber()`. | Field accepts the value. |
| ONBOARD-04 | Select vehicle type | Select "Bike" via `selectVehicleType("Bike")`. | Vehicle type dropdown reflects "Bike". |
| ONBOARD-05 | Click Next advances step | Tap Next via `clickNext()`. | Step indicator advances to 2. |
| ONBOARD-06 | Submit onboarding | On final step, tap Submit via `clickSubmit()`. | Onboarding marked as complete; rider sees dashboard with online toggle. |
| ONBOARD-07 | Submit without vehicle number blocked | Leave vehicle number blank → tap Next. | Validation error; cannot advance to step 2. |
| ONBOARD-08 | Rider settings shows onboarding status | After onboarding, `RiderSettingsPage.isOnboardingWizardVisible()` should be false. | Wizard no longer shown; settings show completed profile. |

## Batch 7 — Complete Profile Modal (`CompleteProfileModalPage`)

Used when admin or any role first logs in and profile is incomplete.

| ID | Description | Action | Expected result |
|---|---|---|---|
| PROFILE-16 | Profile prompt visible on incomplete profile | Login as admin with incomplete profile. | `CompleteProfileModalPage.isProfilePromptVisible()` returns true. |
| PROFILE-17 | Fill name and email | Enter name "E2E Admin" via `fillName()` and email via `fillEmail()`. | Both fields accept input. |
| PROFILE-18 | Submit completes profile | Tap Submit via `submit()`. | Modal closes; dashboard visible; subsequent logins do NOT show the modal again. |
| PROFILE-19 | Submit with blank name blocked | Leave name empty → tap Submit. | Validation error; modal stays open. |

## Batch 8 — Session Management (`SessionManagementPage`)

Active sessions modal: view devices, terminate individual or all sessions.

| ID | Description | Action | Expected result |
|---|---|---|---|
| SESSION-MGMT-01 | Session management modal opens | Open Settings → tap "Active Sessions" / "Manage Sessions". | `SessionManagementPage.isSessionModalOpen()` returns true. |
| SESSION-MGMT-02 | Session count visible | On session modal. | `getSessionCount()` ≥ 1 (current session always present). |
| SESSION-MGMT-03 | Terminate another session | If > 1 session, tap "Terminate" on another session via `terminateSession(1)`. | Session count decreases by 1; that session is no longer listed. |
| SESSION-MGMT-04 | Terminate all sessions | Tap "Terminate All" via `terminateAll()`. | All sessions terminated; current browser redirected to login/role selector. |
| SESSION-MGMT-05 | Current session always present | With only 1 session. | "Terminate" may be disabled for the current session; cannot terminate self without "Terminate All". |

## Batch 9 — SharedSettings Addresses and Wallet tabs

| ID | Description | Action | Expected result |
|---|---|---|---|
| SETTINGS-01 | Addresses tab opens | Open Settings → `SharedSettingsPage.openAddressesTab()`. | Addresses list visible; `getSavedAddressCount()` ≥ 1. |
| SETTINGS-02 | Home address in saved list | On Addresses tab. | `hasAddress("Home")` returns true. |
| SETTINGS-03 | Wallet tab opens | `SharedSettingsPage.openWalletTab()`. | Wallet section visible; `isWalletVisible()` returns true; balance shown. |
| SETTINGS-04 | Wallet balance is non-negative | On Wallet tab. | `getWalletBalance()` ≥ ₹0; not `null`. |
| SETTINGS-05 | History tab opens | `SharedSettingsPage.openHistoryTab()`. | `isTransactionHistoryVisible()` returns true; table or list renders. |
| SETTINGS-06 | Logout via settings | `SharedSettingsPage.clickLogout()`. | Redirected to role selector; dashboard no longer visible. |

## Batch 10 — Customer Address Modal (`CustomerAddressModalPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADDR-MODAL-01 | Address modal opens | Customer taps "Deliver to" → `CustomerAddressModalPage.waitForModalOpen()`. | `isModalOpen()` returns true; "Delivery Location" heading visible. |
| ADDR-MODAL-02 | Select existing address | Tap "Home" via `selectExistingAddress("Home")`. | Modal closes; dashboard shows "Home" as delivery address. |
| ADDR-MODAL-03 | Add new address form | Tap "Add New Address" via `clickAddNewAddress()`. | Address form fields appear (label, address line). |
| ADDR-MODAL-04 | Fill new address label and line | `fillAddressLabel("Office")` + `fillAddressLine("123 Tech Park")`. | Both fields accept input. |
| ADDR-MODAL-05 | Save new address | Tap "Save" via `saveAddress()`. | New address "Office" appears in address list; `getAddressCount()` increments. |
| ADDR-MODAL-06 | Save address without label blocked | Leave label empty → tap Save. | Validation error; address not saved. |
| ADDR-MODAL-07 | Address count accurate | After saving. | `getAddressCount()` matches the number of visible address entries. |

