# Deferred duration tests — refunds and payment recovery

User instruction: exclude scenarios that require waiting for natural expiry/backoff/retry/grace windows. Do not execute a five-minute stuck-refund aging test or longer provider/reconciliation timeout test in the fast audit. If implemented later, tag/skip or opt in explicitly; this document is not proof of execution. Immediate unit tests with pre-aged timestamps/fake clocks and ordinary bounded callback readiness are allowed. Money boundaries use the existing local committed fixture without a provider or timer.
