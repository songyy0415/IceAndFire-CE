#!/usr/bin/env python3
"""Render the Yarn->MojMap mapping table to Markdown.

Output: D:/IceAndFire-CE/docs/yarn-to-mojmap-mapping.md
"""
import os, json, re, glob

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = r'D:/IceAndFire-CE/docs/yarn-to-mojmap-mapping.md'
PROJECT_DIRS = [r'D:/IceAndFire-CE/common/src', r'D:/IceAndFire-CE/fabric/src']

def load(name):
    with open(os.path.join(HERE, name), encoding='utf-8') as f:
        return json.load(f)

def collect_project_classes():
    """All net.minecraft class FQNs referenced by the project."""
    refs = set()
    for d in PROJECT_DIRS:
        for p in glob.glob(d + '/**/*.java', recursive=True):
            with open(p, encoding='utf-8', errors='replace') as f:
                t = f.read()
            t = re.sub(r'//[^\n]*|/\*.*?\*/', ' ', t, flags=re.S)
            for m in re.finditer(r'\bnet\.minecraft\.[A-Za-z0-9_.]+\b', t):
                refs.add(m.group(0).rstrip('.'))
    return sorted(r for r in refs if r.split('.')[-1][0].isupper())

def main():
    cm = load('class_map.json')
    mm = load('member_map.json')
    yidx = load('yarn_index.json')

    project_classes = collect_project_classes()
    lines = []
    lines.append('# IceAndFire-CE：Yarn → MojMap 映射表')
    lines.append('')
    lines.append('- Minecraft 版本：1.21.1')
    lines.append('- 依据：提供的 Yarn 源码（`minecraft 1.21.1 yarn`）与 MojMap 源码（`minecraft1.21.1mojmap`，Loom common 分片）')
    lines.append('- 方法：以两套源码的内容指纹（字符串/方法签名/字段类型/枚举/引用类型）+ 声明顺序进行类级与成员级对齐')
    lines.append('- 说明：MojMap 源码为 common 分片，不含 client 端类；client 类映射待用户提供完整 MojMap 源码后补充。')
    lines.append('')

    # ---- Class mapping ----
    mapped = [c for c in project_classes if c in cm]
    client_pending = [c for c in project_classes if 'net.minecraft.client.' in c and c not in cm]
    other_unmapped = [c for c in project_classes if c not in cm and 'net.minecraft.client.' not in c]

    lines.append('## 一、类级映射（项目涉及的类）')
    lines.append('')
    lines.append(f'共涉及 {len(project_classes)} 个类：已映射 {len(mapped)}，client 待源码 {len(client_pending)}，其他未映射 {len(other_unmapped)}。')
    lines.append('')
    lines.append('| Yarn 类 | MojMap 类 |')
    lines.append('|---|---|')
    for c in mapped:
        lines.append(f'| {c} | {cm[c]} |')
    lines.append('')

    if client_pending:
        lines.append('### 待补充（client 端类，MojMap common 源码缺失）')
        lines.append('')
        lines.append('| Yarn 类 | 预期 MojMap 包 |')
        lines.append('|---|---|')
        for c in sorted(client_pending):
            lines.append(f'| {c} | net.minecraft.client.*（待源码确认） |')
        lines.append('')

    if other_unmapped:
        lines.append('### 未映射（common 端，需人工确认）')
        lines.append('')
        for c in sorted(other_unmapped):
            lines.append(f'- {c}')
        lines.append('')

    # ---- Member mapping ----
    lines.append('## 二、成员级映射（按类）')
    lines.append('')
    lines.append('状态说明：`exact` 精确签名；`name` 同名匹配；`value` 初始化值匹配；`type` 类型匹配；`order` 声明顺序匹配；`verified` 人工核对；`unmatched` 未对齐（需要人工确认或项目未使用）。')
    lines.append('')

    for cls in mapped:
        info = mm.get(cls)
        if not info:
            continue
        lines.append(f'### {cls} → {info["moj"]}')
        lines.append('')
        # group: methods, fields, enums, records, ctors
        for kind in ('method', 'field', 'enum', 'record', 'ctor'):
            entries = [m for m in info['members'] if m['kind'] == kind]
            if not entries:
                continue
            label = {'method': '方法', 'field': '字段', 'enum': '枚举常量', 'record': 'Record 组件', 'ctor': '构造器'}[kind]
            lines.append(f'#### {label}')
            lines.append('')
            lines.append('| Yarn | MojMap | 状态 |')
            lines.append('|---|---|---|')
            for m in entries:
                st = m['status']
                if st == 'unmatched':
                    lines.append(f'| ~~{m["yarn"]}~~ | *(未对齐)* | unmatched |')
                else:
                    lines.append(f'| {m["yarn"]} | {m["moj"]} | {st} |')
            lines.append('')
        lines.append('')

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines))
    print('wrote', OUT)

if __name__ == '__main__':
    main()
