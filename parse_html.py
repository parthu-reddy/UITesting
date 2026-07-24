import re
with open('test-output.txt', 'r') as f:
    text = f.read()

# find HTML part
html = text[text.find('<!DOCTYPE html>'):]
with open('dump.html', 'w') as f:
    f.write(html)

print("Saved to dump.html")
