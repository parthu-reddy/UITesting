# Suite execution limits

Use focused UI test batches while sharing seeded phone numbers. Running every login-heavy class together may exceed application OTP request limits; a Codex usage reset does not reset those limits. Session reuse or paced scheduling needs a separate design before declaring the full repository suite regression-ready. Do not bypass limits with direct backend access.

JUnit currently overwrites class XML reports on focused method reruns. Record previous unresolved failures in their owning PENDING.md; do not infer a full-class pass from a method-only report. Existing legacy suites have not all been audited or run.

New batches used only browser interaction and rendered UI checks. Screenshot/HTML failures are captured before context teardown. Browser console 4xx responses still occur during passing navigation tests; data/API correctness is not guaranteed by rendering checks. Avoid concurrent runs that log into the same account or modify the same shared state.
