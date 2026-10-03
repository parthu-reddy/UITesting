# Two contracts for one endpoint: an ambiguous stub passes locally and fails in CI

Recorded 2026-10-02 (e2e-plan checkpoint21/23). Fixed by a later publish run with `priority`.

## What happened

Checkpoint21 added `getActiveOrdersForDriverWithConfirmedAssignments.groovy` beside the existing
`getActiveOrdersForDriver.groovy`, for the same `GET /api/v1/internal/orders/driver/{id}/active`:

- the original stub constrains only `page` and `size`;
- the new stub constrains `page`, `size` and `confirmedOrderIds`.

**WireMock ignores query parameters a stub does not mention**, so a request carrying `confirmedOrderIds`
matched *both* stubs. With equal priority WireMock picks by stub load order, which differs between
machines. `DeliveryExecutiveContractConsumerTest.testGetActiveOrdersForDriverWithConfirmedAssignments`
got the right body locally (5/5 passed) and the other body in CI, so Phase 2 contract testing failed for
DeliveryExecutiveApplication.

The local pass was taken as proof. It was luck: the test had never been seen to fail for the right
reason, and nobody asked whether one request could match two stubs.

## Fix

Spring Cloud Contract `priority` becomes the WireMock stub priority; **lower number wins**:

```groovy
// specific variant: requests with confirmedOrderIds
priority 1
// general variant: everything else (e.g. a rider holding no orders)
priority 2
```

The producer side is unaffected: each contract still generates its own producer test.

## Rule

When adding a contract for a method+URL that already has one, decide whether a single request can
match both. A variant distinguished only by an *extra* query parameter or header always overlaps, so
give the more specific contract the lower `priority` number. To prove it, run the consumer test with the
specific stub's response deliberately changed and watch it fail; or run with `-Dstubrunner` stubs
installed fresh in a clean `~/.m2` order.

Find contracts sharing a method and URL (run from the workspace root):

```bash
python3 -c "
import re,glob,collections
p=collections.defaultdict(list)
for f in glob.glob('*/src/test/resources/contracts/**/*.groovy',recursive=True):
    s=open(f).read();m=re.search(r\"method\s+'(\w+)'\",s);u=re.search(r\"urlPath\(value\(consumer\(regex\('([^']+)'\",s) or re.search(r\"url(?:Path)?\s*\(?\s*'([^']+)'\",s)
    if m and u:p[(f.split('/')[0],m.group(1),u.group(1))].append((f.split('/')[-1],'priority' in s))
[print(k[0],k[1],k[2][:70],v) for k,v in p.items() if len(v)>1]"
```

On 2026-10-02 it reports only this pair, both prioritised.
