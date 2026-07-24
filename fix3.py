import re

file_path = "/Users/parthureddy/Documents/Food Delivery.nosync/UITesting/src/test/java/com/fooddelivery/e2e/HappyPathE2ETest.java"
with open(file_path, "r") as f:
    content = f.read()

# Add step to click Cash on Delivery in the Payment Modal
content = content.replace(
    'customerPage.locator("button", new Page.LocatorOptions().setHasText("Place Cash-on-Delivery Order")).click();',
    'customerPage.locator("button", new Page.LocatorOptions().setHasText("Place Cash-on-Delivery Order")).click();\n            \n            // Wait for payment modal\n            customerPage.locator("text=Cash on Delivery").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));\n            customerPage.locator("button:has-text(\'Cash on Delivery\')").click();'
)

with open(file_path, "w") as f:
    f.write(content)

