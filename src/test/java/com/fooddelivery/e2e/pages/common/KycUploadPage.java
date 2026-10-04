package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Paths;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Real private upload completion, bound to the rendered accessible input and response. */
public class KycUploadPage {
    private final Page page;
    private String lastLabel;
    public KycUploadPage(Page page) {this.page=page;}
    public void uploadDocument(String label,String path) {
        lastLabel=label;
        var entitlementChallenges = new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<Response> observer = response -> {
            if (java.net.URI.create(response.url()).getPath().matches("/api/v1/verification/documents/[^/]+/complete")
                    && response.request().method().equals("POST") && response.status() == 401
                    && "ENTITLEMENTS_CHANGED".equals(response.headerValue("X-Auth-Reason"))) entitlementChallenges.incrementAndGet();
        };
        page.onResponse(observer);
        try {
        var completed=page.waitForResponse(response -> java.net.URI.create(response.url()).getPath().matches("/api/v1/verification/documents/[^/]+/complete")
                && response.request().method().equals("POST") && response.status() == 200,
                () -> page.waitForFileChooser(() -> page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^(Upload|Replace) " + label + "$"))).click())
                        .setFiles(Paths.get(path)));
        org.assertj.core.api.Assertions.assertThat(completed.status()).as("Server must confirm the private upload").isEqualTo(200);
        assertThat(page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Replace "+label).setExact(true))).containsText(label+" uploaded");
        System.out.printf("[UPLOAD] %s confirmed 200 and Uploaded; %d observed entitlement challenges%n", label, entitlementChallenges.get());
        } finally { page.offResponse(observer); }
    }
    public void uploadFirstAvailable(String path) {
        String accessible=page.locator("input[type=file][aria-label]").first().getAttribute("aria-label");
        org.assertj.core.api.Assertions.assertThat(accessible).endsWith(" file");
        uploadDocument(accessible.substring(0,accessible.length()-5),path);
    }
    public boolean isUploadSuccess() {return lastLabel!=null && page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Replace "+lastLabel).setExact(true)).isVisible();}
}
