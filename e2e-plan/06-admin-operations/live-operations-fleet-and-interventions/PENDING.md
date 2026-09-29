# Validation and pending work

## Added read-only E2E coverage

`AdminLiveOpsFleetTest` now verifies successful read responses before asserting live-order and intervention UI state. It checks empty-state messages, strict pagination when multiple pages exist, available-driver counts only after selecting an order, zero-value refund blocking, rider marker details when a rider has a plotted location, and all Money Operations panels. The force-assignment check opens and cancels its confirmation and asserts that no assignment POST was sent.

`AdminManualInterventionsPage` selectors now map to the actual queue cards, page controls, detail heading, no-driver state, and confirmation dialog.

Validation performed: `mvn -q -DskipTests test-compile` passed. Browser E2E has not run against deployed Dev because the Oracle tunnel URL does not resolve from this workstation.

## Production issues found in source

- `AdminManualInterventions.tsx` posts `Cancel & Refund (Normal)` directly. It does not ask for confirmation, and an empty cancellation reason is accepted by the UI. This can cancel a live order and trigger a refund with one click. The fix plan is to require a reason, show an order-specific confirmation, and add a request-intercepted E2E that cancels the dialog and proves no POST was sent.
- `AdminLiveOperations.tsx` sends driver assignment and partial/post-delivery refund requests directly from the selected-order panel without a confirmation dialog. Financial and dispatch mutations need an isolated fixture before their committed outcomes can be tested. Consider adding an order-specific confirmation before enabling these actions in shared operations.
- Manual cancellation, dispatch, live-order assignment and refund completion tests remain pending an isolated disposable fixture. No such action was sent by the new checks.
- Live fleet marker assertions are explicitly skipped when the shared Dev fixture has no rider with coordinates; an empty marker list is not reported as a pass.

These are frontend findings from the checked-in UI source. No backend defect is confirmed by them.
