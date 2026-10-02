# Deferred duration tests — cart and pricing

Do not execute intentional waits during the current audit. Real 24-hour cart retention/expiry and quote-expiry transitions must be opt-in and excluded until the user’s final separate run. No current cart test waits for expiry. Local mocked-clock boundary checks may be implemented without waiting; they are distinct from real elapsed-time proof. Quote expiry is owned by checkout-and-payment and linked there when reviewed.
