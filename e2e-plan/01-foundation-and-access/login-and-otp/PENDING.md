# Pending work and observations

## OTP expiry — deferred slow test

Not implemented. With current UI-only access, real expiry requires requesting an OTP, waiting the backend's five-minute lifetime plus a small margin, then submitting the original code and checking visible rejection and continued logged-out state. Redis/database manipulation is unavailable and outside scope; no accelerated backend expiry test will be created. Browser clock changes cannot expire a server-side OTP.

Confirmation for later: include this as a separately tagged slow test? It does not block other UI tests. Old-code rejection after resend would test replacement, not natural expiry.

## Other pending cases

- Rate-limit exhaustion: defer to avoid blocking shared seeded accounts.
- Short phone/OTP input: agree expected validation; generated schemas currently lack length constraints. Do not invent acceptance criteria or create accounts from malformed test data.
- Profile-form validation remains unimplemented beyond authorized admin completion.

## Validation evidence

RoleNavigationUiTest passed all eight desktop/mobile cases. Earlier login assertions passed despite background HTTP 403 resource responses; affected endpoints and impact remain undiagnosed. Reports are under UITesting/target/surefire-reports and are overwritten by later runs.
