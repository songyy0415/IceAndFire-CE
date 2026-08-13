import re, io, collections, sys

path = sys.argv[1]
cats = collections.Counter()
files = collections.Counter()
for line in io.open(path, encoding="utf-8", errors="replace"):
    m = re.match(r"^D:\\.*?\\([^\\]+\.java):(\d+): error: (.*)", line)
    if not m:
        continue
    fn = m.group(1)
    if re.search(r"render|Render|Screen|screen|Particle|Hud|Gui|Model|Tabula|Layer|layer|texture|Texture", fn):
        cats["RENDER(P6)"] += 1
    elif re.search(r"[Ee]gg", fn):
        cats["EGG"] += 1
    else:
        cats["OTHER"] += 1
    files[fn] += 1
for c in cats:
    print(c, cats[c])
print("--- non-render non-egg files (top 50) ---")
for f, c in files.most_common(60):
    if not re.search(r"render|Render|Screen|screen|Particle|Hud|Gui|Model|Tabula|Layer|layer|texture|Texture", f):
        print(c, f)
