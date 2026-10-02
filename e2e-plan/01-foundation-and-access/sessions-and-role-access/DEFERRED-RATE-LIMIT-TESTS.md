# Deferred rate-limit tests

User instruction 2026-10-01: ignore rate limits on the relaxed Dev deployment; tests requiring them must remain opt-in and excluded.

Session listing/logout/removal controllers do not implement a feature-specific authentication bucket in the inspected source. The OTP initiation/verification limit tests belong to login-and-otp/DEFERRED-RATE-LIMIT-TESTS.md and require auth.limits.enabled=true plus the auth-rate-limit tag. No duplicate session rate test is created or executed here. Any future gateway/session quota scenario must use a separate rate-enabled target, own data, explicit opt-in/tag and its documented actual configuration.
