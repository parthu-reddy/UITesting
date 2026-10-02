# Restaurant-status SSE exhausted the delivery JDBC pool — 2026-10-01

**Status:** source fix and local regression verified; backend/config deployment and retained assigned-order delivery verified. The full resumed E2E invocation failed at a separate restored customer-receipt UI defect; do not call that invocation passed. The earlier UI acceptance/403 fix is deployed and its initial subscription returned200 before any reload.

## Failure and evidence

During the stronger lifecycle run, `AT_RESTAURANT` returned500, followed by504 on the rider's active-order read. Read-only diagnostics found all five delivery database connections idle in PostgreSQL, while the service could not borrow one. Sanitized log counters showed five connection-leak warnings, five `DeliveryExecutiveController.streamRestaurantStatus` stack frames, zero returned-leak warnings and repeated pool timeouts. Container health still reported healthy, so that status alone did not prove database availability.

Evidence: [connection state/assignment](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/08-arrival-failure-db.json), [leak counters](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/08-arrival-failure-leak-categories.txt), [initial stream200](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/08-initial-stream-deployed-stream.json).

## Cause

The stream performs a JPA assignment read before returning an emitter with a ten-minute lifetime. Delivery left `spring.jpa.open-in-view` unset in both its packaged application config and Config Server config. Spring Boot3.3.0 defaults it to true. A request-bound EntityManager retained the JDBC connection after the authorization read while the response remained asynchronous. Repeated open streams consumed the five-connection pool even though PostgreSQL was not executing expensive queries. JPA Open-in-View is different from a transaction intentionally spanning a write; transactions must remain bounded to each operation.

## Implemented correction

Set `spring.jpa.open-in-view: false` in both [packaged delivery config](../../../../DeliveryExecutiveApplication/src/main/resources/application.yml) and [Config Server delivery config](../../../../Deployment/delivery-service.yml). Read/transaction work ends before streaming begins. Existing assignment checks, Redis subscriptions and completion/error/timeout cleanup remain intact. No pool-size increase or change to authorization was needed. Delivery entities were checked for lazy relationship dependencies; none were declared in this service's entity classes.

Apply the updated Config Server configuration and restart delivery-service: the interceptor configuration is created at startup. This also releases old process-held connections. Do not declare resolution from a file edit or container-health flag alone.

## Regression and remaining live proof

[RestaurantStatusStreamConnectionTest](../../../../DeliveryExecutiveApplication/src/test/java/com/fooddelivery/delivery/controller/RestaurantStatusStreamConnectionTest.java) uses real Hibernate/JPA/Hikari, the real stream controller, one local H2 connection and mocked Redis collaborators. The test loads both packaged-only and deployment configuration. Before the fix, both positive stream cases held the only JDBC connection; four assignment-denial cases passed. Afterward all six passed: an open stream held zero JDBC connections, independent reads/updates succeeded, another rider was denied and a released assignment was denied. Five existing assignment-authorization and six endpoint-authorization checks also passed:17focused checks total, no intentional duration/expiry waits.

This is resource-lifetime and authorization proof with H2/mocked Redis, not full deployed PostgreSQL/Redis proof. For any future mid-delivery failure, resume the manifest's existing order using its original customer/restaurant/rider contexts; require stream200, arrival/pickup/delivery200, delivered receipt/payout and authoritative idle riderOFFLINE. Keep the assigned rider ON_DELIVERY until completion. Do not create a replacement order or clean up the retained records to hide the failed attempt.

Reusable prevention: [JPA standards](../../../../CodingPracticesAcrossAllServices/05_DataLayer/jpa-and-flyway.md) and [realtime standards](../../../../CodingPracticesAcrossAllServices/06_ApiAndIntegration/websocket-and-realtime.md). Pool gauges and borrow timeouts must be monitored alongside PostgreSQL activity; idle database sessions can still be checked out by the application.

Standalone delivery builds exercise packaged configuration (three stream regression invocations). When the sibling Deployment repo is present, the test also loads its real delivery configuration (six invocations here). Supply `-Ddelivery.deployment.config=/absolute/path/delivery-service.yml` to require that variant; an explicitly missing file fails setup. A standalone run is not proof of Config Server overrides.

## Deployed verification update

Retained orderced55d8c-c5ad-451e-aaba-71ce9421db27 resumed: initial stream200, arrival/pickup/deliveryPOST200, exact rider history/payout/earnings assertions reached. Read-only postcondition confirms customer deliveryStatusDELIVERED, assignmentRELEASED/DELIVERED, riderOFFLINE,9customerorders and2addresses retained. Since the delivery deployment, sanitized counters show0connection leaks,0pool timeouts,0Flyway/bean/startup failures and1successful application startup. Config checks passed and report-only reconciliation found29services,0image drift. The resumed browser invocation had1error after123.526s at the restored customer receipt, separately diagnosed and fixed in the UI. [Deployment diagnostics](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/08-pool-deployed-service-diagnostics.json), [database postcondition](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/08-pool-deployed-postcondition.json).
