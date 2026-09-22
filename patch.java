        // Assert we are in the settings tab
        String preReloadContent = customerPage.content();
        assertThat(preReloadContent).contains("Account Settings");

        System.out.println("[RELOAD TEST] URL BEFORE RELOAD: " + customerPage.url());

        // Full page reload
        System.out.println("[RELOAD TEST] Reloading page while in settings tab...");
        customerPage.reload();
        customerPage.waitForTimeout(3000);

        System.out.println("[RELOAD TEST] URL AFTER RELOAD: " + customerPage.url());
