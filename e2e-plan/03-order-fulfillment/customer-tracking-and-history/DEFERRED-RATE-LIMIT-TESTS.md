# Deferred rate-limit tests

Dev has rate limiting disabled. Do not execute quota-exhaustion/throttling tests in this audit. Any later implementation must be opt-in and skipped by default until a rate-limited environment is explicitly available. Immediate handling of a routed response is a UI contract only and does not prove rate-limit enforcement.
