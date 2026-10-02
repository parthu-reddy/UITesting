# Deferred duration tests

User requested all deliberate-duration tests remain excluded until final separate execution. Do not run wall-clock ETA countdown, stale telemetry, long reconnect/heartbeat exhaustion, restaurant acceptance timeout or delayed order expiry in this feature. Record/implement such cases with an opt-in slow tag and skip by default. Immediate fake-clock unit checks and ordinary response/UI readiness remain eligible. Naturally cancelled retained history is valid data; do not create a ten-minute wait fixture.
