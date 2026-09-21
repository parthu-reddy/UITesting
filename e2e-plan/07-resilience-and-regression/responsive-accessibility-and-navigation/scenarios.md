# 07 — Responsive, Accessibility, and Navigation — All Scenarios

Uses: Playwright viewport override, keyboard simulation, standard ARIA assertions.

## Batch 1 — Responsive layout

| ID | Description | Action | Expected result |
|---|---|---|---|
| RESP-01 | Desktop layout (1280×800) | Set viewport 1280×800 → login as customer. | Customer dashboard renders with full-width layout; no horizontal scroll. |
| RESP-02 | Mobile layout (390×844) | Set viewport 390×844 → login as customer. | Mobile layout renders; bottom nav tabs visible; no elements overflow off screen. |
| RESP-03 | Mobile layout restaurant | Set viewport 390×844 → login as restaurant. | Restaurant dashboard renders in mobile layout; all order tabs accessible via bottom nav. |
| RESP-04 | Mobile layout rider | Set viewport 390×844 → login as rider. | Rider dashboard renders in mobile layout; online toggle accessible. |
| RESP-05 | Tablet layout (768×1024) | Set viewport 768×1024. | App renders without broken layout; no elements overlapping. |
| RESP-06 | No horizontal overflow at mobile | Set viewport 390×844 → check for horizontal scrollbar. | `document.body.scrollWidth <= 390` (no horizontal overflow). |
| RESP-07 | Images don't overflow | At mobile viewport. | No `<img>` elements wider than the viewport. |

## Batch 2 — Keyboard navigation

| ID | Description | Action | Expected result |
|---|---|---|---|
| ACCESS-01 | Profile tab → ArrowRight | Rider logged in → focus Profile tab → press ArrowRight. | Next tab (History) becomes selected and focused. |
| ACCESS-02 | Tab key cycles through interactive elements | On customer dashboard → press Tab repeatedly. | Focus moves through all interactive elements in DOM order; no focus trap. |
| ACCESS-03 | Enter key activates button | Focus on "Proceed to Checkout" button → press Enter. | Checkout action executes (same as click). |
| ACCESS-04 | Escape closes modal | Open cart drawer or address modal → press Escape. | Modal/drawer closes. |
| ACCESS-05 | ArrowUp/ArrowDown in dropdown | Open outlet dropdown → press ArrowDown. | Next outlet option highlighted. |

## Batch 3 — Accessibility (ARIA)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ACCESS-06 | Images have alt text | All `<img>` elements on customer home page. | No image with missing `alt` attribute or empty `alt` on non-decorative images. |
| ACCESS-07 | Buttons have accessible names | All `<button>` elements. | No button with empty `aria-label` and no visible text. |
| ACCESS-08 | Form fields have labels | On login, checkout, and settings forms. | Every input field has an associated `<label>` or `aria-label`. |
| ACCESS-09 | Cart quantity output ARIA | Cart quantity stepper output element. | `aria-live="polite"` or equivalent announced for quantity changes. |
| ACCESS-10 | Role selector tabs have ARIA roles | On login role selector. | Tab elements have `role="tab"` or are native radio/button elements with visible labels. |

## Batch 4 — Dark mode and theme

| ID | Description | Action | Expected result |
|---|---|---|---|
| ACCESS-11 | Toggle dark mode | Open Settings → toggle Dark Mode. | `html` or `body` element gets dark-mode class (e.g. `dark`); background color changes. |
| ACCESS-12 | Toggle light mode | Toggle Dark Mode off. | Light theme restored; dark class removed. |
| ACCESS-13 | Customer phone form in dark mode | Enable dark → navigate to login phone form. | Phone form renders without invisible text (white on white or black on black). |
| ACCESS-14 | Theme toggle label changes | After toggling. | Toggle label changes from "Dark Mode" to "Light Mode" (or vice versa). |

## Batch 5 — Navigation regression

| ID | Description | Action | Expected result |
|---|---|---|---|
| NAV-01 | All role dashboards navigate | Each of the four roles can navigate between their main tabs without a crash. | No `TypeError` or blank page on any tab in any role. |
| NAV-02 | Deep link to settings | Navigate directly to the settings URL (if routed). | Settings page loads for the authenticated user; role-correct settings shown. |
| NAV-03 | 404 / not found | Navigate to a non-existent route (e.g. `/i-do-not-exist`). | 404 page or redirect to dashboard; not a blank page. |
