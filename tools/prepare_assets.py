# -*- coding: utf-8 -*-
"""把 E 盘素材整理成 APK 内置 assets：原图直拷 + 280px 缩略图 + library.json"""
import os, sys, json, shutil, io, datetime

SRC = r"E:\多宫格素材库\PoseLib"
OUT = r"C:\PoseLibDev\PoseLibApp\assets"
IMG_EXT = (".jpg", ".jpeg", ".png", ".webp")
THUMB_MAX = 280
THUMB_Q = 74

HOME_ORDER = [
    ("单人", None),
    ("双人", None),
    ("闺蜜", ("双人", "闺蜜双人")),
    ("情侣", ("双人", "情侣双人")),
    ("男生", None),
    ("亲子", ("儿童亲子",)),
    ("手部动作", None),
    ("镜头角度", None),
    ("身材适配", None),
    ("艾寿花甲", None),
    ("照片风格", None),
    ("多人", None),
    ("室内场景", None),
    ("户外场景", None),
]

RANDOM = ["单人", "户外场景", "室内场景", "照片风格", "镜头角度", "场景用途"]


def main():
    from PIL import Image
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    os.makedirs(os.path.join(OUT, "lib"))
    os.makedirs(os.path.join(OUT, "thumbs"))

    nodes = {}
    order = []  # 按目录顺序

    def walk(rel, depth, code):
        d = os.path.join(SRC, *rel.split("/")) if rel else SRC
        files = sorted([f for f in os.listdir(d)
                        if f.lower().endswith(IMG_EXT) and os.path.isfile(os.path.join(d, f))])
        subs = sorted([f for f in os.listdir(d) if os.path.isdir(os.path.join(d, f))])
        node = {"title": rel.split("/")[-1] if rel else "全部",
                "images": [], "children": [], "depth": depth, "count": 0}
        for i, f in enumerate(files):
            src = os.path.join(d, f)
            ext = os.path.splitext(f)[1].lower()
            if ext == ".jpeg":
                ext = ".jpg"
            base = "%s_%03d" % (code, i + 1)
            libp = "lib/" + base + ext
            thp = "thumbs/" + base + ".webp"
            shutil.copy2(src, os.path.join(OUT, libp.replace("/", os.sep)))
            try:
                im = Image.open(src)
                im = im.convert("RGB")
                im.thumbnail((THUMB_MAX, THUMB_MAX), Image.LANCZOS)
                im.save(os.path.join(OUT, thp.replace("/", os.sep)), "WEBP",
                        quality=THUMB_Q, method=6)
            except Exception as e:
                print("thumb fail", src, e)
                continue
            node["images"].append([libp, thp, base])
        for j, s in enumerate(subs):
            cid = (rel + "/" if rel else "") + s
            nodes[cid] = None
            sub = walk(cid, depth + 1, code + "s%02d" % (j + 1))
            nodes[cid] = sub
            node["children"].append(cid)
        direct = len(node["images"])
        total = direct + sum(nodes[c]["count"] for c in node["children"])
        node["count"] = total
        if node["images"]:
            node["cover"] = node["images"][0][1]
            node["coverFull"] = node["images"][0][0]
        else:
            for c in node["children"]:
                if nodes[c].get("coverFull"):
                    node["cover"] = nodes[c]["cover"]
                    node["coverFull"] = nodes[c]["coverFull"]
                    break
        return node

    roots = sorted([f for f in os.listdir(SRC) if os.path.isdir(os.path.join(SRC, f))])
    root_ids = []
    for k, r in enumerate(roots):
        code = "c%02d" % (k + 1)
        node = walk(r, 1, code)
        nodes[r] = node
        root_ids.append(r)

    # 首页顺序
    home = []
    for title, path in HOME_ORDER:
        nid = (path[0] if path else title)
        if nid not in nodes:
            print("missing node", nid)
            continue
        home.append({"node": nid, "title": title, "kind": "node"})

    total = sum(nodes[r]["count"] for r in root_ids)

    lib = {
        "generated": datetime.datetime.now().strftime("%Y-%m-%d %H:%M"),
        "root": root_ids,
        "nodes": nodes,
        "home": home,
        "random": [r for r in RANDOM if r in nodes],
        "total": total,
    }
    with open(os.path.join(OUT, "library.json"), "w", encoding="utf-8") as f:
        json.dump(lib, f, ensure_ascii=False, separators=(",", ":"))

    sz_lib = sum(os.path.getsize(os.path.join(dp, fn))
                 for dp, _, fns in os.walk(os.path.join(OUT, "lib")) for fn in fns)
    sz_th = sum(os.path.getsize(os.path.join(dp, fn))
                for dp, _, fns in os.walk(os.path.join(OUT, "thumbs")) for fn in fns)
    print("nodes", len(nodes), "total", total)
    print("lib MB", round(sz_lib / 1048576.0, 1), "thumbs MB", round(sz_th / 1048576.0, 1))
    print("home", [h["title"] for h in home])


main()
