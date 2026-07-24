import re

with open('dump.html', 'r') as f:
    html = f.read()

# very basic tag stripping
text = re.sub(r'<style.*?>.*?</style>', '', html, flags=re.DOTALL)
text = re.sub(r'<script.*?>.*?</script>', '', text, flags=re.DOTALL)
text = re.sub(r'<.*?>', '\n', text)
text = re.sub(r'\n+', '\n', text)
print(text.strip())
