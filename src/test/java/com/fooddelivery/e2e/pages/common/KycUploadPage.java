package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;

/**
 * Page Object for the KYC document and image upload fields.
 * Maps to: {@code DocumentUploadField.tsx}, {@code ImageUploadField.tsx}
 * <p>
 * Used in rider onboarding and restaurant registration flows.
 * </p>
 */
public class KycUploadPage {

    private final Page page;

    public KycUploadPage(Page page) {
        this.page = page;
    }

    /**
     * Uploads a file using the file chooser dialog.
     * @param fieldLabel label near the upload field
     * @param filePath absolute path to the file to upload
     */
    public void uploadDocument(String fieldLabel, String filePath) {
        page.locator("text=" + fieldLabel).locator("xpath=..").locator("input[type='file']")
                .setInputFiles(java.nio.file.Paths.get(filePath));
        page.waitForTimeout(1000);
    }

    /**
     * Generic file upload via the first visible input[type=file].
     */
    public void uploadFirstAvailable(String filePath) {
        page.locator("input[type='file']").first()
                .setInputFiles(java.nio.file.Paths.get(filePath));
        page.waitForTimeout(1000);
    }

    public boolean isUploadSuccess() {
        return page.locator("text=uploaded, text=Uploaded, svg.lucide-check").first().isVisible();
    }
}
