# refunds-and-payment-recovery

Status: source review and consolidation in progress; deployed refund-quote verification shares the existing happy flow. Actual approval/payment completion remains unverified on the fresh seed.

`RefundQuoteChecks` uses the delivered order owned by `HappyDeliveryFlowTest#completeOrderLifecycle`; it requests an itemized quote without submitting or approving a refund. Admin refund policy uses existing browser-routed tests. Backend quote, routing, amount and support override checks passed locally (54 focused checks, including the earlier quote checks).

Approval closes the support ticket and requests a refund. Verify the separate refund status and payment/ledger completion before calling money returned. Full-order requests and selected-item requests have different quote caps. Delivered/cancelled order status is retained; there is no Refunded order enum.

Distinct restaurant rejection and customer cancellation flows retain their own owned order. Reuse those orders for recovery assertions, not additional successful delivery lifecycles. See `scenarios.md` for the source-grounded contract and the priority audit checklist for verification boundaries.

Current priority checkpoint: ../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/14-postdeployment-financial-contracts.md. Deployed terminal UI evidence is separate from still-blocked refund/ledger completion; reuse owned manifests after deployment.
