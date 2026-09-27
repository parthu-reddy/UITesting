# Disagreements with the not-defects report

Checked against the current source on 2026-09-27.

## NAV-02 is implemented and must not remain classified as not applicable

The README says Account Settings is only in-memory state and that a direct settings URL cannot
render it. That is no longer true. `FoodDeliveryAppUI/src/features/customer-orders/model/useCustomerRoute.ts`
derives the settings view from any `/customer/settings` path and maps these direct subpaths:

- `/customer/settings/profile`
- `/customer/settings/history`
- `/customer/settings/addresses`
- `/customer/settings/wallet`
- `/customer/settings/reviews`

The wildcard route in `CustomerDashboard.tsx` does not disprove deep-link support: it mounts the
customer shell, and `useCustomerRoute` interprets the current URL inside that shell. Consequently,
NAV-02 describes current product behavior and is a valid regression scenario.

The existing `CustomerRoutingUiTest` has been updated (not duplicated) to navigate directly to
`/customer/settings/profile`, assert Account Settings, reload, and assert the same URL and view.

## Other entries

I found no source-based reason to reject the other classifications. In particular,
`AdminLedgerView.tsx` still has no statement/detail panel: its transaction-id control only copies
the id, so LEDGER-ADV-11 remains a scenario for a feature the ledger does not implement.
