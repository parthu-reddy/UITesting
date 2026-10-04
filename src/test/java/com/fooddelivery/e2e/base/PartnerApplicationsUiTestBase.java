package com.fooddelivery.e2e.base;

import com.microsoft.playwright.BrowserContext;
import org.junit.jupiter.api.AfterEach;

import java.util.Arrays;

/**
 * O3 retains each applicant and closes browser contexts without invoking legacy state helpers.
 * Duty changes belong to the visible scenario itself; teardown never reads or changes backend state.
 */
public abstract class PartnerApplicationsUiTestBase extends TestBase {
    @Override
    @AfterEach
    public void tearDownContexts() {
        RuntimeException closeFailure = null;
        for (BrowserContext context : Arrays.asList(
                customerContext, restaurantContext, riderContext, adminContext)) {
            if (context == null) continue;
            try {
                context.close();
            } catch (RuntimeException failure) {
                if (closeFailure == null) closeFailure = failure;
                else closeFailure.addSuppressed(failure);
            }
        }
        if (closeFailure != null) throw closeFailure;
    }
}
