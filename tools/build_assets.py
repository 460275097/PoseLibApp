# -*- coding: utf-8 -*-
"""拍照姿势集 · 资源构建
- 从 D:\庞娜\多宫格素材库\PoseLib 原图直拷到 assets/lib（不压缩、不生成缩略图）
- 超过 2000 万像素的图剔除（不进入 APK）
- 生成 library.json：分类树 / 首页封面 / 每个叶子分类的一句话技巧 / 随机池
- 生成 tips.json：5 个拍照技巧主题图文
- 打赏码、封面、技巧配图、启动图标
"""
import os, json, shutil, datetime
from PIL import Image

SRC = r"D:\庞娜\多宫格素材库\PoseLib"
APP = r"C:\PoseLibDev\PoseLibApp"
ASSETS = os.path.join(APP, "assets")
OUT_LIB = os.path.join(ASSETS, "lib")
OUT_COVER = os.path.join(ASSETS, "covers")
OUT_TIPS = os.path.join(ASSETS, "tips")
GEN = r"C:\PoseLibDev\tools\gen"
DONATE_SRC = r"C:\Users\Administrator\Downloads\打赏码.png"

SCAN = r"C:\PoseLibDev\tools\scan_src.json"
MAX_MP = 20.0
MAX_RATIO = 2.8

# 从 App 中剔除的目录（原图仍保留在素材盘，不删源文件）
EXCLUDE_DIRS = {"运动/其他/现代五项"}

# 首页快速分类顺序（标题, 节点, 封面文件）
HOME = [
    ("单人", "单人", "cover01_single"),
    ("双人", "双人", "cover02_duo"),
    ("闺蜜", "双人/闺蜜双人", "cover03_bestie"),
    ("情侣", "双人/情侣双人", "cover04_couple"),
    ("男生", "男生", "cover05_boy"),
    ("亲子", "儿童亲子", "cover06_family"),
    ("镜头角度", "镜头角度", "cover08_angle"),
    ("身材适配", "身材适配", "cover09_body"),
    ("引导话术", "引导话术", "cover15_guide"),
    ("照片风格", "照片风格", "cover11_style"),
    ("多人", "多人", "cover12_group"),
    ("室内场景", "室内场景", "cover13_indoor"),
    ("户外场景", "户外场景", "cover14_outdoor"),
    ("运动", "运动", "cover16_sport"),
]

# 目录分类：全部一级目录；与快速分类重复的封面复用已有快速封面
CATALOG = [
    ("儿童亲子", "儿童亲子", "cover06_family"),
    ("单人", "单人", "cover01_single"),
    ("双人", "双人", "cover02_duo"),
    ("场景用途", "场景用途", "cover17_scene"),
    ("多人", "多人", "cover12_group"),
    ("室内场景", "室内场景", "cover13_indoor"),
    ("小众特色", "小众特色", "cover18_niche"),
    ("引导话术", "引导话术", "cover15_guide"),
    ("录像技巧", "录像技巧", "cover19_video"),
    ("户外场景", "户外场景", "cover14_outdoor"),
    ("手部动作", "手部动作", "cover07_hands"),
    ("摄像参数", "摄像参数", "cover20_params"),
    ("模特", "模特", "cover21_model"),
    ("泳装", "泳装", "cover22_swim"),
    ("照片风格", "照片风格", "cover11_style"),
    ("男生", "男生", "cover05_boy"),
    ("艾寿花甲", "艾寿花甲", "cover10_senior"),
    ("身材适配", "身材适配", "cover09_body"),
    ("运动", "运动", "cover16_sport"),
    ("通勤日常", "通勤日常", "cover23_commute"),
    ("镜头角度", "镜头角度", "cover08_angle"),
]

RANDOM = ["单人", "户外场景", "室内场景", "照片风格", "镜头角度", "场景用途"]

TIPS = json.load(open(r"C:\PoseLibDev\tools\tips_pose.json", encoding="utf-8"))
TIPS.setdefault("引导话术",
                "先夸再指：这光正好，你侧一点、看我手这边——边说话边按快门，表情最松。")

