# Validation and pending work

## Restaurant earnings panel

Current deployed validation on 2026-09-23 fails because clicking Earnings returns to Live Kitchen; Net Earnings never becomes visible. This matches the restaurant dashboard route override recorded under restaurant acceptance/navigation. A previous run rendered Net Earnings, Pending Balance and Clawbacks, but that result is stale for the current deployment. Amount accuracy remains unverified.

Evidence: UITesting/target/surefire-reports (reports are overwritten by focused reruns); failure screenshots/HTML in target/screenshots. Broader coverage remains pending.
