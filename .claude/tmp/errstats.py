import re, collections, io, sys

path = sys.argv[1]
files = collections.Counter()
for line in io.open(path, encoding="utf-8", errors="replace"):
    m = re.match(r"^D:\\.*?\\([^\\]+\.java):(\d+)", line)
    if m:
        files[m.group(1)] += 1
for f, c in files.most_common(30):
    print(c, f)
print("TOTAL unique files:", len(files))
print("TOTAL errors:", sum(files.values()))
