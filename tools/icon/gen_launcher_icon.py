#!/usr/bin/env python3
"""Generates the «Рулько» launcher icon layers as Android VectorDrawables.

Design space is 1024x1024 (the visible icon). Android adaptive layers are 108dp with
the visible area being the central 72dp, so the viewport is 1536 and the design is
shifted by 256. Run: python3 tools/icon/gen_launcher_icon.py [--preview DIR]
"""
import os, sys

NAVY, BLUE, YELLOW = "#14202E", "#2F62C9", "#FFC83D"
RES = os.path.join(os.path.dirname(__file__), "..", "..", "app", "src", "main", "res", "drawable")

def rect(x, y, w, h, r):
    r = min(r, w / 2, h / 2)
    return (f"M{x+r},{y} H{x+w-r} A{r},{r} 0 0 1 {x+w},{y+r} V{y+h-r} A{r},{r} 0 0 1 {x+w-r},{y+h} "
            f"H{x+r} A{r},{r} 0 0 1 {x},{y+h-r} V{y+r} A{r},{r} 0 0 1 {x+r},{y} Z")

def ell(cx, cy, rx, ry=None):
    ry = rx if ry is None else ry
    return f"M{cx-rx},{cy} A{rx},{ry} 0 1 0 {cx+rx},{cy} A{rx},{ry} 0 1 0 {cx-rx},{cy} Z"

CABIN = "M330,488 L364,352 Q376,300 430,300 L594,300 Q648,300 660,352 L694,488 Z"
GLASS = "M370,474 L396,372 Q404,342 436,342 L588,342 Q620,342 628,372 L654,474 Z"
GLASS_MONO = "M374,456 L396,372 Q404,342 436,342 L588,342 Q620,342 628,372 L650,456 Z"
SMILE = "M462,628 Q512,676 562,628"
SMILE_HOLE = "M450,630 Q512,694 574,630 Q577,618 566,616 Q512,662 458,616 Q447,618 450,630 Z"

# A node is ('path', attrs) or ('group', attrs, [children]).
def P(d, **a): return ("path", dict(d=d, **a))
def G(children, **a): return ("group", a, children)

def car(mono=False):
    fg = "#FFFFFF" if mono else BLUE
    dark = "#FFFFFF" if mono else NAVY
    n = [P(rect(262, 664, 116, 112, 34), fill=dark), P(rect(646, 664, 116, 112, 34), fill=dark),
         G([P(ell(300, 440, 40, 26), fill=fg)], rotation=-18, pivotX=300, pivotY=440),
         G([P(ell(724, 440, 40, 26), fill=fg)], rotation=18, pivotX=724, pivotY=440)]
    if mono:
        # Monochrome layer: only alpha counts, so eyes, glass and mouth are real holes (evenOdd).
        n += [P(CABIN + " " + GLASS_MONO, fill=fg, fillType="evenOdd"),
              P(rect(232, 460, 560, 262, 110) + " " + ell(372, 580, 66, 72) + " " + ell(652, 580, 66, 72) + " " + SMILE_HOLE,
                fill=fg, fillType="evenOdd"),
              P(ell(386, 590, 34), fill=fg), P(ell(666, 590, 34), fill=fg),
              P(rect(252, 690, 520, 36, 18), fill=dark)]
        return n
    n += [P(CABIN, fill=fg), P(rect(232, 460, 560, 262, 110), fill=fg),
          P("M270,540 Q280,486 340,476 L460,472", stroke="#6E9BF0", width=16, alpha=0.8),
          P(GLASS, fill="#D6E5FF"),
          P("M430,360 L478,360 L448,456 L408,456 Z", fill="#FFFFFF", alpha=0.55),
          P(ell(372, 580, 66, 72), fill="#FFFFFF"), P(ell(652, 580, 66, 72), fill="#FFFFFF"),
          P(ell(386, 590, 34), fill=NAVY), P(ell(666, 590, 34), fill=NAVY),
          P(ell(398, 574, 12), fill="#FFFFFF"), P(ell(678, 574, 12), fill="#FFFFFF"),
          P(ell(374, 606, 5), fill="#FFFFFF"), P(ell(654, 606, 5), fill="#FFFFFF"),
          P(SMILE, stroke=NAVY, width=20),
          P(ell(282, 660, 30, 16), fill="#FF8096", alpha=0.55), P(ell(742, 660, 30, 16), fill="#FF8096", alpha=0.55),
          P(rect(252, 690, 520, 36, 18), fill=NAVY), P(rect(462, 694, 100, 28, 8), fill="#FFFFFF")]
    return n

def foreground(mono=False):
    kids = [] if mono else [P(ell(512, 780, 300, 28), fill="#000000", alpha=0.3)]
    kids.append(G(car(mono), scaleX=1.08, scaleY=1.08, pivotX=512, pivotY=600))
    return [G(kids, translateX=256, translateY=256)]

