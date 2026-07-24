import re

file_path = "/Users/parthureddy/Documents/Food Delivery.nosync/UITesting/src/test/java/com/fooddelivery/e2e/HappyPathE2ETest.java"
with open(file_path, "r") as f:
    content = f.read()

# Fix 1: exact text for waiting for restaurant
content = content.replace(
    'customerPage.locator("text=Waiting for Restaurant...").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));',
    'customerPage.locator("text=Waiting for Restaurant to Accept").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));'
)

with open(file_path, "w") as f:
    f.write(content)

base_path = "/Users/parthureddy/Documents/Food Delivery.nosync/UITesting/src/test/java/com/fooddelivery/e2e/base/TestBase.java"
with open(base_path, "r") as f:
    base_content = f.read()

# Set headless to false
base_content = base_content.replace(
    'new BrowserType.LaunchOptions().setHeadless(true)',
    'new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500)'
)

with open(base_path, "w") as f:
    f.write(base_content)

