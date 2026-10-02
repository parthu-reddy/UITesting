# Telemetry batch publication — source audit follow-up

Status: missing publication guards confirmed in current source; targeted regression and deployed validation not yet performed. Do not call this an observed live exploit or completed fix.

DeliveryTelemetryController.processBatchTelemetry builds validPayloads for ingestion, but lines73–79 subsequently publish from the original telemetryBatch when the authenticated driver ID matches. That publication does not require the event to have survived the mock-location/coordinate checks and does not compare its supplied orderId with the driver's active assignment. LocationTrackingWebSocketHandler checks the active order before publishing; syncLocation derives it server-side. The three boundaries therefore differ.

Existing controller tests cover valid location, spoofed driver ID and mock-location ingestion rejection; targeted publication/assignment assertions must be reviewed and added in the owning chat-calls-and-live-tracking / role-access security scope. Require rejected/mock events to publish nothing, an unrelated supplied order ID to publish nothing, and a legitimate assigned event to publish only on the authoritative channel. Preserve all deployed records; do not exercise unrelated live customer channels as a shortcut.

Source: DeliveryExecutiveApplication/src/main/java/com/fooddelivery/delivery/controller/DeliveryTelemetryController.java:73, websocket/LocationTrackingWebSocketHandler.java, and controller/DeliveryTelemetryControllerTest.java. This follow-up arose while tracing feature09map telemetry; its successful WebSocket happy path cannot prove batch security.
