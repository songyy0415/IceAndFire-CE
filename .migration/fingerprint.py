#!/usr/bin/env python3
"""Extract name-independent fingerprints from Java source files.

Fingerprint fields:
  - strings: frozenset of all string literal contents (strong, name-independent)
  - str_count: number of string literals
  - arities: sorted tuple of (arity, static) for all methods (name-independent)
  - field_count: number of declared fields
  - ctor_arities: sorted tuple of constructor parameter counts
  - has_extends: superclass simple name (renamed -> weak, use only as tiebreak)
  - inner: whether it's an inner class (name contains $)
"""
import os
import re
import sys

STR_RE = re.compile(r'"((?:[^"\\]|\\.)*)"')
# Match class/enum/interface/record declaration
CLASS_RE = re.compile(r'\b(?:public\s+|protected\s+|private\s+|static\s+|abstract\s+|final\s+)*(?:class|interface|enum|record)\s+([A-Za-z_$][A-Za-z0-9_$]*)')
# Match method declarations (rough): visibility + (static|final|abstract|synchronized|native|default)* + return type + name + ( params )
METHOD_RE = re.compile(r'\b(?:public|protected|private)\s+(?:static\s+|final\s+|abstract\s+|synchronized\s+|native\s+|default\s+)*[\w<>\[\],\s.]+?([A-Za-z_$][A-Za-z0-9_$]*)\s*\(([^)]*)\)\s*(?:throws[\s\w.,]+)?\{', re.S)
# Simple count of '{' '}' isn't reliable; use heuristic for field declarations:
#   visibility + static/final + type + name = value ; or '=' not at end of line... too messy.
# Instead count lines ending with ';' that look like field decls.
FIELD_RE = re.compile(r'\b(?:public|protected|private)\s+(?:static\s+|final\s+|transient\s+|volatile\s+)*[\w<>\[\],.\s]+?([A-Za-z_$][A-Za-z0-9_$]*)\s*(?:=|;)')

def count_params(param_str: str) -> int:
    s = param_str.strip()
    if not s:
        return 0
    # split top-level commas
    depth = 0
    parts = []
    cur = []
    for ch in s:
        if ch in '<([':
            depth += 1
        elif ch in '>)]':
            depth -= 1
        if ch == ',' and depth == 0:
            parts.append(''.join(cur).strip())
            cur = []
        else:
            cur.append(ch)
    if cur:
        parts.append(''.join(cur).strip())
    return len([p for p in parts if p and p != 'var'])

def extract(path):
    with open(path, encoding='utf-8', errors='replace') as f:
        text = f.read()
    strings = STR_RE.findall(text)
    # exclude obvious junk: translation keys / registry strings are fine; keep all
    strset = frozenset(strings)
    methods = []
    ctor_arities = []
    for m in METHOD_RE.finditer(text):
        name = m.group(1)
        n = count_params(m.group(2))
        static = 'static' in m.group(0)[:m.start(1)-m.start(0)]
        if name == '<init>':
            continue
        # ignore if this is a method call or lambda? The regex requires visibility keyword so
        # it should be declarations. But returns like `public Foo bar(` match; also constructors
        # written as `public ClassName(` match name==ClassName -> treat as ctor.
        if name[:1].isupper() and name in text.split('{')[0]:
            ctor_arities.append(n)
            continue
        methods.append((n, static))
    field_count = len(FIELD_RE.findall(text))
    base = os.path.basename(path)
    inner = '$' in base
    return {
        'path': path,
        'class': base[:-5],
        'strings': strset,
        'str_count': len(strings),
        'arities': tuple(sorted(methods)),
        'field_count': field_count,
        'ctor_arities': tuple(sorted(ctor_arities)),
        'inner': inner,
    }

if __name__ == '__main__':
    print(extract(sys.argv[1]))
