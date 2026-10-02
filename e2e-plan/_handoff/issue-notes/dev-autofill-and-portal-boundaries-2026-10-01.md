# Dev autofill and portal authorization — 2026-10-01

## Restricting every Dev phone to a seed allowlist blocked registration tests

Generated-number customer/rider/restaurant signup needs the same visible Dev Autofill Code path as seeded manual login. An all-role seed-only policy prevented that flow. [DevOtpAccessPolicy](../../../../IdentityService/src/main/java/com/fooddelivery/identity/service/DevOtpAccessPolicy.java) now permits valid ten-digit non-admin phones only under explicit `dev & !prod` and the enabled Dev OTP flag. Admin autofill remains restricted to provisioned test administrators. This is development convenience, not role provisioning or approval to enable a production bypass.

The selected portal, active account and assigned role are still checked during ordinary OTP verification. Logging a RESTAURANT account into Delivery Executive correctly returns an unprovisioned-portal error; changing that denial into automatic role granting would be an authorization defect. Generated signup pools must remain unseeded so registration is actually exercised. Partner approval uses the configured Dev mock providers; production provider behavior is separate proof.

Verification:18local authentication/policy/controller checks;39fast deployed login/OTP/profile/signup invocations, including three fresh-account flows. Remaining signed tuple replay, direct Identity exposure and concurrency checks stay open in the [session audit](../checkpoints/03-sessions-and-role-access.md).

## Preserving mocked authentication must not weaken the production trust boundary

An earlier proposed shared-filter change copied any pre-existing SecurityContext authentication into the verified-caller attribute to accommodate `@WithMockUser`. That is a source-review concern: a mock principal and a caller verified from signed gateway identity are different proofs. Do not declare arbitrary prior authentication verified merely to make a test pass. The current [SecurityContextFilter](../../../../CommonLibrary/common-web/src/main/java/com/fooddelivery/common/security/SecurityContextFilter.java) starts from a fresh context and publishes its request attribute only after identity verification. Test harnesses must establish the authenticated path through explicit test configuration/fixtures without changing the production rule. No exploitation is claimed by this note.

The rider stream additionally verifies an active order assignment; matching a driver path ID and DELIVERY role alone does not authorize every order. Fix an early client subscription by waiting for confirmed assignment, preserving those backend checks. The pool fix's wrong-rider/released-assignment regressions remain denied.

[Reusable security rules](../../../../CodingPracticesAcrossAllServices/07_Security/security-standards.md).
