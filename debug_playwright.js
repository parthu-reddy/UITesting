const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();
  
  await page.goto('http://140.245.234.137/');
  
  // Login
  await page.fill('input[type="tel"]', '8000000015');
  await page.getByRole('button', { name: 'Get OTP' }).click();
  await page.getByRole('button', { name: 'Auto fill' }).click();
  await page.getByRole('button', { name: 'Verify and Secure Login' }).click();
  
  // Wait for dashboard
  await page.waitForTimeout(2000);
  
  // Deliver to Home
  await page.locator('button', { hasText: 'Deliver to' }).click();
  await page.locator('text=Home').first().click();
  
  // Wait for modal to close
  await page.locator('text=Select Delivery Location').waitFor({ state: 'hidden' });
  await page.waitForTimeout(1000);
  
  // Click Brand 1
  await page.locator('div.group', { hasText: 'Brand 1' }).first().click();
  
  // Click Add
  const addBtn = page.getByRole('button', { name: 'Add' }).first();
  await addBtn.click();
  console.log("Clicked Add");
  
  await page.waitForTimeout(1000);
  
  // Check if cart appears
  const cartText = await page.content();
  if (cartText.includes('View Cart')) {
    console.log("View Cart FOUND in HTML!");
  } else {
    console.log("View Cart NOT FOUND in HTML!");
  }
  
  await browser.close();
})();