TOPICS = [
    {
        "id": "camera",
        "title": "数码相机拍人像",
        "sub": "单反 / 微单 · 参数与机位",
        "image": "tips/tip_camera.jpg",
        "points": [
            "光圈：单人或情侣用 f/1.8–f/2.8 虚化背景；三人以上收小到 f/4–f/5.6，保证每个人都清晰。",
            "快门：静止摆拍不低于 1/125s，走动抓拍用 1/250s 以上，手抖模糊多半是快门太慢。",
            "ISO：白天 100，室内 400–800，尽量不要超过 1600，噪点会毁掉皮肤质感。",
            "对焦：站着不动用单次对焦（AF-S），走动连拍切换到连续对焦（AF-C），对焦点放眼睛。",
            "焦段：半身用 50–85mm 最讨喜，全身 35–50mm，别用广角贴近脸会变形。",
            "机位：镜头对准胸口高度最稳；想拍长腿再降到腰下，但要让人物下巴微收。",
        ],
    },
    {
        "id": "android",
        "title": "安卓手机拍人像",
        "sub": "专业模式 · 一秒调对参数",
        "image": "tips/tip_android.jpg",
        "points": [
            "打开相机「专业模式 / Pro」：先设 ISO 50–100（白天），快门 1/200s 左右，能明显减少手抖糊片。",
            "白平衡（WB）固定 4800K–5200K，肤色更通透，不会一会儿偏黄一会儿偏蓝。",
            "曝光补偿 EV：逆光拍脸加 +0.3～+0.7，正对大太阳减 -0.3，别让脸黑成剪影。",
            "人像 / 大光圈模式选 2x 焦段，距离 1.5–2 米，主体离背景越远虚化越自然。",
            "美颜别开满档，磨皮过强会把鼻梁和下颌线抹平，30% 左右最像真人。",
            "按快门用音量键或手势，比戳屏幕稳；拍全身把网格线打开，脚底贴住下三分线。",
        ],
    },
    {
        "id": "ai",
        "title": "AI 修图流程",
        "sub": "手机上的 6 步精修",
        "image": "tips/tip_ai.jpg",
        "points": [
            "顺序：裁切构图 → 曝光对比 → 色彩 → 皮肤 → 液化微调 → 锐化/颗粒，别跳步。",
            "皮肤只做减法：纹理 -20、清晰度 -25，再补 +15 颗粒，既有质感又不假。",
            "一次只修 20–30%：磨皮、瘦脸、放大眼睛都留一点余地，过度就是塑料感。",
            "眼睛和牙齿单独提亮，眼神光亮起来，整张照片就有神。",
            "液化先修轮廓再修细节：下颌线、肩线、手臂外侧，别推歪背景的直线。",
            "同一场景的一组装图，统一色温和参数再批量套用，发出去才像一组作品。",
            "常用工具：Lightroom 移动版「修饰 / 快速操作」、醒图、Snapseed 局部调整。",
        ],
    },
    {
        "id": "params",
        "title": "参数设置速查",
        "sub": "光圈 · 快门 · ISO 三件事",
        "image": "tips/tip_params.jpg",
        "points": [
            "光圈管进光和虚化：数字越小（f/1.8）光圈越大、背景越虚；f/8 之后前后都清楚。",
            "快门管定格：1/1000s 冻结跳跃，1/125s 拍站姿，1/15s 会拖影（夜景要靠它出氛围）。",
            "ISO 管亮度但换来噪点：宁可开大光圈、放慢快门，也别轻易把 ISO 推到 3200。",
            "白天户外：f/4、1/250s、ISO100 是万能起点；室内窗边：f/2.8、1/125s、ISO800。",
            "逆光人像用点测光对准脸，或直接 +0.7EV，脸亮了背景过曝一点反而更好看。",
            "白平衡 5500K 是中性值，日落想更暖调到 6000–6500K，冷调夜景压到 4000K。",
        ],
    },
    {
        "id": "iphone",
        "title": "苹果手机拍人像",
        "sub": "人像模式 · 实况 · 对焦",
        "image": "tips/tip_iphone.jpg",
        "points": [
            "人像模式先切 2x 镜头，站到离人约 2 米，人脸透视自然、抠图也更准。",
            "把人离背景拉开距离，虚化才自然；贴着墙拍人像模式，边缘一定发虚。",
            "光圈模拟调到 f/2.8 左右（别用默认 f/4.5），头发丝边缘更像真实镜头。",
            "对焦对眼睛：轻点屏幕眼睛位置，出现黄色方框再按快门，五官才实。",
            "长按屏幕锁定 AE/AF，再上下拖动小太阳调亮度，逆光时脸不会黑。",
            "开实况（Live Photo）：拍完可以挑关键帧、改封面、做长曝光，废片率大幅下降。",
            "开网格线，人物眼睛放在上三分线；拍全身把脚底留一点点空间，别切脚。",
        ],
    },
    {
        "id": "huawei",
        "title": "华为手机拍人像",
        "sub": "人像模式 · 大光圈 · XMAGE",
        "image": "tips/tip_huawei.jpg",
        "points": [
            "打开相机切「人像模式」，让人脸落在人像框里，再点屏幕把对焦放在眼睛上。",
            "有可变光圈的机型在大光圈模式把光圈调到 f/2.8–f/4，别用最大档，脸边缘和发丝容易糊。",
            "华为官方建议：大光圈 / 人像模式拍摄距离控制在 2 米内，太远虚化会显得像贴上去的。",
            "美颜别拉满：磨皮 30–40%、瘦脸调到最低，保住鼻梁和下颌线，脸才不会糊成一片。",
            "色彩风格怎么选：要肤色准用「原色」，想直出发朋友圈用「明快」，夜景人像别开「鲜艳」。",
            "半身用 2x / 3x 长焦，手机举到对方胸口高度，背景离人越远，虚化越自然。",
        ],
    },
    {
        "id": "oppo",
        "title": "OPPO 手机拍人像",
        "sub": "哈苏人像 · 3x 长焦 · 光斑",
        "image": "tips/tip_oppo.jpg",
        "points": [
            "切到 3x 长焦（约 70mm）拍半身：退后一步把距离拉开，五官不变形，这是哈苏人像最出片的焦段。",
            "人像模式里选哈苏风格，虚化强度放中档；头发、眼镜这种穿帮边缘，过渡会更自然。",
            "背景要选有层次的：街道纵深、树影、灯串都行，别贴白墙，不然看不出虚化。",
            "对焦锁在眼睛，再上下拖动曝光补偿 +0.3 左右，脸亮起来皮肤才有通透感。",
            "室内暖光把白平衡往暖调一点，肤色不会发青；夜景人像用自带夜景人像，别开闪光灯直怼脸。",
            "一组图怎么拍：先全身、再走近半身、最后特写侧脸或手部，三张拼起来才有故事。",
        ],
    },
    {
        "id": "honor",
        "title": "荣耀手机拍人像",
        "sub": "雅顾人像 · 5:4 画幅 · 光影",
        "image": "tips/tip_honor.jpg",
        "points": [
            "人像模式里点开「雅顾」图标，挑一个风格，画幅会自动切成 5:4，更像杂志肖像。",
            "雅顾最吃光：找窗边侧光、走廊顶光，让脸一半亮一半暗，立体感一下就出来。",
            "用 2x / 2.5x 拍半身，虚化够又不会把五官压平，人离背景 2 米以上效果最好。",
            "大光圈模式把光圈值调到中等、距离控制在 2 米内，发丝边缘过渡更干净。",
            "觉得脸太白太亮，把美颜降到 20–30%，或者切专业模式自己压曝光，肤色更像真人。",
            "开网格线，眼睛放上三分线；拍 5:4 时头顶留一点空间，别把发顶切掉。",
        ],
    },
]


