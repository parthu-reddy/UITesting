import re

file_path = "/Users/parthureddy/Documents/Food Delivery.nosync/UITesting/src/test/java/com/fooddelivery/e2e/HappyPathE2ETest.java"
with open(file_path, "r") as f:
    content = f.read()

# Fix Home click
content = content.replace(
    'customerPage.locator("text=Home").click();',
    'customerPage.locator("button", new Page.LocatorOptions().setHasText("Home")).first().click();'
)

with open(file_path, "w") as f:
    f.write(content)

