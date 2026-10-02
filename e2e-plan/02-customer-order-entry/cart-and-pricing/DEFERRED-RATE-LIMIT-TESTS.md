# Deferred rate-limit tests — cart and pricing

Dev rate limits are removed/relaxed, so do not assert production limits or execute quota exhaustion now. Repeated order quote/availability limit behavior belongs to checkout-and-payment, with explicit opt-in before the final separate run. No rate-limit test ran in this feature.
