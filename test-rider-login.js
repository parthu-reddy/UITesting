const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  page.on('console', msg => console.log('BROWSER CONSOLE:', msg.text()));
  page.on('pageerror', err => console.log('PAGE ERROR:', err.message));
  page.on('response', async res => {
    if (res.url().includes('/api/v1/delivery-executives/profile/')) {
      console.log('PROFILE API STATUS:', res.status());
      try {
        console.log('PROFILE API BODY:', await res.text());
      } catch (e) {}
    }
  });

  await page.goto('http://localhost:3000/login');
  
  // Find Rider role card and click
  const cards = await page.locator('.cursor-pointer').all();
  for (const card of cards) {
    const text = await card.textContent();
    if (text.includes('Delivery Executive')) {
      await card.click();
      break;
    }
  }

  // Enter phone number
  await page.getByPlaceholder("9876543210").fill("5000000001");
  await page.getByRole("button", { name: "Get OTP" }).click();
  await page.getByRole("button", { name: "Auto Fill" }).click();
  await page.getByRole("button", { name: "Verify & Secure Login" }).click();

  // Wait for dashboard
  await page.waitForTimeout(5000);
  
  // take screenshot
  await page.screenshot({ path: 'rider_dashboard_after_login.png' });

  await browser.close();
})();
