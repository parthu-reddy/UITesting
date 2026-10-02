# Deferred rate-limit tests — checkout and payment

Dev rate limits are removed/relaxed at the user's request. Do not exhaust order/quote request quotas or assert production limits in the current run. Production-configured controller rate boundaries require explicit opt-in, should be skipped by default, and remain for the final separate run. No rate-limit test ran here.
