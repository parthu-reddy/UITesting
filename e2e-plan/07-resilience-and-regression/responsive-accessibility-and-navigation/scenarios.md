# 07 — Responsive, Accessibility, and Navigation — All Scenarios

Uses: Playwright viewport override, keyboard simulation, standard ARIA assertions.

## Batch 1 — Responsive layout

| ID | Description | Action | Expected result |
|---|---|---|---|
| RESP-01 | Desktop layout (1280×800) | Set viewport 1280×800. | Implemented on role selector; no horizontal overflow and role controls render. |
| RESP-02 | Mobile layout (390×844) | Set viewport 390×844 → login as customer. | Implemented; Home header renders with no page-level horizontal overflow. |
| RESP-03 | Mobile layout restaurant | Set viewport 390×844 → login as restaurant. | Implemented; Kitchen Kanban renders with no page-level horizontal overflow. Tab navigation remains covered by the known restaurant navigation defect. |
| RESP-04 | Mobile layout rider | Set viewport 390×844 → login as rider. | Implemented; duty control is accessible and no page-level horizontal overflow occurs. |
| RESP-05 | Tablet layout (768×1024) | Set viewport 768×1024 → login as customer. | Implemented; Home header renders with no page-level horizontal overflow. |
| RESP-06 | No horizontal overflow at mobile | Set viewport 390×844 → check document widths. | Implemented across all three operational dashboards. |
| RESP-07 | Images don't overflow | At tested viewport. | Implemented on the role selector. |

## Batch 2 — Keyboard navigation

| ID | Description | Action | Expected result |
|---|---|---|---|
| ACCESS-01 | Profile tab → ArrowRight | Rider logged in → focus Profile tab → press ArrowRight. | Next tab (History) becomes selected and focused. |
| ACCESS-02 | Tab key advances through interactive elements | On customer dashboard → press Tab repeatedly. | Implemented and live-passed twice: focus reaches multiple distinct dashboard controls without remaining on the document body. |
| ACCESS-03 | Enter key activates button | Focus the enabled cart Checkout button → press Enter. | Implemented and live-passed: the named payment dialog opens with the exact cart item; it is closed without payment. |
| ACCESS-04 | Escape closes modal | Open address modal or cart drawer → press Escape. | Both variants implemented; cart validation also checks the drawer is named `Your cart`. |
| ACCESS-05 | ArrowUp/ArrowDown in dropdown | Focus the first option in the live outlet dialog → press ArrowDown. | Active and failing: focus does not move to the next outlet option. |

## Batch 3 — Accessibility (ARIA)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ACCESS-06 | Images declare alt text | All `<img>` elements on customer home page. | Implemented and live-passed twice: no rendered image is missing the `alt` attribute; decorative empty values are allowed. |
| ACCESS-07 | Buttons have accessible names | Visible login role controls and authenticated customer settings. | Implemented and live-passed: all login role controls and every visible customer-settings button expose text or an explicit accessible name. |
| ACCESS-08 | Form fields have labels | On login, checkout, and settings forms. | Strict tests implemented for login, customer profile, and new-address fields; all currently expose at least one unassociated field. Checkout remains blocked by delivery availability. |
| ACCESS-09 | Cart quantity output ARIA | Add an available item → open cart → inspect quantity output. | Implemented; output must expose `aria-live="polite"`. |
| ACCESS-10 | Role selector tabs have ARIA roles | On login role selector. | Implemented through visible native role buttons with accessible names. |

## Batch 4 — Dark mode and theme

| ID | Description | Action | Expected result |
|---|---|---|---|
| ACCESS-11 | Toggle dark mode | Toggle on login screen. | Implemented and passed at 390 px and 1280 px. |
| ACCESS-12 | Toggle light mode | Toggle Dark Mode off. | Implemented and passed at 390 px and 1280 px. |
| ACCESS-13 | Customer phone form in dark mode | Enable dark → navigate to login phone form and enter visible text. | Implemented and live-passed at 390 px and 1280 px; rendered text/background contrast is at least 4.5:1. |
| ACCESS-14 | Theme toggle label changes | After toggling. | Implemented and live-passed through exact accessible names. |

## Batch 5 — Navigation regression

| ID | Description | Action | Expected result |
|---|---|---|---|
| NAV-01 | All role dashboards navigate | Each of the four roles can navigate between their main tabs without a crash. | No `TypeError` or blank page on any tab in any role. |
| NAV-02 | Deep link to settings | Navigate directly to the settings URL if one exists. | Not currently applicable: customer routing uses a wildcard main view and Account Settings has no direct route. |
| NAV-03 | Unknown route fallback | Navigate an authenticated customer to `/i-do-not-exist`. | Implemented and live-passed: URL is retained while the customer dashboard and Home address render safely instead of a blank page. |

Additional validated navigation: `RoleNavigationUiTest` passes all four role-selector forward/back paths at 1280 px and 390 px. Customer Account Settings now live-passes selection of Profile, History, Addresses, My Reviews, and Store Credit while retaining the settings shell. Restaurant tabs remain blocked by the recorded route override defect.
