#!/usr/bin/env python3
"""Fast single-pass Java source scanner.

Extracts:
  - string literals (contents, unescaped)
  - method declarations: (name, arity, static, is_ctor)
  - field declarations: (name, type_token_seq, static, final)
  - enum constant names + ordinal
  - record component names (in order)
  - superclass / interface simple names
  - referenced type simple names (set)
Handles comments, strings, generics nesting.
"""
import re

def scan(text):
    i = 0
    n = len(text)
    strings = []
    methods = []   # (name, arity, static)
    ctors = []     # arity
    fields = []    # (name, type_str, static, final)
    enum_consts = []
    records = []
    record_in_progress = False
    record_components = []
    tokens = []
    # Tokenizer that strips comments but keeps structure, and captures strings
    tk = []
    cur_tok = []
    def flush():
        if cur_tok:
            tk.append(''.join(cur_tok)); cur_tok.clear()
    while i < n:
        c = text[i]
        # line comment
        if c == '/' and i+1 < n and text[i+1] == '/':
            while i < n and text[i] != '\n': i += 1
            continue
        # block comment
        if c == '/' and i+1 < n and text[i+1] == '*':
            j = text.find('*/', i+2)
            i = j + 2 if j != -1 else n
            continue
        # string literal
        if c == '"':
            j = i+1
            buf = []
            while j < n:
                if text[j] == '\\' and j+1 < n:
                    buf.append(text[j+1]); j += 2
                elif text[j] == '"':
                    break
                else:
                    buf.append(text[j]); j += 1
            strings.append(''.join(buf))
            i = j+1
            continue
        # char literal
        if c == "'":
            j = i+1
            while j < n and text[j] != "'":
                j += 1
            i = j+1
            continue
        # word / symbol
        if c.isalnum() or c in '_$':
            cur_tok.append(c)
        else:
            flush()
            if c in '{}();,=<>[] \t\r\n:.':
                tk.append(c)
        i += 1
    flush()

    # Now parse tokens for declarations
    idx = 0
    N = len(tk)
    # Build a more structured scan
    i = 0
    # helper: read a type+name sequence starting at visibility keyword
    def parse_decl(start):
        # tokens[start] is visibility; returns (kind, name, arity, static, final, type_str, end_idx, is_ctor)
        j = start + 1
        static = False
        final = False
        # consume modifiers
        while j < N and tk[j] in ('static', 'final', 'abstract', 'synchronized', 'native', 'default', 'transient', 'volatile', 'strictfp'):
            if tk[j] == 'static': static = True
            if tk[j] == 'final': final = True
            j += 1
        # read type tokens until name: we need to find the token before '(' for methods, or '=' / ';' for fields.
        # Collect sequence of tokens that look like type parts.
        type_toks = []
        k = j
        # Look ahead: find the name token followed by '(' or '=' / ';' or ',' or '(' for methods
        # We'll scan forward token by token, treating the last identifier before '(' as method name,
        # and for fields, identifier before = / ; / ,
        # Type tokens: identifiers, '.', '<', '>', ',', '[', ']', '?', '&'
        depth = 0
        last_id = None
        last_id_idx = -1
        saw_angle = 0
        while k < N:
            t = tk[k]
            if t == '<': depth += 1
            elif t == '>': depth -= 1
            if t == '(' and depth == 0:
                # method: name = last_id
                # count params (tokens until matching ')')
                params, k2 = read_params(k)
                if last_id is None:
                    # anonymous ctor with '<' generic? shouldn't happen
                    return None
                is_ctor = last_id[:1].isupper()
                return ('ctor' if is_ctor else 'method', last_id, params, static, final, k2+1)
            if t in ('=', ';', ',') and depth == 0:
                if last_id is None:
                    return None
                type_str = tokens_to_type(type_toks)
                return ('field', last_id, type_str, static, final, k)
            # accumulate
            if last_id is not None:
                # we're past the name; for fields the name token already found; for methods name found then '(' expected
                if t in ('(',) and depth == 0:
                    params, k2 = read_params(k)
                    is_ctor = last_id[:1].isupper()
                    return ('ctor' if is_ctor else 'method', last_id, params, static, final, k2+1)
                # could be array dims or generic bound; keep going but if next is '(' handle
                if t in ('=', ';', ','):
                    type_str = tokens_to_type(type_toks)
                    return ('field', last_id, type_str, static, final, k)
            # token is part of type or the name
            if t == '.':
                type_toks.append(t)
            elif t in ('<','>',',','[',']','?','&'):
                type_toks.append(t)
            elif is_ident(t):
                if is_ident_keyword(t):
                    type_toks.append(t)
                else:
                    # could be type (generic param) or name
                    if last_id is None and not (depth > 0):
                        last_id = t; last_id_idx = k
                    else:
                        type_toks.append(t)
            else:
                type_toks.append(t)
            k += 1
        return None

    def tokens_to_type(toks):
        return ''.join(toks)

    def read_params(open_idx):
        # tokens[open_idx] == '('
        k = open_idx + 1
        depth = 0
        count = 0
        has_param = False
        while k < N:
            t = tk[k]
            if t == '(': depth += 1
            elif t == ')':
                if depth == 0:
                    return (count, k)
                depth -= 1
            elif t == ',' and depth == 0:
                count += 1; has_param = True
            elif t != ' ' and has_param is False and depth == 0:
                if t not in (' ', '\t', '\n', '\r'):
                    if not (is_ident(t) and t in ('final',)):
                        has_param = True
            k += 1
        return (count, open_idx)

    def is_ident(t):
        return t and (t[0].isalpha() or t[0] in '_$')

    def is_ident_keyword(t):
        return t in ('var',)

    # Top-level loop
    i = 0
    in_enum = False
    enum_body_depth = 0
    in_record = False
    record_depth = 0
    in_interface = False
    seen_class = False
    while i < N:
        t = tk[i]
        if t in ('class', 'interface', 'enum', 'record'):
            # look for class/enum/record/interface declaration
            # check it's a declaration (has name next, then extends/implements or {)
            j = i+1
            while j < N and tk[j] in ('<', '>', ',', '[', ']', '?', 'extends', 'implements'):
                j += 1
            if j < N and is_ident(tk[j]):
                name = tk[j]
                if t == 'enum': in_enum = True; enum_body_depth = 0; seen_class = True
                elif t == 'record':
                    in_record = True; record_depth = 0; record_components = []; seen_class = True
                else:
                    seen_class = True
                # record components: find '(' after name
                if t == 'record':
                    # scan for components until '{'
                    k = j+1
                    if k < N and tk[k] == '(':
                        k += 1
                        depth = 0
                        comps = []
                        cur = []
                        while k < N:
                            c = tk[k]
                            if c == '(': depth += 1
                            elif c == ')':
                                if depth == 0: break
                                depth -= 1
                            if c == ',' and depth == 0:
                                comps.append(cur); cur = []
                            elif c != ' ':
                                cur.append(c)
                            k += 1
                        if cur: comps.append(cur)
                        for comp in comps:
                            # find last identifier = component name
                            nm = None
                            for tok in comp:
                                if is_ident(tok) and tok not in ('final',):
                                    nm = tok
                            if nm: records.append(nm)
                i = j
                continue
        if t == '{':
            if in_enum:
                enum_body_depth += 1
            if in_record:
                record_depth += 1
        elif t == '}':
            if in_enum:
                enum_body_depth -= 1
                if enum_body_depth <= 0:
                    in_enum = False; enum_body_depth = 0
            if in_record:
                record_depth -= 1
                if record_depth <= 0:
                    in_record = False; record_depth = 0
        elif t in ('public', 'protected', 'private'):
            d = parse_decl(i)
            if d:
                kind, name, info, static, final, end = d
                if kind == 'method':
                    methods.append((name, info, static))
                elif kind == 'ctor':
                    ctors.append(info)
                else:
                    fields.append((name, info, static, final))
                i = end
                continue
        i += 1

    return {
        'strings': strings,
        'methods': methods,
        'ctors': ctors,
        'fields': fields,
        'records': records,
        'in_enum': in_enum,
    }
