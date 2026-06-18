#!/usr/bin/env python3
"""Build the Play Store phone screenshots for Journey.

Composites each unframed device capture (``docs/store-assets/raw_*.png``) onto
the brand gradient inside a rounded phone frame with a caption rendered in the
app's own Manrope font. Output is 1242 x 2208 PNG, matching Play's phone
screenshot slot (ratio 1.78:1, within the 2:1 maximum).

Usage:
    python3 tools/build_store_assets.py            # write into docs/store-assets/
    python3 tools/build_store_assets.py --out /tmp/preview   # preview elsewhere

Re-shoot the raw captures with a debug build + sample data:
    adb exec-out screencap -p > docs/store-assets/raw_<screen>.png
"""

from __future__ import annotations

import argparse
import os

from PIL import Image, ImageDraw, ImageFilter, ImageFont

# --- repo-relative paths -----------------------------------------------------
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
ASSETS = os.path.join(ROOT, "docs", "store-assets")
FONT_BOLD = os.path.join(ROOT, "app", "src", "main", "res", "font", "manrope_bold.ttf")

# --- canvas / frame geometry (measured from the original listing assets) -----
CANVAS = (1242, 2208)
GRADIENT_TOP = (94, 96, 180)      # indigo
GRADIENT_BOTTOM = (150, 110, 196)  # violet
FRAME_XYWH = (236, 266, 768, 1727)  # x, y, width, height of the screen inside the frame
FRAME_RADIUS = 34
TITLE_RGB = (255, 255, 255)
TITLE_SIZE = 64
TITLE_CENTER_Y = 174
SUPERSAMPLE = 4  # for clean anti-aliased rounded corners

# --- the five listing screenshots: raw capture -> caption --------------------
SCREENS = [
    ("raw_home.png", "screenshot_1.png", "Build a daily writing habit"),
    ("raw_analytics.png", "screenshot_2.png", "See your streaks and habits"),
    ("raw_addentry.png", "screenshot_3.png", "Distraction-free journaling"),
    ("raw_viewentry.png", "screenshot_4.png", "Every entry, beautifully kept"),
    ("raw_settings.png", "screenshot_5.png", "Private, secure, and yours"),
]


def vertical_gradient(size, top, bottom):
    w, h = size
    base = Image.new("RGB", (1, h))
    px = base.load()
    for y in range(h):
        t = y / (h - 1)
        px[0, y] = tuple(round(a + (b - a) * t) for a, b in zip(top, bottom))
    return base.resize((w, h))


def rounded_mask(w, h, radius):
    m = Image.new("L", (w * SUPERSAMPLE, h * SUPERSAMPLE), 0)
    ImageDraw.Draw(m).rounded_rectangle(
        [0, 0, w * SUPERSAMPLE - 1, h * SUPERSAMPLE - 1],
        radius=radius * SUPERSAMPLE,
        fill=255,
    )
    return m.resize((w, h), Image.LANCZOS)


def build(raw_path, caption):
    x, y, w, h = FRAME_XYWH
    canvas = vertical_gradient(CANVAS, GRADIENT_TOP, GRADIENT_BOTTOM)

    # Soft drop shadow behind the phone frame.
    shadow = Image.new("RGBA", CANVAS, (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow)
    sd.rounded_rectangle([x, y + 16, x + w, y + h + 16], radius=FRAME_RADIUS, fill=(20, 20, 40, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(28))
    canvas = Image.alpha_composite(canvas.convert("RGBA"), shadow).convert("RGB")

    # The device capture, scaled to the frame and rounded.
    shot = Image.open(raw_path).convert("RGB").resize((w, h), Image.LANCZOS)
    canvas.paste(shot, (x, y), rounded_mask(w, h, FRAME_RADIUS))

    # Caption, centered above the frame.
    draw = ImageDraw.Draw(canvas)
    font = ImageFont.truetype(FONT_BOLD, TITLE_SIZE)
    draw.text((CANVAS[0] // 2, TITLE_CENTER_Y), caption, font=font, fill=TITLE_RGB, anchor="mm")
    return canvas


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--out", default=ASSETS, help="output directory (default: docs/store-assets)")
    args = ap.parse_args()
    os.makedirs(args.out, exist_ok=True)

    for raw_name, out_name, caption in SCREENS:
        raw_path = os.path.join(ASSETS, raw_name)
        if not os.path.exists(raw_path):
            print(f"skip {out_name}: missing {raw_name}")
            continue
        img = build(raw_path, caption)
        out_path = os.path.join(args.out, out_name)
        img.save(out_path)
        print(f"wrote {out_path}  ({caption})")


if __name__ == "__main__":
    main()
