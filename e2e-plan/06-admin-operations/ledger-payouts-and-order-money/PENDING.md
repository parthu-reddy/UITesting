# Validation and pending work

## Ledger filters

AdminReadOnlyUiTest.ledgerFiltersCanBeCleared passed. Typed transaction and owner IDs can be cleared and Apply Filters remains available. No ledger mutation or reconciliation action.

Evidence: UITesting/target/surefire-reports (reports are overwritten by focused reruns); failure screenshots/HTML in target/screenshots. Broader coverage remains pending.

## Additional read-only money panels

AdminReadOnlyUiTest.moneyOperationsTabsRender passed all four tabs (ledger rejections, reconciliation runs, payment DLQ, wallet DLQ) with selected-state and panel-heading checks. payoutHistorySearchForm passed after correcting the expected queue heading to Pending Payouts Queue. No payouts, reprocessing or reconciliation actions were invoked. These checks cover navigation/forms, not correctness of returned monetary data.
