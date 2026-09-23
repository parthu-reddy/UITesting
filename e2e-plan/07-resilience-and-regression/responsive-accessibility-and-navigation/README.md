# responsive-accessibility-and-navigation

Status: first UI-only checks implemented; broader scenarios remain pending.

Scope: Responsive layouts, keyboard accessibility and shared navigation.

When starting this folder, follow the workflow in the [main plan](../../README.md). Record source/page-object references, prerequisites, scenario IDs, implementation links and validation results here. Add `scenarios.md` only at that point.

See [validation and pending items](PENDING.md).

Live UI coverage now includes desktop, tablet, and mobile overflow checks, explicit alt attributes on customer-home images, mobile customer/restaurant/rider dashboards, customer dashboard Tab traversal, visible-button accessible names, Escape dismissal for the address dialog and cart drawer, cart quantity live-region semantics, desktop/mobile theme switching with measured phone-input text contrast, forward/back navigation through all four role cards, and a safe authenticated fallback for an unknown URL. The login telephone label defect remains deliberately failing in `ResponsiveAccessibilityTest` and is documented in `PENDING.md`.
