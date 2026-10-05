# Deferred tests requiring intentional waits

User instruction 2026-10-01: no intentional duration scenarios during this audit. Ordinary DOM/network readiness waits remain allowed.

| Scenario | Current implementation | Later requirements |
|---|---|---|
| Natural JWT/server-session expiry and recovery | Not implemented/executed; deferred | Use a separate short-TTL configuration and test-owned session. Read exp/server policy, let real server lifetime elapse, assert protected API rejection and browser login recovery. Browser time changes do not prove server expiry. When implemented, require an opt-in flag and slow-session tag; exclude from normal runs. |
| Session survives a full 15-minute access-token lifetime through background renewal (review D-R1, 2026-10-05) | Not implemented/executed; deferred. Boundary proof is fake-clock unit tests `FoodDeliveryAppUI/src/lib/sessionKeepAlive.test.ts` (renew before exp, hidden-tab catch-up, other-tab token, network backoff, refused renewal, logout) | After deploy of the 15-minute `jwt.expiration`, keep one test-owned signed-in page idle beyond 15 minutes, then use a normal UI control; expect it to work with an observed `POST /api/v1/auth/session/refresh` 200 and no login screen. Opt-in slow-session tag only; never in normal runs. |

The four-role reload and immediate logout/device-revocation tests require no expiry wait and are in current fast coverage. OTP expiry belongs to the login folder's DEFERRED-WAIT-TESTS.md. Do not use a month-long token wait or alter the shared Dev policy during this audit.
