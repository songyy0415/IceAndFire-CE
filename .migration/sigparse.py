#!/usr/bin/env python3
"""Extract method signatures (with types) and field types from cleaned source.

The cleaned text has comments/strings stripped and whitespace collapsed to one
space, so signatures can be matched with a bounded regex.
"""
import re
import build_index as bi

VIS = r'(?:public|protected|private)'
MOD = r'(?:static|final|abstract|synchronized|native|default|transient|volatile|strictfp)\s+'
# method sig: VIS MOD* RETTYPE NAME ( params ) {|;
SIG_RE = re.compile(
    r'\b' + VIS + r'\s+(?:' + MOD + r')*'
    r'([\w<>\[\],.\s]+?)\s+'          # return type (group1)
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'   # name (group2)
    r'\(([^)]*)\)'                     # params (group3)
)
# field: VIS MOD* TYPE NAME =|;  (exclude ones followed by '(' -> method)
FIELD_RE = re.compile(
    r'\b' + VIS + r'\s+(?:' + MOD + r')*'
    r'([\w<>\[\],.\s]+?)\s+'
    r'([A-Za-z_$][A-Za-z0-9_$]*)\s*'
    r'(?:=|;)'
)

def split_top(s):
    """Split by top-level commas."""
    if not s.strip():
        return []
    depth = 0; parts = []; cur = []
    for ch in s:
        if ch in '<([': depth += 1
        elif ch in '>)]': depth -= 1
        if ch == ',' and depth == 0:
            parts.append(''.join(cur).strip()); cur = []
        else:
            cur.append(ch)
    if cur: parts.append(''.join(cur).strip())
    return [p for p in parts if p]

def parse_param(ptok):
    """'Type name' or 'Type' or 'Type... name' -> (type_str, name)."""
    ptok = ptok.strip()
    if not ptok:
        return ('', '')
    if ' ' in ptok:
        *type_parts, name = ptok.rsplit(' ', 1)
        return (' '.join(type_parts).strip(), name)
    return (ptok, '')

def type_simples(typestr):
    """Extract outermost type simple names from a type string like
    'java.util.Map<net.minecraft.util.Identifier, X>' or 'Identifier[]'."""
    simples = set()
    # strip generics at each level: walk tokens
    depth = 0
    cur = []
    for ch in typestr:
        if ch == '<':
            if cur: simples.add(''.join(cur).strip()); cur = []
            depth += 1
        elif ch == '>':
            depth = max(0, depth-1)
        elif ch in '[].,':
            if cur and depth == 0:
                simples.add(''.join(cur).strip()); cur = []
            # '.' is part of package (don't split) -> handle: only split on '.' if depth>0 means generic param path
            if ch == '.' and depth == 0 and cur:
                # package separator; keep accumulating but track last segment
                cur.append(ch)
            elif ch == '?':
                pass
        else:
            cur.append(ch)
    if cur:
        simples.add(''.join(cur).strip())
    return {s for s in simples if s and not s.islower() and not s.isdigit()}

def extract(file_path):
    with open(file_path, encoding='utf-8', errors='replace') as f:
        text = f.read()
    strings, liny, cleaned = bi.preprocess(text)
    sigs = []
    fields = []
    for m in SIG_RE.finditer(cleaned):
        ret = m.group(1).strip()
        name = m.group(2)
        params_raw = split_top(m.group(3))
        ptypes = []
        for p in params_raw:
            t, pn = parse_param(p)
            ptypes.append(t)
        if name[:1].isupper() and not ret:
            pass
        static = bool(re.search(r'\bstatic\b', cleaned[:m.start()]))
        sigs.append((ret, tuple(ptypes), static))
    for m in FIELD_RE.finditer(cleaned):
        t = m.group(1).strip()
        fields.append(t)
    return {'strings': strings, 'sigs': sigs, 'fields': fields}