def main():
    big = []
    tall = []
    try:
        scan = json.load(open(SCAN, encoding="utf-8"))
        big = [b["rel"] for b in scan["big"]]
        tall = [a["rel"] for a in scan["all"]
                if a.get("w") and a["h"] / float(a["w"]) >= MAX_RATIO]
    except Exception:
        pass
    big_set = set(big)
    tall_set = set(tall)
    print("剔除 >%.0fMP %d 张；过高(h/w>=%.1f) %d 张"
          % (MAX_MP, len(big_set), MAX_RATIO, len(tall_set)))

    if os.path.isdir(ASSETS):
        shutil.rmtree(ASSETS)
    for d in (OUT_LIB, OUT_COVER, OUT_TIPS):
        os.makedirs(d, exist_ok=True)

    nodes = {}
    order = []

    def walk(rel, depth, code):
        d = os.path.join(SRC, *rel.split("/")) if rel else SRC
        files = sorted([f for f in os.listdir(d)
                        if f.lower().endswith((".jpg", ".jpeg", ".png", ".webp"))
                        and os.path.isfile(os.path.join(d, f))])
        subs = sorted([f for f in os.listdir(d) if os.path.isdir(os.path.join(d, f))])
        node = {"title": rel.split("/")[-1] if rel else "全部", "depth": depth,
                "direct": 0, "count": 0, "children": [], "images": []}
        skipped = 0
        for i, f in enumerate(files):
            relfile = (rel + "/" if rel else "") + f
            if relfile in big_set or relfile in tall_set:
                skipped += 1
                continue
            if any(rel == x or relfile.startswith(x + "/") for x in EXCLUDE_DIRS):
                skipped += 1
                continue
            src = os.path.join(d, f)
            ext = os.path.splitext(f)[1].lower()
            if ext == ".jpeg":
                ext = ".jpg"
            base = "%s_%03d" % (code, len(node["images"]) + 1)
            rel_lib = "lib/%s%s" % (base, ext)
            shutil.copy2(src, os.path.join(ASSETS, rel_lib.replace("/", os.sep)))
            name = os.path.splitext(f)[0]
            node["images"].append([rel_lib, name])
        if skipped:
            print("  剔除超大图 %d 张 @ %s" % (skipped, rel))
        node["direct"] = len(node["images"])
        for j, sub in enumerate(subs):
            cid = (rel + "/" if rel else "") + sub
            if cid in EXCLUDE_DIRS:
                continue
            child = walk(cid, depth + 1, code + "s%02d" % (j + 1))
            if child["count"] > 0:
                nodes[cid] = child
                node["children"].append(cid)
            else:
                nodes.pop(cid, None)
        node["count"] = node["direct"] + sum(nodes[c]["count"] for c in node["children"])
        tip = TIPS.get(rel, "")
        if tip:
            node["tip"] = tip
        if node["images"]:
            node["cover"] = node["images"][0][0]
        else:
            for c in node["children"]:
                if nodes[c].get("cover"):
                    node["cover"] = nodes[c]["cover"]
                    break
        return node

    root = []
    for k, r in enumerate(sorted([f for f in os.listdir(SRC) if os.path.isdir(os.path.join(SRC, f))])):
        code = "c%02d" % (k + 1)
        node = walk(r, 1, code)
        if node["count"] <= 0:
            print("  跳过空目录 %s" % r)
            continue
        nodes[r] = node
        root.append(r)

    total = sum(nodes[r]["count"] for r in root)

    # 首页封面（快速分类 / 目录分类复用同一批封面）
    copied_covers = set()

    def cover_item(title, nid, cov):
        if nid not in nodes:
            print("!! 缺少节点", nid)
            return None
        if cov not in copied_covers:
            src = os.path.join(GEN, cov + ".jpg")
            out = os.path.join(OUT_COVER, cov + ".jpg")
            if not os.path.exists(src):
                print("!! 缺少封面源图", cov)
                return None
            im = Image.open(src).convert("RGB")
            im = im.resize((1080, 1440), Image.LANCZOS)
            im.save(out, "JPEG", quality=90, optimize=True, progressive=True)
            copied_covers.add(cov)
        return {"node": nid, "title": title, "cover": "covers/" + cov + ".jpg"}

    home = [x for x in (cover_item(t, n, c) for t, n, c in HOME) if x]
    catalog = [x for x in (cover_item(t, n, c) for t, n, c in CATALOG) if x]

    # 技巧配图
    for t in TOPICS:
        src = os.path.join(GEN, os.path.basename(t["image"]).replace(".jpg", "") + ".jpg")
        out = os.path.join(ASSETS, t["image"].replace("/", os.sep))
        im = Image.open(src).convert("RGB")
        im = im.resize((1280, 720), Image.LANCZOS)
        im.save(out, "JPEG", quality=87, optimize=True, progressive=True)

    # 打赏码
    im = Image.open(DONATE_SRC).convert("RGB")
    im.thumbnail((720, 720), Image.LANCZOS)
    im.save(os.path.join(ASSETS, "donate.png"), "PNG", optimize=True)

    lib = {
        "generated": datetime.datetime.now().strftime("%Y-%m-%d %H:%M"),
        "root": root,
        "nodes": nodes,
        "home": home,
        "catalog": catalog,
        "random": [r for r in RANDOM if r in nodes],
        "total": total,
    }
    with open(os.path.join(ASSETS, "library.json"), "w", encoding="utf-8") as f:
        json.dump(lib, f, ensure_ascii=False, separators=(",", ":"))

    with open(os.path.join(ASSETS, "tips.json"), "w", encoding="utf-8") as f:
        json.dump({"topics": TOPICS}, f, ensure_ascii=False, separators=(",", ":"))

    # 启动图标：裁掉四周白边 + 圆角透明（统一由 make_icons.py 生成五档 mipmap）
    import subprocess, sys
    subprocess.run([sys.executable, os.path.join(os.path.dirname(os.path.abspath(__file__)),
                     "make_icons.py")], check=True)

    sz = sum(os.path.getsize(os.path.join(dp, fn))
             for dp, _, fns in os.walk(OUT_LIB) for fn in fns)
    print("节点", len(nodes), "总图", total, "剔除超大", len(big_set))
    print("lib 体积 MB", round(sz / 1048576.0, 1))
    print("首页", [h["title"] for h in home])


main()
