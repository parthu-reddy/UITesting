# Validation and pending work

## Local browser-routed coverage

`AdminLiveOpsFleetTest` now verifies successful read responses before asserting live-order and intervention UI state. It checks empty-state messages, strict pagination when multiple pages exist, available-driver counts only after selecting an order, zero-value refund blocking, rider marker details when a rider has a plotted location, and all Money Operations panels. The force-assignment check opens and cancels its confirmation and asserts that no assignment POST was sent.

`AdminManualInterventionsPage` selectors now map to the actual queue cards, page controls, detail heading, no-driver state, and confirmation dialog.

Deployed Dev validation: fresh headed and headless FLEET-01 runs both failed. The fleet panel itself measures 718 px high, but its `.maplibregl-map` child measures 0 px. The deployed bundle already contains the 500 px minimum-height change, so the failure is not a stale deployment or a Chromium-specific result. MapLibre adds `position: relative` to the same element after the Tailwind `absolute` class, which leaves its absolutely positioned canvas with no containing height. The map style, source metadata, and sample vector tile endpoints returned HTTP 200, so no backend defect is indicated.

The working-tree map layout and Fleet Map scope changes pass the UI typecheck, production build, and focused component tests. `AdminFleetSafetyRoutedUiTest` supplies two cities and proves the browser sends the selected `cityId` to rider, restaurant, and customer map layers. Deployment and a target rerun remain required before FLEET-01 can be closed.

## Production issues found in source

- Manual intervention now requires a meaningful reason and an order-specific confirmation for force assignment and cancellation. `AdminDispatchSafetyRoutedUiTest` proves cancelled confirmations make no request, accepted fixture actions remove the resolved intervention, a dispatch rejection becomes visible and re-enables the next attempt, and a direct cancellation rejection retains the intervention and typed reason without a false success.
- These browser fixtures do not prove delivery outbox persistence, Kafka delivery, customer intervention serialization, rider notification, or actual refund completion. Those checks require coordinated target services and an isolated order fixture.
- Live fleet marker assertions are explicitly skipped when the shared Dev fixture has no rider with coordinates; an empty marker list is not reported as a pass.

The remaining source-backed backend risk is documented separately as `P2-MANUAL-ASSIGNMENT-INFRASTRUCTURE-FAILURE-VISIBILITY-2026-09-29.md`; no new backend defect is inferred from fixture coverage.