def background():
    # Drawn in 1536 space directly; the sky gradient and road cover the full bleed.
    o = 256
    def t(x, y): return x + o, y + o
    sky = P(f"M0,0 H1536 V1536 H0 Z", gradient=("linear", 0, o, 0, o + 1024, "#7FB8FF", "#D9ECFF"))
    sun = [P(ell(*t(806, 212), 92), fill="#FFD95A", alpha=0.25), P(ell(*t(806, 212), 62), fill="#FFD95A")]
    cloud = [P(ell(*t(236, 232), 86, 34), fill="#FFFFFF"), P(ell(*t(210, 212), 36), fill="#FFFFFF"), P(ell(*t(262, 204), 44), fill="#FFFFFF")]
    y = 752 + o
    road = [P(f"M0,{y} H1536 V1536 H0 Z", fill="#3C4B63"), P(f"M0,{y} H1536 V{y+14} H0 Z", fill="#56688A")]
    dashes = [P(rect(x + o, 880 + o, 120, 18, 9), fill=YELLOW) for x in (-372, 40, 452, 864, 1276)]
    return [sky] + sun + cloud + road + dashes

# ---------- emitters ----------
def vd(nodes, ind="    "):
    out = []
    for n in nodes:
        if n[0] == "group":
            attrs = "".join(f'\n{ind}    android:{k}="{v}"' for k, v in n[1].items())
            out.append(f"{ind}<group{attrs}>\n{vd(n[2], ind + '    ')}{ind}</group>\n")
            continue
        a = n[1]
        s = f'{ind}<path\n{ind}    android:pathData="{a["d"]}"'
        if "fillType" in a: s += f'\n{ind}    android:fillType="{a["fillType"]}"'
        if "stroke" in a:
            s += (f'\n{ind}    android:strokeColor="{a["stroke"]}"\n{ind}    android:strokeWidth="{a["width"]}"'
                  f'\n{ind}    android:strokeLineCap="round"\n{ind}    android:strokeLineJoin="round"')
            if "alpha" in a: s += f'\n{ind}    android:strokeAlpha="{a["alpha"]}"'
        elif "gradient" in a:
            _, x1, y1, x2, y2, c1, c2 = a["gradient"]
            s += (f'>\n{ind}    <aapt:attr name="android:fillColor">\n{ind}        <gradient\n'
                  f'{ind}            android:type="linear"\n{ind}            android:startX="{x1}" android:startY="{y1}"\n'
                  f'{ind}            android:endX="{x2}" android:endY="{y2}"\n'
                  f'{ind}            android:startColor="{c1}" android:endColor="{c2}" />\n'
                  f'{ind}    </aapt:attr>\n{ind}</path>\n')
            out.append(s); continue
        else:
            s += f'\n{ind}    android:fillColor="{a["fill"]}"'
            if "alpha" in a: s += f'\n{ind}    android:fillAlpha="{a["alpha"]}"'
        out.append(s + " />\n")
    return "".join(out)

def write_vd(name, nodes, comment):
    xml = ('<?xml version="1.0" encoding="utf-8"?>\n'
           f'<!-- {comment} Generated by tools/icon/gen_launcher_icon.py, do not edit by hand. -->\n'
           '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
           '    xmlns:aapt="http://schemas.android.com/aapt"\n'
           '    android:width="108dp"\n    android:height="108dp"\n'
           '    android:viewportWidth="1536"\n    android:viewportHeight="1536">\n'
           f'{vd(nodes)}</vector>\n')
    with open(os.path.join(RES, name), "w") as f: f.write(xml)

def svg(nodes):
    out = []
    for n in nodes:
        if n[0] == "group":
            a = n[1]; px, py = a.get("pivotX", 0), a.get("pivotY", 0)
            tf = f'translate({a.get("translateX",0)} {a.get("translateY",0)}) translate({px} {py}) rotate({a.get("rotation",0)}) scale({a.get("scaleX",1)} {a.get("scaleY",1)}) translate({-px} {-py})'
            out.append(f'<g transform="{tf}">{svg(n[2])}</g>'); continue
        a = n[1]
        if "stroke" in a:
            out.append(f'<path d="{a["d"]}" fill="none" stroke="{a["stroke"]}" stroke-width="{a["width"]}" stroke-linecap="round" stroke-opacity="{a.get("alpha",1)}"/>')
        elif "gradient" in a:
            _, x1, y1, x2, y2, c1, c2 = a["gradient"]
            out.append(f'<defs><linearGradient id="sky" gradientUnits="userSpaceOnUse" x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}"><stop offset="0" stop-color="{c1}"/><stop offset="1" stop-color="{c2}"/></linearGradient></defs><path d="{a["d"]}" fill="url(#sky)"/>')
        else:
            out.append(f'<path d="{a["d"]}" fill="{a["fill"]}" fill-opacity="{a.get("alpha",1)}" fill-rule="{"evenodd" if a.get("fillType")=="evenOdd" else "nonzero"}"/>')
    return "".join(out)

if __name__ == "__main__":
    write_vd("ic_launcher_background.xml", background(), "Launcher icon background: sky, sun, cloud and road.")
    write_vd("ic_launcher_foreground.xml", foreground(), "Launcher icon foreground: the «Рулько» car.")
    write_vd("ic_launcher_monochrome.xml", foreground(True), "Themed (monochrome) launcher icon, Android 13+.")
    if "--preview" in sys.argv:
        d = sys.argv[sys.argv.index("--preview") + 1]
        os.makedirs(d, exist_ok=True)
        wrap = lambda body: f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1536 1536" width="1536" height="1536">{body}</svg>'
        open(os.path.join(d, "bg.svg"), "w").write(wrap(svg(background())))
        open(os.path.join(d, "fg.svg"), "w").write(wrap(svg(foreground())))
        open(os.path.join(d, "mono.svg"), "w").write(wrap(svg(foreground(True))))
