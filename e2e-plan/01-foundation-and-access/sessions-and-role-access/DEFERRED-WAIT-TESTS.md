# Deferred tests requiring intentional waits

User instruction 2026-10-01: no intentional duration scenarios during this audit. Ordinary DOM/network readiness waits remain allowed.

| Scenario | Current implementation | Later requirements |
|---|---|---|
| Natural JWT/server-session expiry and recovery | Not implemented/executed; deferred | Use a separate short-TTL configuration and test-owned session. Read exp/server policy, let real server lifetime elapse, assert protected API rejection and browser login recovery. Browser time changes do not prove server expiry. When implemented, require an opt-in flag and slow-session tag; exclude from normal runs. |

The four-role reload and immediate logout/device-revocation tests require no expiry wait and are in current fast coverage. OTP expiry belongs to the login folder's DEFERRED-WAIT-TESTS.md. Do not use a month-long token wait or alter the shared Dev policy during this audit.
