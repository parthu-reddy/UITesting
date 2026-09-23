# Validation and pending accessibility defects

`ResponsiveAccessibilityTest` was replaced with measurable viewport, overflow, image, keyboard-focus, accessible-name, form-label and Escape behavior assertions.

Live validation on 2026-09-23 passes eight non-label tests. Desktop role selection, tablet and mobile customer, mobile restaurant, and mobile rider layouts render without page-level horizontal overflow; images do not exceed the viewport; visible role controls have names; Escape closes both the delivery-location dialog and cart drawer; and cart quantity output exposes `aria-live="polite"`. The focused tablet/cart run passed against Brand 1 Outlet 7 at 0.4 km. `LoginThemeUiTest` also passes at 390 px and 1280 px.

`RoleNavigationUiTest` passes all eight desktop/mobile cases: each of the four role cards opens its phone form, Back returns safely, and switching to Customer starts with an empty phone input.

NAV-03 live-passed. Navigating an authenticated customer to `/i-do-not-exist` retains that URL but safely renders the customer dashboard with the selected Home address rather than showing a blank or crashed page.

ACCESS-02 and ACCESS-06 passed twice in consecutive focused runs. Tab moved through multiple distinct controls on the authenticated customer dashboard, and every rendered customer-home image explicitly declared an `alt` attribute. Empty alt text remains permitted for images that the application marks as decorative.

ACCESS-13 is now implemented in `LoginThemeUiTest` and passed at 390 px and 1280 px. The test enters visible phone text in dark mode, resolves the nearest opaque ancestor background, calculates the WCAG relative-luminance contrast ratio in the rendered browser, and requires at least 4.5:1 before switching back to light mode.

## Confirmed product defect: outlet keyboard navigation

`SavedAddressOutletUiTest.outletSelectorArrowDownMovesFocus` opens the live Brand1 outlet dialog, focuses its first option and presses ArrowDown. The next option does not receive focus. The test remains active and failing for ACCESS-05; mouse selection and selected-checkmark behavior pass separately.

NAV-02 has no route to test in the current UI. `CustomerDashboard.tsx` maps `path="*"` to `CustomerMainView`, while Account Settings is controlled by in-memory view state from the profile button. A direct settings URL therefore cannot show role-correct settings until the application adds such a route.

ACCESS-03 live-passed after using an animation-aware assertion. With a rider made Online through the UI, the test focuses the cart's enabled Checkout button, presses Enter, waits for the named `Complete Your Order` dialog, verifies the exact cart item in the payment summary, and closes without paying.

## Confirmed product defect: phone input label

`phoneInputHasAssociatedLabel` fails on the deployed login form. The visible `PHONE NUMBER` `<label>` has no `for` attribute, the telephone input is not nested inside it, and the input has no `aria-label` or `aria-labelledby`. Assistive technology therefore cannot programmatically associate the visible label with the field. The test remains active and failing until the UI is fixed and deployed.

The same association problem is present in customer Account Settings: the visible `Full Name` and `Email Address` labels do not resolve through accessible label lookup. The read-only functional settings test uses explicit input types to continue validating loaded values; the accessibility gap remains recorded here and in the profile folder.

## Historical environment timeout

**Test Class**: `ResponsiveAccessibilityTest.java`

## Issue
The accessibility tests fail with a `TimeoutError` when trying to verify successful login by waiting for the dashboard elements to load. 

**Error Trace:**
```
Timeout 15000ms exceeded.
Call log:
  - waiting for locator("text=Deliver to, text=Good Morning, text=What are you craving").first() to be visible
```

The earlier dashboard timeout is retained as historical context and was not reproduced by the customer checks in the current batch.
