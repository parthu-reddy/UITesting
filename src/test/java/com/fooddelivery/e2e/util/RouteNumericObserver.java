package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Observes the route responses the UI itself requests ({@code GET /api/v1/logistics/route}, made
 * when an order map draws its route) and proves M1: the provider's numeric totals reach the UI.
 * Bodies stay in memory; only status, the numeric fields and the readable labels are persisted.
 */
public final class RouteNumericObserver {
    private final List<Map<String, Object>> raw = new ArrayList<>();

    public RouteNumericObserver attach(Page page, String actor) {
        page.onResponse(response -> record(response, actor));
        return this;
    }

    private void record(Response response, String actor) {
        if (!"/api/v1/logistics/route".equals(UrlPaths.path(response.url()))) return;
        String body;
        try { body = response.text(); } catch (PlaywrightException unavailable) { body = null; }
        raw.add(new java.util.HashMap<>(Map.of("actor", actor, "status", response.status(),
                "body", body == null ? "" : body)));
    }

    /** The persisted, body-free view: one entry per observed route response. */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> summary(Page parser) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> entry : raw) {
            Map<String, Object> fields = (Map<String, Object>) parser.evaluate("text => {"
                    + "let d = null; try { d = JSON.parse(text); } catch (e) { return { parsed: false }; }"
                    + "const kind = v => v === null || v === undefined ? 'absent' : Number.isInteger(v) ? 'integer' : typeof v;"
                    + "return { parsed: true, durationSeconds: d.durationSeconds ?? null, distanceMeters: d.distanceMeters ?? null,"
                    + "  durationKind: kind(d.durationSeconds), distanceKind: kind(d.distanceMeters),"
                    + "  duration: d.duration ?? null, distance: d.distance ?? null, hasPolyline: typeof d.polyline === 'string' && d.polyline.length > 0 }; }",
                    entry.get("body"));
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("actor", entry.get("actor"));
            row.put("status", entry.get("status"));
            row.putAll(fields);
            out.add(row);
        }
        return out;
    }

    /** M1: at least one UI route response carries the provider's positive whole-number totals. */
    public List<Map<String, Object>> assertNumeric(Page parser, Path evidence) {
        List<Map<String, Object>> summary = summary(parser);
        try {
            Files.createDirectories(evidence.getParent());
            Files.writeString(evidence, (String) parser.evaluate("d => JSON.stringify(d, null, 2)", summary));
        } catch (IOException failure) { throw new AssertionError("Cannot retain route evidence", failure); }
        org.assertj.core.api.Assertions.assertThat(summary).as("the UI requested at least one route").isNotEmpty();
        boolean numeric = summary.stream().anyMatch(r -> Integer.valueOf(200).equals(r.get("status"))
                && "integer".equals(r.get("durationKind")) && "integer".equals(r.get("distanceKind"))
                && ((Number) r.get("durationSeconds")).intValue() > 0 && ((Number) r.get("distanceMeters")).intValue() > 0);
        org.assertj.core.api.Assertions.assertThat(numeric)
                .as("a 200 route response with positive integer durationSeconds/distanceMeters: %s", summary)
                .isTrue();
        return summary;
    }
}
