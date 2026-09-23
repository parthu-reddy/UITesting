# Login and OTP

Status: first batch validated — **8 tests passed, 0 failures, 0 errors, 0 skipped** against the configured development deployment.

See [scenarios.md](scenarios.md). Customer, restaurant, rider and admin successful login passed; wrong-OTP rejection passed for all four. The authorized admin first-login profile flow now uses the existing profile page object with exact selectors and a checked backend save response before dashboard assertions.

Run: `mvn -f UITesting/pom.xml -Dtest=LoginSmokeTest -Dheadless=true -Dslow.mo=0 -Drecord.video=false -Ddefault.timeout=20000 test`

Evidence: `UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.LoginSmokeTest.xml` (overwritten on subsequent runs).

Implementation: LoginSmokeTest, LoginPage, CompleteProfileModalPage and TestConfig under `src/test/java/com/fooddelivery/e2e`. UI source inspected: `FoodDeliveryAppUI/src/features/identity/model/useOtpLogin.ts` and `src/shared/ui/CompleteProfileModal.tsx`.

Admin profile setup is explicitly enabled only in the admin positive login case; name/email can be overridden with `admin.profile.name` / `admin.profile.email`. Other roles retain the requirement for existing complete profiles. Failed saves stop the test instead of being swallowed. The admin test profile is retained for reuse.

Further input-validation, resend/expiry and profile validation batches remain planned; this result does not claim exhaustive authentication coverage.

`SessionUiTest` live-passed logout and post-logout reload for customer and rider using randomized seeded accounts. Restaurant reload persistence passed, but logout is blocked by the deployed restaurant settings navigation defect. Admin is excluded from this operational-role run per current project scope.

## Second batch validation

LoginValidationTest: **12 passed, 0 failures, 0 errors, 0 skipped** against the configured deployment. Covers required/normalized phone and OTP inputs, Back navigation, and successful resent-code login for every role. See AUTH-09–20 in scenarios.md.

Command: `mvn -f UITesting/pom.xml -Dtest=LoginValidationTest -Dheadless=true -Dslow.mo=0 -Drecord.video=false -Ddefault.timeout=20000 test`

Evidence: `UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.LoginValidationTest.xml`. Together with the previously passing eight smoke cases, 20 login cases are implemented. The original eight were not rerun in this batch to avoid unnecessarily consuming shared OTP limits.

Browser console reported four HTTP 403 resource responses during this run; the asserted resend verification responses and dashboards passed. These resource errors have not been attributed to a particular endpoint and are not proof of a clean application console.

Remaining scope: short-input validation expectations, expiry/rate-limit tests under controlled scheduling, profile-form validation, and the separate sessions-and-role-access phase. This folder remains in progress.

See [pending work, failures and confirmations](PENDING.md).
