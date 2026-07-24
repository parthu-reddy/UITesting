import re

file_path = "/Users/parthureddy/Documents/Food Delivery.nosync/UITesting/src/test/java/com/fooddelivery/e2e/HappyPathE2ETest.java"
with open(file_path, "r") as f:
    content = f.read()

# Fix 1: exact match for restaurant
content = content.replace(
    'customerPage.locator("div.group", new Page.LocatorOptions().setHasText("Brand 1 Outlet 1")).first().click();',
    'customerPage.locator("div.group").filter(new Locator.FilterOptions().setHasText(java.util.regex.Pattern.compile("^Brand 1 Outlet 1$"))).first().click();'
)

# Fix 2: exact match for Add button
content = content.replace(
    'customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).first().click();',
    'customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Add$"))).first().click();'
)

with open(file_path, "w") as f:
    f.write(content)

