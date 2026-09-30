# ledger-payouts-and-order-money

Status: the earlier deployed read-only checks ran 13 tests with 0 failures, 0 errors, and 1 fixture-dependent skip. Local browser-routed payout and Money Operations lifecycle coverage now compiles; deployment and controlled target validation remain required.

Scope: Ledger, payouts and order-level money operations.

Current coverage: ledger filters, validation, data rendering, pagination, payout history, routed order money, pending balance display, Money Operations resolution/retry success and failure paths, and the payout state machine including explicit unverified-bank override and ambiguous-create retry recovery through browser-local fixtures. Real financial mutations remain target-fixture-gated.

See [validation and pending work](PENDING.md).
