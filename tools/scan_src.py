# -*- coding: utf-8 -*-
"""扫描素材库：完整目录树、图片数、超大像素图（>20MP）、各图尺寸分布"""
import os, json, sys
from PIL import Image

SRC = r"D:\庞娜\多宫格素材库\PoseLib"
OUT = r"C:\PoseLibDev\tools\scan_src.json"
EXT = (".jpg", ".jpeg", ".png", ".webp")

tree = {}
big = []
allinfo = []


def walk(rel):
    d = os.path.join(SRC, *rel.split("/")) if rel else SRC
    files = sorted([f for f in os.listdir(d) if f.lower().endswith(EXT) and os.path.isfile(os.path.join(d, f))])
    subs = sorted([f for f in os.listdir(d) if os.path.isdir(os.path.join(d, f))])
    node = {"title": rel.split("/")[-1] if rel else "全部", "direct": len(files), "children": [], "count": 0}
    for f in files:
        p = os.path.join(d, f)
        try:
            with Image.open(p) as im:
                w, h = im.size
            mp = w * h / 1e6
            sz = os.path.getsize(p)
            allinfo.append({"rel": (rel + "/" if rel else "") + f, "w": w, "h": h, "mp": round(mp, 2), "kb": round(sz / 1024)})
            if mp > 20:
                big.append({"rel": (rel + "/" if rel else "") + f, "w": w, "h": h, "mp": round(mp, 2), "kb": round(sz / 1024)})
        except Exception as e:
            allinfo.append({"rel": (rel + "/" if rel else "") + f, "err": str(e)})
    for s in subs:
        cid = (rel + "/" if rel else "") + s
        node["children"].append(cid)
        tree[cid] = walk(cid)
        node["count"] += tree[cid]["count"]
    node["count"] += node["direct"]
    return node


tree[""] = {"title": "全部", "direct": 0, "children": [], "count": 0}
for r in sorted([f for f in os.listdir(SRC) if os.path.isdir(os.path.join(SRC, f))]):
    tree[r] = walk(r)
    tree[""]["children"].append(r)
    tree[""]["count"] += tree[r]["count"]

mp_sorted = sorted(allinfo, key=lambda a: -a.get("mp", 0))
with open(OUT, "w", encoding="utf-8") as f:
    json.dump({"tree": tree, "big": big, "all": allinfo}, f, ensure_ascii=False, indent=1)

print("total", tree[""]["count"], "big>20MP", len(big))
print("max mp", mp_sorted[0])
print("over16MP", len([a for a in allinfo if a.get("mp", 0) > 16]))
print("under4MP", len([a for a in allinfo if a.get("mp", 0) <= 4]))
print("dupe names", len(allinfo) - len(set(a["rel"] for a in allinfo)))
print("avg kb", round(sum(a.get("kb", 0) for a in allinfo) / len(allinfo)))
