# Deferred rate-limit tests — refunds and payment recovery

Dev rate limiting is relaxed/removed. Do not exhaust the refund/support/admin endpoints or shared seeded actors. Document any later limiter/throttling scenario and skip it in normal execution; enable only on a separately approved suitable target. Routed HTTP 429 recovery can be UI fixture proof but is not live rate-enforcement proof.
