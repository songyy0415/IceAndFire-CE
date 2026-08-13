#!/usr/bin/env python3
"""Parse Loom's layered mappings.tiny (namespaces: official, intermediary, named).

Builds Yarn(named) -> MojMap(official) maps for classes and members.
"""
import sys, os, json
from collections import defaultdict

TINY = r'C:\Users\songy\.gradle\caches\fabric-loom\1.21.1\loom.mappings.1_21_1.layered+hash.397750588-v2\mappings.tiny'

def parse_tiny(path):
    classes = {}   # named_fqn -> {'official': official_fqn, 'intermediary': inter_fqn, 'methods': {...}, 'fields': {...}}
    cur_class = None
    with open(path, encoding='utf-8', errors='replace') as f:
        for line in f:
            line = line.rstrip('\n')
            if not line:
                continue
            parts = line.split('\t')
            kind = parts[0]
            if kind == 'tiny':
                # header: tiny 2 0 ns0 ns1 ns2 ...
                namespaces = parts[3:]
                continue
            if kind == 'c':
                # c <ns0> <ns1> <ns2> ...
                official = parts[1]
                intermediary = parts[2] if len(parts) > 2 else ''
                named = parts[3] if len(parts) > 3 else ''
                cur_class = named
                classes[named] = {
                    'official': official,
                    'intermediary': intermediary,
                    'methods': {},
                    'fields': {},
                }
            elif kind == 'm':
                # m <owner-ns0> <owner-ns1> ... <desc> <ns0-name> <ns1-name> ...
                # owner is parts[1:ns] joined, then desc, then names
                if cur_class is None:
                    continue
                ns = len(namespaces)
                owner = '.'.join(parts[1:1+ns])
                desc = parts[1+ns]
                names = parts[2+ns:2+ns+ns]
                named_name = names[-1]
                official_name = names[0]
                classes[cur_class]['methods'][named_name + desc] = official_name + desc
            elif kind == 'f':
                if cur_class is None:
                    continue
                ns = len(namespaces)
                owner = '.'.join(parts[1:1+ns])
                desc = parts[1+ns]
                names = parts[2+ns:2+ns+ns]
                named_name = names[-1]
                official_name = names[0]
                classes[cur_class]['fields'][named_name + desc] = official_name + desc
    return classes

if __name__ == '__main__':
    cls = parse_tiny(TINY)
    print('Total classes in tiny:', len(cls))
    # Test lookups
    for y in ['net.minecraft.block.Block', 'net.minecraft.util.Identifier',
              'net.minecraft.text.Text', 'net.minecraft.client.MinecraftClient',
              'net.minecraft.client.gui.DrawContext', 'net.minecraft.client.render.RenderLayer',
              'net.minecraft.entity.mob.MobEntity', 'net.minecraft.util.math.Direction']:
        if y in cls:
            print(f'{y:45s} -> {cls[y]["official"]}')
        else:
            print(f'{y:45s} -> NOT FOUND')
    # member sample
    b = cls.get('net.minecraft.util.Identifier', {})
    print()
    print('Identifier methods:', len(b.get('methods', {})))
    for m in list(b.get('methods', {}).items())[:8]:
        print('   ', m)
    print('Identifier fields:', len(b.get('fields', {})))
