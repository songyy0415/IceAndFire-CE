import re, io, collections, sys

path = sys.argv[1]
files = collections.Counter()
for line in io.open(path, encoding="utf-8", errors="replace"):
    m = re.match(r"^D:\\.*?\\([^\\]+\.java):(\d+): error: (.*)", line)
    if not m:
        continue
    fn, msg = m.group(1), m.group(3)
    if re.search(r"ValueOutput|ValueInput|CompoundTag|saveAdditional|loadAdditional|addAdditionalSaveData|readAdditionalSaveData", msg):
        files[fn] += 1
for f, c in files.most_common(40):
    print(c, f)
