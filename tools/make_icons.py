import os
from PIL import Image, ImageDraw

SRC = r"C:\Users\Administrator\Downloads\美拍图标.png"
APP = r"C:\PoseLibDev\PoseLibApp"
MASTER = r"C:\PoseLibDev\tools\gen\icon_cropped.png"


def white(p):
    return p[0] >= 246 and p[1] >= 246 and p[2] >= 246


def rounded_mask(size, radius, ss=4):
    big = size * ss
    m = Image.new("L", (big, big), 0)
    d = ImageDraw.Draw(m)
    r = radius * ss
    d.rectangle([r, 0, big - r - 1, big - 1], fill=255)
    d.rectangle([0, r, big - 1, big - r - 1], fill=255)
    for cx, cy in ((r, r), (big - r - 1, r), (r, big - r - 1), (big - r - 1, big - r - 1)):
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=255)
    return m.resize((size, size), Image.LANCZOS)


def load_icon():
    im = Image.open(SRC).convert("RGB")
    w, h = im.size
    px = im.load()
    xs, ys = [], []
    for y in range(h):
        for x in range(w):
            if not white(px[x, y]):
                xs.append(x)
                ys.append(y)
    x0, x1, y0, y1 = min(xs), max(xs), min(ys), max(ys)
    print("内容边界", x0, y0, x1, y1, "原图", w, h)

    box = im.crop((x0, y0, x1 + 1, y1 + 1))
    side = max(box.size)
    canvas = Image.new("RGB", (side, side), (255, 255, 255))
    canvas.paste(box, ((side - box.size[0]) // 2, (side - box.size[1]) // 2))
    canvas = canvas.convert("RGBA")
    canvas.putalpha(rounded_mask(side, int(side * 0.22)))
    return canvas


def main():
    icon = load_icon()
    os.makedirs(os.path.dirname(MASTER), exist_ok=True)
    icon.resize((512, 512), Image.LANCZOS).save(MASTER, "PNG", optimize=True)
    for dpi, size in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96),
                      ("xxhdpi", 144), ("xxxhdpi", 192)):
        d = os.path.join(APP, "res", "mipmap-" + dpi)
        os.makedirs(d, exist_ok=True)
        out = os.path.join(d, "ic_launcher.png")
        icon.resize((size, size), Image.LANCZOS).save(out, "PNG", optimize=True)
        print(dpi, size, os.path.getsize(out))
    print("master", MASTER, os.path.getsize(MASTER))


if __name__ == "__main__":
    main()
